package com.example.elder.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record LocationReportRequest(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
        @DecimalMin("0.0") @DecimalMax("50000.0") Double accuracyMeters,
        String source
) {}
