package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 */
public class HubCentralityController {

    private final S3_RailwayRepository repo;

    public HubCentralityController() {
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
     * Computes all hub centrality metrics and returns a ranked list.
     * Uses the undirected graph from the repository.
     */
    public List<HubScore> calculateHubCentrality() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded. Call buildGraphFromCsv first.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildUndirectedGraph();

        List<Station> vertices = new ArrayList<>(graph.vertices());

        Map<Station, Integer> degreeMap = computeDegree(graph, vertices);
        Map<Station, Double> strengthMap = computeStrength(graph, vertices);
        Map<Station, Double> betweennessMap = computeBetweenness(graph, vertices);
        Map<Station, Double> harmonicMap = computeHarmonicCloseness(graph, vertices);
        Map<Station, Double> strengthNormMap = normalizeStrength(strengthMap);

        return buildHubScores(vertices, degreeMap, strengthMap,
                strengthNormMap, betweennessMap, harmonicMap);
    }

    private Map<Station, Integer> computeDegree(MapGraph<Station, Double> graph, List<Station> vertices) {
        Map<Station, Integer> degreeMap = new HashMap<>();
        for (Station v : vertices) {
            degreeMap.put(v, graph.outDegree(v));
        }
        return degreeMap;
    }

    private Map<Station, Double> computeStrength(MapGraph<Station, Double> graph, List<Station> vertices) {
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

    private Map<Station, Double> computeBetweenness(MapGraph<Station, Double> graph, List<Station> vertices) {
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
                    betweennessMap.put(w, betweennessMap.get(w) + dependency.get(w));
                }
            }
        }

        double betweennessDivisor = (n - 1) * (n - 2) / 2.0;
        if (betweennessDivisor > 0) {
            for (Station v : vertices) {
                betweennessMap.put(v, betweennessMap.get(v) / betweennessDivisor);
            }
        }

        return betweennessMap;
    }

    private Map<Station, Double> computeHarmonicCloseness(MapGraph<Station, Double> graph, List<Station> vertices) {
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

            for (Station target : vertices) {
                if (!source.equals(target)) {
                    double dist = distances.get(target);
                    if (dist > 0 && dist < Double.POSITIVE_INFINITY) {
                        harmonicMap.put(target, harmonicMap.get(target) + (1.0 / dist));
                    }
                }
            }
        }

        double harmonicDivisor = n - 1.0;
        if (harmonicDivisor > 0) {
            for (Station v : vertices) {
                harmonicMap.put(v, harmonicMap.get(v) / harmonicDivisor);
            }
        }

        return harmonicMap;
    }

    private Map<Station, Double> normalizeStrength(Map<Station, Double> strengthMap) {
        double maxStrength = 0.0;
        for (double s : strengthMap.values()) {
            if (s > maxStrength) maxStrength = s;
        }

        Map<Station, Double> strengthNormMap = new HashMap<>();
        for (Map.Entry<Station, Double> e : strengthMap.entrySet()) {
            double normalized = maxStrength > 0 ? e.getValue() / maxStrength : 0.0;
            strengthNormMap.put(e.getKey(), normalized);
        }
        return strengthNormMap;
    }

    private List<HubScore> buildHubScores(List<Station> vertices,
                                          Map<Station, Integer> degreeMap,
                                          Map<Station, Double> strengthMap,
                                          Map<Station, Double> strengthNormMap,
                                          Map<Station, Double> betweennessMap,
                                          Map<Station, Double> harmonicMap) {
        List<HubScore> results = new ArrayList<>();

        for (Station v : vertices) {
            double betw = betweennessMap.get(v);
            double harm = harmonicMap.get(v);
            double strNorm = strengthNormMap.get(v);
            double hubScore = 0.35 * betw + 0.35 * harm + 0.30 * strNorm;

            results.add(new HubScore(v, degreeMap.get(v), strengthMap.get(v),
                    betw, harm, hubScore));
        }

        results.sort(null);
        return results;
    }
}