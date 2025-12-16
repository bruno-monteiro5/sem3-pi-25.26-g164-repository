package domain;

import domain.graph.map.MapGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Test suite for {@link MinimalBackboneResult}.
 * <p>
 * Each test method targets a specific public member of {@code MinimalBackboneResult}
 * and documents the scenario, preconditions, and expected outcome.
 * <p>
 * All test-generated outputs are written under {@code Outputs/tests/}
 * to avoid mixing with production outputs.
 */
public class MinimalBackboneResultTest {

    private static final String TESTS_DIR = "tests";

    // Fixed filenames to overwrite outputs between test runs
    private static final String DOT_SIMPLE   = "backbone.dot";
    private static final String DOT_EMPTY    = "empty.dot";
    private static final String DOT_SPECIAL  = "special.dot";
    private static final String DOT_MISSING  = "missing.dot";
    private static final String SVG_SIMPLE   = "backbone.svg";
    private static final String SVG_INTERRUPT = "interrupt.svg";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // Helper graph builders
    // -------------------------------------------------------------------------

    /**
     * Creates a simple undirected graph with 3 stations and 2 edges.
     *
     * @return a MapGraph with 3 vertices and 2 edges
     */
    private MapGraph<Station, Double> createSimpleGraph() {
        MapGraph<Station, Double> graph = new MapGraph<>(false);

        Location loc1 = new Location(41.15, -8.61);
        Location loc2 = new Location(40.64, -8.65);
        Location loc3 = new Location(39.29, -8.91);

        Station s1 = new Station(1, "Porto", loc1, 100.0, 200.0);
        Station s2 = new Station(2, "Aveiro", loc2, 150.0, 250.0);
        Station s3 = new Station(3, "Coimbra", loc3, 200.0, 300.0);

        graph.addVertex(s1);
        graph.addVertex(s2);
        graph.addVertex(s3);

        graph.addEdge(s1, s2, 50.5);
        graph.addEdge(s2, s3, 75.3);

        return graph;
    }

    /**
     * Creates an empty undirected graph.
     *
     * @return an empty MapGraph
     */
    private MapGraph<Station, Double> createEmptyGraph() {
        return new MapGraph<>(false);
    }

    /**
     * Creates a graph with station names containing special characters.
     *
     * @return a MapGraph with special-character labels
     */
    private MapGraph<Station, Double> createGraphWithSpecialChars() {
        MapGraph<Station, Double> graph = new MapGraph<>(false);

        Location loc1 = new Location(41.0, -8.0);
        Location loc2 = new Location(40.0, -8.0);

        Station s1 = new Station(1, "São \"João\"", loc1, 10.0, 20.0);
        Station s2 = new Station(2, "Test\nStation", loc2, 30.0, 40.0);

        graph.addVertex(s1);
        graph.addVertex(s2);
        graph.addEdge(s1, s2, 25.0);

        return graph;
    }

    // -------------------------------------------------------------------------
    // Output helpers (Outputs/tests/)
    // -------------------------------------------------------------------------

    /**
     * Returns a file reference under Outputs/tests/.
     *
     * @param filename file name
     * @return file under Outputs/tests/
     */
    private File outputsTestFile(String filename) {
        return new File("Outputs", TESTS_DIR + File.separator + filename);
    }

    /**
     * Deletes a test output file if it exists.
     *
     * @param filename file name under Outputs/tests/
     */
    private void deleteIfExists(String filename) {
        File f = outputsTestFile(filename);
        if (f.exists()) {
            // best-effort cleanup; tests should still work even if delete fails
            // (e.g., file locked on some systems)
            // no assertion here to avoid flakiness
            f.delete();
        }
    }

    // -------------------------------------------------------------------------
    // Constructor and basic getters
    // -------------------------------------------------------------------------

    /**
     * Verifies that constructor and basic getters return consistent values.
     *
     * @see MinimalBackboneResult#MinimalBackboneResult(MapGraph, double)
     * @see MinimalBackboneResult#getBackboneGraph()
     * @see MinimalBackboneResult#getTotalDistance()
     * @see MinimalBackboneResult#getStationCount()
     * @see MinimalBackboneResult#getConnectionCount()
     */
    @Test
    public void constructorAndGetters_workCorrectly() {
        MapGraph<Station, Double> graph = createSimpleGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 125.8);

