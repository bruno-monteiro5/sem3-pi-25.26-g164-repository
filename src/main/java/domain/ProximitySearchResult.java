package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Result of a proximity search containing the nearest stations with distances.
 * <p>
 * Maintains stations sorted by distance ascending, with station name as tie-breaker.
 */
public class ProximitySearchResult {

    private final double targetLatitude;
    private final double targetLongitude;
    private final int requestedCount;
    private final TimeZoneFilter filter;
    private final List<StationDistance> results;

    /**
     * Creates a new proximity search result.
     *
     * @param targetLatitude target latitude
     * @param targetLongitude target longitude
     * @param requestedCount number of neighbors requested
     * @param filter time zone filter applied (may be disabled)
     */
    public ProximitySearchResult(double targetLatitude, double targetLongitude,
                                 int requestedCount, TimeZoneFilter filter) {
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.requestedCount = requestedCount;
        this.filter = filter;
        this.results = new ArrayList<>();
    }

    /**
     * Adds a station with its distance to the results.
     *
     * @param station the station
     * @param distanceKm distance in kilometers
     */
    public void addResult(Station station, double distanceKm) {
        results.add(new StationDistance(station, distanceKm));
    }

    /**
     * Sorts the results by distance ascending, then by station name ascending.
     */
    public void sortResults() {
        Collections.sort(results, new Comparator<StationDistance>() {
            @Override
            public int compare(StationDistance sd1, StationDistance sd2) {
                int distCompare = Double.compare(sd1.getDistanceKm(), sd2.getDistanceKm());
                if (distCompare != 0) {
                    return distCompare;
                }
                return sd1.getStation().getName().compareTo(sd2.getStation().getName());
            }
        });
    }

    /**
     * Gets the target latitude.
     *
     * @return target latitude in decimal degrees
     */
    public double getTargetLatitude() {
        return targetLatitude;
    }

    /**
     * Gets the target longitude.
     *
     * @return target longitude in decimal degrees
     */
    public double getTargetLongitude() {
        return targetLongitude;
    }

    /**
     * Gets the number of neighbors requested.
     *
     * @return requested count
     */
    public int getRequestedCount() {
        return requestedCount;
    }

    /**
     * Gets the time zone filter applied.
     *
     * @return the filter
     */
    public TimeZoneFilter getFilter() {
        return filter;
    }

    /**
     * Gets the list of results.
     *
     * @return unmodifiable list of station-distance pairs
     */
    public List<StationDistance> getResults() {
        return Collections.unmodifiableList(results);
    }

    /**
     * Gets the actual number of results found.
     *
     * @return number of results
     */
    public int getResultCount() {
        return results.size();
    }

    /**
     * Represents a station with its distance from the target point.
     */
    public static class StationDistance {
        private final Station station;
        private final double distanceKm;

        public StationDistance(Station station, double distanceKm) {
            this.station = station;
            this.distanceKm = distanceKm;
        }

        public Station getStation() {
            return station;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        @Override
        public String toString() {
            return String.format("%.3f km - %s", distanceKm, station.getName());
        }
    }
}