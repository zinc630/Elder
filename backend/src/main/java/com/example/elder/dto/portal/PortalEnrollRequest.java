package com.example.elder.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record PortalEnrollRequest(
        @NotBlank String contactName,
        @NotBlank String contactPhone,
        String note,
        String preferredTime
) {}
