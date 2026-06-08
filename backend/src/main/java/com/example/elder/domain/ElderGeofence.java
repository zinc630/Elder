package com.example.elder.domain;

/**
 * Demo electronic fence: circle around institution / living area.
 */
public record ElderGeofence(
        String elderId,
        double centerLat,
        double centerLng,
        double radiusMeters,
        String label
) {}
