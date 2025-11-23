package domain;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Minimal JUnit 4 test suite achieving 100% coverage of {@link LatKey}.
 * Tests:
 * <ul>
 *     <li>Constructor + getStation()</li>
 *     <li>compareTo() with different latitudes</li>
 *     <li>compareTo() with equal latitude and different names</li>
 *     <li>compareTo() with same latitude and same name</li>
 * </ul>
 */
public class LatKeyTest {

    /** Utility to create a Station quickly for testing. */
    private Station station(String name, double lat) {
        return new Station(
                name,
                "PT",
                new Location(lat, 0),
                "Europe/Lisbon",
                false, false, false
        );
    }

    /** Ensures constructor stores the station correctly. */
    @Test
    public void testConstructorAndGetter() {
        Station s = station("A", 10.0);
        LatKey key = new LatKey(s);
        assertSame(s, key.getStation());
    }

    /** Different latitudes must be compared numerically. */
    @Test
    public void testCompareDifferentLatitudes() {
        LatKey k1 = new LatKey(station("A", 10.0));
        LatKey k2 = new LatKey(station("B", 20.0));
        assertTrue(k1.compareTo(k2) < 0);
        assertTrue(k2.compareTo(k1) > 0);
    }

    /** Equal latitude → comparison falls back to station name. */
    @Test
    public void testCompareSameLatitudeDifferentNames() {
        LatKey k1 = new LatKey(station("Alpha", 10.0));
        LatKey k2 = new LatKey(station("Beta", 10.0));
        assertTrue(k1.compareTo(k2) < 0);
        assertTrue(k2.compareTo(k1) > 0);
    }

    /** Equal latitude and equal name → compareTo must return 0. */
    @Test
    public void testCompareFullyEqual() {
        Station s1 = station("Same", 10.0);
        Station s2 = station("Same", 10.0);
        LatKey k1 = new LatKey(s1);
        LatKey k2 = new LatKey(s2);
        assertEquals(0, k1.compareTo(k2));
    }
}
