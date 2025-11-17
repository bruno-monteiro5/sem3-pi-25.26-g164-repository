package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link KDNode}.
 * Covers construction, bucket sorting, getters/setters, and coordinate retrieval for both axes,
 * including the empty-stations edge case.
 */
class KDNodeTest {

    /** Helper to create a Location (adjust to your actual constructor). */
    private Location loc(double lat, double lon) {
        return new Location(lat, lon);
    }

    /** Helper to create a Station with minimal valid data. */
    private Station st(String name, double lat, double lon) {
        // Adjust to your Station constructor if needed:
        // Station(String name, String country, Location location, String timeZoneGroup, boolean isCity, boolean isMainStation, boolean isAirport)
        return new Station(name, "PT", loc(lat, lon), "WET", false, false, false);
    }

    /**
     * Constructor: bucket must be sorted by Station name (ascending) regardless of input order.
     * Expected:
     *  - KDNode#getStations() returns stations ordered alphabetically by name.
     */
    @Test
    void constructor_sortsBucketByName() {
        List<Station> input = new ArrayList<>();
        input.add(st("Charlie", 10.0, 20.0));
        input.add(st("Alice",   10.0, 20.0));
        input.add(st("Bob",     10.0, 20.0));

        KDNode node = new KDNode(0, input);
        List<Station> bucket = node.getStations();

        assertEquals(3, bucket.size());
        assertEquals("Alice",  bucket.get(0).getName());
        assertEquals("Bob",    bucket.get(1).getName());
        assertEquals("Charlie",bucket.get(2).getName());
    }

    /**
     * Defensive copy: later changes to the original list must not affect the node's bucket.
     * Expected:
     *  - Modifying the original input list does not change KDNode's internal list.
     */
    @Test
    void constructor_makesDefensiveCopy() {
        List<Station> input = new ArrayList<>();
        input.add(st("A", 1.0, 2.0));

        KDNode node = new KDNode(0, input);
        input.add(st("Z", 1.0, 2.0)); // mutate original

        assertEquals(1, node.getStations().size());
        assertEquals("A", node.getStations().get(0).getName());
    }

    /**
     * Axis 0 (latitude): getCoordinate() returns the latitude of the bucket's first station.
     * Expected:
     *  - Returned value equals latitude.
     */
    @Test
    void getCoordinate_axisLatitude() {
        List<Station> bucket = List.of(
                st("B", 41.178, -8.598),
                st("A", 41.178, -8.598) // will be sorted before B
        );
        KDNode node = new KDNode(0, bucket); // axis 0 = latitude
        assertEquals(41.178, node.getCoordinate(), 1e-9);
        assertEquals(0, node.getAxis());
    }

    /**
     * Axis 1 (longitude): getCoordinate() returns the longitude of the bucket's first station.
     * Expected:
     *  - Returned value equals longitude.
     */
    @Test
    void getCoordinate_axisLongitude() {
        List<Station> bucket = List.of(
                st("Z", 51.5007, -0.1246),
                st("Y", 51.5007, -0.1246)
        );
        KDNode node = new KDNode(1, bucket); // axis 1 = longitude
        assertEquals(-0.1246, node.getCoordinate(), 1e-9);
        assertEquals(1, node.getAxis());
    }

    /**
     * Empty bucket: getCoordinate() must return 0.0 to avoid NPE.
     * Expected:
     *  - 0.0
     */
    @Test
    void getCoordinate_emptyBucketReturnsZero() {
        KDNode node = new KDNode(0, List.of());
        assertEquals(0.0, node.getCoordinate(), 1e-9);
    }

    /**
     * Left/Right setters and getters: node should store and return child references.
     * Expected:
     *  - getLeft()/getRight() return the instances previously set.
     */
    @Test
    void leftRight_settersAndGetters() {
        KDNode parent = new KDNode(0, List.of(st("Root", 0.0, 0.0)));
        KDNode left   = new KDNode(1, List.of(st("L", 1.0, 1.0)));
        KDNode right  = new KDNode(1, List.of(st("R", 2.0, 2.0)));

        parent.setLeft(left);
        parent.setRight(right);

        assertSame(left, parent.getLeft());
        assertSame(right, parent.getRight());
    }
}
