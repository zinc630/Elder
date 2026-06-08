package com.example.elder.dto;

import java.time.Instant;

public record AgencyClockInRecordDto(
        String id,
        String workerName,
        String elderName,
        String serviceTypeLabel,
        Instant clockAt,
        String address,
        String statusLabel
) {
}
