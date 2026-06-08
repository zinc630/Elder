package com.example.elder.dto;

import java.time.Instant;
import java.util.List;

public record AgencyEvaluationDto(
        String id,
        String elderName,
        String workerName,
        String serviceType,
        int rating,
        Integer attitudeRating,
        Integer skillRating,
        Integer responseRating,
        Integer punctualityRating,
        Integer communicationRating,
        String comment,
        List<String> tags,
        Boolean isAnonymous,
        Integer serviceDurationMinutes,
        String taskId,
        String taskIdLabel,
        List<String> images,
        Instant createdAt
) {
}
