package com.example.elder.dto;

import java.time.Instant;

public record FamilyVideoCallDto(
        String callId,
        String elderId,
        String childId,
        String childName,
        String initiatorRole,
        String elderDisplayName,
        String status,
        String offerSdp,
        String answerSdp,
        Instant updatedAt
) {
}
