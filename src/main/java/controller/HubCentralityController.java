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

        // Load stations
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        stationsById.clear();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        // Load lines
        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        // Store in repository
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.loadStations(stations);
        repo.loadLines(lines);

        // Build undirected graph
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
     * Returns the number of stations in the network.
     *
     * @return station count
     */
    public int getStationCount() {
        return graph != null ? graph.numVertices() : 0;
    }

    /**
     * Returns the number of connections in the network.
     *
     * @return edge count (undirected, so each line counted once)
     */
    public int getLineCount() {
        return graph != null ? graph.numEdges() : 0;
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

        // 1) Basic metrics
        Map<Station, Integer> degreeMap      = computeDegree(vertices);
        Map<Station, Double> strengthMap     = computeStrength(vertices);

        // 2) Centrality metrics
        Map<Station, Double> betweennessMap  = computeBetweenness(vertices);
        Map<Station, Double> harmonicMap     = computeHarmonicCloseness(vertices);

        // 3) Normalize strength to [0,1]
        Map<Station, Double> strengthNormMap = normalizeStrength(strengthMap);

        // 4) Build HubScore objects and rank them
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





}
