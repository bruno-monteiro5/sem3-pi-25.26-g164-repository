package domain;

/**
 * Filter for proximity search based on time zone criteria.
 * <p>
 * Allows filtering stations by time zone group during nearest-neighbor queries.
 */
public class TimeZoneFilter {

    private final String timeZoneGroup;
    private final boolean enabled;

    /**
     * Creates a disabled filter (no filtering applied).
     */
    public TimeZoneFilter() {
        this.timeZoneGroup = null;
        this.enabled = false;
    }

    /**
     * Creates an enabled filter for a specific time zone group.
     *
     * @param timeZoneGroup the time zone group to filter by (non-null, non-blank)
     * @throws IllegalArgumentException if timeZoneGroup is null or blank
     */
    public TimeZoneFilter(String timeZoneGroup) {
        if (timeZoneGroup == null || timeZoneGroup.trim().isEmpty()) {
            throw new IllegalArgumentException("timeZoneGroup cannot be null or blank");
        }
        this.timeZoneGroup = timeZoneGroup.trim();
        this.enabled = true;
    }

    /**
     * Checks if this filter is enabled.
     *
     * @return true if filtering is active, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the time zone group to filter by.
     *
     * @return the time zone group, or null if filter is disabled
     */
    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    /**
     * Tests if a station matches this filter.
     *
     * @param station the station to test
     * @return true if the station passes the filter (or filter is disabled), false otherwise
     */
    public boolean matches(Station station) {
        if (!enabled) {
            return true;
        }
        if (station == null) {
            return false;
        }
        String stationTz = station.getTimeZoneGroup();
        if (stationTz == null) {
            return false;
        }
        return timeZoneGroup.equals(stationTz);
    }

    @Override
    public String toString() {
        if (!enabled) {
            return "TimeZoneFilter{disabled}";
        }
        return "TimeZoneFilter{timeZoneGroup='" + timeZoneGroup + "'}";
    }
}