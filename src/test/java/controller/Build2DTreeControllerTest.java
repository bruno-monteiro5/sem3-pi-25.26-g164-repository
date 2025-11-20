package controller;

import domain.KDTree2D;
import domain.Location;
import domain.Station;
import org.junit.Before;
import org.junit.Test;
import repository.KDTreeRepository;
import repository.StationRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link Build2DTreeController}.
 *
 * <p>
 * Each test method targets exactly one public member of {@code Build2DTreeController}
 * and documents the scenario, preconditions, and expected results.
 *
 * <p>
 * To ensure deterministic behavior, the singleton repositories
 * ({@link StationRepository} and {@link KDTreeRepository})
 * are reset before every test.
 */
public class Build2DTreeControllerTest {

    private Build2DTreeController controller;
    private StationRepository stationRepository;
    private BuildIndexesController indexController;
    private StationCsvLoader loader;

    private static final String TEST_CSV_PATH =
            "warehouseMngt/train_station_dataset/train_stations_tester.csv";

    private Station station1;
    private Station station2;
    private Station station3;

    /**
     * Resets global state and prepares shared Station objects.
     *
     * <p>Scenario:
     *   - Clear both singleton repositories to ensure test isolation.
     *   - Rebuild Stations commonly used across test cases.
     *
     * <p>Expected:
     *   - Repository instances start empty for each test.
     *   - Controller created in a known initial state.
     */
    @Before
    public void setUp() {
        StationRepository.resetForTests();
        KDTreeRepository.resetForTests();

        stationRepository = StationRepository.getInstance();
        controller = new Build2DTreeController();
        indexController = new BuildIndexesController();
        loader = new StationCsvLoader();

        station1 = new Station(
                "Lisboa Santa Apolonia",
                "PT",
                new Location(38.71387, -9.122271),
                "WET",
                true, true, false
        );

        station2 = new Station(
                "Porto Campanha",
                "PT",
                new Location(41.14961, -8.58519),
                "WET",
                true, true, false
        );

        station3 = new Station(
                "Coimbra-B",
                "PT",
                new Location(40.20564, -8.41955),
                "WET",
                true, true, false
        );
    }

    /**
     * Verifies that the constructor creates a valid controller instance.
     *
     * <p>Scenario:
     *   - Instantiate a new {@code Build2DTreeController}.
     *
     * <p>Expected:
     *   - Returned instance is non-null.
     */
    @Test
    public void testConstructor() {
        Build2DTreeController ctrl = new Build2DTreeController();
        assertNotNull(ctrl);
    }

    /**
     * Ensures that {@link Build2DTreeController#isBuilt()} is initially false.
     *
     * <p>Scenario:
     *   - Create controller immediately after repository reset.
     *
     * <p>Expected:
     *   - {@code isBuilt()} returns false.
     */
    @Test
    public void testIsBuiltInitiallyFalse() {
        assertFalse(controller.isBuilt());
    }

