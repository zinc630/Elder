package com.example.elder.domain.portal;

public record PortalActivity(
        String id,
        String icon,
        String tag,
        String title,
        String timeLabel,
        String location,
        String description,
        int capacity,
        int enrolledCount,
        boolean open,
        String imageUrl
) {}
