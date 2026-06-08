package com.example.elder.dto;

import com.example.elder.domain.DispatchTask;
import java.time.Instant;

public record ServiceOrderDto(
        String id,
        String elderId,
        String serviceType,
        String serviceTypeLabel,
        DispatchTask.Status status,
        String statusLabel,
        Instant appointmentTime,
        String notes,
        String bookedByRole,
        String bookedByName,
        Instant createdAt
) {
}
