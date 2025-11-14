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

    /**
     * Tests latitude window queries, including filtering
     * and the exceptional case where the minimum bound
     * is greater than the maximum bound.
     */
    @Test
    public void inLatitudeWindow() {
        StationIndexes indexes = new StationIndexes();

        Station sLow = new Station(
                "Low",
                "PT",
                new Location(10.0, 0.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station sMid = new Station(
                "Mid",
                "PT",
                new Location(20.0, 0.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station sHigh = new Station(
                "High",
                "PT",
                new Location(30.0, 0.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );

        indexes.add(sLow);
        indexes.add(sMid);
        indexes.add(sHigh);

        List<Station> result = indexes.inLatitudeWindow(15.0, 25.0);

        assertEquals(1, result.size());
        assertSame(sMid, result.get(0));

        try {
            indexes.inLatitudeWindow(50.0, 10.0);
            fail("Expected IllegalArgumentException when minLatitude > maxLatitude");
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Tests longitude window queries, including filtering
     * and the exceptional case where the minimum bound
     * is greater than the maximum bound.
     */
    @Test
    public void inLongitudeWindow() {
        StationIndexes indexes = new StationIndexes();

        Station sLeft = new Station(
                "Left",
                "PT",
                new Location(0.0, -10.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station sCenter = new Station(
                "Center",
                "PT",
                new Location(0.0, 0.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station sRight = new Station(
                "Right",
                "PT",
                new Location(0.0, 10.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );

        indexes.add(sLeft);
        indexes.add(sCenter);
        indexes.add(sRight);

        List<Station> result = indexes.inLongitudeWindow(-5.0, 5.0);

        assertEquals(1, result.size());
        assertSame(sCenter, result.get(0));

        try {
            indexes.inLongitudeWindow(5.0, -5.0);
            fail("Expected IllegalArgumentException when minLongitude > maxLongitude");
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Tests querying stations by time zone group.
     * Covers normal behavior and invalid parameter cases.
     */
    @Test
    public void byTimeZoneGroup() {
        StationIndexes indexes = new StationIndexes();

        Station lisPt = new Station(
                "Lisbon-PT",
                "PT",
                new Location(38.7, -9.1),
                "Europe/Lisbon",
                true,
                true,
                false
        );
        Station lisEs = new Station(
                "Lisbon-ES",
                "ES",
                new Location(36.0, -5.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station madPt = new Station(
                "Madrid-PT",
                "PT",
                new Location(40.4, -3.7),
                "Europe/Madrid",
                false,
                false,
                false
        );

        indexes.add(lisPt);
        indexes.add(lisEs);
        indexes.add(madPt);

        List<Station> result = indexes.byTimeZoneGroup("Europe/Lisbon");

        assertEquals(2, result.size());
        assertTrue(result.contains(lisPt));
        assertTrue(result.contains(lisEs));
        assertFalse(result.contains(madPt));

        try {
            indexes.byTimeZoneGroup(null);
            fail("Expected IllegalArgumentException for null timeZoneGroup");
        } catch (IllegalArgumentException expected) {
        }

        try {
            indexes.byTimeZoneGroup("   ");
            fail("Expected IllegalArgumentException for blank timeZoneGroup");
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * Tests querying stations by time zone group and country.
     * Covers correct filtering and invalid parameter cases.
     */
    @Test
    public void byTimeZoneAndCountry() {
        StationIndexes indexes = new StationIndexes();

        Station lisPt1 = new Station(
                "Lisbon-PT-1",
                "PT",
                new Location(38.7, -9.1),
                "Europe/Lisbon",
                true,
                true,
                false
        );
        Station lisPt2 = new Station(
                "Lisbon-PT-2",
                "PT",
                new Location(38.8, -9.2),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station lisEs = new Station(
                "Lisbon-ES",
                "ES",
                new Location(36.0, -5.0),
                "Europe/Lisbon",
                false,
                false,
                false
        );
        Station madPt = new Station(
                "Madrid-PT",
                "PT",
                new Location(40.4, -3.7),
                "Europe/Madrid",
                false,
                false,
                false
        );

        indexes.add(lisPt1);
        indexes.add(lisPt2);
        indexes.add(lisEs);
        indexes.add(madPt);

        List<Station> result = indexes.byTimeZoneAndCountry("Europe/Lisbon", "PT");

        assertEquals(2, result.size());
        assertTrue(result.contains(lisPt1));
        assertTrue(result.contains(lisPt2));
        assertFalse(result.contains(lisEs));
        assertFalse(result.contains(madPt));

        try {
            indexes.byTimeZoneAndCountry(null, "PT");
            fail("Expected IllegalArgumentException for null timeZoneGroup");
        } catch (IllegalArgumentException expected) {
        }

        try {
            indexes.byTimeZoneAndCountry("Europe/Lisbon", "   ");
            fail("Expected IllegalArgumentException for blank country");
        } catch (IllegalArgumentException expected) {
        }
    }
}
