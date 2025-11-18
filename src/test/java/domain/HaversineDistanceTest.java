package domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HaversineDistance}.
 * <p>
 * These tests validate the correctness of the Haversine formula
 * and ensure proper exception behavior in {@link HaversineDistance#distanceToStation(double, double, Station)}.
 */
public class HaversineDistanceTest {

    /**
     * Tests the Haversine calculation using known values.
     * Example: Distance between Paris (48.8566, 2.3522)
     * and London (51.5074, -0.1278) ≈ 343.8 km.
     */
    @Test
    public void testCalculateKnownDistance() {
        double parisLat = 48.8566;
        double parisLon = 2.3522;

        double londonLat = 51.5074;
        double londonLon = -0.1278;

        double distance = HaversineDistance.calculate(parisLat, parisLon, londonLat, londonLon);

        // Expected approx value is ~343.8 km
        assertEquals(343.8, distance, 1.0); // ±1 km tolerance
    }

    /**
     * Tests that the Haversine distance between identical points is 0.
     */
    @Test
    public void testCalculateZeroDistance() {
        double lat = 40.0;
        double lon = -7.0;

        double distance = HaversineDistance.calculate(lat, lon, lat, lon);

        assertEquals(0.0, distance, 0.0000001);
    }

    /**
     * Ensures that distanceToStation throws NullPointerException
     * when station is null.
     */
    @Test(expected = NullPointerException.class)
    public void testDistanceToStationNullStationThrows() {
        HaversineDistance.distanceToStation(0.0, 0.0, null);
    }

    /**
     * Ensures that distanceToStation throws NullPointerException
     * when station.getLocation() is null.
     */
    @Test(expected = NullPointerException.class)
    public void testDistanceToStationNullLocationThrows() {
        Station s = createStationWithLocation(null);
        HaversineDistance.distanceToStation(0.0, 0.0, s);
    }

    /**
     * Tests distanceToStation with a real Station and real Location.
     * Example: Madrid (40.4168, -3.7038) to Barcelona (41.3851, 2.1734)
     * ≈ 505 km.
     */
    @Test
    public void testDistanceToStationValid() {
        Location madrid = new Location(40.4168, -3.7038);
        Location barcelona = new Location(41.3851, 2.1734);

        Station station = createStationWithLocation(barcelona);

        double distance = HaversineDistance.distanceToStation(
                madrid.getLatitude(),
                madrid.getLongitude(),
                station
        );

        assertEquals(505, distance, 5.0); // ±5km tolerance
    }

    /**
     * Creates a Station with a given Location. If loc is null, the station
     * still compiles because the constructor will throw only when the location
     * is null, which we need for exception testing.
     */
    private Station createStationWithLocation(Location loc) {
        if (loc == null) {
            return new Station("X", "PT", null, "EU", false, false, false);
        }
        return new Station("StationX", "PT", loc, "EU", false, false, false);
    }
}
