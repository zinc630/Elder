package com.example.elder.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record PortalCourseProgressRequest(@NotBlank String lessonId) {}
