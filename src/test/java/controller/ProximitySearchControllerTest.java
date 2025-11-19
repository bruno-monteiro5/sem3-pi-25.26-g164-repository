package controller;

import domain.HaversineDistance;
import domain.KDNode;
import domain.KDTree2D;
import domain.Location;
import domain.ProximitySearchResult;
import domain.Station;
import domain.TimeZoneFilter;
import org.junit.Before;
import org.junit.Test;
import repository.KDTreeRepository;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ProximitySearchController}.
 * Uses plain JUnit 4 and simple helper methods.
 */
public class ProximitySearchControllerTest {

    private KDTreeRepository kdTreeRepository;
    private StationRepository stationRepository;

    /**
     * Initializes repositories and clears the KD-Tree before each test.
     * Adapt the reset logic if your KDTreeRepository has a different API.
     */
    @Before
    public void setUp() {
        kdTreeRepository = KDTreeRepository.getInstance();
        stationRepository = StationRepository.getInstance();

        kdTreeRepository.setKDTree(null);
    }

    /**
     * Verifies that the controller can be constructed without throwing.
     */
    @Test
    public void testConstructor() {
        ProximitySearchController controller = new ProximitySearchController(null);
        assertNotNull(controller);
    }

    /**
     * Verifies that findNearestN(lat, lon, n) without filter
     * delegates correctly and returns one result when the tree is built.
     */
    @Test
    public void testFindNearestNWithoutFilter() {
        Station s1 = createStation("StationA", 10.0, 20.0, "Europe/Lisbon");
        KDNode root = createLeafNode(s1);
        installTree(root, 1);

        ProximitySearchController controller = new ProximitySearchController(null);

        ProximitySearchResult result = controller.findNearestN(10.0, 20.0, 1);

        assertNotNull(result);
        assertEquals(1, result.getResultCount());
    }

    /**
     * Verifies that when the KD-Tree is not built, findNearestN throws IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testFindNearestNTreeNotBuiltThrows() {
        // Tree is null from setUp(), so isBuilt() should be false
        ProximitySearchController controller = new ProximitySearchController(null);
        controller.findNearestN(10.0, 20.0, 1);
    }

    /**
     * Verifies that when n <= 0, findNearestN throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindNearestNWithNonPositiveNThrows() {
        Station s1 = createStation("StationA", 10.0, 20.0, "Europe/Lisbon");
        KDNode root = createLeafNode(s1);
        installTree(root, 1);

        ProximitySearchController controller = new ProximitySearchController(null);
        controller.findNearestN(10.0, 20.0, 0);
    }

    /**
     * Verifies that when the filter parameter is null, the method
     * creates a default TimeZoneFilter and still returns results.
     */
    @Test
    public void testFindNearestNWithNullFilterUsesDefault() {
        Station s1 = createStation("StationA", 10.0, 20.0, "Europe/Lisbon");
        KDNode root = createLeafNode(s1);
        installTree(root, 1);

        ProximitySearchController controller = new ProximitySearchController(null);

        ProximitySearchResult result = controller.findNearestN(10.0, 20.0, 1, null);

        assertNotNull(result);
        assertEquals(1, result.getResultCount());
    }

    /**
     * Verifies that when the filter rejects all stations,
     * the method returns a result with zero stations and hits the distances.isEmpty() branch.
     */
    @Test
    public void testFindNearestNNoMatchesDueToFilter() {
        Station s1 = createStation("StationA", 10.0, 20.0, "Europe/Lisbon");
        KDNode root = createLeafNode(s1);
        installTree(root, 1);

        ProximitySearchController controller = new ProximitySearchController(null);

        // Filter that never matches any station
        TimeZoneFilter filter = new TimeZoneFilter() {
            @Override
            public boolean matches(Station station) {
                return false;
            }
        };

        ProximitySearchResult result = controller.findNearestN(10.0, 20.0, 5, filter);

        assertNotNull(result);
        assertEquals(0, result.getResultCount());
    }

    /**
     * Verifies that ties at the N-th distance are included when stations
     * share the same coordinates and distance to the target.
     */
    @Test
    public void testFindNearestNWithTiesAtBoundary() {
        // Three stations at exactly the same coordinates (same distance).
        Station s1 = createStation("A", 10.0, 20.0, "Europe/Lisbon");
        Station s2 = createStation("B", 10.0, 20.0, "Europe/Lisbon");
        Station s3 = createStation("C", 10.0, 20.0, "Europe/Lisbon");

        List<Station> bucket = new ArrayList<Station>();
        bucket.add(s1);
        bucket.add(s2);
        bucket.add(s3);

        KDNode root = new KDNode(0, bucket);
        installTree(root, 3);

        ProximitySearchController controller = new ProximitySearchController(null);

        // Request only 1 neighbor, but all 3 have the same distance and coordinates,
        // so the tie-handling logic should include them all.
        ProximitySearchResult result = controller.findNearestN(10.0, 20.0, 1);

        assertNotNull(result);
        assertEquals(3, result.getResultCount());
    }

    /**
     * Verifies that the sorting by distance and then by station name is used
     * inside the controller (StationWithDistance.compareTo).
     * We do not assert exact order, only that at least two stations are returned
     * and the flow completes without errors.
     */
    @Test
    public void testSortingByDistanceAndName() {
        // Two stations at same distance but different names, one farther
        Station s1 = createStation("B", 10.0, 20.0, "Europe/Lisbon"); // closer
        Station s2 = createStation("A", 10.0, 20.0, "Europe/Lisbon"); // same distance as s1
        Station s3 = createStation("Z", 12.0, 20.0, "Europe/Lisbon"); // farther

        List<Station> bucketRoot = new ArrayList<Station>();
        bucketRoot.add(s3); // farther station on root

        List<Station> bucketLeft = new ArrayList<Station>();
        bucketLeft.add(s1);
        bucketLeft.add(s2); // same coordinates (tie on distance)

        KDNode left = new KDNode(0, bucketLeft);
        KDNode root = new KDNode(0, bucketRoot);
        root.setLeft(left);

        installTree(root, 3);

        ProximitySearchController controller = new ProximitySearchController(null);

        ProximitySearchResult result = controller.findNearestN(10.0, 20.0, 2);

        assertNotNull(result);
        assertTrue(result.getResultCount() >= 2);
    }



    /**
     * Creates a Station with the given basic fields.
     * Adjust this constructor call to match your real Station class if needed.
     */
    private Station createStation(String name, double lat, double lon, String tzGroup) {
        Location location = new Location(lat, lon);
        // From your previous error: Station(String, String, Location, String, boolean, boolean, boolean)
        return new Station(
                name,          // station name
                "PT",          // country
                location,      // location
                tzGroup,       // time zone group
                false,         // isCity
                false,         // isAirport
                false          // isPort
        );
    }

    /**
     * Creates a KDNode with a single station in its bucket and no children.
     */
    private KDNode createLeafNode(Station station) {
        List<Station> bucket = new ArrayList<Station>();
        bucket.add(station);
        return new KDNode(0, bucket);
    }

    /**
     * Installs a KDTree2D in the KDTreeRepository with the given root and size.
     * Uses an anonymous subclass to override getRoot() (and size()).
     */
    private void installTree(final KDNode root, final int size) {
        KDTree2D tree = new KDTree2D() {
            @Override
            public KDNode getRoot() {
                return root;
            }

            @Override
            public int size() {
                return size;
            }
        };

        kdTreeRepository.setKDTree(tree);
    }
}
