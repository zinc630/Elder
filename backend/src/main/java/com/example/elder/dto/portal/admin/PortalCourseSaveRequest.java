package com.example.elder.dto.portal.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PortalCourseSaveRequest(
        String id,
        @NotBlank String icon,
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String duration,
        double rating,
        @NotEmpty @Valid List<PortalLessonAdminDto> lessons,
        String imageUrl) {}
