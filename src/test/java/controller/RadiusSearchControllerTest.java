package controller;

import domain.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link RadiusSearchController}.
 * <p>
 * Each test method targets exactly one public member of {@code RadiusSearchController}
 * and documents the scenario, preconditions and expected outcome.
 */
public class RadiusSearchControllerTest {

    /**
     * Fake KDTree2D used to control the root node returned in tests.
     */
    private static class FakeKDTree2D extends KDTree2D {
        private final KDNode root;

        FakeKDTree2D(KDNode root) {
            this.root = root;
        }

        @Override
        public KDNode getRoot() {
            return root;
        }
    }

    /**
     * Fake Build2DTreeController used to control the 2D-Tree state in tests.
     */
    private static class FakeBuild2DTreeController extends Build2DTreeController {

        private final boolean built;
        private final KDTree2D tree;

        FakeBuild2DTreeController(boolean built, KDTree2D tree) {
            this.built = built;
            this.tree = tree;
        }

        @Override
        public boolean isBuilt() {
            return built;
        }

        @Override
        public KDTree2D getKDTree() {
            return tree;
        }
    }

    /**
     * Helper method to create a Station with given attributes.
     */
    private Station makeStation(String name, String country,
                                double lat, double lon,
                                boolean isCity) {
        Location loc = new Location(lat, lon);
        return new Station(
                name,
                country,
                loc,
                "Europe/Lisbon",
                isCity,
                false,
                false
        );
    }

    /**
     * Verifies that the constructor enforces non-null treeController.
     *
     * <p>Scenario:
     *   - Create a {@code RadiusSearchController} with {@code null} as argument.
     *
     * <p>Expected:
     *   - A {@link NullPointerException} is thrown.
     *
     * @see RadiusSearchController#RadiusSearchController(Build2DTreeController)
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullTreeController_throwsException() {
        new RadiusSearchController(null);
    }

    /**
     * Verifies that searchWithinRadius fails when the 2D-Tree has not been built.
     *
     * <p>Scenario:
     *   - Fake {@link Build2DTreeController} reports {@code isBuilt() == false}.
     *
     * <p>Expected:
     *   - An {@link IllegalStateException} is thrown.
     *
     * @see RadiusSearchController#searchWithinRadius(double, double, double)
     */
    @Test(expected = IllegalStateException.class)
    public void searchWithinRadius_treeNotBuilt_throwsIllegalState() {
        FakeBuild2DTreeController fakeTreeController = new FakeBuild2DTreeController(false, null);

        RadiusSearchController controller = new RadiusSearchController(fakeTreeController);

        controller.searchWithinRadius(0.0, 0.0, 100.0);
    }

    /**
     * Verifies that searchWithinRadius rejects negative radius values.
     *
     * <p>Scenario:
     *   - 2D-Tree is marked as built, but radius is negative.
     *
     * <p>Expected:
     *   - An {@link IllegalArgumentException} is thrown.
     *
     * @see RadiusSearchController#searchWithinRadius(double, double, double)
     */
    @Test(expected = IllegalArgumentException.class)
    public void searchWithinRadius_negativeRadius_throwsIllegalArgument() {
        FakeBuild2DTreeController fakeTreeController = new FakeBuild2DTreeController(true, null);

        RadiusSearchController controller = new RadiusSearchController(fakeTreeController);

        controller.searchWithinRadius(0.0, 0.0, -5.0);
    }

