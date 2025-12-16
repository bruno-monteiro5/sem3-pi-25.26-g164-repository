package controller;

import domain.RiskAwarePathResult;
import domain.Station;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link RiskAwarePathController}.
 * <p>
 * Each test method targets exactly one public member of {@code RiskAwarePathController}
 * and documents the scenario, preconditions and expected outcome.
 * <p>
 * Coverage goals:
 * <ul>
 *   <li>Constructor and public getters</li>
 *   <li>{@link RiskAwarePathController#buildGraphFromCsv(String, String)} success and IOException branch</li>
 *   <li>{@link RiskAwarePathController#calculateShortestPath(Station, Station)} covering:
 *     <ul>
 *       <li>data-not-loaded exception</li>
 *       <li>reachable path (including early termination branch)</li>
 *       <li>unreachable target</li>
 *       <li>negative cycle detected</li>
 *       <li>negativeCycle detected but extracted as empty (edge-case branch)</li>
 *     </ul>
 *   </li>
 * </ul>
 */
class RiskAwarePathControllerTest {

    private RiskAwarePathController controller;
    private S3_RailwayRepository repository;

    private Path tempStationsFile;
    private Path tempLinesFile;

    @BeforeEach
    void setUp() throws IOException {
        controller = new RiskAwarePathController();
        repository = S3_RailwayRepository.getInstance();
        repository.clear();

        tempStationsFile = Files.createTempFile("stations", ".csv");
        tempLinesFile = Files.createTempFile("lines", ".csv");
    }

    @AfterEach
    void tearDown() throws IOException {
        repository.clear();
        Files.deleteIfExists(tempStationsFile);
        Files.deleteIfExists(tempLinesFile);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Writes CSV content into the temp files and loads them via controller.
     *
     * @param stationsCsv CSV content for stations
     * @param linesCsv CSV content for lines
     * @throws IOException if file write/load fails
     */
    private void loadNetwork(String stationsCsv, String linesCsv) throws IOException {
        Files.writeString(tempStationsFile, stationsCsv);
        Files.writeString(tempLinesFile, linesCsv);
        controller.buildGraphFromCsv(tempStationsFile.toString(), tempLinesFile.toString());
    }

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Verifies constructor initializes the controller instance.
     *
     * <p>Expected:
     *   - Controller object is created successfully.
     *
     * @see RiskAwarePathController#RiskAwarePathController()
     */
    @Test
    void constructor_initializesController() {
        assertNotNull(controller);
    }

    // =========================================================================
    // buildGraphFromCsv
    // =========================================================================

    /**
     * Verifies buildGraphFromCsv loads valid files and throws for invalid paths.
     *
     * <p>Scenario:
     *   - Load a valid network (2 stations, 1 line)
     *   - Attempt load with invalid file paths
     *
     * <p>Expected:
     *   - For valid files: graph becomes "built"
     *   - For invalid paths: IOException is thrown
     *
     * @see RiskAwarePathController#buildGraphFromCsv(String, String)
     */
    @Test
    void buildGraphFromCsv_validAndInvalidPaths_behaveCorrectly() throws IOException {
        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);
        assertTrue(controller.isGraphBuilt());

        assertThrows(IOException.class, () ->
                controller.buildGraphFromCsv("nonexistent.csv", "nonexistent2.csv"));
    }

    // =========================================================================
    // isGraphBuilt
    // =========================================================================

    /**
     * Verifies isGraphBuilt reflects repository load state.
     *
     * <p>Scenario:
     *   - Before load: no data
     *   - After load: stations and lines loaded
     *
     * <p>Expected:
     *   - false before load
     *   - true after load
     *
     * @see RiskAwarePathController#isGraphBuilt()
     */
    @Test
    void isGraphBuilt_beforeAndAfterLoading_returnsExpectedValues() throws IOException {
        assertFalse(controller.isGraphBuilt());

        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);

        assertTrue(controller.isGraphBuilt());
    }

    // =========================================================================
    // getStationCount
    // =========================================================================

    /**
     * Verifies getStationCount returns 0 before load and correct value after load.
     *
     * @see RiskAwarePathController#getStationCount()
     */
    @Test
    void getStationCount_beforeAndAfterLoading_returnsExpectedValues() throws IOException {
        assertEquals(0, controller.getStationCount());

        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);

        assertEquals(2, controller.getStationCount());
    }

    // =========================================================================
    // getLineCount
    // =========================================================================

    /**
     * Verifies getLineCount returns 0 before load and correct value after load.
     *
     * @see RiskAwarePathController#getLineCount()
     */
    @Test
    void getLineCount_beforeAndAfterLoading_returnsExpectedValues() throws IOException {
        assertEquals(0, controller.getLineCount());

        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);

        assertEquals(1, controller.getLineCount());
    }

    // =========================================================================
    // getStationById
    // =========================================================================

    /**
     * Verifies getStationById returns:
     * <ul>
     *   <li>null before loading</li>
     *   <li>a valid station after loading</li>
     *   <li>null for non-existent IDs</li>
     * </ul>
     *
     * @see RiskAwarePathController#getStationById(int)
     */
    @Test
    void getStationById_beforeAndAfterLoading_returnsExpectedValues() throws IOException {
        assertNull(controller.getStationById(1));

        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,Brussels,50.8503,4.3517,100,100\n" +
                        "2,Antwerp,51.2194,4.4025,150,120\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,45.5,10,20.0\n";

        loadNetwork(stations, lines);

        Station s1 = controller.getStationById(1);
        assertNotNull(s1);
        assertEquals(1, s1.getId());
        assertEquals("Brussels", s1.getName());

        assertNull(controller.getStationById(999));
    }

    // =========================================================================
    // getAllStations
    // =========================================================================

    /**
     * Verifies getAllStations returns:
     * <ul>
     *   <li>empty list before loading</li>
     *   <li>all loaded stations after loading</li>
     * </ul>
     *
     * @see RiskAwarePathController#getAllStations()
     */
    @Test
    void getAllStations_beforeAndAfterLoading_returnsExpectedValues() throws IOException {
        List<Station> before = controller.getAllStations();
        assertNotNull(before);
        assertTrue(before.isEmpty());

        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);

        List<Station> after = controller.getAllStations();
        assertNotNull(after);
        assertEquals(2, after.size());
    }

    // =========================================================================
    // calculateShortestPath
    // =========================================================================

    /**
     * Verifies calculateShortestPath throws when data is not loaded.
     *
     * @see RiskAwarePathController#calculateShortestPath(Station, Station)
     */
    @Test
    void calculateShortestPath_dataNotLoaded_throwsException() {
        assertThrows(IllegalStateException.class, () ->
                controller.calculateShortestPath(null, null));
    }

    /**
     * Verifies calculateShortestPath returns a reachable multi-hop optimal path.
     *
     * <p>Scenario:
     *   - A->B->C cheaper than A->C
     *
     * <p>Expected:
     *   - Reachable result with total cost 11.0 and path length 3.
     *
     * @see RiskAwarePathController#calculateShortestPath(Station, Station)
     */
    @Test
    void calculateShortestPath_reachableOptimalPath_returnsExpectedResult() throws IOException {
        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,50.5,4.5,150,150\n" +
                        "3,C,51.0,5.0,200,200\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n" +
                        "2,3,10.0,10,6.0\n" +
                        "1,3,10.0,10,20.0\n";

        loadNetwork(stations, lines);

        Station a = controller.getStationById(1);
        Station c = controller.getStationById(3);

        RiskAwarePathResult result = controller.calculateShortestPath(a, c);

        assertTrue(result.isReachable());
        assertEquals(11.0, result.getTotalCost(), 1e-6);
        assertEquals(3, result.getPath().size());
    }

    /**
     * Verifies calculateShortestPath returns infinity for unreachable target.
     *
     * @see RiskAwarePathController#calculateShortestPath(Station, Station)
     */
    @Test
    void calculateShortestPath_unreachableTarget_returnsInfinity() throws IOException {
        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,51.0,5.0,150,150\n" +
                        "3,C,52.0,6.0,200,200\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n";

        loadNetwork(stations, lines);

        Station a = controller.getStationById(1);
        Station c = controller.getStationById(3);

        RiskAwarePathResult result = controller.calculateShortestPath(a, c);

        assertFalse(result.isReachable());
        assertEquals(Double.POSITIVE_INFINITY, result.getTotalCost(), 1e-6);
        assertNull(result.getPath());
    }

    /**
     * Verifies calculateShortestPath detects a negative cycle and returns it.
     *
     * @see RiskAwarePathController#calculateShortestPath(Station, Station)
     */
    @Test
    void calculateShortestPath_negativeCycle_detectedReturnsCycleResult() throws IOException {
        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n" +
                        "2,B,50.5,4.5,150,150\n" +
                        "3,C,51.0,5.0,200,200\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,2,10.0,10,5.0\n" +
                        "2,3,10.0,10,-8.0\n" +
                        "3,1,10.0,10,-5.0\n";

        loadNetwork(stations, lines);

        Station a = controller.getStationById(1);
        Station b = controller.getStationById(2);

        RiskAwarePathResult result = controller.calculateShortestPath(a, b);

        assertTrue(result.hasNegativeCycle());
        assertNotNull(result.getNegativeCycle());
        assertFalse(result.isReachable());
    }

    /**
     * Verifies calculateShortestPath handles the edge-case where a negative cycle is detected
     * but extraction returns an empty list (single-vertex self-loop with null predecessors).
     *
     * <p>Expected:
     *   - Should NOT return a negative-cycle result (because list is empty).
     *   - Returns a reachable path for source==target with cost 0.0.
     *
     * @see RiskAwarePathController#calculateShortestPath(Station, Station)
     */
    @Test
    void calculateShortestPath_negativeCycleExtractedAsEmpty_returnsNormalResult() throws IOException {
        String stations =
                "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                        "1,A,50.0,4.0,100,100\n";
        String lines =
                "departure_stid,arrival_stid,dist,capacity,cost\n" +
                        "1,1,0.0,10,-1.0\n";

        loadNetwork(stations, lines);

        Station a = controller.getStationById(1);

        RiskAwarePathResult result = controller.calculateShortestPath(a, a);

        assertFalse(result.hasNegativeCycle());
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalCost(), 1e-6);
    }

}
