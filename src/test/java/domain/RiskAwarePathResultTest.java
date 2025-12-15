package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link RiskAwarePathResult}.
 * <p>
 * Each test method targets exactly one public member of {@code RiskAwarePathResult}
 * and documents the scenario, preconditions and expected outcome.
 */
class RiskAwarePathResultTest {

    /**
     * Helper to create sample stations.
     */
    private Station createStation(int id, String name, double lat, double lon) {
        Location loc = new Location(lat, lon);
        return new Station(id, name, loc, 0.0, 0.0);
    }

    // =========================================================================
    // Constructor tests
    // =========================================================================

    /**
     * Verifies the constructor for a valid path result initializes all fields correctly.
     *
     * <p>Scenario:
     *   - Create a result with source, target, non-empty path and cost.
     *
     * <p>Expected:
     *   - hasNegativeCycle() returns false;
     *   - all fields match inputs;
     *   - negativeCycle is null;
     *   - isReachable() returns true.
     *
     * @see RiskAwarePathResult#RiskAwarePathResult(Station, Station, List, double)
     */
    @Test
    void constructor_validPath_initializesFieldsCorrectly() {
        Station source = createStation(1, "Brussels", 50.8503, 4.3517);
        Station target = createStation(2, "Antwerp", 51.2194, 4.4025);
        List<Station> path = Arrays.asList(source, target);

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, path, 42.75);

