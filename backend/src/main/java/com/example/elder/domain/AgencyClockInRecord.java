package com.example.elder.domain;

import java.time.Instant;
import java.util.UUID;

public class AgencyClockInRecord {
    private final String id;
    private final String workerName;
    private final String elderName;
    private final String serviceTypeLabel;
    private final Instant clockAt;
    private final String address;
    private final String statusLabel;

    public AgencyClockInRecord(
            String workerName,
            String elderName,
            String serviceTypeLabel,
            Instant clockAt,
            String address,
            String statusLabel) {
        this.id = UUID.randomUUID().toString();
        this.workerName = workerName;
        this.elderName = elderName;
        this.serviceTypeLabel = serviceTypeLabel;
        this.clockAt = clockAt;
        this.address = address;
        this.statusLabel = statusLabel;
    }

    public AgencyClockInRecord(
            String id,
            String workerName,
            String elderName,
            String serviceTypeLabel,
            Instant clockAt,
            String address,
            String statusLabel) {
        this.id = id;
        this.workerName = workerName;
        this.elderName = elderName;
        this.serviceTypeLabel = serviceTypeLabel;
        this.clockAt = clockAt;
        this.address = address;
        this.statusLabel = statusLabel;
    }

    public String getId() {
        return id;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getElderName() {
        return elderName;
    }

    public String getServiceTypeLabel() {
        return serviceTypeLabel;
    }

    public Instant getClockAt() {
        return clockAt;
    }

    public String getAddress() {
        return address;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
