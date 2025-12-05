package domain;

import domain.graph.Edge;
import domain.graph.map.MapGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinimalBackboneResult {

    private final MapGraph<Station, Double> backboneGraph;
    private final double totalDistance;

    public MinimalBackboneResult(MapGraph<Station, Double> backboneGraph, double totalDistance) {
        this.backboneGraph = backboneGraph;
        this.totalDistance = totalDistance;
    }

    public MapGraph<Station, Double> getBackboneGraph() {
        return backboneGraph;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public int getStationCount() {
        return backboneGraph.numVertices();
    }

    public int getConnectionCount() {
        return backboneGraph.numEdges();
    }

    public void exportToDot(String filename) throws IOException {
        FileWriter w = new FileWriter(filename);

        w.write("graph Backbone {\n");
        w.write(" layout=neato;\n");
        w.write(" node [shape=circle, style=filled, fillcolor=lightblue];\n");

        for (Station s: backboneGraph.vertices()) {
            w.write(String.format(
                    " S%d [label=\"%s\", pos=\"%.2f,%.2f!\"];\n",
                    s.getId(), s.getName(), s.getCordX(), s.getCordY()
            ));
        }

        List<String> used = new ArrayList<>();

        for (Edge<Station, Double> e : backboneGraph.edges()) {
            Station u = e.getVOrig();
            Station v = e.getVDest();

            String key = Math.min(u.getId(), v.getId()) + "-" + Math.max(u.getId(), v.getId());
            if (used.contains(key)) continue;

            used.add(key);

            w.write(String.format(
                    " S%d -- S%d [label=\"%.2f km\"];\n",
                    u.getId(), v.getId(), e.getWeight()
            ));
        }

        w.write("}\n");
        w.close();
    }
}
