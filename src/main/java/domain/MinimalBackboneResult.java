package domain;

import domain.graph.Edge;
import domain.graph.map.MapGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Result object containing the minimal backbone network computed by USEI12.
 * <p>
 * Encapsulates the minimum spanning tree graph and provides methods to
 * export the result to DOT format for visualization with Graphviz.
 */
public class MinimalBackboneResult {

    private final MapGraph<Station, Double> backboneGraph;
    private final double totalDistance;

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
     *
     * @return number of vertices
     */
    public int getStationCount() {
        return backboneGraph.numVertices();
    }

    /**
     * Returns the number of connections in the backbone network.
     * <p>
     * For a tree, this is always (number of vertices - 1).
     *
     * @return number of edges
     */
    public int getConnectionCount() {
        return backboneGraph.numEdges();
    }

    /**
     * Exports the backbone network to DOT format for visualization with Graphviz.
     * <p>
     * The generated DOT file can be processed with:
     * <pre>
     * neato -Tsvg filename.dot -o output.svg
     * </pre>
     *
     * @param filename path to the output DOT file
     * @throws IOException if file writing fails
     */
    public void exportToDot(String filename) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            writer.write("graph MinimalBackbone {\n");
            writer.write("  layout=neato;\n");
            writer.write("  node [shape=circle, style=filled, fillcolor=lightblue];\n");
            writer.write("  edge [color=blue];\n");
            writer.write("\n");

            List<Station> vertices = backboneGraph.vertices();
            for (Station station : vertices) {
                String nodeId = createNodeId(station);
                double x = station.getCordX();
                double y = station.getCordY();
                String label = escapeLabel(station.getName());

                // Using String.format compatible with older Java versions if needed
                writer.write(String.format("  %s [label=\"%s\", pos=\"%.2f,%.2f!\"];\n",
                        nodeId, label, x, y));
            }

            writer.write("\n");

            List<String> writtenEdges = new ArrayList<>();
            for (Edge<Station, Double> edge : backboneGraph.edges()) {
                Station u = edge.getVOrig();
                Station v = edge.getVDest();
                String edgeKey = createUndirectedEdgeKey(u, v);

                if (!writtenEdges.contains(edgeKey)) {
                    String nodeIdU = createNodeId(u);
                    String nodeIdV = createNodeId(v);
                    double distance = edge.getWeight();

                    writer.write(String.format("  %s -- %s [label=\"%.2f km\"];\n",
                            nodeIdU, nodeIdV, distance));
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
        return label.replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return String.format("MinimalBackboneResult{stations=%d, connections=%d, totalDistance=%.2f km}",
                getStationCount(), getConnectionCount(), totalDistance);
    }
}