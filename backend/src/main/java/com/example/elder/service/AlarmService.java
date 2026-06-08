package com.example.elder.service;

import com.example.elder.domain.AlarmEvent;
import com.example.elder.domain.Measurement;
import com.example.elder.dto.AlarmConfirmRequest;
import com.example.elder.dto.AlarmDto;
import com.example.elder.dto.MeasurementIngestRequest;
import com.example.elder.dto.MeasurementIngestResponse;
import com.example.elder.service.agency.AgencyModulesDbService;
import com.example.elder.service.alarm.AlarmDbService;
import com.example.elder.service.elder.ElderDbService;
import com.example.elder.service.inference.LongAbnormalResult;
import com.example.elder.service.inference.VitalsInferenceService;
import com.example.elder.service.inference.VitalsInferenceService.InferenceContext;
import com.example.elder.service.inference.VitalsInferenceService.InferenceOutcome;
import com.example.elder.service.notification.NotificationService;
import com.example.elder.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AlarmService {
    private static final int SAMPLING_INTERVAL_SEC = 15;
    private static final int WINDOW_SLOTS = 20;
    private static final int MAX_MEASUREMENTS_PER_ELDER = 500;

    private final InMemoryDataStore store;
    private final AlarmDbService alarmDbService;
    private final ElderDbService elderDbService;
    private final VitalsInferenceService inferenceService;
    private final NotificationService notificationService;
    private final AgencyModulesDbService agencyModulesDbService;

    public AlarmService(
            InMemoryDataStore store,
            AlarmDbService alarmDbService,
            ElderDbService elderDbService,
            VitalsInferenceService inferenceService,
            NotificationService notificationService,
            AgencyModulesDbService agencyModulesDbService
    ) {
        this.store = store;
        this.alarmDbService = alarmDbService;
        this.elderDbService = elderDbService;
        this.inferenceService = inferenceService;
        this.notificationService = notificationService;
        this.agencyModulesDbService = agencyModulesDbService;
    }

    public MeasurementIngestResponse ingestMeasurement(MeasurementIngestRequest req) {
        Instant ts = Instant.ofEpochMilli(req.timestampMillis());
        Measurement measurement = new Measurement(req.deviceId(), req.elderId(), ts, req.heartRate(), req.systolic(), req.diastolic());
        store.appendVitals(measurement, MAX_MEASUREMENTS_PER_ELDER);
        try {
            agencyModulesDbService.upsertVitalsSnapshot(
                    req.elderId(),
                    req.heartRate(),
                    req.systolic(),
                    req.diastolic(),
                    null
            );
        } catch (Exception ignored) {
            // 体征快照写入失败不影响主流程
        }

        long slotKey = (ts.getEpochSecond() / SAMPLING_INTERVAL_SEC);
        InferenceContext ctx = new InferenceContext(req.elderId(), slotKey, SAMPLING_INTERVAL_SEC, WINDOW_SLOTS);
        InferenceOutcome outcome = inferenceService.infer(measurement, ctx);

        if (outcome.alarmTypeOrNull() == null) {
            return new MeasurementIngestResponse(req.deviceId(), req.elderId(), false, null);
        }

        AlarmEvent.Type type = outcome.alarmTypeOrNull();
        String elderId = req.elderId().trim().toUpperCase();
        Optional<AlarmEvent> dup = alarmDbService.listByElder(elderId, 10).stream()
                .filter(a -> a.getStatus() != AlarmEvent.Status.CLOSED)
                .filter(a -> a.getType() == type)
                .filter(a -> a.getTriggeredAt().isAfter(ts.minusSeconds(90)))
                .findFirst();
        if (dup.isPresent()) {
            return new MeasurementIngestResponse(req.deviceId(), req.elderId(), false, dup.get().getId());
        }

        AlarmEvent event = new AlarmEvent(elderId, type, ts);

        LongAbnormalResult longAbnormal = outcome.longAbnormalResultOrNull();
        if (longAbnormal != null) {
            event.setRiskScore(longAbnormal.riskScore());
            event.setAbnormalPointCount(longAbnormal.abnormalPointCount());
            event.setAbnormalPointThreshold(longAbnormal.abnormalPointThreshold());
            Instant windowStart = ts.minusSeconds((long) WINDOW_SLOTS * SAMPLING_INTERVAL_SEC);
            event.setWindow(windowStart, ts);
        }

        event.setStatus(AlarmEvent.Status.CHILD_NOTIFIED);
        alarmDbService.insert(event);
        notificationService.sendChildSmsAndVibration(event);

        return new MeasurementIngestResponse(req.deviceId(), req.elderId(), true, event.getId());
    }

    public AlarmDto triggerSos(String elderId) {
        String normalized = elderId.trim().toUpperCase();
        if (elderDbService.getElderProfile(normalized).isEmpty()) {
            throw new IllegalArgumentException("invalid_elder_id: 请使用老人系统账号（如 E100001）");
        }
        Instant now = Instant.now();
        Optional<AlarmEvent> dup = alarmDbService.listByElder(normalized, 5).stream()
                .filter(a -> a.getType() == AlarmEvent.Type.SOS_SUSPECTED)
                .filter(a -> a.getStatus() != AlarmEvent.Status.CLOSED)
                .filter(a -> a.getTriggeredAt().isAfter(now.minusSeconds(90)))
                .findFirst();
        if (dup.isPresent()) {
            return toDto(dup.get());
        }

        AlarmEvent event = new AlarmEvent(normalized, AlarmEvent.Type.SOS_SUSPECTED, now);
        event.setStatus(AlarmEvent.Status.CHILD_NOTIFIED);
        alarmDbService.insert(event);
        notificationService.sendChildSmsAndVibration(event);
        notificationService.notifyAgency(event);
        return toDto(event);
    }

    public List<AlarmDto> listAlarmsByElder(String elderId, int limit) {
        return alarmDbService.listByElder(elderId, limit).stream()
                .map(this::toDto)
                .toList();
    }

    public void confirmAlarm(String alarmEventId, AlarmConfirmRequest req) {
        if (alarmDbService.findById(alarmEventId).isEmpty()) {
            throw new IllegalArgumentException("alarm_not_found");
        }
        alarmDbService.confirm(alarmEventId, req.confirmationSource());
    }

    public AlarmDto getAlarm(String alarmEventId) {
        AlarmEvent e = alarmDbService.findById(alarmEventId)
                .orElseThrow(() -> new IllegalArgumentException("alarm_not_found"));
        return toDto(e);
    }

    private AlarmDto toDto(AlarmEvent e) {
        return new AlarmDto(
                e.getId(),
                e.getElderId(),
                e.getType(),
                e.getStatus(),
                e.getTriggeredAt(),
                e.getRiskScore(),
                e.getAbnormalPointCount(),
                e.getAbnormalPointThreshold(),
                e.getConfirmationSource()
        );
    }
}
