package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class CallAttempt {
    public enum Result {
        ANSWERED,
        NO_ANSWER,
        BUSY,
        FAIL
    }

    public enum Stage {
        PRIMARY,
        BACKUP
    }

    private final String id;
    private final String alarmEventId;
    private final Stage stage;
    private final int attemptNo; // 1-based within the stage
    private final Instant createdAt;

    private Result result; // nullable until call result is reported
    private String resultDetail;

    public CallAttempt(String alarmEventId, Stage stage, int attemptNo, Instant createdAt) {
        this.id = UUID.randomUUID().toString();
        this.alarmEventId = alarmEventId;
        this.stage = stage;
        this.attemptNo = attemptNo;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAlarmEventId() {
        return alarmEventId;
    }

    public Stage getStage() {
        return stage;
    }

    public int getAttemptNo() {
        return attemptNo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result, String resultDetail) {
        this.result = result;
        this.resultDetail = resultDetail;
    }

    public String getResultDetail() {
        return resultDetail;
    }
}

