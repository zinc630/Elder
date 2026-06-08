package com.example.elder.dto;

public record GeofenceDto(
        double centerLat,
        double centerLng,
        double radiusMeters,
        String label
) {}
