package com.example.elder.dto;

public record LocationPointDto(
        double lat,
        double lng,
        Double accuracyMeters,
        String source,
        String updatedAtIso
) {}
