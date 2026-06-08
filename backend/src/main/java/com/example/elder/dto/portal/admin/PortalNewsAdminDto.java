package com.example.elder.dto.portal.admin;

public record PortalNewsAdminDto(
        String id,
        String icon,
        String title,
        String summary,
        String publishDate,
        String source,
        String body,
        int viewCount,
        String imageUrl) {}