        assertSame(graph, result.getBackboneGraph());
        assertEquals(125.8, result.getTotalDistance(), 1e-9);
        assertEquals(3, result.getStationCount());
        assertEquals(2, result.getConnectionCount());
    }

    /**
     * Tests getters on an empty graph.
     *
     * @see MinimalBackboneResult#getStationCount()
     * @see MinimalBackboneResult#getConnectionCount()
     */
    @Test
    public void getters_emptyGraph_returnZeros() {
        MinimalBackboneResult result =
                new MinimalBackboneResult(createEmptyGraph(), 0.0);

        assertEquals(0, result.getStationCount());
        assertEquals(0, result.getConnectionCount());
    }

    // -------------------------------------------------------------------------
    // DOT export
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#exportToDot(String)} for a simple graph.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_simpleGraph_createsNodesAndEdges() throws IOException {
        deleteIfExists(DOT_SIMPLE);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createSimpleGraph(), 125.8);

        result.exportToDot(TESTS_DIR + "/" + DOT_SIMPLE);

        File produced = outputsTestFile(DOT_SIMPLE);
        assertTrue(produced.exists());
        assertTrue(produced.length() > 0);

        String content = new String(Files.readAllBytes(produced.toPath()));
        assertTrue(content.contains("graph MinimalBackbone"));
        assertTrue(content.contains("layout=neato"));
        assertTrue(content.contains("S1"));
        assertTrue(content.contains("--"));
    }

    /**
     * Tests {@link MinimalBackboneResult#exportToDot(String)} for an empty graph.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_emptyGraph_writesHeaderOnly() throws IOException {
        deleteIfExists(DOT_EMPTY);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createEmptyGraph(), 0.0);

        result.exportToDot(TESTS_DIR + "/" + DOT_EMPTY);

        String content = new String(Files.readAllBytes(
                outputsTestFile(DOT_EMPTY).toPath()));

        assertTrue(content.contains("graph MinimalBackbone"));
        assertFalse(content.contains("S1"));
        assertFalse(content.contains("--"));
    }

    /**
     * Tests that {@link MinimalBackboneResult#exportToDot(String)} escapes labels correctly.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_escapesSpecialCharactersInLabels() throws IOException {
        deleteIfExists(DOT_SPECIAL);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createGraphWithSpecialChars(), 25.0);

        result.exportToDot(TESTS_DIR + "/" + DOT_SPECIAL);

        String content = new String(Files.readAllBytes(
                outputsTestFile(DOT_SPECIAL).toPath()));

        assertTrue("Quotes should be escaped", content.contains("\\\""));
        assertFalse("Label must not contain raw newline characters",
                content.contains("S2 [label=\"Test\nStation\""));
    }

    // -------------------------------------------------------------------------
    // SVG export
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#exportToSvg(String, String)} behavior.
     *
     * <p>Scenario:
     *  - Generate a DOT file under Outputs/tests and attempt to export it to SVG.
     *
     * <p>Expected:
     *  - If Graphviz is available, the SVG file is created.
     *  - If Graphviz is not available, an IOException is thrown mentioning Graphviz/neato.
     *
     * @see MinimalBackboneResult#exportToSvg(String, String)
     */
    @Test
    public void exportToSvg_handlesEnvironmentGracefully() throws IOException {
        deleteIfExists(DOT_SIMPLE);
        deleteIfExists(SVG_SIMPLE);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createSimpleGraph(), 125.8);

        result.exportToDot(TESTS_DIR + "/" + DOT_SIMPLE);

        try {
            result.exportToSvg(TESTS_DIR + "/" + DOT_SIMPLE,
                    TESTS_DIR + "/" + SVG_SIMPLE);
            assertTrue(outputsTestFile(SVG_SIMPLE).exists());
        } catch (IOException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Graphviz") || msg.contains("neato"));
        }
    }

    /**
     * Tests {@link MinimalBackboneResult#exportToSvg(String, String)} failure branch when DOT is missing.
     *
     * <p>Scenario:
     *  - Call exportToSvg with a DOT filename that does not exist under Outputs/tests.
     *
     * <p>Expected:
     *  - An IOException is thrown (either because Graphviz is missing or because neato fails with non-zero exit code).
     *
     * @see MinimalBackboneResult#exportToSvg(String, String)
     */
    @Test
    public void exportToSvg_missingDotFile_triggersFailureBranch() throws IOException {
        deleteIfExists(DOT_MISSING);
        deleteIfExists(SVG_SIMPLE);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createSimpleGraph(), 125.8);

        try {
            result.exportToSvg(TESTS_DIR + "/" + DOT_MISSING,
                    TESTS_DIR + "/" + SVG_SIMPLE);
            fail("Expected IOException due to missing DOT file or missing Graphviz.");
        } catch (IOException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Graphviz") || msg.contains("neato") || msg.contains("exit code"));
        }
    }

    /**
     * Tests {@link MinimalBackboneResult#exportToSvg(String, String)} interruption handling.
     *
     * <p>Scenario:
     *  - Ensure DOT exists and interrupt the current thread before calling exportToSvg.
     *
     * <p>Expected:
     *  - If Graphviz is available (process starts), waitFor should throw InterruptedException and method should throw IOException.
     *  - If Graphviz is not available, IOException is still acceptable (environment-dependent).
     *
     * @see MinimalBackboneResult#exportToSvg(String, String)
     */
    @Test
    public void exportToSvg_interruptedThread_throwsIOException() throws IOException {
        deleteIfExists(DOT_SIMPLE);
        deleteIfExists(SVG_INTERRUPT);

        MinimalBackboneResult result =
                new MinimalBackboneResult(createSimpleGraph(), 125.8);

        result.exportToDot(TESTS_DIR + "/" + DOT_SIMPLE);

        Thread.currentThread().interrupt();
        try {
            result.exportToSvg(TESTS_DIR + "/" + DOT_SIMPLE,
                    TESTS_DIR + "/" + SVG_INTERRUPT);
            fail("Expected IOException due to interruption or missing Graphviz.");
        } catch (IOException e) {
            String msg = e.getMessage();
            assertTrue(msg.toLowerCase().contains("interrupted")
                    || msg.contains("Graphviz")
                    || msg.contains("neato"));
        } finally {
            // Clear interruption flag so it doesn't affect other tests
            Thread.interrupted();
        }
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#toString()} contains key fields.
     *
     * @see MinimalBackboneResult#toString()
     */
    @Test
    public void toString_containsKeyInformation() {
        MinimalBackboneResult result =
                new MinimalBackboneResult(createSimpleGraph(), 125.8);

        String str = result.toString();

        assertTrue(str.contains("MinimalBackboneResult"));
        assertTrue(str.contains("stations=3"));
        assertTrue(str.contains("connections=2"));
        assertTrue(str.contains("km"));
    }
}
