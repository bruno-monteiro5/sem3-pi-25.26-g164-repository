package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link KDTree2D}.
 * <p>
 * Covers:
 * <ul>
 *     <li>Balanced build from AVL indexes (empty and non-empty)</li>
 *     <li>Axis alternation and recursive structure</li>
 *     <li>Bucket grouping for stations sharing the same coordinate</li>
 *     <li>Size, height and bucket size distribution</li>
 *     <li>{@link KDTree2D.KDTreeStats} snapshot and toString()</li>
 * </ul>
 */
class KDTree2DTest {

    /** Helper to create a Location (adjust constructor if needed). */
    private Location loc(double lat, double lon) {
        return new Location(lat, lon);
    }

    /** Helper to create a Station with minimal valid data. */
    private Station st(String name, double lat, double lon) {
        // Adjust to your Station constructor if needed
        return new Station(name, "PT", loc(lat, lon), "WET",
                false, false, false);
    }

    /** Helper to build a latitude AVL index from a list of stations. */
    private AVL<LatKey> buildLatIndex(List<Station> stations) {
        AVL<LatKey> avl = new AVL<>();
        for (Station s : stations) {
            avl.insert(new LatKey(s));
        }
        return avl;
    }

    /** Helper to build a longitude AVL index from a list of stations. */
    private AVL<LonKey> buildLonIndex(List<Station> stations) {
        AVL<LonKey> avl = new AVL<>();
        for (Station s : stations) {
            avl.insert(new LonKey(s));
        }
        return avl;
    }

    /**
     * buildBalanced: when latitude index is empty, tree must be empty.
     * <p>
     * Expected:
     *  - root == null
     *  - size == 0
     *  - height == -1
     *  - bucket distribution is empty
     */
    @Test
    void buildBalanced_withEmptyIndex_producesEmptyTree() {
        KDTree2D tree = new KDTree2D();

        AVL<LatKey> latIndex = new AVL<>();
        AVL<LonKey> lonIndex = new AVL<>();

        tree.buildBalanced(latIndex, lonIndex);

        assertNull(tree.getRoot());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertTrue(tree.getBucketSizeDistribution().isEmpty());
    }

    /**
     * buildBalanced: builds a balanced tree with alternating axes and correct size.
     * <p>
     * Construction:
     *  - 4 stations:
     *      C (-10, 0)
     *      A (  0, 0)
     *      B (  0, 0)  -> same coordinates as A (bucket of size 2)
     *      D (+10, 0)
     *
     * Expected after build:
     *  - size = 4
     *  - root axis = 0 (latitude)
     *  - height = 1 (root + 2 children)
     *  - bucket distribution:
     *      2-station bucket: 1
     *      1-station bucket: 2
     */
    @Test
    void buildBalanced_nonEmpty_buildsTreeWithBucketsAndAlternatingAxes() {
        KDTree2D tree = new KDTree2D();

        Station sC = st("C", -10.0, 0.0);
        Station sA = st("A",   0.0, 0.0);
        Station sB = st("B",   0.0, 0.0); // same coords as A
        Station sD = st("D",  10.0, 0.0);

        List<Station> stations = List.of(sC, sA, sB, sD);

        AVL<LatKey> latIndex = buildLatIndex(stations);
        AVL<LonKey> lonIndex = buildLonIndex(stations);

        tree.buildBalanced(latIndex, lonIndex);

        assertEquals(4, tree.size());
        assertNotNull(tree.getRoot());
        assertEquals(0, tree.getRoot().getAxis(), "Root must split on latitude (axis=0)");

        // Check left and right children exist
        KDNode left = tree.getRoot().getLeft();
        KDNode right = tree.getRoot().getRight();

        assertNotNull(left, "Left subtree should not be null");
        assertNotNull(right, "Right subtree should not be null");

        // Children must split on longitude (axis=1) because depth=1
        assertEquals(1, left.getAxis());
        assertEquals(1, right.getAxis());

        // Bucket sizes: root has A,B (2); children have C and D (1 each)
        assertEquals(2, tree.getRoot().getStations().size());
        assertEquals(1, left.getStations().size());
        assertEquals(1, right.getStations().size());

        // Height for 3-node structure (root + 2 children) is 1
        assertEquals(1, tree.height());

        // Bucket distribution => {1=2, 2=1}
        Map<Integer, Integer> dist = tree.getBucketSizeDistribution();
        assertEquals(2, dist.get(1));
        assertEquals(1, dist.get(2));
        assertEquals(2, dist.size());
    }

