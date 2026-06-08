package com.example.elder.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilySendMessageRequest(
        @NotBlank String senderRole,
        String senderUserId,
        @NotBlank String senderName,
        @NotBlank String content
) {
}
