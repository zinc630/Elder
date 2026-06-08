package com.example.elder.dto;

public record AdminAlarmTypeTrendPointDto(
        String label,
        int fallCount,
        int sosCount,
        int vitalsCount
) {
}
