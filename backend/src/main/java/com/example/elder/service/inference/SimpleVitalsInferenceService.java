package com.example.elder.service.inference;

import com.example.elder.config.VitalsThresholdProperties;
import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.Measurement;
import com.example.elder.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimpleVitalsInferenceService implements VitalsInferenceService {
    private final VitalsThresholdProperties t;
    private final InMemoryDataStore store;

    // windowSlots=20 means 5 minutes with samplingInterval=15 seconds.
    public static final int SAMPLING_INTERVAL_SEC_DEFAULT = 15;
    public static final int WINDOW_SLOTS_DEFAULT = 20;

    public SimpleVitalsInferenceService(VitalsThresholdProperties t, InMemoryDataStore store) {
        this.t = t;
        this.store = store;
    }

    @Override
    public InferenceOutcome infer(Measurement latest, InferenceContext ctx) {
        // 1) compute long abnormal using the 5-minute, slot-based sampling rules.
        LongAbnormalResult longAbnormal = computeLongAbnormal(latest, ctx);
        AlarmEvent.Type alarmType = null;

        // 2) fall/sos are placeholders for now; you can replace with real model/heuristics.
        if (isFallExtreme(latest)) {
            alarmType = AlarmEvent.Type.FALL_SUSPECTED;
        } else if (isSosLikely(latest, longAbnormal)) {
            alarmType = AlarmEvent.Type.SOS_SUSPECTED;
        } else if (longAbnormal.longAbnormal()) {
            alarmType = AlarmEvent.Type.VITALS_LONG_ABNORMAL;
        }

        return new InferenceOutcome(alarmType, longAbnormal);
    }

    private LongAbnormalResult computeLongAbnormal(Measurement latest, InferenceContext ctx) {
        int samplingIntervalSec = ctx.samplingIntervalSec();
        int windowSlots = ctx.windowSlots();

        // slotKey: (unixSeconds / samplingIntervalSec) floor
        long currentSlotKey = ctx.slotKey();
        long windowStartSlotKey = currentSlotKey - (windowSlots - 1L);

        // missing samples are treated as "normal" => abnormal=false for that slot.
        Map<Long, Measurement> bySlot = new HashMap<>();
        List<Measurement> recent = store.recentVitals(latest.getElderId());
        for (Measurement m : recent) {
            long slotKey = slotKey(m.getTimestamp(), samplingIntervalSec);
            if (slotKey < windowStartSlotKey || slotKey > currentSlotKey) continue;
            // keep the newest measurement within the same slot
            Measurement prev = bySlot.get(slotKey);
            if (prev == null || prev.getTimestamp().isBefore(m.getTimestamp())) {
                bySlot.put(slotKey, m);
            }
        }

        int abnormalCount = 0;
        for (long s = windowStartSlotKey; s <= currentSlotKey; s++) {
            Measurement m = bySlot.get(s);
            if (m == null) continue; // missing sample => normal

            boolean hrAbnormal = isHrAbnormal(m.getHeartRate());
            boolean bpAbnormal = isBpAbnormal(m.getSystolic(), m.getDiastolic());
            if (hrAbnormal || bpAbnormal) {
                abnormalCount++;
            }
        }

        // abnormal point threshold: 80% of windowSlots
        int threshold = (int) Math.ceil(0.8 * windowSlots);
        double riskScore = ((double) abnormalCount) / windowSlots;
        return new LongAbnormalResult(abnormalCount >= threshold, abnormalCount, threshold, riskScore);
    }

    private long slotKey(Instant ts, int samplingIntervalSec) {
        long unixSec = ts.getEpochSecond();
        return unixSec / samplingIntervalSec;
    }

    private boolean isHrAbnormal(Integer hr) {
        if (hr == null) return false; // missing treated as normal
        return hr < t.getHrLow() || hr > t.getHrHigh();
    }

    private boolean isBpAbnormal(Integer sbp, Integer dbp) {
        boolean sbpAbnormal = sbp != null && (sbp < t.getSbpLow() || sbp > t.getSbpHigh());
        boolean dbpAbnormal = dbp != null && (dbp < t.getDbpLow() || dbp > t.getDbpHigh());
        return sbpAbnormal || dbpAbnormal;
    }

    private boolean isFallExtreme(Measurement m) {
        if (m.getHeartRate() == null) return false;
        return m.getHeartRate() < (t.getHrLow() / 2.0) || m.getHeartRate() > (t.getHrHigh() * 1.3);
    }

    private boolean isSosLikely(Measurement m, LongAbnormalResult longAbnormal) {
        // placeholder: if long abnormal and both HR+BP are abnormal currently, treat as SOS likely
        boolean hrAbnormal = isHrAbnormal(m.getHeartRate());
        boolean bpAbnormal = isBpAbnormal(m.getSystolic(), m.getDiastolic());
        return longAbnormal.longAbnormal() && hrAbnormal && bpAbnormal;
    }
}

