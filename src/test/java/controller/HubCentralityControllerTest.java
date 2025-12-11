package controller;

import domain.HubScore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import repository.S3_RailwayRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for HubCentralityController.
 *
 * These tests cover:
 *  - Graph loading from CSV (success and failure)
 *  - Graph state queries (isGraphBuilt, getStationCount, getLineCount)
 *  - Centrality computation (calculateHubCentrality)
 */
public class HubCentralityControllerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // Use the same default paths as in the UI / production code
    private static final String STATIONS_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "stations.csv";

    private static final String LINES_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "lines.csv";

    /**
     * Reset the singleton repository before each test to ensure test isolation.
     */
    @Before
    public void setUp() {
        // Clear the repository state before each test
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.clear();
    }

    /**
     * Helper to assert that the real CSV files exist.
     */
    private void assertDatasetFilesExist() {
        File stationsFile = new File(STATIONS_CSV_PATH);
        File linesFile = new File(LINES_CSV_PATH);

        assertTrue("Stations CSV not found at: " + stationsFile.getAbsolutePath(),
                stationsFile.isFile());
        assertTrue("Lines CSV not found at: " + linesFile.getAbsolutePath(),
                linesFile.isFile());
    }

    /**
     * buildGraphFromCsv should throw IOException when given invalid paths.
     */
    @Test
    public void buildGraphFromCsv_invalidPaths_throwsIOException() throws IOException {
        HubCentralityController controller = new HubCentralityController();

        thrown.expect(IOException.class);

        controller.buildGraphFromCsv("non_existing_stations.csv", "non_existing_lines.csv");
    }

    /**
     * buildGraphFromCsv should build the graph when valid CSV paths are provided.
     */
    @Test
    public void buildGraphFromCsv_validFiles_buildsGraph() throws IOException {
        assertDatasetFilesExist();

        HubCentralityController controller = new HubCentralityController();

        controller.buildGraphFromCsv(STATIONS_CSV_PATH, LINES_CSV_PATH);

        assertTrue("Graph should be built after loading CSV files", controller.isGraphBuilt());
        assertTrue("Station count should be greater than 0", controller.getStationCount() > 0);
        assertTrue("Line count should be greater than 0", controller.getLineCount() > 0);
    }

    /**
     * isGraphBuilt() should be false for a new controller with no graph loaded.
     */
    @Test
    public void isGraphBuilt_initiallyFalse() {
        HubCentralityController controller = new HubCentralityController();

        assertFalse("Graph should not be built initially", controller.isGraphBuilt());
    }

    /**
     * isGraphBuilt() should be true after building the graph.
     */
    @Test
    public void isGraphBuilt_afterBuildingGraph_true() throws IOException {
        assertDatasetFilesExist();

        HubCentralityController controller = new HubCentralityController();

        controller.buildGraphFromCsv(STATIONS_CSV_PATH, LINES_CSV_PATH);

        assertTrue("Graph should be built", controller.isGraphBuilt());
    }

    /**
     * getStationCount() should return 0 when the graph has not been built.
     */
    @Test
    public void getStationCount_graphNotBuilt_returnsZero() {
        HubCentralityController controller = new HubCentralityController();

        assertEquals("Station count should be 0 when graph is not built",
                0, controller.getStationCount());
    }

    /**
     * getStationCount() should return a positive value after the graph is built.
     */
    @Test
    public void getStationCount_afterBuildingGraph_positive() throws IOException {
        assertDatasetFilesExist();

        HubCentralityController controller = new HubCentralityController();

        controller.buildGraphFromCsv(STATIONS_CSV_PATH, LINES_CSV_PATH);

        int stationCount = controller.getStationCount();
        assertTrue("Station count should be > 0 after building the graph",
                stationCount > 0);
    }

    /**
     * getLineCount() should return 0 when the graph has not been built.
     */
    @Test
    public void getLineCount_graphNotBuilt_returnsZero() {
        HubCentralityController controller = new HubCentralityController();

        assertEquals("Line count should be 0 when graph is not built",
                0, controller.getLineCount());
    }

    /**
     * getLineCount() should return a positive value after the graph is built.
     */
    @Test
    public void getLineCount_afterBuildingGraph_positive() throws IOException {
        assertDatasetFilesExist();

        HubCentralityController controller = new HubCentralityController();

        controller.buildGraphFromCsv(STATIONS_CSV_PATH, LINES_CSV_PATH);

        int lineCount = controller.getLineCount();
        assertTrue("Line count should be > 0 after building the graph",
                lineCount > 0);
    }

    /**
     * calculateHubCentrality() should throw IllegalStateException
     * if called before the graph is built.
     */
    @Test
    public void calculateHubCentrality_graphNotBuilt_throwsIllegalStateException() {
        HubCentralityController controller = new HubCentralityController();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Data not loaded");

        controller.calculateHubCentrality();
    }

    /**
     * calculateHubCentrality() should return a non-empty ranked list of HubScore
     * when the graph has been built correctly.
     *
     * This indirectly exercises:
     *  - computeDegree
     *  - computeStrength
     *  - computeBetweenness
     *  - computeHarmonicCloseness
     *  - normalizeStrength
     *  - buildHubScores
     */
    @Test
    public void calculateHubCentrality_graphBuilt_returnsRankedHubScores() throws IOException {
        assertDatasetFilesExist();

        HubCentralityController controller = new HubCentralityController();

        controller.buildGraphFromCsv(STATIONS_CSV_PATH, LINES_CSV_PATH);

        List<HubScore> results = controller.calculateHubCentrality();

        assertNotNull("Result list should not be null", results);
        assertFalse("Result list should not be empty", results.isEmpty());

        // Size should match the number of stations in the graph
        assertEquals("Result list size should match station count",
                controller.getStationCount(), results.size());

        // Check that the list is sorted in descending order by HubScore
        for (int i = 0; i < results.size() - 1; i++) {
            double currentScore = results.get(i).getHubScore();
            double nextScore = results.get(i + 1).getHubScore();
            assertTrue("HubScores should be sorted in descending order",
                    currentScore >= nextScore);
        }

        // Sanity checks on each HubScore
        for (HubScore hs : results) {
            assertNotNull("Station should not be null", hs.getStation());
            assertTrue("Degree should be >= 0", hs.getDegree() >= 0);
            assertTrue("Strength should be >= 0", hs.getStrength() >= 0.0);
            assertTrue("Betweenness should be >= 0", hs.getBetweenness() >= 0.0);
            assertTrue("Harmonic closeness should be >= 0", hs.getHarmonicCloseness() >= 0.0);
        }
    }
}