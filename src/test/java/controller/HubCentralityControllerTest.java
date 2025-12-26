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

public class HubCentralityControllerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String STATIONS_CSV = "warehouseMngt" + File.separator +
            "train_station_dataset" + File.separator + "stations.csv";
    private static final String LINES_CSV = "warehouseMngt" + File.separator +
            "train_station_dataset" + File.separator + "lines.csv";

    private HubCentralityController controller;

    @Before
    public void setUp() {
        S3_RailwayRepository.getInstance().clear();
        controller = new HubCentralityController();
    }

    private void loadGraph() throws IOException {
        controller.buildGraphFromCsv(STATIONS_CSV, LINES_CSV);
    }

    @Test
    public void constructor_createsController() {
        assertNotNull(controller);
    }

    @Test
    public void buildGraphFromCsv_invalidPaths_throwsIOException() throws IOException {
        thrown.expect(IOException.class);
        controller.buildGraphFromCsv("invalid.csv", "invalid.csv");
    }

    @Test
    public void buildGraphFromCsv_validFiles_buildsGraph() throws IOException {
        loadGraph();
        assertTrue(controller.isGraphBuilt());
        assertTrue(controller.getStationCount() > 0);
        assertTrue(controller.getLineCount() > 0);
    }

    @Test
    public void isGraphBuilt_initiallyFalse() {
        assertFalse(controller.isGraphBuilt());
    }

    @Test
    public void isGraphBuilt_afterBuild_true() throws IOException {
        loadGraph();
        assertTrue(controller.isGraphBuilt());
    }

    @Test
    public void getStationCount_notBuilt_returnsZero() {
        assertEquals(0, controller.getStationCount());
    }

    @Test
    public void getStationCount_afterBuild_positive() throws IOException {
        loadGraph();
        assertTrue(controller.getStationCount() > 0);
    }

    @Test
    public void getLineCount_notBuilt_returnsZero() {
        assertEquals(0, controller.getLineCount());
    }

    @Test
    public void getLineCount_afterBuild_positive() throws IOException {
        loadGraph();
        assertTrue(controller.getLineCount() > 0);
    }

    @Test
    public void calculateHubCentrality_notBuilt_throwsException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Data not loaded");
        controller.calculateHubCentrality(false);
    }

    @Test
    public void calculateHubCentrality_undirected_returnsResults() throws IOException {
        loadGraph();
        List<HubScore> results = controller.calculateHubCentrality(false);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(controller.getStationCount(), results.size());

        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getHubScore() >= results.get(i + 1).getHubScore());
        }
    }

    @Test
    public void calculateHubCentrality_directed_returnsResults() throws IOException {
        loadGraph();
        List<HubScore> results = controller.calculateHubCentrality(true);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(controller.getStationCount(), results.size());

        for (HubScore hs : results) {
            assertNotNull(hs.getStation());
            assertTrue(hs.getDegree() >= 0);
            assertTrue(hs.getStrength() >= 0.0);
            assertTrue(hs.getBetweenness() >= 0.0 && hs.getBetweenness() <= 1.0);
            assertTrue(hs.getHarmonicCloseness() >= 0.0 && hs.getHarmonicCloseness() <= 1.0);
            assertTrue(hs.getHubScore() >= 0.0);
        }
    }

    @Test
    public void calculateHubCentrality_undirected_validMetrics() throws IOException {
        loadGraph();
        List<HubScore> results = controller.calculateHubCentrality(false);

        for (HubScore hs : results) {
            assertNotNull(hs.getStation());
            assertTrue(hs.getDegree() >= 0);
            assertTrue(hs.getStrength() >= 0.0);
            assertTrue(hs.getBetweenness() >= 0.0 && hs.getBetweenness() <= 1.0);
            assertTrue(hs.getHarmonicCloseness() >= 0.0 && hs.getHarmonicCloseness() <= 1.0);
        }
    }

    @Test
    public void calculateHubCentrality_consistentResults() throws IOException {
        loadGraph();
        List<HubScore> results1 = controller.calculateHubCentrality(false);
        List<HubScore> results2 = controller.calculateHubCentrality(false);

        assertEquals(results1.size(), results2.size());

        for (int i = 0; i < results1.size(); i++) {
            assertEquals(results1.get(i).getStation(), results2.get(i).getStation());
            assertEquals(results1.get(i).getHubScore(), results2.get(i).getHubScore(), 0.001);
        }
    }
}