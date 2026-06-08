package com.example.elder.dto.portal;

public record PortalNewsDto(
        String id,
        String icon,
        String title,
        String summary,
        String publishDate,
        String source,
        String viewsLabel,
        String imageUrl
) {}
