package domain;

/**
 * Utility class for calculating Haversine distance between two geographic points.
 * <p>
 * Uses the Haversine formula to calculate the great-circle distance between two points
 * on Earth given their latitude and longitude in decimal degrees.
 */
public final class HaversineDistance {

    /** Earth radius in kilometers */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Private constructor to prevent instantiation */
    private HaversineDistance() {
    }

    /**
     * Calculates the Haversine distance between two geographic points.
     * <p>
     * Formula: a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
     *          c = 2 ⋅ atan2(√a, √(1−a))
     *          d = R ⋅ c
     *
     * @param lat1 latitude of first point in decimal degrees
     * @param lon1 longitude of first point in decimal degrees
     * @param lat2 latitude of second point in decimal degrees
     * @param lon2 longitude of second point in decimal degrees
     * @return distance in kilometers
     */
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the Haversine distance between a point and a station.
     *
     * @param targetLat latitude of target point in decimal degrees
     * @param targetLon longitude of target point in decimal degrees
     * @param station the station to calculate distance to
     * @return distance in kilometers
     * @throws NullPointerException if station or its location is null
     */
    public static double distanceToStation(double targetLat, double targetLon, Station station) {
        if (station == null || station.getLocation() == null) {
            throw new NullPointerException("Station and its location must not be null");
        }
        Location loc = station.getLocation();
        return calculate(targetLat, targetLon, loc.getLatitude(), loc.getLongitude());
    }
}