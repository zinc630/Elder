package com.example.elder.dto;

public record AgencyScheduleDto(
        String id,
        String day,
        int hour,
        String workerName,
        String type
) {
}
