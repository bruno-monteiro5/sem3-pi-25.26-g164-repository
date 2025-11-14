package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * StationIndexes maintains three AVL-based indexes over Station objects:
 * <ul>
 *   <li>Latitude index – ordered by (latitude, station name)</li>
 *   <li>Longitude index – ordered by (longitude, station name)</li>
 *   <li>Time zone index – ordered by (time zone group, country, name)</li>
 * </ul>
 * <p>
 * This version does not modify the professor's BST or AVL classes.
 * The "window queries" (interval searches) are implemented by scanning
 * the in-order traversal of each tree, which is already sorted.
 * Each query runs in O(n), but stops early when the upper bound is exceeded.
 */
public class StationIndexes {

    /** AVL index for latitude-based ordering. */
    private final AVL<LatKey> latitudeIndex = new AVL<>();

    /** AVL index for longitude-based ordering. */
    private final AVL<LonKey> longitudeIndex = new AVL<>();

    /** AVL index for time zone (group → country → name) ordering. */
    private final AVL<TZKey> timeZoneIndex = new AVL<>();

    /** Sentinel strings used to close open-ended string ranges. */
    private static final String LOWEST  = "!";
    private static final String HIGHEST = "\uFFFF";

    /**
     * Inserts a valid Station into all indexes.
     * <p>
     * Precondition: Station and Location constructors already enforce
     * domain invariants such as valid coordinates and non-empty strings.
     *
     * @param station the Station object to insert
     * @throws NullPointerException if the station is null
     */
    public void add(Station station) {
        if (station == null) {
            throw new NullPointerException("Station cannot be null");
        }
        latitudeIndex.insert(new LatKey(station));
        longitudeIndex.insert(new LonKey(station));
        timeZoneIndex.insert(new TZKey(station));
    }

    /**
     * Returns all stations with latitude between {@code minLatitude} and {@code maxLatitude}
     * (inclusive), ordered by (latitude, station name).
     * <p>
     * Uses in-order traversal with filtering; complexity is O(n).
     *
     * @param minLatitude minimum latitude (inclusive)
     * @param maxLatitude maximum latitude (inclusive)
     * @return list of stations in the given latitude window
     * @throws IllegalArgumentException if {@code minLatitude > maxLatitude}
     */
    public List<Station> inLatitudeWindow(double minLatitude, double maxLatitude) {
        if (minLatitude > maxLatitude) {
            throw new IllegalArgumentException("minLatitude cannot be greater than maxLatitude");
        }

        LatKey lowerBoundary = createLatitudeBoundary(minLatitude, LOWEST);
        LatKey upperBoundary = createLatitudeBoundary(maxLatitude, HIGHEST);

        List<LatKey> latitudeKeys = filterWindow(latitudeIndex.inOrder(), lowerBoundary, upperBoundary);
        return unwrapLatitude(latitudeKeys);
    }

    /**
     * Returns all stations with longitude between {@code minLongitude} and {@code maxLongitude}
     * (inclusive), ordered by (longitude, station name).
     * <p>
     * Uses in-order traversal with filtering; complexity is O(n).
     *
     * @param minLongitude minimum longitude (inclusive)
     * @param maxLongitude maximum longitude (inclusive)
     * @return list of stations in the given longitude window
     * @throws IllegalArgumentException if {@code minLongitude > maxLongitude}
     */
    public List<Station> inLongitudeWindow(double minLongitude, double maxLongitude) {
        if (minLongitude > maxLongitude) {
            throw new IllegalArgumentException("minLongitude cannot be greater than maxLongitude");
        }

        LonKey lowerBoundary = createLongitudeBoundary(minLongitude, LOWEST);
        LonKey upperBoundary = createLongitudeBoundary(maxLongitude, HIGHEST);

        List<LonKey> longitudeKeys = filterWindow(longitudeIndex.inOrder(), lowerBoundary, upperBoundary);
        return unwrapLongitude(longitudeKeys);
    }

