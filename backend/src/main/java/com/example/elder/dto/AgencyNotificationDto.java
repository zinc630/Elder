package com.example.elder.dto;

import java.time.Instant;

public record AgencyNotificationDto(
        String id,
        String title,
        String content,
        String status,
        Instant createdAt,
        String author,
        int views
) {
}
