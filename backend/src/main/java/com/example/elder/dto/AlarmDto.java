package com.example.elder.dto;

import com.example.elder.domain.AlarmEvent;

import java.time.Instant;

public record AlarmDto(
        String id,
        String elderId,
        AlarmEvent.Type type,
        AlarmEvent.Status status,
        Instant triggeredAt,
        Double riskScore,
        Integer abnormalPointCount,
        Integer abnormalPointThreshold,
        String confirmationSource
) {
}

