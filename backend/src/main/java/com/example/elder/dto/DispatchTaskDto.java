package com.example.elder.dto;

import com.example.elder.domain.DispatchTask;

import java.time.Instant;

public record DispatchTaskDto(
        String id,
        String elderId,
        String serviceType,
        DispatchTask.Status status,
        Instant appointmentTime,
        String notes,
        String assignedWorkerId,
        Instant createdAt,
        Instant updatedAt,
        String bookedByRole,
        String bookedByUserId,
        String bookedByName
) {
}

