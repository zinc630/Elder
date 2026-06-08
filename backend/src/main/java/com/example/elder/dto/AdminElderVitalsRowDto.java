package com.example.elder.dto;

public record AdminElderVitalsRowDto(
        String elderId,
        String name,
        Integer age,
        Integer heartRateBpm,
        String bloodPressureDisplay,
        int bloodOxygenPercent,
        String latestWarning,
        String warningLevel
) {
}
