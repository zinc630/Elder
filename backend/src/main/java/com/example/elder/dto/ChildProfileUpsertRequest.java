package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChildProfileUpsertRequest(
        @NotBlank String name,
        @NotBlank String gender,
        String address,
        @NotNull Integer age,
        String relationDesc
) {
}
