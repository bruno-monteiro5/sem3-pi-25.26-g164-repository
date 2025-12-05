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
}
