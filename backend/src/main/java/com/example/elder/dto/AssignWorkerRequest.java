package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignWorkerRequest(
        @NotBlank String workerId
) {
}

