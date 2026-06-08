package com.example.elder.dto.portal;

import java.util.List;

public record PortalLifeServiceDto(
        String id,
        String icon,
        String title,
        String description,
        List<String> features,
        String bgGradient,
        String priceHint,
        int slaHours,
        boolean booked
) {}
