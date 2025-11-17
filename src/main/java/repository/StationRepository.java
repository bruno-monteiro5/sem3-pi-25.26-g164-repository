package repository;

import domain.Station;
import domain.StationIndexes;

import java.util.List;

/**
 * Repository responsible for storing and querying Station data through
 * AVL-based indexes. It is the single source of truth for Station queries.
 *
 * Singleton pattern ensures all controllers share the same data.
 */
public class StationRepository {

    /** Singleton instance */
    private static StationRepository instance = null;

    /** Holds all AVL indexes over stations. */
    private final StationIndexes stationIndexes = new StationIndexes();

    /** Counters for load process diagnostics. */
    private int totalLoadedCount = 0;
    private int totalRejectedCount = 0;

    /**
     * Private constructor to prevent external instantiation.
     */
    private StationRepository() {
    }

    /**
     * Returns the singleton instance of StationRepository.
     */
    public static synchronized StationRepository getInstance() {
        if (instance == null) {
            instance = new StationRepository();
        }
        return instance;
    }

    /**
     * Loads and indexes all stations from a provided list.
     * <p>
     * This method assumes Station/Location constructors have enforced domain invariants.
     * Any malformed object should have been filtered out before reaching here; however,
     * null checks and defensive catches are kept for safety.
     *
     * @param stationList list of Station objects to be indexed (may be empty, but not null)
     * @throws NullPointerException if stationList is null
     */
    public void loadAll(List<Station> stationList) {
        if (stationList == null) {
            throw new NullPointerException("stationList cannot be null");
        }

        totalLoadedCount = 0;
        totalRejectedCount = 0;

        for (Station station : stationList) {
            try {
                if (station == null) {
                    totalRejectedCount++;
                } else {
                    stationIndexes.add(station);
                    totalLoadedCount++;
                }
            } catch (RuntimeException exception) {
                totalRejectedCount++;
            }
        }
    }

    /** Returns the number of successfully loaded stations. */
    public int getTotalLoadedCount() {
        return totalLoadedCount;
    }

    /** Returns the number of rejected stations. */
    public int getTotalRejectedCount() {
        return totalRejectedCount;
    }

    /** Returns stations in the inclusive latitude window [minLatitude, maxLatitude]. */
    public List<Station> findByLatitudeWindow(double minLatitude, double maxLatitude) {
        return stationIndexes.inLatitudeWindow(minLatitude, maxLatitude);
    }

    /** Returns stations in the inclusive longitude window [minLongitude, maxLongitude]. */
    public List<Station> findByLongitudeWindow(double minLongitude, double maxLongitude) {
        return stationIndexes.inLongitudeWindow(minLongitude, maxLongitude);
    }

    /** Returns stations for a time zone group, ordered by (country, station name). */
    public List<Station> findByTimeZoneGroup(String timeZoneGroup) {
        return stationIndexes.byTimeZoneGroup(timeZoneGroup);
    }

    /** Returns stations for a (time zone group, country), ordered by station name. */
    public List<Station> findByTimeZoneAndCountry(String timeZoneGroup, String country) {
        return stationIndexes.byTimeZoneAndCountry(timeZoneGroup, country);
    }

    /**
     * Returns the internal StationIndexes object for advanced queries.
     */
    public StationIndexes getStationIndexes() {
        return stationIndexes;
    }

    /**
     * Checks if indexes have been built (data loaded).
     */
    public boolean isLoaded() {
        return totalLoadedCount > 0;
    }

    /**
     * Clears all loaded data (for testing purposes).
     */
    public void clear() {
        totalLoadedCount = 0;
        totalRejectedCount = 0;
        // Note: StationIndexes doesn't have a clear method,
        // stationIndexes = new StationIndexes();
    }
}