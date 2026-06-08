package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilyStartVideoCallRequest(
        String childUserId,
        @NotBlank String callerDisplayName,
        String initiatorRole,
        String elderDisplayName
) {
}
