package domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of a radius search (USEI10).
 * <p>
 * Contains:
 * <ul>
 *   <li>An AVL tree of stations sorted by {@link DistanceKey} (distance ASC, name DESC)</li>
 *   <li>Density summaries by country</li>
 *   <li>Counts of city vs non-city stations</li>
 *   <li>The search center and radius used</li>
 * </ul>
 */
public class RadiusSearchResult {

    /** AVL tree with all stations within the radius, sorted by distance/name. */
    private final AVL<DistanceKey> sortedStations;

    /** Map of country code to number of stations within the radius. */
    private final Map<String, Integer> countByCountry;

    /** Number of stations where isCity = true. */
    private final int cityCount;

    /** Number of stations where isCity = false. */
    private final int nonCityCount;

    /** Search radius used (in kilometers). */
    private final double searchRadius;

    /** Center location used for the search. */
    private final Location searchCenter;

    /**
     * Creates a new radius search result.
     *
     * @param sortedStations AVL tree sorted by distance (ASC) and station name (DESC)
     * @param countByCountry map of country code to station count
     * @param cityCount      number of stations where isCity = true
     * @param nonCityCount   number of stations where isCity = false
     * @param searchRadius   radius used in the search (km)
     * @param searchCenter   center point of the search
     * @throws NullPointerException if any of {@code sortedStations}, {@code countByCountry}
     *                              or {@code searchCenter} is null
     */
    public RadiusSearchResult(AVL<DistanceKey> sortedStations,
                              Map<String, Integer> countByCountry,
                              int cityCount,
                              int nonCityCount,
                              double searchRadius,
                              Location searchCenter) {

        if (sortedStations == null) {
            throw new NullPointerException("sortedStations cannot be null");
        }
        if (countByCountry == null) {
            throw new NullPointerException("countByCountry cannot be null");
        }
        if (searchCenter == null) {
            throw new NullPointerException("searchCenter cannot be null");
        }

        this.sortedStations = sortedStations;
        this.countByCountry = new HashMap<>(countByCountry);
        this.cityCount = cityCount;
        this.nonCityCount = nonCityCount;
        this.searchRadius = searchRadius;
        this.searchCenter = searchCenter;
    }

    /**
     * Returns the AVL tree sorted by distance and station name.
     * <p>
     * This is the live tree used internally; callers should not modify its structure.
     */
    public AVL<DistanceKey> getSortedStations() {
        return sortedStations;
    }

    /**
     * Returns a defensive copy of the map of country → station count.
     */
    public Map<String, Integer> getCountByCountry() {
        return new HashMap<>(countByCountry);
    }

    /**
     * Returns the number of stations with {@code isCity = true}.
     */
    public int getCityCount() {
        return cityCount;
    }

    /**
     * Returns the number of stations with {@code isCity = false}.
     */
    public int getNonCityCount() {
        return nonCityCount;
    }

    /**
     * Returns the total number of stations inside the radius.
     */
    public int getTotalCount() {
        return sortedStations.size();
    }

    /**
     * Returns the search radius (in kilometers).
     */
    public double getSearchRadius() {
        return searchRadius;
    }

    /**
     * Returns the search center location.
     */
    public Location getSearchCenter() {
        return searchCenter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Radius Search Result:\n");
        sb.append(String.format("  Center: (%.5f, %.5f)%n",
                searchCenter.getLatitude(), searchCenter.getLongitude()));
        sb.append(String.format("  Radius: %.2f km%n", searchRadius));
        sb.append(String.format("  Total stations: %d%n", getTotalCount()));
        sb.append(String.format("  Cities: %d, Non-cities: %d%n",
                cityCount, nonCityCount));
        sb.append("  By country:\n");
        for (Map.Entry<String, Integer> entry : countByCountry.entrySet()) {
            sb.append(String.format("    %s: %d%n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
