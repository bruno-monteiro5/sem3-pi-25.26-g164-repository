package domain;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TimeZoneFilter}.
 * <p>
 * These tests cover constructor behavior, enabled/disabled state,
 */
public class TimeZoneFilterTest {

    /**
     * Tests that the no-argument constructor creates a disabled filter.
     */
    @Test
    public void testIsEnabledDisabled() {
        TimeZoneFilter filter = new TimeZoneFilter();
        assertFalse("Default constructor should create a disabled filter", filter.isEnabled());
        assertNull("Disabled filter should have null time zone group", filter.getTimeZoneGroup());
    }

    /**
     * Tests that the constructor with a valid time zone group
     * creates an enabled filter and trims the input.
     */
    @Test
    public void testIsEnabledEnabled() {
        TimeZoneFilter filter = new TimeZoneFilter("  EU  ");
        assertTrue("Filter with a non-blank group should be enabled", filter.isEnabled());
        assertEquals("Time zone group should be trimmed", "EU", filter.getTimeZoneGroup());
    }

    /**
     * Ensures that providing a null time zone group throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullThrows() {
        new TimeZoneFilter(null);
    }

    /**
     * Ensures that providing a blank time zone group throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBlankThrows() {
        new TimeZoneFilter("   ");
    }

    /**
     * Tests the matches method for both disabled and enabled filters
     * and for matching and non-matching stations.
     */
    @Test
    public void testMatches() {
        // Disabled filter: should always return true
        TimeZoneFilter disabled = new TimeZoneFilter();
        assertTrue("Disabled filter should accept null station", disabled.matches(null));
        assertTrue("Disabled filter should accept any station",
                disabled.matches(createStationWithTz("EU")));

        // Enabled filter with group "EU"
        TimeZoneFilter filter = new TimeZoneFilter("EU");

        // station == null → false
        assertFalse("Enabled filter should reject null station", filter.matches(null));

        // Matching time zone
        assertTrue("Station with same time zone group should match",
                filter.matches(createStationWithTz("EU")));

        // Non-matching time zone
        assertFalse("Station with different time zone group should not match",
                filter.matches(createStationWithTz("NA")));
    }

    /**
     * Tests the string representation of the filter
     * for both enabled and disabled states.
     */
    @Test
    public void testToString() {
        TimeZoneFilter disabled = new TimeZoneFilter();
        assertEquals("TimeZoneFilter{disabled}", disabled.toString());

        TimeZoneFilter enabled = new TimeZoneFilter("EU");
        assertEquals("TimeZoneFilter{timeZoneGroup='EU'}", enabled.toString());
    }

    /**
     * Helper method to create a Station with a given time zone group.
     *
     * @param tz time zone group to assign to the station
     * @return a Station instance with the given time zone group
     */
    private Station createStationWithTz(String tz) {
        // Adjust the Location constructor if your class is different
        Location loc = new Location(0.0, 0.0);
        return new Station("StationX", "PT", loc, tz, false, false, false);
    }
}
