package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilyVideoCallPatchRequest(
        @NotBlank String status
) {
}
