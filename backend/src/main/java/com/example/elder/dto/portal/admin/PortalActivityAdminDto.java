package com.example.elder.dto.portal.admin;

public record PortalActivityAdminDto(
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
        String imageUrl) {}
