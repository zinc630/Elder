package com.example.elder.domain.portal;

import java.util.List;

public record PortalCourse(
        String id,
        String icon,
        String category,
        String title,
        String description,
        String duration,
        int viewCount,
        double rating,
        List<PortalLesson> lessons,
        String imageUrl
) {
    public record PortalLesson(String id, String title, String content, int durationMinutes, String videoUrl) {}
}
