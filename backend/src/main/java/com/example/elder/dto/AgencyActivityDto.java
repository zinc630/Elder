package com.example.elder.dto;

import java.time.Instant;

public record AgencyActivityDto(
        String id,
        String title,
        Instant startTime,
        Instant endTime,
        String location,
        int maxParticipants,
        int registered,
        String status,
        String description
) {
}
