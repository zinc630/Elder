package com.example.elder.dto.portal;

import java.util.List;

public record PortalCourseDetailDto(
        String id,
        String icon,
        String category,
        String title,
        String description,
        String duration,
        String viewsLabel,
        double rating,
        int progressPercent,
        boolean enrolled,
        String imageUrl,
        List<PortalLessonDto> lessons,
        List<String> completedLessonIds
) {
    public record PortalLessonDto(
            String id, String title, String content, int durationMinutes, String videoUrl, boolean completed) {}
}
