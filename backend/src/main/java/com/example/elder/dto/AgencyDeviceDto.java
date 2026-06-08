package com.example.elder.dto;

import java.time.Instant;

public record AgencyDeviceDto(
        String id,
        String name,
        String type,
        String elderName,
        int battery,
        String status,
        Instant lastOnline
) {
}
