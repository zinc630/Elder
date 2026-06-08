package com.example.elder.dto.portal;

public record PortalNewsDetailDto(
        String id,
        String icon,
        String title,
        String summary,
        String publishDate,
        String source,
        String body,
        String viewsLabel,
        String imageUrl
) {}
