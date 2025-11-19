package domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple unit tests for {@link HaversineDistance}.
 * Uses plain JUnit 4 and basic Java.
 */
public class HaversineDistanceTest {

    /**
     * Tests that the distance between the same point is zero.
     */
    @Test
    public void testCalculateSamePoint() {
        double distance = HaversineDistance.calculate(10.0, 20.0, 10.0, 20.0);
        assertEquals(0.0, distance, 1e-9);
    }

    /**
     * Tests a known distance: (0°, 0°) to (0°, 1°) is about 111.19 km
     * for an Earth radius of 6371 km.
     */
    @Test
    public void testCalculateKnownDistance() {
        double distance = HaversineDistance.calculate(0.0, 0.0, 0.0, 1.0);
        assertEquals(111.19, distance, 0.2);
    }

    /**
     * Tests distanceToStation with a valid station and location.
     */
    @Test
    public void testDistanceToStationValid() {
        // Adapt if your Location has a different constructor
        Location location = new Location(50.0, -1.0);

        // Station(String name, String country, Location location,
        //         String timeZoneGroup, boolean isCity,
        //         boolean isAirport, boolean isPort)
        Station station = new Station(
                "TestStation",
                "PT",
                location,
                "Europe/Lisbon",
                false,
                false,
                false
        );

        double targetLat = 51.0;
        double targetLon = -1.0;

        double expected = HaversineDistance.calculate(
                targetLat, targetLon,
                location.getLatitude(), location.getLongitude()
        );

        double result = HaversineDistance.distanceToStation(
                targetLat, targetLon, station
        );

        assertEquals(expected, result, 1e-9);
    }

    /**
     * Tests that passing a null station throws NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testDistanceToStationNullStation() {
        HaversineDistance.distanceToStation(0.0, 0.0, null);
    }

    /**
     * Tests that if station.getLocation() returns null,
     * distanceToStation throws NullPointerException.
     * <p>
     * We use an anonymous subclass of Station so that the constructor
     * still receives a non-null location, but getLocation() returns null.
     */
    @Test(expected = NullPointerException.class)
    public void testDistanceToStationNullLocation() {
        Location dummyLocation = new Location(0.0, 0.0);

        Station station = new Station(
                "NoLocationStation",
                "PT",
                dummyLocation,
                "Europe/Lisbon",
                false,
                false,
                false
        ) {
            @Override
            public Location getLocation() {
                return null;
            }
        };

        HaversineDistance.distanceToStation(0.0, 0.0, station);
    }
}
