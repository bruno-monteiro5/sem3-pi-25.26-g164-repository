package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 * <p>
 * This controller:
 * <ul>
 *   <li>Loads railway network data from CSV files</li>
 *   <li>Builds an undirected weighted graph</li>
 *   <li>Calculates centrality metrics for each station</li>
 *   <li>Computes a combined hub score</li>
 * </ul>
 */
public class HubCentralityController {

    private MapGraph<Station, Double> graph;
    private Map<Integer, Station> stationsById;

    /**
     * Creates a new controller instance.
     */
    public HubCentralityController() {
        this.graph = null;
        this.stationsById = new HashMap<>();
    }

    /**
     * Loads railway network from CSV files and builds the graph.
     *
     * @param stationsCsvPath path to stations CSV file
     * @param linesCsvPath path to lines CSV file
     * @throws IOException if file reading fails
     */
    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath)
            throws IOException {

        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        stationsById.clear();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.loadStations(stations);
        repo.loadLines(lines);

        graph = new MapGraph<>(false); // undirected

        for (Station s : stations) {
            graph.addVertex(s);
        }

        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station destination = line.getDestination();
            double weight = line.getDistance();

            graph.addEdge(origin, destination, weight);
        }
    }

    /**
     * Checks if the railway network graph has been loaded.
     *
     * @return true if graph is built, false otherwise
     */
    public boolean isGraphBuilt() {
        return graph != null && graph.numVertices() > 0;
    }

    /**
     * Returns the number of stations (vertices) in the railway network graph.
     * If the graph has not been built yet, returns 0.
     *
     * @return total number of stations in the graph
     */
    public int getStationCount() {

        if (graph == null) {
            return 0;
        }

        return graph.numVertices();
    }

    /**
     * Returns the number of railway connections (edges) in the graph.
     * If the graph has not been built yet, returns 0.
     *
     * @return total number of edges in the graph
     */
    public int getLineCount() {

        if (graph == null) {
            return 0;
        }

        return graph.numEdges();
    }


    /**
     * Computes all hub centrality metrics and returns a ranked list of results.
     * <p>
     * Steps:
     * <ol>
     *   <li>Compute degree and strength for each station</li>
     *   <li>Compute betweenness and harmonic closeness centralities</li>
     *   <li>Normalize strength to [0,1]</li>
     *   <li>Compute the composite HubScore =
     *       0.35 * betweenness + 0.35 * harmonicCloseness + 0.30 * strengthNorm</li>
     *   <li>Sort stations by HubScore descending and return the list</li>
     * </ol>
     *
     * @return list of HubScore objects sorted by HubScore (highest first)
     * @throws IllegalStateException if the graph has not been built yet
     */
    public List<HubScore> calculateHubCentrality() {
        if (!isGraphBuilt()) {
            throw new IllegalStateException("Graph not built. Load network first.");
        }

        List<Station> vertices = new ArrayList<>(graph.vertices());

        // Basic metrics
        Map<Station, Integer> degreeMap      = computeDegree(vertices);
        Map<Station, Double> strengthMap     = computeStrength(vertices);

        // Centrality metrics
        Map<Station, Double> betweennessMap  = computeBetweenness(vertices);
        Map<Station, Double> harmonicMap     = computeHarmonicCloseness(vertices);

        // Normalize strength to [0,1]
        Map<Station, Double> strengthNormMap = normalizeStrength(strengthMap);

        // Build HubScore objects and rank them
        return buildHubScores(vertices, degreeMap, strengthMap,
                strengthNormMap, betweennessMap, harmonicMap);
    }


    /**
     * Computes the degree (number of direct neighbors) for each station.
     *
     * @param vertices list of all stations in the graph
     * @return map station -> degree
     */
    private Map<Station, Integer> computeDegree(List<Station> vertices) {
        Map<Station, Integer> degreeMap = new HashMap<>();

        for (Station v : vertices) {
            int degree = graph.outDegree(v);
            degreeMap.put(v, degree);
        }

        return degreeMap;
    }

    /**
     * Computes the strength (sum of incident edge weights) for each station.
     *
     * @param vertices list of all stations in the graph
     * @return map station -> strength (sum of edge weights)
     */
    private Map<Station, Double> computeStrength(List<Station> vertices) {
        Map<Station, Double> strengthMap = new HashMap<>();

        for (Station v : vertices) {
            double strength = 0.0;

            for (Edge<Station, Double> edge : graph.outgoingEdges(v)) {
                strength += edge.getWeight();
            }

            strengthMap.put(v, strength);
        }

        return strengthMap;
    }

    /**
     * Computes betweenness centrality for all stations.
     * <p>
     * This implementation is based on Brandes' algorithm adapted to this project.
     * For each source station, it:
     * <ul>
     *   <li>Finds shortest paths to all other stations</li>
     *   <li>Counts how many shortest paths go through each vertex</li>
     *   <li>Accumulates the dependency values to get betweenness</li>
     * </ul>
     * At the end, values are normalized using the standard factor
     * (n - 1)(n - 2)/2 for undirected graphs.
     *
     * @param vertices list of all stations in the graph
     * @return map station -> normalized betweenness centrality in [0,1]
     */
    private Map<Station, Double> computeBetweenness(List<Station> vertices) {
        Map<Station, Double> betweennessMap = new HashMap<>();
        int n = vertices.size();

        for (Station v : vertices) {
            betweennessMap.put(v, 0.0);
        }

        for (Station source : vertices) {

            Map<Station, List<Station>> predecessors = new HashMap<>();
            Map<Station, Integer> pathCount = new HashMap<>();
            Map<Station, Double> distances = new HashMap<>();

            for (Station v : vertices) {
                predecessors.put(v, new ArrayList<>());
                pathCount.put(v, 0);
                distances.put(v, Double.POSITIVE_INFINITY);
            }

            distances.put(source, 0.0);
            pathCount.put(source, 1);

            List<Station> stack = new ArrayList<>();
            List<Station> queue = new ArrayList<>();
            queue.add(source);

            // Shortest-path search
            while (!queue.isEmpty()) {
                Station current = queue.remove(0);
                stack.add(current);

                for (Station neighbor : graph.adjVertices(current)) {
                    Edge<Station, Double> edge = graph.edge(current, neighbor);
                    double newDist = distances.get(current) + edge.getWeight();

                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        pathCount.put(neighbor, pathCount.get(current));
                        predecessors.get(neighbor).clear();
                        predecessors.get(neighbor).add(current);

                        if (!queue.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    } else if (Math.abs(newDist - distances.get(neighbor)) < 1e-9) {
                        pathCount.put(neighbor, pathCount.get(neighbor) + pathCount.get(current));
                        predecessors.get(neighbor).add(current);
                    }
                }
            }

            // Accumulation phase
            Map<Station, Double> dependency = new HashMap<>();
            for (Station v : vertices) {
                dependency.put(v, 0.0);
            }

            for (int i = stack.size() - 1; i >= 0; i--) {
                Station w = stack.get(i);

                for (Station pred : predecessors.get(w)) {
                    double contribution = (pathCount.get(pred) / (double) pathCount.get(w)) * (1.0 + dependency.get(w));
                    dependency.put(pred, dependency.get(pred) + contribution);
                }

                if (!w.equals(source)) {
                    double currentBetweenness = betweennessMap.get(w);
                    betweennessMap.put(w, currentBetweenness + dependency.get(w));
                }
            }
        }

        // Normalize betweenness to [0,1] for undirected graphs
        double betweennessDivisor = (n - 1) * (n - 2) / 2.0;
        if (betweennessDivisor > 0) {
            for (Station v : vertices) {
                double normalized = betweennessMap.get(v) / betweennessDivisor;
                betweennessMap.put(v, normalized);
            }
        }

        return betweennessMap;
    }

    /**
     * Computes harmonic closeness centrality for all stations.
     * <p>
     * For each source station, it computes shortest-path distances to all
     * other stations and accumulates 1/distance for each reachable target.
     * At the end, values are normalized by (n - 1).
     *
     * @param vertices list of all stations in the graph
     * @return map station -> normalized harmonic closeness in [0,1] (approximately)
     */
    private Map<Station, Double> computeHarmonicCloseness(List<Station> vertices) {
        Map<Station, Double> harmonicMap = new HashMap<>();
        int n = vertices.size();

        for (Station v : vertices) {
            harmonicMap.put(v, 0.0);
        }

        for (Station source : vertices) {

            Map<Station, Double> distances = new HashMap<>();
            for (Station v : vertices) {
                distances.put(v, Double.POSITIVE_INFINITY);
            }
            distances.put(source, 0.0);

            List<Station> queue = new ArrayList<>();
            queue.add(source);

            while (!queue.isEmpty()) {
                Station current = queue.remove(0);

                for (Station neighbor : graph.adjVertices(current)) {
                    Edge<Station, Double> edge = graph.edge(current, neighbor);
                    double newDist = distances.get(current) + edge.getWeight();

                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        if (!queue.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }

            // Accumulate harmonic closeness contributions
            for (Station target : vertices) {
                if (!source.equals(target)) {
                    double dist = distances.get(target);
                    if (dist > 0 && dist < Double.POSITIVE_INFINITY) {
                        double current = harmonicMap.get(target);
                        harmonicMap.put(target, current + (1.0 / dist));
                    }
                }
            }
        }

        // Normalize harmonic closeness
        double harmonicDivisor = n - 1.0;
        if (harmonicDivisor > 0) {
            for (Station v : vertices) {
                double normalized = harmonicMap.get(v) / harmonicDivisor;
                harmonicMap.put(v, normalized);
            }
        }

        return harmonicMap;
    }

    /**
     * Normalizes strength values to the range [0,1] by dividing by the maximum strength.
     *
     * @param strengthMap raw strength values (station -> strength)
     * @return map station -> normalized strength in [0,1]
     */
    private Map<Station, Double> normalizeStrength(Map<Station, Double> strengthMap) {
        double maxStrength = 0.0;
        for (double s : strengthMap.values()) {
            if (s > maxStrength) {
                maxStrength = s;
            }
        }

        Map<Station, Double> strengthNormMap = new HashMap<>();
        for (Map.Entry<Station, Double> e : strengthMap.entrySet()) {
            double normalized;

            if (maxStrength > 0) {
                normalized = e.getValue() / maxStrength;
            } else {
                normalized = 0.0;
            }
            strengthNormMap.put(e.getKey(), normalized);
        }

        return strengthNormMap;
    }

    /**
     * Builds the final list of HubScore objects and sorts them by HubScore descending.
     *
     * @param vertices        list of all stations
     * @param degreeMap       degree values per station
     * @param strengthMap     raw strength values per station
     * @param strengthNormMap normalized strength values per station
     * @param betweennessMap  betweenness centrality values per station
     * @param harmonicMap     harmonic closeness centrality values per station
     * @return sorted list of HubScore objects (highest HubScore first)
     */
    private List<HubScore> buildHubScores(List<Station> vertices,
                                          Map<Station, Integer> degreeMap,
                                          Map<Station, Double> strengthMap,
                                          Map<Station, Double> strengthNormMap,
                                          Map<Station, Double> betweennessMap,
                                          Map<Station, Double> harmonicMap) {

        List<HubScore> results = new ArrayList<>();

        for (Station v : vertices) {
            double betw    = betweennessMap.get(v);
            double harm    = harmonicMap.get(v);
            double strNorm = strengthNormMap.get(v);

            double hubScore = 0.35 * betw + 0.35 * harm + 0.30 * strNorm;

            results.add(new HubScore(
                    v,
                    degreeMap.get(v),
                    strengthMap.get(v),
                    betw,
                    harm,
                    hubScore
            ));
        }

        results.sort(null);
        return results;
    }
}