package com.example.elder.dto;

import java.time.Instant;

public record FamilyChatMessageDto(
        String id,
        String elderId,
        String senderRole,
        String senderUserId,
        String senderName,
        String content,
        Instant createdAt
) {
}
