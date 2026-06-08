package com.example.elder.dto;

import com.example.elder.domain.ServiceWorker;

import java.time.Instant;

public record ServiceWorkerDto(
        String id,
        String name,
        String position,
        String phone,
        ServiceWorker.OnlineStatus onlineStatus,
        String serviceType,
        Instant createdAt
) {
}

