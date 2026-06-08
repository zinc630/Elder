package com.example.elder.dto;

import com.example.elder.domain.CallAttempt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CallAttemptResultRequest(
        @NotNull CallAttempt.Result result,
        String detail
) {
}

