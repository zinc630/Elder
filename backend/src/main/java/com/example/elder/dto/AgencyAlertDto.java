package com.example.elder.dto;

import java.time.Instant;

public record AgencyAlertDto(
        String id,
        String elderName,
        String type,
        Instant triggeredAt,
        String status,
        String processLog,
        String source
) {
}
