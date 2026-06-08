package com.example.elder.dto.portal.admin;

import jakarta.validation.constraints.NotBlank;

public record PortalNewsSaveRequest(
        String id,
        @NotBlank String icon,
        @NotBlank String title,
        @NotBlank String summary,
        @NotBlank String publishDate,
        @NotBlank String source,
        @NotBlank String body,
        String imageUrl) {}
