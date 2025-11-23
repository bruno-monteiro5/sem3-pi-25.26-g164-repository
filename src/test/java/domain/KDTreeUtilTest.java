package domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link KDTreeUtil}.
 * <p>
 * Tests cover:
 * - Station collection from KD-Tree (with/without null nodes)
 * - Coordinate validation (valid and invalid ranges)
 * - Edge cases (empty trees, null parameters)
 */
public class KDTreeUtilTest {

    /**
     * Helper to create a Station with minimal required fields.
     */
    private Station makeStation(String name, double lat, double lon) {
        Location loc = new Location(lat, lon);
        return new Station(
                name,
                "PT",
                loc,
                "Europe/Lisbon",
                false,
                false,
                false
        );
    }


    /**
     * Verifies that collectAllStations returns an empty list when tree is null.
     *
     * <p>Scenario:
     *   - Pass {@code null} as the KDTree2D parameter.
     *
     * <p>Expected:
     *   - Returns an empty list (not null).
     */
    @Test
    public void collectAllStations_nullTree_returnsEmptyList() {
        List<Station> result = KDTreeUtil.collectAllStations((KDTree2D) null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Verifies that collectAllStations returns an empty list when tree has null root.
     *
     * <p>Scenario:
     *   - KDTree2D exists but getRoot() returns null.
     *
     * <p>Expected:
     *   - Returns an empty list.
     */
    @Test
    public void collectAllStations_treeWithNullRoot_returnsEmptyList() {
        KDTree2D emptyTree = new KDTree2D() {
            @Override
            public KDNode getRoot() {
                return null;
            }
        };

        List<Station> result = KDTreeUtil.collectAllStations(emptyTree);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Verifies that collectAllStations collects all stations from a single-node tree.
     *
     * <p>Scenario:
     *   - Tree with one root node containing 2 stations.
     *   - No left or right children.
     *
     * <p>Expected:
     *   - Returns list with exactly 2 stations.
     */
    @Test
    public void collectAllStations_singleNodeTree_collectsAllStations() {
        Station s1 = makeStation("Alpha", 10.0, 20.0);
        Station s2 = makeStation("Beta", 10.0, 20.0);

        List<Station> bucket = new ArrayList<>();
        bucket.add(s1);
        bucket.add(s2);

        KDNode root = new KDNode(0, bucket);

        KDTree2D tree = new KDTree2D() {
            @Override
            public KDNode getRoot() {
                return root;
            }
        };

        List<Station> result = KDTreeUtil.collectAllStations(tree);

        assertEquals(2, result.size());
        assertTrue(result.contains(s1));
        assertTrue(result.contains(s2));
    }

    /**
     * Verifies that collectAllStations traverses left and right subtrees correctly.
     *
     * <p>Scenario:
     *   - Root node with 1 station
     *   - Left child with 2 stations
     *   - Right child with 1 station
     *
     * <p>Expected:
     *   - Returns list with all 4 stations (1+2+1).
     */
    @Test
    public void collectAllStations_multiNodeTree_collectsAllStations() {
        Station sRoot = makeStation("Root", 10.0, 10.0);
        Station sLeft1 = makeStation("Left1", 5.0, 5.0);
        Station sLeft2 = makeStation("Left2", 5.0, 5.0);
        Station sRight = makeStation("Right", 15.0, 15.0);

        // Root bucket
        List<Station> rootBucket = new ArrayList<>();
        rootBucket.add(sRoot);

        // Left bucket
        List<Station> leftBucket = new ArrayList<>();
        leftBucket.add(sLeft1);
        leftBucket.add(sLeft2);

        // Right bucket
        List<Station> rightBucket = new ArrayList<>();
        rightBucket.add(sRight);

        // Build tree structure
        KDNode leftNode = new KDNode(1, leftBucket);
        KDNode rightNode = new KDNode(1, rightBucket);
        KDNode rootNode = new KDNode(0, rootBucket);
        rootNode.setLeft(leftNode);
        rootNode.setRight(rightNode);

        KDTree2D tree = new KDTree2D() {
            @Override
            public KDNode getRoot() {
                return rootNode;
            }
        };

        List<Station> result = KDTreeUtil.collectAllStations(tree);

        assertEquals(4, result.size());
        assertTrue(result.contains(sRoot));
        assertTrue(result.contains(sLeft1));
        assertTrue(result.contains(sLeft2));
        assertTrue(result.contains(sRight));
    }

    /**
     * Verifies that the overloaded collectAllStations(KDNode, List) handles null node.
     *
     * <p>Scenario:
     *   - Pass null as KDNode parameter
     *   - Provide empty accumulator list
     *
     * <p>Expected:
     *   - Accumulator remains empty (no NullPointerException)
     */
    @Test
    public void collectAllStations_nullNode_doesNothing() {
        List<Station> accumulator = new ArrayList<>();

        KDTreeUtil.collectAllStations((KDNode) null, accumulator);

        assertTrue(accumulator.isEmpty());
    }

    /**
     * Verifies that collectAllStations(KDNode, List) adds stations to existing list.
     *
     * <p>Scenario:
     *   - Accumulator already contains 1 station
     *   - Collect 2 more stations from a node
     *
     * <p>Expected:
     *   - Accumulator contains 3 stations total
     */
    @Test
    public void collectAllStations_existingList_appendsStations() {
        Station existing = makeStation("Existing", 0.0, 0.0);
        Station s1 = makeStation("New1", 10.0, 20.0);
        Station s2 = makeStation("New2", 10.0, 20.0);

        List<Station> accumulator = new ArrayList<>();
        accumulator.add(existing);

        List<Station> bucket = new ArrayList<>();
        bucket.add(s1);
        bucket.add(s2);
        KDNode node = new KDNode(0, bucket);

        KDTreeUtil.collectAllStations(node, accumulator);

        assertEquals(3, accumulator.size());
        assertTrue(accumulator.contains(existing));
        assertTrue(accumulator.contains(s1));
        assertTrue(accumulator.contains(s2));
    }


    /**
     * Verifies that valid coordinates do not throw exceptions.
     *
     * <p>Scenario:
     *   - Valid latitude/longitude combinations at boundaries and middle ranges.
     *
     * <p>Expected:
     *   - No exceptions thrown.
     */
    @Test
    public void validateCoordinates_validValues_noException() {
        // Boundaries
        KDTreeUtil.validateCoordinates(-90.0, -180.0);
        KDTreeUtil.validateCoordinates(90.0, 180.0);
        KDTreeUtil.validateCoordinates(0.0, 0.0);

        // Middle ranges
        KDTreeUtil.validateCoordinates(38.7223, -9.1393); // Lisbon
        KDTreeUtil.validateCoordinates(-33.8688, 151.2093); // Sydney
    }

    /**
     * Verifies that latitude below -90 throws IllegalArgumentException.
     *
     * <p>Scenario:
     *   - Latitude = -91.0 (invalid)
     *   - Longitude = 0.0 (valid)
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} with message containing the invalid value.
     */
    @Test
    public void validateCoordinates_latitudeTooLow_throwsException() {
        try {
            KDTreeUtil.validateCoordinates(-91.0, 0.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("-91"));
            assertTrue(e.getMessage().toLowerCase().contains("latitude"));
        }
    }

    /**
     * Verifies that latitude above 90 throws IllegalArgumentException.
     *
     * <p>Scenario:
     *   - Latitude = 91.0 (invalid)
     *   - Longitude = 0.0 (valid)
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} with message containing the invalid value.
     */
    @Test
    public void validateCoordinates_latitudeTooHigh_throwsException() {
        try {
            KDTreeUtil.validateCoordinates(91.0, 0.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("91"));
            assertTrue(e.getMessage().toLowerCase().contains("latitude"));
        }
    }

    /**
     * Verifies that longitude below -180 throws IllegalArgumentException.
     *
     * <p>Scenario:
     *   - Latitude = 0.0 (valid)
     *   - Longitude = -181.0 (invalid)
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} with message containing the invalid value.
     */
    @Test
    public void validateCoordinates_longitudeTooLow_throwsException() {
        try {
            KDTreeUtil.validateCoordinates(0.0, -181.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("-181"));
            assertTrue(e.getMessage().toLowerCase().contains("longitude"));
        }
    }

    /**
     * Verifies that longitude above 180 throws IllegalArgumentException.
     *
     * <p>Scenario:
     *   - Latitude = 0.0 (valid)
     *   - Longitude = 181.0 (invalid)
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} with message containing the invalid value.
     */
    @Test
    public void validateCoordinates_longitudeTooHigh_throwsException() {
        try {
            KDTreeUtil.validateCoordinates(0.0, 181.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("181"));
            assertTrue(e.getMessage().toLowerCase().contains("longitude"));
        }
    }

    /**
     * Verifies that both invalid coordinates result in latitude being checked first.
     *
     * <p>Scenario:
     *   - Latitude = 100.0 (invalid)
     *   - Longitude = 200.0 (also invalid)
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} mentioning latitude (checked first in method).
     */
    @Test
    public void validateCoordinates_bothInvalid_throwsForLatitudeFirst() {
        try {
            KDTreeUtil.validateCoordinates(100.0, 200.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Should fail on latitude check first
            assertTrue(e.getMessage().toLowerCase().contains("latitude"));
        }
    }

    /**
     * Verifies that edge case coordinates at exact boundaries are accepted.
     *
     * <p>Scenario:
     *   - Test all four boundary combinations:
     *     * (-90, -180)
     *     * (-90, 180)
     *     * (90, -180)
     *     * (90, 180)
     *
     * <p>Expected:
     *   - No exceptions thrown for any combination.
     */
    @Test
    public void validateCoordinates_exactBoundaries_noException() {
        // All four corners of valid coordinate space
        KDTreeUtil.validateCoordinates(-90.0, -180.0);
        KDTreeUtil.validateCoordinates(-90.0, 180.0);
        KDTreeUtil.validateCoordinates(90.0, -180.0);
        KDTreeUtil.validateCoordinates(90.0, 180.0);
    }

    /**
     * Verifies that special floating-point values are handled correctly.
     *
     * <p>Scenario:
     *   - Very small valid values near zero
     *   - Negative zero
     *
     * <p>Expected:
     *   - No exceptions for valid ranges
     */
    @Test
    public void validateCoordinates_specialFloatingPointValues_handlesCorrectly() {
        // Very small but valid
        KDTreeUtil.validateCoordinates(0.0000001, 0.0000001);
        KDTreeUtil.validateCoordinates(-0.0000001, -0.0000001);

        // Negative zero (should be treated as 0.0)
        KDTreeUtil.validateCoordinates(-0.0, -0.0);
    }
}