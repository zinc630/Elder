package com.example.elder.dto.portal;

public record PortalCourseDto(
        String id,
        String icon,
        String category,
        String title,
        String description,
        String duration,
        String viewsLabel,
        double rating,
        int lessonCount,
        int progressPercent,
        boolean enrolled,
        String imageUrl
) {}