        assertFalse(result.hasNegativeCycle());
        assertSame(source, result.getSource());
        assertSame(target, result.getTarget());
        assertSame(path, result.getPath());
        assertEquals(42.75, result.getTotalCost(), 1e-9);
        assertNull(result.getNegativeCycle());
        assertTrue(result.isReachable());
    }

    /**
     * Verifies the constructor for negative cycle detection initializes fields correctly.
     *
     * <p>Scenario:
     *   - Create a result indicating a negative cycle with a cycle list.
     *
     * <p>Expected:
     *   - hasNegativeCycle() returns true;
     *   - getNegativeCycle() returns the provided list;
     *   - getSource(), getTarget(), getPath() return null;
     *   - getTotalCost() returns 0.0;
     *   - isReachable() returns false.
     *
     * @see RiskAwarePathResult#RiskAwarePathResult(List)
     */
    @Test
    void constructor_negativeCycle_initializesFieldsCorrectly() {
        Station a = createStation(100, "A", 50.0, 4.0);
        Station b = createStation(101, "B", 50.5, 4.5);
        Station c = createStation(102, "C", 51.0, 5.0);
        List<Station> cycle = Arrays.asList(a, b, c, a);

        RiskAwarePathResult result = new RiskAwarePathResult(cycle);

        assertTrue(result.hasNegativeCycle());
        assertSame(cycle, result.getNegativeCycle());

        assertNull(result.getSource());
        assertNull(result.getTarget());
        assertNull(result.getPath());
        assertEquals(0.0, result.getTotalCost(), 1e-9);
        assertFalse(result.isReachable());
    }

    // =========================================================================
    // isReachable tests
    // =========================================================================

    /**
     * Verifies isReachable returns false when path is null (unreachable target).
     *
     * <p>Scenario:
     *   - Valid-path constructor with path=null and cost=POSITIVE_INFINITY.
     *
     * <p>Expected:
     *   - isReachable() returns false.
     *
     * @see RiskAwarePathResult#isReachable()
     */
    @Test
    void isReachable_nullPath_returnsFalse() {
        Station source = createStation(10, "Isolated A", 48.0, 2.0);
        Station target = createStation(11, "Isolated B", 49.0, 3.0);

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, null, Double.POSITIVE_INFINITY);

        assertFalse(result.hasNegativeCycle());
        assertFalse(result.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost(), 0.0);
        assertNull(result.getPath());
    }

    /**
     * Verifies isReachable returns false when path is empty.
     *
     * <p>Scenario:
     *   - Valid-path constructor with an empty path list.
     *
     * <p>Expected:
     *   - isReachable() returns false.
     *
     * @see RiskAwarePathResult#isReachable()
     */
    @Test
    void isReachable_emptyPath_returnsFalse() {
        Station source = createStation(20, "X", 40.0, 5.0);
        Station target = createStation(21, "Y", 41.0, 6.0);
        List<Station> emptyPath = new ArrayList<>();

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, emptyPath, 0.0);

        assertFalse(result.hasNegativeCycle());
        assertFalse(result.isReachable());
        assertNotNull(result.getPath());
        assertTrue(result.getPath().isEmpty());
    }

    // =========================================================================
    // toString tests
    // =========================================================================

    /**
     * Verifies toString for negative cycle result contains expected information.
     *
     * <p>Scenario:
     *   - Result created with negative-cycle constructor.
     *
     * <p>Expected:
     *   - Output contains "negative cycle detected" and station count text.
     *
     * @see RiskAwarePathResult#toString()
     */
    @Test
    void toString_negativeCycle_containsExpectedInfo() {
        Station a = createStation(1, "A", 50.0, 4.0);
        Station b = createStation(2, "B", 50.5, 4.5);
        List<Station> cycle = Arrays.asList(a, b, a);

        RiskAwarePathResult result = new RiskAwarePathResult(cycle);

        String out = result.toString();
        assertTrue(out.contains("negative cycle detected"));
        assertTrue(out.contains("stations"));
    }

    /**
     * Verifies toString for unreachable path (path is null) contains expected information.
     *
     * <p>Scenario:
     *   - Result created with valid-path constructor but path=null.
     *
     * <p>Expected:
     *   - Output contains "no path found" and endpoint names.
     *
     * @see RiskAwarePathResult#toString()
     */
    @Test
    void toString_noPathFound_containsExpectedInfo() {
        Station source = createStation(1, "Brussels", 50.8503, 4.3517);
        Station target = createStation(2, "Paris", 48.8566, 2.3522);

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, null, Double.POSITIVE_INFINITY);

        String out = result.toString();
        assertTrue(out.contains("no path found"));
        assertTrue(out.contains("Brussels"));
        assertTrue(out.contains("Paris"));
    }

    /**
     * Verifies toString for valid path contains expected path length and cost.
     *
     * <p>Scenario:
     *   - Result created with valid-path constructor and non-empty path.
     *
     * <p>Expected:
     *   - Output contains "path length" and "total cost" with the correct values.
     *
     * @see RiskAwarePathResult#toString()
     */
    @Test
    void toString_validPath_containsExpectedInfo() {
        Station source = createStation(1, "A", 50.0, 4.0);
        Station target = createStation(2, "B", 51.0, 5.0);
        List<Station> path = Arrays.asList(source, target);

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, path, 42.75);

        String out = result.toString();
        assertTrue(out.contains("path length=2"));
        assertTrue(out.contains("total cost=42.75"));
    }

    /**
     * Verifies toString for unreachable path (empty path list) contains expected information.
     *
     * <p>Scenario:
     *   - Result created with valid-path constructor but path is an empty list (not null).
     *
     * <p>Expected:
     *   - Output contains "no path found" and endpoint names.
     *
     * @see RiskAwarePathResult#toString()
     */
    @Test
    void toString_emptyPath_containsExpectedInfo() {
        Station source = createStation(1, "Brussels", 50.8503, 4.3517);
        Station target = createStation(2, "Paris", 48.8566, 2.3522);
        List<Station> emptyPath = new ArrayList<>();

        RiskAwarePathResult result = new RiskAwarePathResult(source, target, emptyPath, 0.0);

        String out = result.toString();
        assertTrue(out.contains("no path found"));
        assertTrue(out.contains("Brussels"));
        assertTrue(out.contains("Paris"));
    }

}
