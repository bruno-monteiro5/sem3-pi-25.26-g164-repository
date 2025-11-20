package domain;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test suite for {@link RadiusSearchResult}.
 * <p>
 * Each test method targets exactly one public member of {@code RadiusSearchResult}
 * and documents the scenario, preconditions and expected outcome.
 */
public class RadiusSearchResultTest {

    /** Helper to create a valid sample RadiusSearchResult. */
    private RadiusSearchResult createSampleResult() {

        AVL<DistanceKey> tree = new AVL<>();

        Location loc1 = new Location(41.15, -8.61);
        Location loc2 = new Location(40.64, -8.65);

        Station s1 = new Station("Porto", "PT", loc1,
                "Europe/Lisbon", true, false, false);
        Station s2 = new Station("Aveiro", "PT", loc2,
                "Europe/Lisbon", false, false, false);

        tree.insert(new DistanceKey(10.0, s1));
        tree.insert(new DistanceKey(20.0, s2));

        Map<String, Integer> counts = new HashMap<>();
        counts.put("PT", 2);
        counts.put("ES", 1);

        return new RadiusSearchResult(
                tree,
                counts,
                1,
                2,
                50.0,
                new Location(41.15, -8.61)
        );
    }

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    /**
     * Verifies that the constructor enforces non-null sortedStations.
     *
     * <p>Scenario:
     *   - Call constructor with {@code null} as the AVL tree.
     *
     * <p>Expected:
     *   - A {@link NullPointerException} is thrown.
     *
     * @see RadiusSearchResult#RadiusSearchResult(AVL, Map, int, int, double, Location)
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullSortedStations_throwsException() {
        Map<String, Integer> map = new HashMap<>();
        new RadiusSearchResult(null, map, 0, 0, 10.0, new Location(0, 0));
    }

    /**
     * Verifies that the constructor enforces non-null country-count map.
     *
     * <p>Scenario:
     *   - Call constructor with {@code null} as the map.
     *
     * <p>Expected:
     *   - {@link NullPointerException} is thrown.
     *
     * @see RadiusSearchResult#RadiusSearchResult(AVL, Map, int, int, double, Location)
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullCountByCountry_throwsException() {
        AVL<DistanceKey> tree = new AVL<>();
        new RadiusSearchResult(tree, null, 0, 0, 10.0, new Location(0, 0));
    }

    /**
     * Verifies that the constructor enforces non-null searchCenter.
     *
     * <p>Scenario:
     *   - Call constructor with {@code null} as the center location.
     *
     * <p>Expected:
     *   - {@link NullPointerException} is thrown.
     *
     * @see RadiusSearchResult#RadiusSearchResult(AVL, Map, int, int, double, Location)
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullSearchCenter_throwsException() {
        AVL<DistanceKey> tree = new AVL<>();
        Map<String, Integer> map = new HashMap<>();
        new RadiusSearchResult(tree, map, 0, 0, 10.0, null);
    }

    // -------------------------------------------------------------------------
    // Getters and behaviors
    // -------------------------------------------------------------------------

    /**
     * Verifies that getSortedStations returns the stored AVL instance.
     *
     * <p>Scenario:
     *   - Create a sample result and retrieve the AVL tree.
     *
     * <p>Expected:
     *   - Tree is not null and has correct size.
     *
     * @see RadiusSearchResult#getSortedStations()
     */
    @Test
    public void getSortedStations_returnsStoredTree() {
        RadiusSearchResult result = createSampleResult();
        AVL<DistanceKey> tree = result.getSortedStations();

        assertNotNull(tree);
        assertEquals(2, tree.size());
    }

    /**
     * Verifies that getCountByCountry returns a defensive copy.
     *
     * <p>Scenario:
     *   - Modify the returned map and ensure internal map is unchanged.
     *
     * <p>Expected:
     *   - Each call returns a new map instance, independent from modifications.
     *
     * @see RadiusSearchResult#getCountByCountry()
     */
    @Test
    public void getCountByCountry_returnsDefensiveCopy() {
        RadiusSearchResult result = createSampleResult();

        Map<String, Integer> m1 = result.getCountByCountry();
        Map<String, Integer> m2 = result.getCountByCountry();

        assertNotSame(m1, m2);

        m1.put("FR", 10);

        Map<String, Integer> m3 = result.getCountByCountry();
        assertFalse(m3.containsKey("FR"));
    }

    /**
     * Verifies city count getter.
     *
     * <p>Scenario:
     *   - Use sample result with a known cityCount.
     *
     * <p>Expected:
     *   - Returned value matches constructor argument.
     *
     * @see RadiusSearchResult#getCityCount()
     */
    @Test
    public void getCityCount_returnsValue() {
        assertEquals(1, createSampleResult().getCityCount());
    }

    /**
     * Verifies non-city count getter.
     *
     * <p>Scenario:
     *   - Use sample result with known nonCityCount.
     *
     * <p>Expected:
     *   - Returned value matches constructor argument.
     *
     * @see RadiusSearchResult#getNonCityCount()
     */
    @Test
    public void getNonCityCount_returnsValue() {
        assertEquals(2, createSampleResult().getNonCityCount());
    }

    /**
     * Verifies totalCount is the size of the sortedStations AVL.
     *
     * <p>Scenario:
     *   - Sample result contains exactly 2 entries.
     *
     * <p>Expected:
     *   - getTotalCount() returns 2.
     *
     * @see RadiusSearchResult#getTotalCount()
     */
    @Test
    public void getTotalCount_returnsTreeSize() {
        assertEquals(2, createSampleResult().getTotalCount());
    }

    /**
     * Verifies search radius getter.
     *
     * <p>Scenario:
     *   - Sample result radius = 50.0 km.
     *
     * <p>Expected:
     *   - getSearchRadius() returns 50.0.
     *
     * @see RadiusSearchResult#getSearchRadius()
     */
    @Test
    public void getSearchRadius_returnsValue() {
        assertEquals(50.0, createSampleResult().getSearchRadius(), 1e-6);
    }

    /**
     * Verifies center location getter.
     *
     * <p>Scenario:
     *   - Sample has a known Location instance.
     *
     * <p>Expected:
     *   - Returned location matches original latitude/longitude.
     *
     * @see RadiusSearchResult#getSearchCenter()
     */
    @Test
    public void getSearchCenter_returnsLocation() {
        Location c = createSampleResult().getSearchCenter();
        assertEquals(41.15, c.getLatitude(), 1e-6);
        assertEquals(-8.61, c.getLongitude(), 1e-6);
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Verifies that toString contains all expected summary fields.
     *
     * <p>Scenario:
     *   - Sample result with known values for coordinates, radius and counts.
     *
     * <p>Expected:
     *   - Output contains formatted center, radius, totals and country counts.
     *
     * @see RadiusSearchResult#toString()
     */
    @Test
    public void testToString_containsExpectedData() {

        RadiusSearchResult result = createSampleResult();
        String str = result.toString();

        assertTrue(str.contains("Radius Search Result"));
        assertTrue(str.contains("41,15000"));
        assertTrue(str.contains("-8,61000"));
        assertTrue(str.contains("Radius: 50,00"));
        assertTrue(str.contains("Total stations: 2"));
        assertTrue(str.contains("Cities: 1, Non-cities: 2"));
        assertTrue(str.contains("PT: 2"));
        assertTrue(str.contains("ES: 1"));
    }
}
