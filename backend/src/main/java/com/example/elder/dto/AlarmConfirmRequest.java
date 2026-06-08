package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record AlarmConfirmRequest(
        @NotBlank String confirmationSource // elder_app / child_app / guardian_call_answered
) {
}

