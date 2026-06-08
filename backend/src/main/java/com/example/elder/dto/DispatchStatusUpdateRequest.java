package com.example.elder.dto;

import com.example.elder.domain.DispatchTask;
import jakarta.validation.constraints.NotBlank;

public record DispatchStatusUpdateRequest(
        @NotBlank String status
) {
    public DispatchTask.Status toStatus() {
        return DispatchTask.Status.valueOf(status.trim().toUpperCase());
    }
}

