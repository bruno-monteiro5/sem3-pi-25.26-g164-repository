package domain;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link StationIndexes} class.
 * <p>
 * These tests verify all public methods:
 * <ul>
 *     <li>{@code add}</li>
 *     <li>{@code inLatitudeWindow}</li>
 *     <li>{@code inLongitudeWindow}</li>
 *     <li>{@code byTimeZoneGroup}</li>
 *     <li>{@code byTimeZoneAndCountry}</li>
 * </ul>
 * <p>
 * Both normal execution paths and exceptional cases are covered
 * to ensure full line and branch coverage.
 */
public class StationIndexesTest {

    /**
     * Tests adding valid stations and verifies that adding a null
     * station throws {@link NullPointerException}.
     */
    @Test
    public void add() {
        StationIndexes indexes = new StationIndexes();

        Station s = new Station(
                "Station A",
                "PT",
                new Location(40.0, -8.0),
                "Europe/Lisbon",
                true,
                false,
                false
        );

        indexes.add(s);

        try {
            indexes.add(null);
            fail("Expected NullPointerException when adding null station");
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void inLatitudeWindow() {
        StationIndexes indexes = new StationIndexes();

        indexes.add(new Station("Low", "PT", new Location(10, 0), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Mid", "PT", new Location(20, 0), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("High", "PT", new Location(30, 0), "Europe/Lisbon", false, false, false));

        List<Station> result = indexes.inLatitudeWindow(15, 25);

        assertEquals(1, result.size());
        assertEquals("Mid", result.get(0).getName());

        try {
            indexes.inLatitudeWindow(30, 10);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void inLongitudeWindow() {
        StationIndexes indexes = new StationIndexes();

        indexes.add(new Station("Left", "PT", new Location(0, -10), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Center", "PT", new Location(0, 0), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Right", "PT", new Location(0, 10), "Europe/Lisbon", false, false, false));

        List<Station> result = indexes.inLongitudeWindow(-5, 5);

        assertEquals(1, result.size());
        assertEquals("Center", result.get(0).getName());

        try {
            indexes.inLongitudeWindow(5, -5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void byTimeZoneGroup() {
        StationIndexes indexes = new StationIndexes();

        indexes.add(new Station("Lisbon-PT", "PT", new Location(38.7, -9.1), "Europe/Lisbon", true, true, false));
        indexes.add(new Station("Lisbon-ES", "ES", new Location(36.0, -5.0), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Madrid-PT", "PT", new Location(40.4, -3.7), "Europe/Madrid", false, false, false));

        List<Station> result = indexes.byTimeZoneGroup("Europe/Lisbon");

        assertEquals(2, result.size());
        assertTrue(containsName(result, "Lisbon-PT"));
        assertTrue(containsName(result, "Lisbon-ES"));
        assertFalse(containsName(result, "Madrid-PT"));

        try {
            indexes.byTimeZoneGroup(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            indexes.byTimeZoneGroup("   ");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void byTimeZoneAndCountry() {
        StationIndexes indexes = new StationIndexes();

        indexes.add(new Station("Lisbon-PT-1", "PT", new Location(38.7, -9.1), "Europe/Lisbon", true, true, false));
        indexes.add(new Station("Lisbon-PT-2", "PT", new Location(38.8, -9.2), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Lisbon-ES", "ES", new Location(36.0, -5.0), "Europe/Lisbon", false, false, false));
        indexes.add(new Station("Madrid-PT", "PT", new Location(40.4, -3.7), "Europe/Madrid", false, false, false));

        List<Station> result = indexes.byTimeZoneAndCountry("Europe/Lisbon", "PT");

        assertEquals(2, result.size());
        assertTrue(containsName(result, "Lisbon-PT-1"));
        assertTrue(containsName(result, "Lisbon-PT-2"));
        assertFalse(containsName(result, "Lisbon-ES"));
        assertFalse(containsName(result, "Madrid-PT"));

        try {
            indexes.byTimeZoneAndCountry(null, "PT");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            indexes.byTimeZoneAndCountry("Europe/Lisbon", "   ");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /* ---------- helper ---------- */

    private boolean containsName(List<Station> stations, String name) {
        for (Station s : stations) {
            if (name.equals(s.getName())) {
                return true;
            }
        }
        return false;
    }
}
