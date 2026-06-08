package com.example.elder.dto;

public record MeasurementIngestResponse(
        String deviceId,
        String elderId,
        boolean alarmCreated,
        String alarmEventId
) {
}

