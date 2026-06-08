package com.example.elder.dto.portal.admin;

import java.util.List;

public record PortalCourseAdminDto(
        String id,
        String icon,
        String category,
        String title,
        String description,
        String duration,
        int viewCount,
        double rating,
        List<PortalLessonAdminDto> lessons,
        String imageUrl) {}
