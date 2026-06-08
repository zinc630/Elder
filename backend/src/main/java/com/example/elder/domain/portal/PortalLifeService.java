package com.example.elder.domain.portal;

import java.util.List;

public record PortalLifeService(
        String id,
        String icon,
        String title,
        String description,
        List<String> features,
        String bgGradient,
        String priceHint,
        int slaHours
) {}
