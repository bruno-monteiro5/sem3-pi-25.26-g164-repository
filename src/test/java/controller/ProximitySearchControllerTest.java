package controller;

import domain.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ProximitySearchController} using only standard Java and JUnit4.
 * All KDTree2D/KDNode structures are emulated with simple stubs, fully compatible
 * with the project's KDNode constructor.
 */
public class ProximitySearchControllerTest {


    private static class StubKDTree2D extends KDTree2D {
        private final KDNode root;

        StubKDTree2D(KDNode root) {
            this.root = root;
        }

        @Override
        public KDNode getRoot() {
            return root;
        }
    }

    private static class StubBuild2DTreeController extends Build2DTreeController {

        private final boolean built;
        private final KDTree2D tree;

        StubBuild2DTreeController(boolean built, KDTree2D tree) {
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



    private Station createStation(String name, double lat, double lon, String tz) {
        Location loc = new Location(lat, lon);
        return new Station(name, "PT", loc, tz, false, false, false);
    }

    private KDNode createNodeWithStations(Station... stations) {
        List<Station> list = new ArrayList<>();
        for (Station s : stations) list.add(s);
        return new KDNode(0, list); // axis = 0 (latitude split)
    }



    @Test(expected = IllegalStateException.class)
    public void testTreeNotBuiltThrows() {
        StubBuild2DTreeController c =
                new StubBuild2DTreeController(false, new StubKDTree2D(null));

        ProximitySearchController ctrl = new ProximitySearchController(c);
        ctrl.findNearestN(0, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNThrows() {
        KDNode root = createNodeWithStations();
        StubKDTree2D tree = new StubKDTree2D(root);

        StubBuild2DTreeController c =
                new StubBuild2DTreeController(true, tree);

        ProximitySearchController ctrl = new ProximitySearchController(c);
        ctrl.findNearestN(0, 0, 0);
    }

    @Test
    public void testNullFilterDefaultsToDisabled() {
        StubKDTree2D tree = new StubKDTree2D(null);
        StubBuild2DTreeController c =
                new StubBuild2DTreeController(true, tree);

        ProximitySearchController ctrl = new ProximitySearchController(c);

        ProximitySearchResult r = ctrl.findNearestN(0, 0, 3, null);

        assertEquals(0, r.getResultCount());
    }

    @Test
    public void testSimpleNearestN() {
        Station s1 = createStation("A", 0, 1, "EU");
        Station s2 = createStation("B", 0, 2, "EU");
        Station s3 = createStation("C", 0, 3, "EU");

        KDNode root = createNodeWithStations(s1, s2, s3);
        StubKDTree2D tree = new StubKDTree2D(root);

        ProximitySearchController ctrl =
                new ProximitySearchController(new StubBuild2DTreeController(true, tree));

        ProximitySearchResult r = ctrl.findNearestN(0, 0, 2);

        assertEquals(2, r.getResultCount());
    }

    @Test
    public void testFilterRemovesAll() {
        Station s1 = createStation("X", 0, 1, "EU");
        Station s2 = createStation("Y", 0, 2, "EU");

        KDNode root = createNodeWithStations(s1, s2);
        StubKDTree2D tree = new StubKDTree2D(root);

        ProximitySearchController ctrl =
                new ProximitySearchController(new StubBuild2DTreeController(true, tree));

        TimeZoneFilter filter = new TimeZoneFilter("NA");

        ProximitySearchResult r = ctrl.findNearestN(0, 0, 3, filter);

        assertEquals(0, r.getResultCount());
    }

    @Test
    public void testNoExtraBeyondN() {
        Station s1 = createStation("A", 0, 1, "EU");
        Station s2 = createStation("B", 0, 2, "EU");
        Station s3 = createStation("C", 0, 3, "EU");

        KDNode root = createNodeWithStations(s3, s1, s2);
        StubKDTree2D tree = new StubKDTree2D(root);

        ProximitySearchController ctrl =
                new ProximitySearchController(new StubBuild2DTreeController(true, tree));

        ProximitySearchResult r = ctrl.findNearestN(0, 0, 2);

        assertEquals(2, r.getResultCount());
    }

    @Test
    public void testTieCoordinatesIncluded() {
        Station s1 = createStation("A", 1, 1, "EU");
        Station s2 = createStation("B", 1, 1, "EU");
        Station s3 = createStation("C", 1, 1, "EU");

        KDNode root = createNodeWithStations(s3, s1, s2);
        StubKDTree2D tree = new StubKDTree2D(root);

        ProximitySearchController ctrl =
                new ProximitySearchController(new StubBuild2DTreeController(true, tree));

        ProximitySearchResult r = ctrl.findNearestN(0, 0, 2);

        assertEquals(3, r.getResultCount());
    }
}
