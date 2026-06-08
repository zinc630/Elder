package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class AgencyAnomalyReport {
    public enum Status {
        PENDING,
        PROCESSED
    }

    private final String id;
    private final String reporterName;
    private final String elderId;
    private final String elderName;
    private final String anomalyType;
    private final String description;
    private final Instant reportedAt;
    private Status status;

    public AgencyAnomalyReport(
            String reporterName,
            String elderId,
            String elderName,
            String anomalyType,
            String description,
            Instant reportedAt,
            Status status) {
        this.id = UUID.randomUUID().toString();
        this.reporterName = reporterName;
        this.elderId = elderId;
        this.elderName = elderName;
        this.anomalyType = anomalyType;
        this.description = description;
        this.reportedAt = reportedAt;
        this.status = status;
    }

    public AgencyAnomalyReport(
            String id,
            String reporterName,
            String elderId,
            String elderName,
            String anomalyType,
            String description,
            Instant reportedAt,
            Status status) {
        this.id = id;
        this.reporterName = reporterName;
        this.elderId = elderId;
        this.elderName = elderName;
        this.anomalyType = anomalyType;
        this.description = description;
        this.reportedAt = reportedAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getElderId() {
        return elderId;
    }

    public String getElderName() {
        return elderName;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public String getDescription() {
        return description;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
    }
}
