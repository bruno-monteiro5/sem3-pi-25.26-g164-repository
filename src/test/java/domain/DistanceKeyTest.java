package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test suite for {@link DistanceKey}.
 * <p>
 * Each test method targets exactly one public member of {@code DistanceKey}
 * and documents the scenario, preconditions and expected outcome.
 */
class DistanceKeyTest {

    /**
     * Utility method to create a minimal valid {@link Station} required by
     * {@link DistanceKey}. All non-relevant fields use arbitrary valid defaults.
     */
    private Station makeStation(String name) {
        Location location = new Location(10.0, 20.0);
        return new Station(
                name,              // name
                "PT",              // country
                location,          // location
                "Europe/Lisbon",   // timeZoneGroup
                true,              // isCity
                false,             // isMainStation
                false              // isAirport
        );
    }

    /**
     * Verifies that the constructor enforces non-null station references.
     *
     * <p>Scenario:
     *   - Create a {@code DistanceKey} with a {@code null} Station.
     *
     * <p>Expected:
     *   - A {@link NullPointerException} is thrown.
     *
     * @see DistanceKey#DistanceKey(double, Station)
     */
    @Test
    void constructor_nullStation_throwsException() {
        assertThrows(NullPointerException.class, () -> new DistanceKey(5.0, null));
    }

    /**
     * Tests the primary comparison rule: distance ascending.
     *
     * <p>Scenario:
     *   - Two keys with distances 1.0 and 2.0.
     *
     * <p>Expected:
     *   - Key with smaller distance is considered "less".
     *
     * @see DistanceKey#compareTo(DistanceKey)
     */
    @Test
    void compareTo_distanceAscending() {
        Station s1 = makeStation("A");
        Station s2 = makeStation("B");

        DistanceKey dk1 = new DistanceKey(1.0, s1);
        DistanceKey dk2 = new DistanceKey(2.0, s2);

        assertTrue(dk1.compareTo(dk2) < 0);
        assertTrue(dk2.compareTo(dk1) > 0);
    }

    /**
     * Tests the secondary comparison rule: station name descending.
     *
     * <p>Scenario:
     *   - Two stations at identical distance 10.0:
     *     names "Zulu" and "Alpha".
     *
     * <p>Expected:
     *   - "Zulu" precedes "Alpha" in descending alphabetical order,
     *     therefore corresponding key is considered "less".
     *
     * @see DistanceKey#compareTo(DistanceKey)
     */
    @Test
    void compareTo_nameDescending() {
        Station sAlpha = makeStation("Alpha");
        Station sZulu  = makeStation("Zulu");

        DistanceKey dk1 = new DistanceKey(10.0, sAlpha);
        DistanceKey dk2 = new DistanceKey(10.0, sZulu);

        assertTrue(dk2.compareTo(dk1) < 0);
        assertTrue(dk1.compareTo(dk2) > 0);
    }

    /**
     * Tests {@link DistanceKey#getDistance()}.
     *
     * <p>Scenario:
     *   - Create a key with known distance.
     *
     * <p>Expected:
     *   - Getter returns the same value.
     */
    @Test
    void getDistance_returnsValue() {
        Station s = makeStation("Test");
        DistanceKey dk = new DistanceKey(7.5, s);
        assertEquals(7.5, dk.getDistance(), 1e-6);
    }

    /**
     * Tests {@link DistanceKey#getStationName()}.
     *
     * <p>Scenario:
     *   - Key created with a station named "Torino".
     *
     * <p>Expected:
     *   - Getter returns "Torino".
     */
    @Test
    void getStationName_returnsName() {
        Station s = makeStation("Torino");
        DistanceKey dk = new DistanceKey(3.0, s);
        assertEquals("Torino", dk.getStationName());
    }

    /**
     * Tests {@link DistanceKey#getStation()}.
     *
     * <p>Scenario:
     *   - Key created with known Station instance.
     *
     * <p>Expected:
     *   - Getter returns the same reference.
     */
    @Test
    void getStation_returnsReference() {
        Station s = makeStation("Madrid");
        DistanceKey dk = new DistanceKey(3.0, s);
        assertSame(s, dk.getStation());
    }

    /**
     * Tests the formatted string representation.
     *
     * <p>Scenario:
     *   - Key with distance 12.34567 and station "Lisbon".
     *
     * <p>Expected:
     *   - Output matches pattern:
     *     {@code "<number with 3 decimals> km - Lisbon"}.
     *   - Accepts both decimal separators ('.' or ',') depending on locale.
     *
     * @see DistanceKey#toString()
     */
    @Test
    void toString_formatsDistanceAndName() {
        Station s = makeStation("Lisbon");
        DistanceKey dk = new DistanceKey(12.34567, s);

        String str = dk.toString();

        assertTrue(str.contains("Lisbon"));
        assertTrue(str.contains(" km - "));

        assertTrue(str.contains("12,346"));
        assertTrue(str.contains("Lisbon"));
        assertTrue(str.contains("km"));
    }
}
