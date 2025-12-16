package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Algorithms;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 * Computes centrality metrics for railway stations:
 * - Degree: number of direct connections
 * - Strength: sum of edge weights (distances)
 * - Betweenness: fraction of shortest paths passing through a station
 * - Harmonic Closeness: accessibility to all other stations
 * - HubScore: weighted composite metric (0.35*betweenness + 0.35*harmonic + 0.30*strength_norm)
 * All normalized metrics are normalized to [0,1] per connected component.
 */
public class HubCentralityController {

    private final S3_RailwayRepository repo;

    public HubCentralityController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Loads railway network from CSV files and updates the repository.
     */
    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath) throws IOException {
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

    public boolean isGraphBuilt() {
        return repo.isDataLoaded();
    }

    public int getStationCount() {
        return repo.getStationCount();
    }

    public int getLineCount() {
        return repo.getLineCount();
    }

    /**
     * Computes all hub centrality metrics and returns a ranked list.
     * Uses the undirected graph from the repository.
     * All normalized metrics are normalized to [0,1] per connected component.
     */
    public List<HubScore> calculateHubCentrality() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded. Call buildGraphFromCsv first.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildUndirectedGraph();

        List<Station> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        Map<Station, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(vertices.get(i), i);
        }

        // Find connected components
        List<int[]> components = getConnectedComponents(graph, vertices, indexMap);

        // Compute basic metrics
        Map<Station, Integer> degreeMap = computeDegree(graph, vertices);
        Map<Station, Double> strengthMap = computeStrength(graph, vertices);

        // Compute betweenness using shortest paths
        Map<Station, Double> betweennessMap = computeBetweenness(graph, vertices, indexMap, components);

        // Compute harmonic closeness
        Map<Station, Double> harmonicMap = computeHarmonicCloseness(graph, vertices, indexMap, components);

        // Normalize strength per component
        Map<Station, Double> strengthNormMap = normalizeStrengthPerComponent(strengthMap, vertices, components);

        return buildHubScores(vertices, degreeMap, strengthMap, strengthNormMap, betweennessMap, harmonicMap);
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
            for (domain.graph.Edge<Station, Double> e : graph.outgoingEdges(v)) {
                strength += e.getWeight();
            }
            strengthMap.put(v, strength);
        }
        return strengthMap;
    }

    /**
     * Computes betweenness centrality using shortest paths from Algorithms class.
     * For each pair of vertices (s,t), we compute all shortest paths and count
     * how many pass through each intermediate vertex.
     *
     * Betweenness(v) = sum over all pairs (s,t) of: (number of shortest s-t paths through v) / (total number of shortest s-t paths)
     *
     * Normalized per connected component to [0,1] (undirected graph).
     */
    private Map<Station, Double> computeBetweenness(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            List<int[]> components) {

        int n = vertices.size();
        double[] betw = new double[n];

        // For each source vertex
        for (Station source : vertices) {
            // Use shortestPaths from Algorithms to get all paths from source
            ArrayList<LinkedList<Station>> paths = new ArrayList<>();
            ArrayList<Double> dists = new ArrayList<>();

            Algorithms.shortestPaths(graph, source,
                    Double::compare, Double::sum, 0.0, paths, dists);

            // For each destination vertex
            for (int destIdx = 0; destIdx < n; destIdx++) {
                Station dest = vertices.get(destIdx);
                if (source.equals(dest)) continue;

                Double distToDest = dists.get(destIdx);
                if (distToDest == null || distToDest == Double.POSITIVE_INFINITY) {
                    continue; // No path
                }

                // Count shortest paths from source to dest
                List<LinkedList<Station>> shortestPaths = findAllShortestPaths(
                        graph, source, dest, distToDest);

                if (shortestPaths.isEmpty()) continue;

                int totalPaths = shortestPaths.size();

                // For each intermediate vertex, count how many shortest paths pass through it
                for (int vIdx = 0; vIdx < n; vIdx++) {
                    Station v = vertices.get(vIdx);
                    if (v.equals(source) || v.equals(dest)) continue;

                    int pathsThrough = 0;
                    for (LinkedList<Station> path : shortestPaths) {
                        if (path.contains(v)) {
                            pathsThrough++;
                        }
                    }

                    if (pathsThrough > 0 && totalPaths > 0) {
                        betw[vIdx] += (double) pathsThrough / totalPaths;
                    }
                }
            }
        }

        // Normalize per component (undirected: divide by 2 first, then by component divisor)
        normalizeBetweennessPerComponent(betw, components);

        Map<Station, Double> betweennessMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            betweennessMap.put(vertices.get(i), betw[i]);
        }
        return betweennessMap;
    }

    /**
     * Finds all shortest paths from source to dest with the given distance.
     */
    private List<LinkedList<Station>> findAllShortestPaths(
            MapGraph<Station, Double> graph,
            Station source,
            Station dest,
            double targetDist) {

        List<LinkedList<Station>> result = new ArrayList<>();
        ArrayList<LinkedList<Station>> allPaths = Algorithms.allPaths(graph, source, dest);

        for (LinkedList<Station> path : allPaths) {
            double pathDist = calculatePathDistance(graph, path);
            if (Math.abs(pathDist - targetDist) < 1e-9) {
                result.add(path);
            }
        }

        return result;
    }

    /**
     * Calculates the total distance of a path.
     */
    private double calculatePathDistance(MapGraph<Station, Double> graph, LinkedList<Station> path) {
        double dist = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            domain.graph.Edge<Station, Double> e = graph.edge(path.get(i), path.get(i + 1));
            if (e != null) {
                dist += e.getWeight();
            }
        }
        return dist;
    }

    /**
     * Computes harmonic closeness using shortestPaths from Algorithms.
     * Harmonic closeness(v) = (1/(n-1)) * sum_{u != v, reachable} 1/d(v,u)
     *
     * Normalized per connected component.
     */
    private Map<Station, Double> computeHarmonicCloseness(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            List<int[]> components) {

        int n = vertices.size();
        double[] harmonic = new double[n];

        // For each source vertex
        for (int srcIdx = 0; srcIdx < n; srcIdx++) {
            Station source = vertices.get(srcIdx);

            // Use shortestPaths from Algorithms
            ArrayList<LinkedList<Station>> paths = new ArrayList<>();
            ArrayList<Double> dists = new ArrayList<>();

            Algorithms.shortestPaths(graph, source,
                    Double::compare, Double::sum, 0.0, paths, dists);

            double sum = 0.0;
            for (int destIdx = 0; destIdx < n; destIdx++) {
                if (srcIdx == destIdx) continue;

                Double dist = dists.get(destIdx);
                if (dist != null && dist > 0.0 && dist < Double.POSITIVE_INFINITY) {
                    sum += 1.0 / dist;
                }
            }

            harmonic[srcIdx] = sum;
        }

        // Normalize per component by (k-1)
        normalizeHarmonicPerComponent(harmonic, components);

        Map<Station, Double> harmonicMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            harmonicMap.put(vertices.get(i), harmonic[i]);
        }
        return harmonicMap;
    }

    private Map<Station, Double> normalizeStrengthPerComponent(
            Map<Station, Double> strengthMap,
            List<Station> vertices,
            List<int[]> components) {

        Map<Station, Double> out = new HashMap<>();

        for (int[] comp : components) {
            double max = 0.0;

            for (int idx : comp) {
                Station v = vertices.get(idx);
                double s = strengthMap.get(v);
                if (s > max) max = s;
            }

            for (int idx : comp) {
                Station v = vertices.get(idx);
                double s = strengthMap.get(v);
                out.put(v, (max > 0.0) ? (s / max) : 0.0);
            }
        }

        return out;
    }

    private void normalizeBetweennessPerComponent(double[] betw, List<int[]> components) {
        for (int[] comp : components) {
            int k = comp.length;

            // undirected adjustment (/2)
            for (int idx : comp) {
                betw[idx] = betw[idx] / 2.0;
            }

            // normalize by (k-1)*(k-2)/2
            double divisor = (k - 1) * (k - 2) / 2.0;
            if (divisor > 0.0) {
                for (int idx : comp) {
                    betw[idx] = betw[idx] / divisor;
                }
            } else {
                for (int idx : comp) {
                    betw[idx] = 0.0;
                }
            }
        }
    }

    private void normalizeHarmonicPerComponent(double[] harmonic, List<int[]> components) {
        for (int[] comp : components) {
            int k = comp.length;
            double divisor = (k - 1.0);

            for (int idx : comp) {
                harmonic[idx] = (divisor > 0.0) ? (harmonic[idx] / divisor) : 0.0;
            }
        }
    }

    private List<HubScore> buildHubScores(
            List<Station> vertices,
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

            results.add(new HubScore(
                    v,
                    degreeMap.get(v),
                    strengthMap.get(v),
                    betw,
                    harm,
                    hubScore
            ));
        }

        sortHubScores(results);
        return results;
    }

    /**
     * Sorts HubScore list by hubScore in descending order using insertion sort.
     */
    private void sortHubScores(List<HubScore> list) {
        for (int i = 1; i < list.size(); i++) {
            HubScore key = list.get(i);
            int j = i - 1;

            while (j >= 0 && list.get(j).getHubScore() < key.getHubScore()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    /**
     * Connected components for an undirected graph using BFS.
     * Uses BreadthFirstSearch from Algorithms class.
     */
    private List<int[]> getConnectedComponents(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap) {

        int n = vertices.size();
        boolean[] visited = new boolean[n];
        List<int[]> comps = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;

            Station start = vertices.get(i);
            LinkedList<Station> component = Algorithms.BreadthFirstSearch(graph, start);

            if (component == null || component.isEmpty()) continue;

            int[] compIndices = new int[component.size()];
            int idx = 0;
            for (Station s : component) {
                Integer sIdx = indexMap.get(s);
                if (sIdx != null) {
                    visited[sIdx] = true;
                    compIndices[idx++] = sIdx;
                }
            }

            comps.add(compIndices);
        }

        return comps;
    }
}