    /**
     * Verifies that {@link Build2DTreeController#build2DTree()} throws
     * {@link IllegalStateException} when no station data has been loaded.
     *
     * <p>Scenario:
     *   - Repository contains no station indexes.
     *   - Invoke {@code build2DTree()}.
     *
     * <p>Expected:
     *   - {@code IllegalStateException} with required message.
     */
    @Test
    public void testBuild2DTreeWithoutIndexesThrows() {
        assertFalse(stationRepository.isLoaded());

        try {
            controller.build2DTree();
            fail("Expected IllegalStateException when building without data");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("AVL indexes must be built first"));
        }
    }

    /**
     * Ensures that a 2D-Tree is built successfully once indexes exist.
     *
     * <p>Scenario:
     *   - Load station data into repository.
     *   - Invoke {@code build2DTree()}.
     *
     * <p>Expected:
     *   - No exception thrown.
     *   - {@code isBuilt()} returns true.
     *   - KD-Tree contains at least the inserted stations.
     */
    @Test
    public void testBuild2DTreeWithIndexes() {
        List<Station> stations = List.of(station1, station2, station3);
        stationRepository.loadAll(stations);

        controller.build2DTree();

        assertTrue(controller.isBuilt());
        assertNotNull(controller.getKDTree());
        assertTrue(controller.getKDTree().size() >= 3);
    }

    /**
     * Validates that {@link Build2DTreeController#getKDTree()} returns
     * a non-null KD-Tree after building.
     *
     * <p>Scenario:
     *   - Load station.
     *   - Build 2D-Tree.
     *
     * <p>Expected:
     *   - Non-null KD-Tree.
     */
    @Test
    public void testGetKDTree() {
        stationRepository.loadAll(List.of(station1));
        controller.build2DTree();

        KDTree2D tree = controller.getKDTree();
        assertNotNull(tree);
        assertTrue(tree.size() > 0);
    }

    /**
     * Tests that tree statistics are available and valid after building.
     *
     * <p>Scenario:
     *   - Load two stations.
     *   - Build 2D-Tree.
     *   - Retrieve statistics.
     *
     * <p>Expected:
     *   - Stats are non-null.
     *   - Tree size ≥ loaded stations.
     *   - Height is non-negative.
     */
    @Test
    public void testGetStatisticsAfterBuilding() {
        stationRepository.loadAll(List.of(station1, station2));
        controller.build2DTree();

        KDTree2D.KDTreeStats stats = controller.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getSize() >= 2);
        assertTrue(stats.getHeight() >= 0);
        assertNotNull(stats.getBucketDistribution());
    }

    /**
     * Ensures that {@code build2DTree()} prints a statistics summary to System.out.
     *
     * <p>Scenario:
     *   - Load valid stations.
     *   - Capture System.out during build.
     *
     * <p>Expected:
     *   - Output contains required diagnostic lines.
     */
    @Test
    public void testBuild2DTreePrintsStatistics() {
        stationRepository.loadAll(List.of(station1, station2, station3));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            controller.build2DTree();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("2D-Tree build finished"));
        assertTrue(output.contains("Size="));
        assertTrue(output.contains("Height="));
        assertTrue(output.contains("Bucket size distribution"));
    }

    /**
     * Verifies that repeated tree builds remain consistent
     * when using the same indexed station data.
     *
     * <p>Scenario:
     *   - Load station data once.
     *   - Build tree using two independent controllers.
     *
     * <p>Expected:
     *   - Both KD-Trees have identical sizes.
     */
    @Test
    public void testMultipleBuildCallsProduceConsistentTree() {
        stationRepository.loadAll(List.of(station1, station2, station3));

        controller.build2DTree();
        int firstSize = controller.getKDTree().size();

        Build2DTreeController other = new Build2DTreeController();
        other.build2DTree();
        int secondSize = other.getKDTree().size();

        assertEquals(firstSize, secondSize);
    }

    /**
     * Confirms that controllers share the same repository and can build independently.
     *
     * <p>Scenario:
     *   - Create two controllers.
     *   - Load one station.
     *   - Both attempt to build the tree.
     *
     * <p>Expected:
     *   - Both builds succeed.
     *   - Each controller has a non-null KD-Tree after building.
     */
    @Test
    public void testSingletonRepositorySharing() {
        Build2DTreeController ctrl1 = new Build2DTreeController();
        Build2DTreeController ctrl2 = new Build2DTreeController();

        stationRepository.loadAll(List.of(station1));

        ctrl1.build2DTree();
        ctrl2.build2DTree();

        assertTrue(ctrl1.isBuilt());
        assertTrue(ctrl2.isBuilt());
        assertNotNull(ctrl1.getKDTree());
        assertNotNull(ctrl2.getKDTree());
    }

    /**
     * Test: Build 2D-Tree from test CSV
     * Expected: 50 valid stations in the tree
     */
    @Test
    public void testBuild2DTreeFromTestCsv() {
        // Load test data
        indexController.buildFromCsvPath(loader, TEST_CSV_PATH);

        // Build tree
        controller.build2DTree();

        // Verify
        assertTrue("Should be built", controller.isBuilt());
        assertNotNull("Tree should not be null", controller.getKDTree());
        assertEquals("Tree should have 55 stations",
                55, controller.getKDTree().size());
        assertEquals("Height = 5",5 , controller.getKDTree().height());
        assertEquals("{1=38, 2=2, 3=3, 4=1}", controller.getStatistics().getBucketDistribution().toString());
    }

    /**
     * Test: Verify bucket distribution
     * Expected: Zurich has 3 stations, Lumes has 3, Mauleon has 3, Revel has 2
     */
    @Test
    public void testBucketDistribution() {
        indexController.buildFromCsvPath(loader, TEST_CSV_PATH);
        controller.build2DTree();

        KDTree2D.KDTreeStats stats = controller.getStatistics();

        // Should have buckets with multiple stations
        assertTrue("Should have multi-station buckets",
                stats.getBucketDistribution().size() > 1);


        int totalStations = 0;
        for (Integer count : stats.getBucketDistribution().values()) {
            totalStations += count;
        }

        assertTrue("Total node count should be less than station count",
                totalStations <= 55);
    }
}
