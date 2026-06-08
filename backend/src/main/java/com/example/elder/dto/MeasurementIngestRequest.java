package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Wristband -> platform via HTTP.
 * Missing values should be allowed; in this skeleton "null" means missing slot.
 */
public record MeasurementIngestRequest(
        @NotBlank String deviceId,
        @NotBlank String elderId,
        @NotNull Long timestampMillis,
        Integer heartRate,
        Integer systolic,
        Integer diastolic
) {
}