    /**
     * getBucketSizeDistribution: verifies traversal over entire tree and counting logic.
     * <p>
     * Uses a slightly larger set of stations to ensure left and right subtrees are visited.
     *
     * Expected:
     *  - Sum of (bucketSize * bucketCount) equals total number of stations.
     */
    @Test
    void bucketSizeDistribution_coversWholeTreeAndCountsStations() {
        KDTree2D tree = new KDTree2D();

        List<Station> stations = new ArrayList<>();
        stations.add(st("A", 0.0,  0.0));
        stations.add(st("B", 0.0,  0.0)); // duplicate coordinate with A
        stations.add(st("C", -5.0, 1.0));
        stations.add(st("D", 10.0, 2.0));
        stations.add(st("E", 20.0, 3.0));

        AVL<LatKey> latIndex = buildLatIndex(stations);
        AVL<LonKey> lonIndex = buildLonIndex(stations);

        tree.buildBalanced(latIndex, lonIndex);

        Map<Integer, Integer> dist = tree.getBucketSizeDistribution();

        int totalCountedStations = 0;
        for (Map.Entry<Integer, Integer> e : dist.entrySet()) {
            totalCountedStations += e.getKey() * e.getValue();
        }

        assertEquals(tree.size(), totalCountedStations);
    }

    /**
     * getStatistics: returns a snapshot of size, height and bucket distribution
     * and defends against further modification of the original distribution map.
     * <p>
     * Expected:
     *  - stats.size == tree.size()
     *  - stats.height == tree.height()
     *  - stats.bucketDistribution is equal to tree.getBucketSizeDistribution() at creation time
     *  - later changes to a new distribution map do not affect existing stats
     */
    @Test
    void getStatistics_returnsSnapshotIndependentFromFutureChanges() {
        KDTree2D tree = new KDTree2D();

        List<Station> stations = List.of(
                st("A", 0.0, 0.0),
                st("B", 0.0, 0.0),
                st("C", 5.0, 1.0)
        );

        AVL<LatKey> latIndex = buildLatIndex(stations);
        AVL<LonKey> lonIndex = buildLonIndex(stations);

        tree.buildBalanced(latIndex, lonIndex);

        KDTree2D.KDTreeStats stats = tree.getStatistics();

        assertEquals(tree.size(), stats.getSize());
        assertEquals(tree.height(), stats.getHeight());

        Map<Integer, Integer> initialDist = tree.getBucketSizeDistribution();
        assertEquals(initialDist, stats.getBucketDistribution());

        // Create a fresh distribution and mutate it; stats must not change
        Map<Integer, Integer> mutated = tree.getBucketSizeDistribution();
        mutated.put(999, 999);

        assertNotEquals(mutated, stats.getBucketDistribution());
    }

    /**
     * KDTreeStats.toString: formats statistics as a human-readable multi-line summary.
     * <p>
     * Expected:
     *  - Contains basic labels ("2D-Tree Statistics", "Total stations", "Tree height", "Bucket distribution").
     *  - Mentions the correct size and height values.
     */
    @Test
    void kdTreeStats_toString_containsKeyInformation() {
        KDTree2D tree = new KDTree2D();

        List<Station> stations = List.of(
                st("A", 0.0, 0.0),
                st("B", 1.0, 1.0)
        );

        AVL<LatKey> latIndex = buildLatIndex(stations);
        AVL<LonKey> lonIndex = buildLonIndex(stations);

        tree.buildBalanced(latIndex, lonIndex);

        KDTree2D.KDTreeStats stats = tree.getStatistics();
        String text = stats.toString();

        assertTrue(text.contains("2D-Tree Statistics"));
        assertTrue(text.contains("Total stations"));
        assertTrue(text.contains("Tree height"));
        assertTrue(text.contains("Bucket distribution"));
        assertTrue(text.contains(String.valueOf(stats.getSize())));
        assertTrue(text.contains(String.valueOf(stats.getHeight())));
    }
}
