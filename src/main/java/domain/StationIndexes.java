package domain;


import java.util.List;

/**
 * StationIndexes keeps three independent AVL indexes over Station:
 * <ul>
 *   <li>by latitude, ordered as (latitude, station name)</li>
 *   <li>by longitude, ordered as (longitude, station name)</li>
 *   <li>by time zone group, then country, then name (group → country → name)</li>
 * </ul>
 * <p>
 * Rationale:
 * <ul>
 *   <li>Using composite keys (LatKey/LonKey/TZKey) ensures that stations sharing
 *       the same latitude/longitude are all kept, with ties broken by name.</li>
 *   <li>All range queries run in {@code O(log n + k)} (where k is the number of
 *       results) provided that {@code PL.BST.range(low, high)} is implemented.</li>
 *   <li>Domain validation is performed at Station/Location construction time,
 *       so this class only indexes already-valid Stations.</li>
 * </ul>
 */

public class StationIndexes {


    /** Internal AVL indexes. */
    private AVL<LatKey> byLatitude = new AVL<>();
    private AVL<LonKey> byLongitude = new AVL<>();
    private AVL<TZKey> byTzGroup = new AVL<>();

    /**
     * Inserts a valid Station into all indexes.
     * <p>
     * Precondition: {@link Station} (and its {@link Location}) already enforced
     * domain invariants (non-empty name/country/group and valid coordinates).
     *
     * @param s the station to index
     * @throws NullPointerException if {@code s} is null
     */
    public void add(Station s) {
        if (s == null) throw new NullPointerException("station is null");
        byLatitude.insert(new LatKey(s));
        byLongitude.insert(new LonKey(s));
        byTzGroup.insert(new TZKey(s));
    }







}
