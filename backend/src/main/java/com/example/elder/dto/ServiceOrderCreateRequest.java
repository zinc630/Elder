package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServiceOrderCreateRequest(
        @NotBlank String serviceType,
        @NotNull Long appointmentTimeMillis,
        String notes,
        String bookedByRole,
        String bookedByUserId,
        String bookedByName
) {
}
