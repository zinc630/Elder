package com.example.elder.dto;

public record AdminHighRiskRowDto(
        String alarmId,
        String elderName,
        String abnormalityType,
        String statusLabel
) {
}