    /**
     * Returns all stations belonging to a given time zone group,
     * ordered by (country, station name).
     *
     * @param timeZoneGroup time zone group (non-blank)
     * @return list of stations in the given time zone group
     * @throws IllegalArgumentException if {@code timeZoneGroup} is null or blank
     */
    public List<Station> byTimeZoneGroup(String timeZoneGroup) {
        if (isBlank(timeZoneGroup)) {
            throw new IllegalArgumentException("timeZoneGroup cannot be blank");
        }

        TZKey lowerBoundary = createTimeZoneBoundary(timeZoneGroup, LOWEST, LOWEST);
        TZKey upperBoundary = createTimeZoneBoundary(timeZoneGroup, HIGHEST, HIGHEST);

        List<TZKey> timeZoneKeys = filterWindow(timeZoneIndex.inOrder(), lowerBoundary, upperBoundary);
        return unwrapTimeZone(timeZoneKeys);
    }

    /**
     * Returns all stations belonging to a given time zone group and country,
     * ordered by station name.
     *
     * @param timeZoneGroup time zone group (non-blank)
     * @param country country name (non-blank)
     * @return list of stations in the given (timeZoneGroup, country)
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public List<Station> byTimeZoneAndCountry(String timeZoneGroup, String country) {
        if (isBlank(timeZoneGroup)) {
            throw new IllegalArgumentException("timeZoneGroup cannot be blank");
        }
        if (isBlank(country)) {
            throw new IllegalArgumentException("country cannot be blank");
        }

        TZKey lowerBoundary = createTimeZoneBoundary(timeZoneGroup, country, LOWEST);
        TZKey upperBoundary = createTimeZoneBoundary(timeZoneGroup, country, HIGHEST);

        List<TZKey> timeZoneKeys = filterWindow(timeZoneIndex.inOrder(), lowerBoundary, upperBoundary);
        return unwrapTimeZone(timeZoneKeys);
    }


    /**
     * Filters an in-order (sorted) iterable, returning elements within [lower, upper] (inclusive).
     * <p>
     * This method stops early when an element exceeds the upper bound.
     *
     * @param <E> element type (must implement Comparable)
     * @param sortedElements sorted iterable (from AVL.inOrder())
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return list of elements inside the inclusive window
     */
    private  <E extends Comparable<E>> List<E> filterWindow(
            Iterable<E> sortedElements, E lower, E upper) {

        List<E> resultList = new ArrayList<>();

        for (E element : sortedElements) {
            int compareToLower = element.compareTo(lower);

            if (compareToLower >= 0) {
                int compareToUpper = element.compareTo(upper);
                if (compareToUpper > 0) {
                    break;
                }
                resultList.add(element);
            }
        }

        return resultList;
    }

    /** Creates a valid LatKey boundary for (latitude, name). */
    private  LatKey createLatitudeBoundary(double latitude, String name) {
        Station boundaryStation = new Station(
                name, "ZZZ",
                new Location(latitude, 0),
                "ZZZ",
                false, false, false);
        return new LatKey(boundaryStation);
    }

    /** Creates a valid LonKey boundary for (longitude, name). */
    private  LonKey createLongitudeBoundary(double longitude, String name) {
        Station boundaryStation = new Station(
                name, "ZZZ",
                new Location(0, longitude),
                "ZZZ",
                false, false, false);
        return new LonKey(boundaryStation);
    }

    /** Creates a valid TZKey boundary for (group, country, name). */
    private  TZKey createTimeZoneBoundary(String group, String country, String name) {
        Station boundaryStation = new Station(
                name, country,
                new Location(0, 0),
                group,
                false, false, false);
        return new TZKey(boundaryStation);
    }

    /** Converts a list of LatKey objects to their Station references. */
    private  List<Station> unwrapLatitude(List<LatKey> latitudeKeys) {
        List<Station> stationList = new ArrayList<>(latitudeKeys.size());
        for (LatKey key : latitudeKeys) {
            stationList.add(key.getStation());
        }
        return stationList;
    }

    /** Converts a list of LonKey objects to their Station references. */
    private  List<Station> unwrapLongitude(List<LonKey> longitudeKeys) {
        List<Station> stationList = new ArrayList<>(longitudeKeys.size());
        for (LonKey key : longitudeKeys) {
            stationList.add(key.getStation());
        }
        return stationList;
    }

    /** Converts a list of TZKey objects to their Station references. */
    private  List<Station> unwrapTimeZone(List<TZKey> timeZoneKeys) {
        List<Station> stationList = new ArrayList<>(timeZoneKeys.size());
        for (TZKey key : timeZoneKeys) {
            stationList.add(key.getStation());
        }
        return stationList;
    }

    /** Checks whether a string is null or blank. */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
