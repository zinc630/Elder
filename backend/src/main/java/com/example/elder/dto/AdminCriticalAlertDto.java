package com.example.elder.dto;

import java.time.Instant;

public record AdminCriticalAlertDto(
        String id,
        String title,
        Instant occurredAt,
        boolean handled
) {
}
