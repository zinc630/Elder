package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record DispatchCreateRequest(
        @NotBlank String elderId,
        @NotBlank String alarmEventId,
        @NotBlank String serviceType
) {
}

