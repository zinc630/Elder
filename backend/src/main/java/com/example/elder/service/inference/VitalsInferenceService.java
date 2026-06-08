package com.example.elder.service.inference;

import com.example.elder.domain.Measurement;
import com.example.elder.domain.AlarmEvent;

public interface VitalsInferenceService {
    InferenceOutcome infer(Measurement latest, InferenceContext ctx);

    record InferenceContext(String elderId, Long slotKey, int samplingIntervalSec, int windowSlots) {
    }

    record InferenceOutcome(AlarmEvent.Type alarmTypeOrNull, LongAbnormalResult longAbnormalResultOrNull) {
    }
}

