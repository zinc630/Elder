package com.example.elder.dto.portal;

public record PortalActivityDto(
        String id,
        String icon,
        String tag,
        String title,
        String timeLabel,
        String location,
        String description,
        int capacity,
        int enrolledCount,
        int remaining,
        boolean open,
        boolean enrolled,
        String imageUrl
) {}
