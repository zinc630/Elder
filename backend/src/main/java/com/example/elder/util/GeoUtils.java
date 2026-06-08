package com.example.elder.util;

public final class GeoUtils {
    private GeoUtils() {}

    /** Haversine distance in meters (WGS84 sphere approximation). */
    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    public static boolean isInsideCircle(
            double pointLat, double pointLng,
            double centerLat, double centerLng,
            double radiusMeters
    ) {
        return distanceMeters(pointLat, pointLng, centerLat, centerLng) <= radiusMeters;
    }
}
