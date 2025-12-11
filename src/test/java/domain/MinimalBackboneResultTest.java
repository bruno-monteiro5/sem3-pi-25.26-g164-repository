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
 * Coverage includes:
 * <ul>
 *   <li>Constructor and all getter methods</li>
 *   <li>DOT file export for non-empty and empty graphs</li>
 *   <li>Label escaping for special characters</li>
 *   <li>SVG export behavior (with or without Graphviz installed)</li>
 *   <li>{@link MinimalBackboneResult#toString()} representation</li>
 * </ul>
 */
public class MinimalBackboneResultTest {

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
    // Constructor and basic getters
    // -------------------------------------------------------------------------

    /**
     * Verifies that constructor and basic getters return consistent values.
     *
     * Scenario:
     *  - Create a result with a simple graph and known total distance.
     *
     * Expected:
     *  - getBackboneGraph() returns the same instance.
     *  - getTotalDistance() matches the constructor value.
     *  - getStationCount() and getConnectionCount() reflect the graph size.
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
        double totalDistance = 125.8;

        MinimalBackboneResult result = new MinimalBackboneResult(graph, totalDistance);

        assertSame("Backbone graph should be the same instance",
                graph, result.getBackboneGraph());
        assertEquals("Total distance should match constructor value",
                125.8, result.getTotalDistance(), 1e-9);
        assertEquals("Station count should be 3",
                3, result.getStationCount());
        // Undirected graph stores each edge twice internally
        assertEquals("Connection count should be 2",
                2, result.getConnectionCount());
    }

    /**
     * Tests getters on an empty graph.
     *
     * Scenario:
     *  - Create result with an empty graph and zero distance.
     *
     * Expected:
     *  - Station and connection counts are zero.
     *
     * @see MinimalBackboneResult#getStationCount()
     * @see MinimalBackboneResult#getConnectionCount()
     */
    @Test
    public void getters_emptyGraph_returnZeros() {
        MapGraph<Station, Double> graph = createEmptyGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 0.0);

        assertEquals("Station count should be 0", 0, result.getStationCount());
        assertEquals("Connection count should be 0", 0, result.getConnectionCount());
    }

    // -------------------------------------------------------------------------
    // DOT export
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#exportToDot(String)} for a simple graph.
     *
     * Scenario:
     *  - Export a non-empty graph to DOT format.
     *
     * Expected:
     *  - DOT file is created and contains header, node and edge definitions.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_simpleGraph_createsNodesAndEdges() throws IOException {
        MapGraph<Station, Double> graph = createSimpleGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 125.8);

        File dotFile = tempFolder.newFile("backbone.dot");
        result.exportToDot(dotFile.getAbsolutePath());

        assertTrue("DOT file should exist", dotFile.exists());
        assertTrue("DOT file should not be empty", dotFile.length() > 0);

        String content = new String(Files.readAllBytes(dotFile.toPath()));

        assertTrue("Should contain graph declaration", content.contains("graph MinimalBackbone"));
        assertTrue("Should contain neato layout", content.contains("layout=neato"));
        assertTrue("Should contain node IDs", content.contains("S1"));
        assertTrue("Should contain at least one edge", content.contains("--"));
    }

    /**
     * Tests {@link MinimalBackboneResult#exportToDot(String)} for an empty graph.
     *
     * Scenario:
     *  - Export an empty graph.
     *
     * Expected:
     *  - DOT file contains a valid graph declaration with no nodes or edges.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_emptyGraph_writesHeaderOnly() throws IOException {
        MapGraph<Station, Double> graph = createEmptyGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 0.0);

        File dotFile = tempFolder.newFile("empty.dot");
        result.exportToDot(dotFile.getAbsolutePath());

        String content = new String(Files.readAllBytes(dotFile.toPath()));

        assertTrue("Should contain graph declaration", content.contains("graph MinimalBackbone"));
        assertFalse("Should not contain any node IDs", content.contains("S1"));
        assertFalse("Should not contain any edges", content.contains("--"));
    }

    /**
     * Tests that {@link MinimalBackboneResult#exportToDot(String)} escapes labels correctly.
     *
     * Scenario:
     *  - Export graph with station names containing quotes and newlines.
     *
     * Expected:
     *  - DOT file contains escaped quotes and does not break on newlines.
     *
     * @see MinimalBackboneResult#exportToDot(String)
     */
    @Test
    public void exportToDot_escapesSpecialCharactersInLabels() throws IOException {
        MapGraph<Station, Double> graph = createGraphWithSpecialChars();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 25.0);

        File dotFile = tempFolder.newFile("special.dot");
        result.exportToDot(dotFile.getAbsolutePath());

        String content = new String(Files.readAllBytes(dotFile.toPath()));

        assertTrue("Quotes should be escaped", content.contains("\\\""));
        assertFalse("Unescaped newlines should not break node definitions",
                content.contains("Test\nStation ["));
    }

    // -------------------------------------------------------------------------
    // SVG export
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#exportToSvg(String, String)} behavior.
     *
     * Scenario:
     *  - Export a valid DOT file to SVG.
     *
     * Expected:
     *  - If Graphviz is available, an SVG file is created.
     *  - If Graphviz is not available, an IOException is thrown mentioning Graphviz/neato.
     *
     * @see MinimalBackboneResult#exportToSvg(String, String)
     */
    @Test
    public void exportToSvg_handlesEnvironmentGracefully() throws IOException {
        MapGraph<Station, Double> graph = createSimpleGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 125.8);

        File dotFile = tempFolder.newFile("backbone.dot");
        result.exportToDot(dotFile.getAbsolutePath());

        File svgFile = new File(tempFolder.getRoot(), "backbone.svg");

        try {
            result.exportToSvg(dotFile.getAbsolutePath(), svgFile.getAbsolutePath());
            // If no exception is thrown, Graphviz is installed and SVG should exist
            assertTrue("SVG file should be created when Graphviz is available",
                    svgFile.exists());
        } catch (IOException e) {
            // If Graphviz/neato is not available, an IOException is expected
            String msg = e.getMessage();
            assertTrue("Exception message should mention Graphviz or neato",
                    msg.contains("Graphviz") || msg.contains("neato"));
        }
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Tests {@link MinimalBackboneResult#toString()} contains key fields.
     *
     * Scenario:
     *  - Create result with a simple graph and known distance.
     *
     * Expected:
     *  - String representation includes station count, connection count and total distance.
     *
     * @see MinimalBackboneResult#toString()
     */
    @Test
    public void toString_containsKeyInformation() {
        MapGraph<Station, Double> graph = createSimpleGraph();
        MinimalBackboneResult result = new MinimalBackboneResult(graph, 125.8);

        String str = result.toString();

        assertTrue("Should contain class name", str.contains("MinimalBackboneResult"));
        assertTrue("Should contain station count", str.contains("stations=3"));
        assertTrue("Should contain connection count", str.contains("connections=2"));
        assertTrue("Should contain total distance and km unit", str.contains("125") && str.contains("km"));
    }
}
