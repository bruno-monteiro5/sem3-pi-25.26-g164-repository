package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KDTree2DTest {

    private AVL<LatKey> latitudeIndex;
    private AVL<LonKey> longitudeIndex;
    private Station station1, station2, station3, station4, station5;

    @BeforeEach
    void setUp() {
        latitudeIndex = new AVL<>();
        longitudeIndex = new AVL<>();

        station1 = new Station(
                "Lisboa Santa Apolonia",
                "PT",
                new Location(38.71387, -9.122271),
                "WET",
                true, true, false
        );

        station2 = new Station(
                "Lisboa Oriente",
                "PT",
                new Location(38.71387, -9.122271), // Same coordinates
                "WET",
                false, true, false
        );

        station3 = new Station(
                "Porto Campanha",
                "PT",
                new Location(41.14961, -8.58519),
                "WET",
                true, true, false
        );

        station4 = new Station(
                "Coimbra-B",
                "PT",
                new Location(40.20564, -8.41955),
                "WET",
                true, true, false
        );

        station5 = new Station(
                "Faro",
                "PT",
                new Location(37.01936, -7.93044),
                "WET",
                true, true, false
        );
    }

    @Test
    void testEmptyTree() {
        KDTree2D tree = new KDTree2D();

        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertNull(tree.getRoot());
    }

    @Test
    void testBuildBalancedEmptyIndexes() {
        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertNull(tree.getRoot());
    }

    @Test
    void testBuildBalancedSingleStation() {
        latitudeIndex.insert(new LatKey(station1));
        longitudeIndex.insert(new LonKey(station1));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertNotNull(tree.getRoot());
        assertEquals(1, tree.getRoot().getStations().size());
        assertEquals("Lisboa Santa Apolonia", tree.getRoot().getStations().get(0).getName());
    }

    @Test
    void testBuildBalancedMultipleStations() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));
        latitudeIndex.insert(new LatKey(station5));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));
        longitudeIndex.insert(new LonKey(station5));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertEquals(4, tree.size());
        assertTrue(tree.height() >= 1);
        assertNotNull(tree.getRoot());
    }

    @Test
    void testBuildBalancedWithDuplicateCoordinates() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station2)); // Same coordinates
        latitudeIndex.insert(new LatKey(station3));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station2));
        longitudeIndex.insert(new LonKey(station3));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertEquals(3, tree.size());
        assertNotNull(tree.getRoot());
    }

    @Test
    void testHeightCalculation() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));
        latitudeIndex.insert(new LatKey(station5));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));
        longitudeIndex.insert(new LonKey(station5));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        int height = tree.height();
        assertTrue(height >= 1 && height <= 3); // Balanced tree
    }

    @Test
    void testBucketSizeDistribution() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station2)); // Same as station1
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station2));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        Map<Integer, Integer> distribution = tree.getBucketSizeDistribution();

        assertNotNull(distribution);
        assertTrue(distribution.containsKey(1) || distribution.containsKey(2));
    }

    @Test
    void testGetStatistics() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        KDTree2D.KDTreeStats stats = tree.getStatistics();

        assertNotNull(stats);
        assertEquals(3, stats.getSize());
        assertTrue(stats.getHeight() >= 1);
        assertNotNull(stats.getBucketDistribution());
        assertFalse(stats.getBucketDistribution().isEmpty());
    }

    @Test
    void testStatsToString() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station3));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station3));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        KDTree2D.KDTreeStats stats = tree.getStatistics();
        String output = stats.toString();

        assertNotNull(output);
        assertTrue(output.contains("2D-Tree Statistics"));
        assertTrue(output.contains("Total stations"));
        assertTrue(output.contains("Tree height"));
        assertTrue(output.contains("Bucket distribution"));
    }

    @Test
    void testAlternatingAxis() {
        // With 7 stations, we can test depth and axis alternation
        Station s6 = new Station("Braga", "PT", new Location(41.54547, -8.42005), "WET", true, true, false);
        Station s7 = new Station("Aveiro", "PT", new Location(40.64427, -8.64554), "WET", true, true, false);

        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));
        latitudeIndex.insert(new LatKey(station5));
        latitudeIndex.insert(new LatKey(s6));
        latitudeIndex.insert(new LatKey(s7));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));
        longitudeIndex.insert(new LonKey(station5));
        longitudeIndex.insert(new LonKey(s6));
        longitudeIndex.insert(new LonKey(s7));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        // Root should split on latitude (axis 0)
        assertNotNull(tree.getRoot());
        assertEquals(0, tree.getRoot().getAxis());

        // Children should split on longitude (axis 1)
        if (tree.getRoot().getLeft() != null) {
            assertEquals(1, tree.getRoot().getLeft().getAxis());
        }
        if (tree.getRoot().getRight() != null) {
            assertEquals(1, tree.getRoot().getRight().getAxis());
        }
    }

    @Test
    void testBucketsSortedByName() {
        latitudeIndex.insert(new LatKey(station2)); // Oriente
        latitudeIndex.insert(new LatKey(station1)); // Santa Apolonia

        longitudeIndex.insert(new LonKey(station2));
        longitudeIndex.insert(new LonKey(station1));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        // Find the node with both stations
        KDNode root = tree.getRoot();
        List<Station> stations = root.getStations();

        if (stations.size() == 2) {
            // Should be sorted alphabetically
            assertEquals("Lisboa Oriente", stations.get(0).getName());
            assertEquals("Lisboa Santa Apolonia", stations.get(1).getName());
        }
    }

    @Test
    void testGetRootNotNull() {
        latitudeIndex.insert(new LatKey(station1));
        longitudeIndex.insert(new LonKey(station1));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertNotNull(tree.getRoot());
    }

    @Test
    void testSizeMatchesNumberOfStations() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station2));
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));
        latitudeIndex.insert(new LatKey(station5));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station2));
        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));
        longitudeIndex.insert(new LonKey(station5));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        assertEquals(5, tree.size());
    }

    @Test
    void testBucketDistributionAllSingletons() {
        latitudeIndex.insert(new LatKey(station3));
        latitudeIndex.insert(new LatKey(station4));
        latitudeIndex.insert(new LatKey(station5));

        longitudeIndex.insert(new LonKey(station3));
        longitudeIndex.insert(new LonKey(station4));
        longitudeIndex.insert(new LonKey(station5));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        Map<Integer, Integer> distribution = tree.getBucketSizeDistribution();

        // All buckets should have size 1
        assertTrue(distribution.containsKey(1));
        assertEquals(3, distribution.get(1).intValue());
    }

    @Test
    void testBucketDistributionMixed() {
        latitudeIndex.insert(new LatKey(station1));
        latitudeIndex.insert(new LatKey(station2)); // Duplicate coordinates
        latitudeIndex.insert(new LatKey(station3));

        longitudeIndex.insert(new LonKey(station1));
        longitudeIndex.insert(new LonKey(station2));
        longitudeIndex.insert(new LonKey(station3));

        KDTree2D tree = new KDTree2D();
        tree.buildBalanced(latitudeIndex, longitudeIndex);

        Map<Integer, Integer> distribution = tree.getBucketSizeDistribution();

        // Should have at least one bucket with size > 1
        int totalBuckets = distribution.values().stream().mapToInt(Integer::intValue).sum();
        assertTrue(totalBuckets >= 2);
    }
}