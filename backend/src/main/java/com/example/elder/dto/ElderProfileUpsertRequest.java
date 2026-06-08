package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ElderProfileUpsertRequest(
        @NotBlank String name,
        @NotBlank String gender,
        @NotBlank String address,
        @NotNull Integer age,
        String keyHealthNotes
) {
}

