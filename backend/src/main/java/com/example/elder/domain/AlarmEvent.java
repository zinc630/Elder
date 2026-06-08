package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class AlarmEvent {
    public enum Status {
        PENDING_CONFIRM,   // waiting for confirmation
        CHILD_NOTIFIED,    // sms+vibration sent
        CALL_PENDING,      // phone escalation running
        CONFIRMED,         // call answered or explicit confirmation
        CLOSED
    }

    public enum Type {
        FALL_SUSPECTED,
        SOS_SUSPECTED,
        VITALS_LONG_ABNORMAL
    }

    private final String id;
    private final String elderId;
    private final Type type;
    private final Instant triggeredAt;
    private Instant windowStartAt;
    private Instant windowEndAt;

    private Status status = Status.PENDING_CONFIRM;
    private double riskScore;

    private Integer abnormalPointCount;
    private Integer abnormalPointThreshold;

    private String confirmationSource; // elder_app / child_app / guardian_call_answered

    public AlarmEvent(String elderId, Type type, Instant triggeredAt) {
        this(UUID.randomUUID().toString(), elderId, type, triggeredAt, Status.PENDING_CONFIRM,
                null, null, 0.0, null, null, null);
    }

    /** 从数据库还原（保留原 ID 与各字段） */
    public AlarmEvent(
            String id,
            String elderId,
            Type type,
            Instant triggeredAt,
            Status status,
            Instant windowStartAt,
            Instant windowEndAt,
            double riskScore,
            Integer abnormalPointCount,
            Integer abnormalPointThreshold,
            String confirmationSource
    ) {
        this.id = id;
        this.elderId = elderId;
        this.type = type;
        this.triggeredAt = triggeredAt;
        this.status = status;
        this.windowStartAt = windowStartAt;
        this.windowEndAt = windowEndAt;
        this.riskScore = riskScore;
        this.abnormalPointCount = abnormalPointCount;
        this.abnormalPointThreshold = abnormalPointThreshold;
        this.confirmationSource = confirmationSource;
    }

    public String getId() {
        return id;
    }

    public String getElderId() {
        return elderId;
    }

    public Type getType() {
        return type;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setWindow(Instant windowStartAt, Instant windowEndAt) {
        this.windowStartAt = windowStartAt;
        this.windowEndAt = windowEndAt;
    }

    public Instant getWindowStartAt() {
        return windowStartAt;
    }

    public Instant getWindowEndAt() {
        return windowEndAt;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setAbnormalPointCount(Integer abnormalPointCount) {
        this.abnormalPointCount = abnormalPointCount;
    }

    public Integer getAbnormalPointCount() {
        return abnormalPointCount;
    }

    public void setAbnormalPointThreshold(Integer abnormalPointThreshold) {
        this.abnormalPointThreshold = abnormalPointThreshold;
    }

    public Integer getAbnormalPointThreshold() {
        return abnormalPointThreshold;
    }

    public void confirm(String confirmationSource) {
        this.confirmationSource = confirmationSource;
        this.status = Status.CONFIRMED;
    }

    public String getConfirmationSource() {
        return confirmationSource;
    }
}

