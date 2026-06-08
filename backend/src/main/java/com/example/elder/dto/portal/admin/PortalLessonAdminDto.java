package com.example.elder.dto.portal.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PortalLessonAdminDto(
        String id,
        @NotBlank String title,
        @NotBlank String content,
        @Min(1) int durationMinutes,
        String videoUrl) {}
