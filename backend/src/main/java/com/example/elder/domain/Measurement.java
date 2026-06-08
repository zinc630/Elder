package com.example.elder.domain;

import java.time.Instant;

public class Measurement {
    private final String deviceId;
    private final String elderId;
    private final Instant timestamp;

    private final Integer heartRate; // bpm
    private final Integer systolic;  // mmHg
    private final Integer diastolic; // mmHg

    // raw signals omitted for skeleton
    public Measurement(String deviceId, String elderId, Instant timestamp, Integer heartRate, Integer systolic, Integer diastolic) {
        this.deviceId = deviceId;
        this.elderId = elderId;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getElderId() {
        return elderId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }
}

