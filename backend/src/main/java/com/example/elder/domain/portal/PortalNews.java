package com.example.elder.domain.portal;

public record PortalNews(
        String id,
        String icon,
        String title,
        String summary,
        String publishDate,
        String source,
        String body,
        int viewCount,
        String imageUrl
) {}
