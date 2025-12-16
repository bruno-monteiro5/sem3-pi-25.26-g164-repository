package domain;

import domain.graph.Edge;
import domain.graph.map.MapGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;


/**
 * Result object containing the minimal backbone network computed by USEI12.
 * <p>
 * Encapsulates the minimum spanning tree graph and provides methods to
 * export the result to DOT format for visualization with Graphviz.
 */
public class MinimalBackboneResult {

    private final MapGraph<Station, Double> backboneGraph;
    private final double totalDistance;

    private static final String OUTPUT_DIR = "Outputs";


    /**
     * Creates a new result object.
     *
     * @param backboneGraph the minimum spanning tree graph
     * @param totalDistance total distance (sum of all edge weights)
     */
    public MinimalBackboneResult(MapGraph<Station, Double> backboneGraph, double totalDistance) {
        this.backboneGraph = backboneGraph;
        this.totalDistance = totalDistance;
    }

    /**
     * Returns the backbone graph (minimum spanning tree).
     *
     * @return the MST graph
     */
    public MapGraph<Station, Double> getBackboneGraph() {
        return backboneGraph;
    }

    /**
     * Returns the total distance of the backbone network.
     * <p>
     * This is the sum of all edge weights in the minimum spanning tree.
     *
     * @return total distance in kilometers
     */
    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * Returns the number of stations in the backbone network.
     **
     * @return number of vertices
     */
    public int getStationCount() {
        return backboneGraph.numVertices();
    }

    /**
     * Returns the number of connections in the backbone network.
     * <p>
     * For a tree, this is always (number of vertices - 1).
     * Since the graph is undirected, we divide by 2 to get unique connections.
     *
     * @return number of unique edges
     */
    public int getConnectionCount() {
        return backboneGraph.numEdges() / 2;
    }

    private String outputPath(String filename) {
        return OUTPUT_DIR + File.separator + filename;
    }


    /**
     * Exports the backbone network to DOT format for visualization with Graphviz.
     * <p>
     * The generated DOT file can be processed with:
     * <pre>
     * neato -Tsvg filename.dot -o output.svg
     * neato -Tpng filename.dot -o output.png
     * </pre>
     *
     * @param filename path to the output DOT file
     * @throws IOException if file writing fails
     */
    public void exportToDot(String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(outputPath(filename));
            writer.write("graph MinimalBackbone {\n");
            writer.write("  layout=neato;\n");
            writer.write("  overlap=false;\n");
            writer.write("  splines=true;\n");
            writer.write("  node [shape=circle, style=filled, fillcolor=lightblue, fontsize=10];\n");
            writer.write("  edge [color=blue, penwidth=2.0];\n");
            writer.write("\n");

            // Write vertices with XY coordinates
            List<Station> vertices = backboneGraph.vertices();
            for (Station station : vertices) {
                String nodeId = createNodeId(station);
                double x = station.getCordX();
                double y = station.getCordY();
                String label = escapeLabel(station.getName());

                writer.write(String.format(
                        Locale.US,
                        "  %s [label=\"%s\", pos=\"%.2f,%.2f!\"];\n",
                        nodeId, label, x, y
                ));
            }

            writer.write("\n");

            // Write edges (only once for undirected graph)
            Set<String> writtenEdges = new HashSet<>();
            for (Edge<Station, Double> edge : backboneGraph.edges()) {
                Station u = edge.getVOrig();
                Station v = edge.getVDest();
                String edgeKey = createUndirectedEdgeKey(u, v);

                if (!writtenEdges.contains(edgeKey)) {
                    String nodeIdU = createNodeId(u);
                    String nodeIdV = createNodeId(v);
                    double distance = edge.getWeight();

                    writer.write(String.format("  %s -- %s [label=\"%.2f km\"];\n", nodeIdU, nodeIdV, distance));
                    writtenEdges.add(edgeKey);
                }
            }
            writer.write("}\n");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Generates an SVG file from the DOT file using Graphviz neato.
     * <p>
     * This method executes the neato command to generate the SVG visualization.
     * The DOT file must already exist (created by exportToDot).
     * Requires Graphviz to be installed and in the system PATH.
     *
     * Process:
     * 1. Executes: neato -n -Tsvg dotFilename -o svgFilename
     * 2. Waits for command completion
     * 3. Checks exit code
     *
     * @param dotFilename input DOT file (must exist)
     * @param svgFilename output SVG file to create
     * @throws IOException if Graphviz is not installed, command fails, or file operations fail
     */
    public void exportToSvg(String dotFilename, String svgFilename) throws IOException {
        try {
            String dotPath = outputPath(dotFilename);
            String svgPath = outputPath(svgFilename);
            String[] command = {
                    "neato",
                    "-n2",
                    "-Tsvg",
                    dotPath,
                    "-o",
                    svgPath
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorMsg.append(line).append("\n");
                }
                reader.close();

                throw new IOException("Graphviz neato command failed with exit code: " + exitCode +
                        "\nError: " + errorMsg.toString() +
                        "\nMake sure Graphviz is installed and 'neato' is in your PATH.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("SVG generation was interrupted", e);
        } catch (IOException e) {
            throw new IOException("Failed to execute Graphviz neato. " + "Make sure Graphviz is installed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a unique node identifier for a station.
     *
     * @param station the station
     * @return node identifier string
     */
    private String createNodeId(Station station) {
        return "S" + station.getId();
    }

    /**
     * Creates a unique key for an undirected edge.
     *
     * @param s1 first station
     * @param s2 second station
     * @return edge key string
     */
    private String createUndirectedEdgeKey(Station s1, Station s2) {
        int id1 = s1.getId();
        int id2 = s2.getId();
        if (id1 < id2) {
            return id1 + "-" + id2;
        } else {
            return id2 + "-" + id1;
        }
    }

    /**
     * Escapes special characters in labels for DOT format.
     *
     * @param label the original label
     * @return escaped label safe for DOT format
     */
    private String escapeLabel(String label) {
        if (label == null) {
            return "";
        }
        return label.replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public String toString() {
        return String.format("MinimalBackboneResult{stations=%d, connections=%d, totalDistance=%.2f km}",
                getStationCount(), getConnectionCount(), totalDistance);
    }
}