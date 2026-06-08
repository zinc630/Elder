package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkerCreateRequest(
        @NotBlank String name,
        @NotBlank String position,
        @NotBlank String phone,
        String onlineStatus // ONLINE/OFFLINE (optional)
) {
}

