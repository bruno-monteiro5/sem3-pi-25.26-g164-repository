package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.RiskAwarePathResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI15 - Risk-Aware Shortest Paths.
 * Implements Bellman-Ford algorithm to handle negative edge costs
 * and detect negative cycles in the railway network.
 */
public class RiskAwarePathController {

    private final S3_RailwayRepository repo;

    public RiskAwarePathController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Loads railway network from CSV files and updates the repository.
     */
    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath)
            throws IOException {

        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        Map<Integer, Station> stationsById = new HashMap<>();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        repo.loadStations(stations);
        repo.loadLines(lines);
    }

    /**
     * Checks if the railway network has been loaded.
     */
    public boolean isGraphBuilt() {
        return repo.isDataLoaded();
    }

    /**
     * Returns the number of stations in the railway network.
     */
    public int getStationCount() {
        return repo.getStationCount();
    }

    /**
     * Returns the number of railway connections.
     */
    public int getLineCount() {
        return repo.getLineCount();
    }

    /**
     * Returns a station by its ID.
     */
    public Station getStationById(int id) {
        Map<Integer, Station> stations = repo.getStationsById();
        return stations.get(id);
    }

    /**
     * Computes the shortest path between two stations using edge costs.
     * Uses Bellman-Ford algorithm which can handle negative edge weights
     * and detect negative cycles.
     *
     * @param source the source station
     * @param target the target station
     * @return result containing the path or negative cycle information
     */
    public RiskAwarePathResult calculateShortestPath(Station source, Station target) {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded. Call buildGraphFromCsv first.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildDirectedGraphWithCost();

        // Run Bellman-Ford algorithm
        Map<Station, Double> distances = new HashMap<>();
        Map<Station, Station> predecessors = new HashMap<>();

        // Initialize distances
        for (Station v : graph.vertices()) {
            distances.put(v, Double.POSITIVE_INFINITY);
            predecessors.put(v, null);
        }
        distances.put(source, 0.0);

        int numVertices = graph.numVertices();

        // Relax edges V-1 times
        for (int i = 0; i < numVertices - 1; i++) {
            boolean updated = false;

            for (Edge<Station, Double> edge : graph.edges()) {
                Station u = edge.getVOrig();
                Station v = edge.getVDest();
                double weight = edge.getWeight();

                double distU = distances.get(u);
                double distV = distances.get(v);

                if (distU != Double.POSITIVE_INFINITY && distU + weight < distV) {
                    distances.put(v, distU + weight);
                    predecessors.put(v, u);
                    updated = true;
                }
            }

            // Early termination if no updates
            if (!updated) {
                break;
            }
        }

        // Check for negative cycles
        List<Station> negativeCycle = detectNegativeCycle(graph, distances, predecessors);

        if (negativeCycle != null && !negativeCycle.isEmpty()) {
            return new RiskAwarePathResult(negativeCycle);
        }

        // Check if target is reachable
        if (distances.get(target) == Double.POSITIVE_INFINITY) {
            return new RiskAwarePathResult(source, target, null, Double.POSITIVE_INFINITY);
        }

        // Reconstruct path
        List<Station> path = reconstructPath(predecessors, source, target);
        double totalCost = distances.get(target);

        return new RiskAwarePathResult(source, target, path, totalCost);
    }

    /**
     * Detects negative cycles in the graph after Bellman-Ford relaxation.
     * Performs one more iteration and checks if any distance can be improved.
     *
     * @param graph the railway network graph
     * @param distances current distance values
     * @param predecessors predecessor map for path reconstruction
     * @return list of stations involved in negative cycle, or null if no cycle
     */
    private List<Station> detectNegativeCycle(MapGraph<Station, Double> graph,
                                              Map<Station, Double> distances,
                                              Map<Station, Station> predecessors) {

        // Try one more relaxation - if any distance improves, there's a negative cycle
        for (Edge<Station, Double> edge : graph.edges()) {
            Station u = edge.getVOrig();
            Station v = edge.getVDest();
            double weight = edge.getWeight();

            double distU = distances.get(u);
            double distV = distances.get(v);

            if (distU != Double.POSITIVE_INFINITY && distU + weight < distV) {
                // Negative cycle detected - trace it
                return extractNegativeCycle(v, predecessors, graph.numVertices());
            }
        }

        return null;
    }

    /**
     * Extracts the stations involved in a negative cycle.
     *
     * @param startVertex a vertex known to be in or reachable from the cycle
     * @param predecessors predecessor map
     * @param maxSteps maximum steps to prevent infinite loops
     * @return list of stations in the cycle
     */
    private List<Station> extractNegativeCycle(Station startVertex,
                                               Map<Station, Station> predecessors,
                                               int maxSteps) {

        Set<Station> visited = new HashSet<>();
        Station current = startVertex;

        // Move back to find a vertex definitely in the cycle
        for (int i = 0; i < maxSteps && current != null; i++) {
            current = predecessors.get(current);
            if (current == null) {
                break;
            }
        }

        if (current == null) {
            return new ArrayList<>();
        }

        // Now extract the cycle
        List<Station> cycle = new ArrayList<>();
        Station cycleStart = current;

        do {
            cycle.add(current);
            current = predecessors.get(current);

            if (current == null || cycle.size() > maxSteps) {
                break;
            }

        } while (!current.equals(cycleStart));

        // Add cycle start again to close the cycle
        if (current != null && current.equals(cycleStart)) {
            cycle.add(cycleStart);
        }

        // Reverse to get correct order
        List<Station> reversed = new ArrayList<>();
        for (int i = cycle.size() - 1; i >= 0; i--) {
            reversed.add(cycle.get(i));
        }

        return reversed;
    }

    /**
     * Reconstructs the shortest path from source to target using predecessors.
     *
     * @param predecessors predecessor map from Bellman-Ford
     * @param source source station
     * @param target target station
     * @return list of stations forming the path
     */
    private List<Station> reconstructPath(Map<Station, Station> predecessors,
                                          Station source,
                                          Station target) {

        List<Station> path = new ArrayList<>();
        Station current = target;

        // Build path backwards
        while (current != null) {
            path.add(current);
            if (current.equals(source)) {
                break;
            }
            current = predecessors.get(current);
        }

        // Reverse to get correct order
        List<Station> reversed = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            reversed.add(path.get(i));
        }

        return reversed;
    }
}