    /**
     * Verifies that searchWithinRadius returns a correct result and
     * filters stations by radius, country and city/non-city flags.
     *
     * <p>Scenario:
     *   - 4 stations in the 2D-tree:
     *     * 3 inside a 600 km radius around (0,0);
     *     * 1 outside the radius.
     *   - 2 stations from "PT", 1 from "ES".
     *
     * <p>Expected:
     *   - Only 3 stations included in the result;
     *   - Country counts: PT=2, ES=1;
     *   - City count and non-city count computed correctly;
     *   - AVL tree sorted by distance/name (Z > A when distance ties).
     *
     * @see RadiusSearchController#searchWithinRadius(double, double, double)
     */
    @Test
    public void searchWithinRadius_returnsFilteredAndSortedResult() {
        // Center at (0,0)
        Station sAlpha = makeStation("Alpha", "PT", 0.0, 0.0, true);   // dist ~0
        Station sZulu  = makeStation("Zulu",  "PT", 0.0, 0.0, false);  // same dist, different name
        Station sBeta  = makeStation("Beta",  "ES", 0.0, 5.0, false);  // ~556 km (inside)
        Station sFar   = makeStation("Far",   "FR", 0.0, 20.0, true);  // ~2220 km (outside)

        List<Station> bucket = new ArrayList<>();
        bucket.add(sAlpha);
        bucket.add(sZulu);
        bucket.add(sBeta);
        bucket.add(sFar);

        KDNode root = new KDNode(0, bucket);
        FakeKDTree2D fakeTree = new FakeKDTree2D(root);

        FakeBuild2DTreeController fakeTreeController =
                new FakeBuild2DTreeController(true, fakeTree);

        RadiusSearchController controller =
                new RadiusSearchController(fakeTreeController);

        double centerLat = 0.0;
        double centerLon = 0.0;
        double radiusKm = 600.0;

        RadiusSearchResult result = controller.searchWithinRadius(centerLat, centerLon, radiusKm);

        // Total: 3 inside radius (Alpha, Zulu, Beta)
        assertEquals(3, result.getTotalCount());

        // Country counts: PT=2, ES=1 (FR not included)
        assertEquals(Integer.valueOf(2), result.getCountByCountry().get("PT"));
        assertEquals(Integer.valueOf(1), result.getCountByCountry().get("ES"));
        assertFalse(result.getCountByCountry().containsKey("FR"));

        // City / non-city counts
        // Alpha (city), Zulu (non-city), Beta (non-city)
        assertEquals(1, result.getCityCount());
        assertEquals(2, result.getNonCityCount());

        // Search center and radius stored in the result
        assertEquals(centerLat, result.getSearchCenter().getLatitude(), 1e-6);
        assertEquals(centerLon, result.getSearchCenter().getLongitude(), 1e-6);
        assertEquals(radiusKm, result.getSearchRadius(), 1e-6);

        // Order: distance ASC, then name DESC for ties at distance 0
        List<String> namesInOrder = new ArrayList<>();
        for (DistanceKey dk : result.getSortedStations().inOrder()) {
            namesInOrder.add(dk.getStationName());
        }

        assertEquals(3, namesInOrder.size());
        // sAlpha and sZulu have distance 0 -> Zulu must come before Alpha
        assertEquals("Zulu", namesInOrder.get(0));
        assertEquals("Alpha", namesInOrder.get(1));
        assertEquals("Beta", namesInOrder.get(2)); // farthest inside the radius
    }

    /**
     * Verifies that isReady delegates to the underlying treeController.isBuilt().
     *
     * <p>Scenario:
     *   - Two fake controllers: one built, one not built.
     *
     * <p>Expected:
     *   - isReady() returns true/false in accordance with isBuilt().
     *
     * @see RadiusSearchController#isReady()
     */
    @Test
    public void isReady_reflectsTreeControllerState() {
        FakeBuild2DTreeController notBuilt = new FakeBuild2DTreeController(false, null);
        FakeBuild2DTreeController built = new FakeBuild2DTreeController(true, null);

        RadiusSearchController controllerNotBuilt = new RadiusSearchController(notBuilt);
        RadiusSearchController controllerBuilt = new RadiusSearchController(built);

        assertFalse(controllerNotBuilt.isReady());
        assertTrue(controllerBuilt.isReady());
    }

    /**
     * Verifies that searchWithinRadius rejects latitudes outside [-90, 90].
     *
     * <p>Scenario:
     *   - Tree is built; latitude = 123 (invalid); longitude and radius valid.
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} is thrown.
     *
     * @see RadiusSearchController#searchWithinRadius(double, double, double)
     */
    @Test
    public void searchWithinRadius_invalidLatitude_throwsIllegalArgument() {
        FakeBuild2DTreeController fakeTreeController = new FakeBuild2DTreeController(true, null);

        RadiusSearchController controller = new RadiusSearchController(fakeTreeController);

        controller.searchWithinRadius(123.0, 0.0, 10.0);
    }

    /**
     * Verifies that searchWithinRadius rejects longitudes outside [-180, 180].
     *
     * <p>Scenario:
     *   - Tree is built; longitude = 999 (invalid); latitude and radius valid.
     *
     * <p>Expected:
     *   - {@link IllegalArgumentException} is thrown.
     *
     * @see RadiusSearchController#searchWithinRadius(double, double, double)
     */
    @Test
    public void searchWithinRadius_invalidLongitude_throwsIllegalArgument() {
        FakeBuild2DTreeController fakeTreeController = new FakeBuild2DTreeController(true, null);

        RadiusSearchController controller = new RadiusSearchController(fakeTreeController);

        controller.searchWithinRadius(0.0, 999.0, 10.0);
    }

}
