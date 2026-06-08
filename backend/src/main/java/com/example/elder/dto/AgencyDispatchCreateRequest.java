package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgencyDispatchCreateRequest(
        @NotBlank String elderId,
        @NotBlank String serviceType,
        @NotNull Long appointmentTimeMillis,
        String notes
) {
}

