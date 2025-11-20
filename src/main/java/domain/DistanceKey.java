package domain;

/**
 * Composite key for sorting stations by distance (ascending) and name (descending).
 * <p>
 * Used in USEI10 radius search results.
 */
public class DistanceKey implements Comparable<DistanceKey> {

    /** Distance from the search center in kilometers. */
    private final double distance;

    /** Station name (used as tie-breaker). */
    private final String stationName;

    /** Station reference. */
    private final Station station;

    /**
     * Creates a new distance key.
     *
     * @param distance distance in kilometers from the search point
     * @param station  the station object (must not be null)
     * @throws NullPointerException if {@code station} is null
     */
    public DistanceKey(double distance, Station station) {
        if (station == null) {
            throw new NullPointerException("station cannot be null");
        }
        this.distance = distance;
        this.stationName = station.getName();
        this.station = station;
    }

    /**
     * Compares by distance ascending, then by station name descending.
     *
     * @param other the other key to compare to
     * @return negative if this &lt; other, positive if this &gt; other, 0 if equal
     */
    @Override
    public int compareTo(DistanceKey other) {
        // First: distance ascending
        int distCmp = Double.compare(this.distance, other.distance);
        if (distCmp != 0) {
            return distCmp;
        }

        // Tie-breaker: station name descending (reverse alphabetical order)
        return other.stationName.compareTo(this.stationName);
    }

    /**
     * Returns the distance in kilometers from the search center.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the station name.
     */
    public String getStationName() {
        return stationName;
    }

    /**
     * Returns the station associated with this key.
     */
    public Station getStation() {
        return station;
    }

    @Override
    public String toString() {
        return String.format("%.3f km - %s", distance, stationName);
    }
}
