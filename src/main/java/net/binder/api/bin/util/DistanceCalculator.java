package net.binder.api.bin.util;

public class DistanceCalculator {
    private static final double EARTH_RADIUS_METERS = 6371000; // 지구 반경 (미터)
    private static final double NAUTICAL_MILE_TO_METERS = 1852.0; // 1 해리 = 1852 미터

    public static double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);
        double latitude1Rad = Math.toRadians(latitude1);
        double latitude2Rad = Math.toRadians(latitude2);

        double centralAngle = Math.acos(
                Math.sin(latitude1Rad) * Math.sin(latitude2Rad) +
                        Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.cos(deltaLongitude)
        );

        return EARTH_RADIUS_METERS * centralAngle;
    }

    public static double convertToStatuteMiles(double distanceMeters) {
        double nauticalMiles = distanceMeters / NAUTICAL_MILE_TO_METERS;
        return nauticalMiles * 1.15078; // 1 해리 = 1.15078 법정 마일
    }
}
