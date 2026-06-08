package com.example.elder.dto;

public record ElderLocationSnapshotDto(
        String elderId,
        LocationPointDto elderLocation,
        GeofenceDto geofence,
        boolean insideFence,
        Double distanceToFenceCenterMeters
) {}
