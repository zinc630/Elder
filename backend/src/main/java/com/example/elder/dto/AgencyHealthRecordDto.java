package com.example.elder.dto;

import java.time.Instant;

public record AgencyHealthRecordDto(
        String elderId,
        String name,
        Integer age,
        String gender,
        String healthStatus,
        Integer heartRate,
        String bloodPressure,
        Integer bloodOxygen,
        Instant lastUpdated
) {
}
