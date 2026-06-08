package com.example.elder.domain;

import java.time.Instant;

public record ElderLastLocation(
        double lat,
        double lng,
        Double accuracyMeters,
        String source,
        Instant updatedAt
) {}
