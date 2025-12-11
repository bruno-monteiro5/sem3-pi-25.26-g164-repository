package controller;

import domain.Line;
import domain.Location;
import domain.MinimalBackboneResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import repository.S3_RailwayRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link MinimalBackboneController}.
 *
 * Each test focuses on a specific public behavior of the controller,
 * aligned with USEI12 (Minimal Backbone Network).
 *
 * Coverage includes:
 * <ul>
 *   <li>Graph state queries (isGraphBuilt, getStationCount, getLineCount)</li>
 *   <li>Minimal backbone calculation using Kruskal's algorithm</li>
 *   <li>Selection of minimum edges on simple and parallel-edge networks</li>
 *   <li>Inclusion of all original vertices in the backbone</li>
 *   <li>Error handling when the repository is not loaded</li>
 * </ul>
 */
public class MinimalBackboneControllerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MinimalBackboneController controller;
    private S3_RailwayRepository repository;

    /**
     * Sets up a fresh repository and controller before each test.
     */
    @Before
    public void setUp() {
        repository = S3_RailwayRepository.getInstance();
        repository.clear();
        controller = new MinimalBackboneController();
    }

    // -------------------------------------------------------------------------
    // Helper network builders
    // -------------------------------------------------------------------------

    /**
     * Creates a simple triangle network:
     *
     *   Porto --50-- Aveiro --60-- Coimbra
     *        \                      /
     *         \-------100----------/
     *
     * MST expected: 50 + 60 = 110
     */
    private void loadSimpleTriangleNetwork() {
        List<Station> stations = new ArrayList<>();
        Location loc1 = new Location(41.15, -8.61);
        Location loc2 = new Location(40.64, -8.65);
        Location loc3 = new Location(39.29, -8.91);

        Station s1 = new Station(1, "Porto", loc1, 100.0, 200.0);
        Station s2 = new Station(2, "Aveiro", loc2, 150.0, 250.0);
        Station s3 = new Station(3, "Coimbra", loc3, 200.0, 300.0);

        stations.add(s1);
        stations.add(s2);
        stations.add(s3);

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(s1, s2, 50.0, 10, 1.0));
        lines.add(new Line(s2, s3, 60.0, 10, 1.0));
        lines.add(new Line(s1, s3, 100.0, 10, 1.0));

        repository.loadStations(stations);
        repository.loadLines(lines);
    }

    /**
     * Creates a square network with a diagonal:
     *
     *   A --10-- B
     *   |        |
     *  25       20
     *   |        |
     *   D --15-- C
     *
     * + diagonal A-C (30)
     *
     * MST expected: 10 + 15 + 20 = 45
     */
    private void loadSquareNetwork() {
        List<Station> stations = new ArrayList<>();
        Location loc1 = new Location(41.0, -8.0);
        Location loc2 = new Location(41.0, -7.0);
        Location loc3 = new Location(40.0, -7.0);
        Location loc4 = new Location(40.0, -8.0);

        Station s1 = new Station(1, "A", loc1, 0.0, 100.0);
        Station s2 = new Station(2, "B", loc2, 100.0, 100.0);
        Station s3 = new Station(3, "C", loc3, 100.0, 0.0);
        Station s4 = new Station(4, "D", loc4, 0.0, 0.0);

        stations.add(s1);
        stations.add(s2);
        stations.add(s3);
        stations.add(s4);

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(s1, s2, 10.0, 10, 1.0));
        lines.add(new Line(s2, s3, 20.0, 10, 1.0));
        lines.add(new Line(s3, s4, 15.0, 10, 1.0));
        lines.add(new Line(s4, s1, 25.0, 10, 1.0));
        lines.add(new Line(s1, s3, 30.0, 10, 1.0)); // diagonal

        repository.loadStations(stations);
        repository.loadLines(lines);
    }

    /**
     * Creates a network where two stations have multiple parallel edges:
     *
     *   A → B : 50
     *   A → B : 30  (minimum)
     *   B → A : 40
     *
     * MST must select 30.
     */
    private void loadNetworkWithParallelEdges() {
        List<Station> stations = new ArrayList<>();
        Location loc1 = new Location(41.0, -8.0);
        Location loc2 = new Location(40.0, -8.0);

        Station s1 = new Station(1, "A", loc1, 0.0, 0.0);
        Station s2 = new Station(2, "B", loc2, 100.0, 100.0);

        stations.add(s1);
        stations.add(s2);

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(s1, s2, 50.0, 10, 1.0));
        lines.add(new Line(s1, s2, 30.0, 10, 1.0));
        lines.add(new Line(s2, s1, 40.0, 10, 1.0));

        repository.loadStations(stations);
        repository.loadLines(lines);
    }

    // -------------------------------------------------------------------------
    // Graph state queries
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneController#isGraphBuilt()} returns false initially.
     *
     * Scenario:
     *   - New controller with no data loaded.
     *
     * Expected:
     *   - Method returns false.
     *
     * @see MinimalBackboneController#isGraphBuilt()
     */
    @Test
    public void isGraphBuilt_initiallyFalse() {
        assertFalse(controller.isGraphBuilt());
    }

    /**
     * Tests {@link MinimalBackboneController#isGraphBuilt()} returns true after loading data.
     *
     * Scenario:
     *   - Load stations and lines in the repository.
     *
     * Expected:
     *   - Method returns true.
     *
     * @see MinimalBackboneController#isGraphBuilt()
     */
    @Test
    public void isGraphBuilt_afterLoadingData_returnsTrue() {
        loadSimpleTriangleNetwork();
        assertTrue(controller.isGraphBuilt());
    }

    /**
     * Tests initial station count.
     *
     * Scenario:
     *   - Repository initially empty.
     *
     * Expected:
     *   - getStationCount() returns 0.
     *
     * @see MinimalBackboneController#getStationCount()
     */
    @Test
    public void getStationCount_initiallyZero() {
        assertEquals(0, controller.getStationCount());
    }

    /**
     * Tests station count after loading data.
     *
     * Scenario:
     *   - Load 3 stations.
     *
     * Expected:
     *   - getStationCount() returns 3.
     *
     * @see MinimalBackboneController#getStationCount()
     */
    @Test
    public void getStationCount_afterLoadingStations_returnsCorrectCount() {
        loadSimpleTriangleNetwork();
        assertEquals(3, controller.getStationCount());
    }

    /**
     * Tests initial line count.
     *
     * Scenario:
     *   - Repository initially empty.
     *
     * Expected:
     *   - getLineCount() returns 0.
     *
     * @see MinimalBackboneController#getLineCount()
     */
    @Test
    public void getLineCount_initiallyZero() {
        assertEquals(0, controller.getLineCount());
    }

    /**
     * Tests line count after loading lines.
     *
     * Scenario:
     *   - Load 3 lines.
     *
     * Expected:
     *   - getLineCount() returns 3.
     *
     * @see MinimalBackboneController#getLineCount()
     */
    @Test
    public void getLineCount_afterLoadingLines_returnsCorrectCount() {
        loadSimpleTriangleNetwork();
        assertEquals(3, controller.getLineCount());
    }

    // -------------------------------------------------------------------------
    // Backbone calculation
    // -------------------------------------------------------------------------

    /**
     * Tests exception when calculating backbone with no data.
     *
     * Scenario:
     *   - Repository is empty.
     *
     * Expected:
     *   - IllegalStateException.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_dataNotLoaded_throwsException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Data not loaded");
        controller.calculateMinimalBackbone();
    }

    /**
     * Tests backbone calculation on a simple triangle network.
     *
     * Scenario:
     *   - Load triangle network (50, 60, 100).
     *
     * Expected:
     *   - MST selects 50 + 60.
     *   - Total distance = 110.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_simpleNetwork_returnsValidResult() {
        loadSimpleTriangleNetwork();

        MinimalBackboneResult result = controller.calculateMinimalBackbone();

        assertEquals(3, result.getStationCount());
        assertEquals(2, result.getConnectionCount());
        assertEquals(110.0, result.getTotalDistance(), 1e-9);
    }

    /**
     * Tests that MST excludes the longest edge when cheaper paths exist.
     *
     * Scenario:
     *   - Triangle network: edge 100 must not appear.
     *
     * Expected:
     *   - No Porto–Coimbra direct edge in the backbone.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_selectsMinimumEdges() {
        loadSimpleTriangleNetwork();

        MinimalBackboneResult result = controller.calculateMinimalBackbone();
        MapGraph<Station, Double> backbone = result.getBackboneGraph();

        Station porto = null, coimbra = null;
        for (Station s : backbone.vertices()) {
            if (s.getName().equals("Porto")) porto = s;
            if (s.getName().equals("Coimbra")) coimbra = s;
        }

        assertNotNull(porto);
        assertNotNull(coimbra);

        Edge<Station, Double> directEdge = backbone.edge(porto, coimbra);
        assertNull(directEdge);
    }

    /**
     * Tests MST on a square network with diagonal.
     *
     * Scenario:
     *   - Edges: 10, 15, 20, 25, 30.
     *
     * Expected:
     *   - MST = 10 + 15 + 20 = 45.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_squareNetwork_selectsOptimalEdges() {
        loadSquareNetwork();

        MinimalBackboneResult result = controller.calculateMinimalBackbone();

        assertEquals(4, result.getStationCount());
        assertEquals(3, result.getConnectionCount());
        assertEquals(45.0, result.getTotalDistance(), 1e-9);
    }

    /**
     * Tests MST selection on networks with parallel edges.
     *
     * Scenario:
     *   - A→B edges: 50, 30, 40.
     *
     * Expected:
     *   - MST uses only the minimum edge (30).
     *   - Total distance = 30.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_parallelEdges_selectsShortest() {
        loadNetworkWithParallelEdges();

        MinimalBackboneResult result = controller.calculateMinimalBackbone();

        assertEquals(2, result.getStationCount());
        assertEquals(1, result.getConnectionCount());
        assertEquals(30.0, result.getTotalDistance(), 1e-9);
    }

    /**
     * Tests that all original vertices are present in the backbone.
     *
     * Scenario:
     *   - Load triangle network.
     *
     * Expected:
     *   - Backbone contains exactly the same stations.
     *
     * @see MinimalBackboneController#calculateMinimalBackbone()
     */
    @Test
    public void calculateMinimalBackbone_includesAllVertices() {
        loadSimpleTriangleNetwork();

        MinimalBackboneResult result = controller.calculateMinimalBackbone();
        MapGraph<Station, Double> backbone = result.getBackboneGraph();

        List<Station> original = new ArrayList<>(repository.getStationsById().values());
        List<Station> backboneStations = backbone.vertices();

        assertEquals(original.size(), backboneStations.size());

        for (Station st : original) {
            boolean found = backboneStations.stream().anyMatch(b -> b.getId() == st.getId());
            assertTrue("Station " + st.getName() + " not found in backbone", found);
        }
    }
}
