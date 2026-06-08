package com.example.elder.dto.portal;

public record PortalActionResultDto(
        boolean success,
        String message,
        String registrationId
) {}
