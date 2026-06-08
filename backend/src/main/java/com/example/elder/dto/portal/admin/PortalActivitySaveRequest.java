package com.example.elder.dto.portal.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PortalActivitySaveRequest(
        String id,
        @NotBlank String icon,
        @NotBlank String tag,
        @NotBlank String title,
        @NotBlank String timeLabel,
        @NotBlank String location,
        @NotBlank String description,
        @Min(1) int capacity,
        boolean open,
        String imageUrl) {}
