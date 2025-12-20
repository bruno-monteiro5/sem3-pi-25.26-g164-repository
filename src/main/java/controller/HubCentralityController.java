package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Algorithms;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import domain.graph.priorityqueue.Entry;
import domain.graph.priorityqueue.HeapPriorityQueue;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 *
 * Computes centrality metrics for railway stations:
 * - Degree: number of direct connections
 * - Strength: sum of edge weights
 * - Betweenness: fraction of shortest paths passing through a station
 * - Harmonic Closeness: accessibility to all other stations
 * - HubScore: weighted composite metric
 *
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

    /**
     * Computes all hub centrality metrics and returns a ranked list.
     */
    public List<HubScore> calculateHubCentrality() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildUndirectedGraph();

        List<Station> vertices = new ArrayList<>(graph.vertices());
        int n = vertices.size();

        Map<Station, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(vertices.get(i), i);
        }

        List<int[]> components = getConnectedComponentsUsingBFS(graph, vertices, indexMap);

        Map<Station, Integer> degreeMap = computeDegree(graph, vertices);
        Map<Station, Double> strengthMap = computeStrength(graph, vertices);

        Map<Station, Double> betweennessMap =
                computeBetweenness(graph, vertices, indexMap, components);

        Map<Station, Double> harmonicMap =
                computeHarmonicCloseness(graph, vertices, indexMap, components);

        Map<Station, Double> strengthNormMap =
                normalizeStrengthPerComponent(strengthMap, vertices, components);

        return buildHubScores(vertices, degreeMap, strengthMap,
                strengthNormMap, betweennessMap, harmonicMap);
    }

    /**
     * Computes degree centrality.
     */
    private Map<Station, Integer> computeDegree(MapGraph<Station, Double> graph,
                                                List<Station> vertices) {
        Map<Station, Integer> degreeMap = new HashMap<>();
        for (Station v : vertices) {
            degreeMap.put(v, graph.outDegree(v));
        }
        return degreeMap;
    }

    /**
     * Computes strength centrality.
     */
    private Map<Station, Double> computeStrength(
            MapGraph<Station, Double> graph,
            List<Station> vertices) {

        Map<Station, Double> strengthMap = new HashMap<>();

        for (Station v : vertices) {
            strengthMap.put(v, 0.0);
        }

        for (Line line : repo.getLines()) {
            Station o = line.getOrigin();
            Station d = line.getDestination();
            double cap = line.getCapacity();

            strengthMap.put(o, strengthMap.get(o) + cap);
            strengthMap.put(d, strengthMap.get(d) + cap);
        }

        return strengthMap;
    }


    /**
     * Computes betweenness centrality using Brandes-style accumulation.
     */
    private Map<Station, Double> computeBetweenness(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            List<int[]> components) {

        int n = vertices.size();
        double[] betw = new double[n];

        for (int s = 0; s < n; s++) {
            DijkstraResult r = dijkstraWithPathCounting(graph, vertices, indexMap, s);
            double[] delta = new double[n];

            for (int i = r.stackSize - 1; i >= 0; i--) {
                int w = r.stack[i];
                for (int pred : r.preds[w]) {
                    if (r.sigma[w] > 0) {
                        delta[pred] +=
                                ((double) r.sigma[pred] / (double) r.sigma[w]) *
                                        (1.0 + delta[w]);
                    }
                }
                if (w != s) {
                    betw[w] += delta[w];
                }
            }
        }

        normalizeBetweennessPerComponent(betw, components);

        Map<Station, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(vertices.get(i), betw[i]);
        }
        return result;
    }

    /**
     * Computes harmonic closeness centrality using heap-based Dijkstra distances.
     */
    private Map<Station, Double> computeHarmonicCloseness(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            List<int[]> components) {

        int n = vertices.size();
        double[] harmonic = new double[n];

        for (int s = 0; s < n; s++) {
            double[] dist = dijkstraDistancesWithHeap(graph, vertices, indexMap, s);

            double acc = 0.0;
            for (int t = 0; t < n; t++) {
                if (t == s) continue;
                double d = dist[t];
                if (d > 0.0 && d < Double.POSITIVE_INFINITY) {
                    acc += 1.0 / d;
                }
            }
            harmonic[s] = acc;
        }

        normalizeHarmonicPerComponent(harmonic, components);

        Map<Station, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(vertices.get(i), harmonic[i]);
        }
        return result;
    }

    /**
     * Heap-based Dijkstra that returns only distances from a source to all vertices.
     */
    private double[] dijkstraDistancesWithHeap(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            int sourceIdx) {

        int n = vertices.size();
        double[] dist = new double[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
        }
        dist[sourceIdx] = 0.0;

        HeapPriorityQueue<Double, Integer> pq = new HeapPriorityQueue<>();
        pq.insert(0.0, sourceIdx);

        while (!pq.isEmpty()) {
            Entry<Double, Integer> entry = pq.removeMin();
            int u = entry.getValue();

            if (visited[u]) continue;
            visited[u] = true;

            Station uS = vertices.get(u);

            for (Station nb : graph.adjVertices(uS)) {
                Integer vObj = indexMap.get(nb);
                if (vObj == null) continue;
                int v = vObj;

                if (visited[v]) continue;

                Edge<Station, Double> e = graph.edge(uS, nb);
                if (e == null) continue;

                double alt = dist[u] + e.getWeight();
                if (alt < dist[v]) {
                    dist[v] = alt;
                    pq.insert(alt, v);
                }
            }
        }

        return dist;
    }

    /**
     * Normalizes strength per connected component.
     */
    private Map<Station, Double> normalizeStrengthPerComponent(
            Map<Station, Double> strengthMap,
            List<Station> vertices,
            List<int[]> components) {

        Map<Station, Double> out = new HashMap<>();

        for (int[] comp : components) {

            double max = 0.0;
            for (int idx : comp) {
                double value = strengthMap.get(vertices.get(idx));
                if (value > max) {
                    max = value;
                }
            }

            for (int idx : comp) {
                Station v = vertices.get(idx);
                double normalized;

                if (max > 0.0) {
                    normalized = strengthMap.get(v) / max;
                } else {
                    normalized = 0.0;
                }

                out.put(v, normalized);
            }
        }

        return out;
    }


    /**
     * Normalizes betweenness per connected component.
     */
    private void normalizeBetweennessPerComponent(
            double[] betw,
            List<int[]> components) {

        for (int[] comp : components) {

            int k = comp.length;

            for (int idx : comp) {
                betw[idx] = betw[idx] / 2.0;
            }

            double divisor = (k - 1) * (k - 2) / 2.0;

            for (int idx : comp) {
                if (divisor > 0.0) {
                    betw[idx] = betw[idx] / divisor;
                } else {
                    betw[idx] = 0.0;
                }
            }
        }
    }


    /**
     * Normalizes harmonic closeness per connected component.
     */
    private void normalizeHarmonicPerComponent(
            double[] harmonic,
            List<int[]> components) {

        for (int[] comp : components) {

            double divisor = comp.length - 1.0;

            for (int idx : comp) {
                if (divisor > 0.0) {
                    harmonic[idx] = harmonic[idx] / divisor;
                } else {
                    harmonic[idx] = 0.0;
                }
            }
        }
    }


    /**
     * Builds and sorts hub scores.
     */
    private List<HubScore> buildHubScores(
            List<Station> vertices,
            Map<Station, Integer> degreeMap,
            Map<Station, Double> strengthMap,
            Map<Station, Double> strengthNormMap,
            Map<Station, Double> betweennessMap,
            Map<Station, Double> harmonicMap) {

        List<HubScore> result = new ArrayList<>();

        for (Station v : vertices) {
            double hubScore =
                    0.35 * betweennessMap.get(v)
                            + 0.35 * harmonicMap.get(v)
                            + 0.30 * strengthNormMap.get(v);

            result.add(new HubScore(
                    v,
                    degreeMap.get(v),
                    strengthMap.get(v),
                    betweennessMap.get(v),
                    harmonicMap.get(v),
                    hubScore));
        }

        for (int i = 1; i < result.size(); i++) {
            HubScore key = result.get(i);
            int j = i - 1;
            while (j >= 0 && result.get(j).getHubScore() < key.getHubScore()) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, key);
        }

        return result;
    }

    /**
     * Computes connected components using BFS from Algorithms.
     */
    private List<int[]> getConnectedComponentsUsingBFS(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap) {

        boolean[] visited = new boolean[vertices.size()];
        List<int[]> comps = new ArrayList<>();

        for (Station start : vertices) {
            int startIdx = indexMap.get(start);
            if (visited[startIdx]) continue;

            LinkedList<Station> bfs =
                    Algorithms.BreadthFirstSearch(graph, start);

            int[] temp = new int[bfs.size()];
            int size = 0;

            for (Station v : bfs) {
                int idx = indexMap.get(v);
                if (!visited[idx]) {
                    visited[idx] = true;
                    temp[size++] = idx;
                }
            }

            comps.add(Arrays.copyOf(temp, size));
        }
        return comps;
    }

    /**
     * Result container for Dijkstra with path counting.
     */
    private static class DijkstraResult {
        final double[] dist;
        final long[] sigma;
        final int[][] preds;
        final int[] stack;
        int stackSize;

        DijkstraResult(int n) {
            dist = new double[n];
            sigma = new long[n];
            preds = new int[n][];
            stack = new int[n];
        }
    }

    /**
     * Dijkstra algorithm with path counting for betweenness computation using a heap-based priority queue.
     */
    private DijkstraResult dijkstraWithPathCounting(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            int sourceIdx) {

        int n = vertices.size();
        DijkstraResult r = new DijkstraResult(n);

        boolean[] visited = new boolean[n];
        List<List<Integer>> predLists = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            r.dist[i] = Double.POSITIVE_INFINITY;
            r.sigma[i] = 0;
            predLists.add(new ArrayList<>());
        }

        r.dist[sourceIdx] = 0.0;
        r.sigma[sourceIdx] = 1;

        HeapPriorityQueue<Double, Integer> pq = new HeapPriorityQueue<>();
        pq.insert(0.0, sourceIdx);

        while (!pq.isEmpty()) {
            Entry<Double, Integer> entry = pq.removeMin();
            int u = entry.getValue();

            if (visited[u]) continue;
            visited[u] = true;

            r.stack[r.stackSize++] = u;

            Station uS = vertices.get(u);

            for (Station nb : graph.adjVertices(uS)) {
                Integer vObj = indexMap.get(nb);
                if (vObj == null) continue;
                int v = vObj;

                if (visited[v]) continue;

                Edge<Station, Double> e = graph.edge(uS, nb);
                if (e == null) continue;

                double alt = r.dist[u] + e.getWeight();

                if (alt < r.dist[v]) {
                    r.dist[v] = alt;
                    r.sigma[v] = r.sigma[u];
                    predLists.get(v).clear();
                    predLists.get(v).add(u);
                    pq.insert(alt, v);
                } else if (Math.abs(alt - r.dist[v]) < 1e-9) {
                    r.sigma[v] += r.sigma[u];
                    predLists.get(v).add(u);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            r.preds[i] = predLists.get(i).stream().mapToInt(Integer::intValue).toArray();
        }

        return r;
    }

    /**
     * Returns the number of loaded stations.
     */
    public int getStationCount() {
        return repo.getStationCount();
    }

    /**
     * Returns the number of loaded lines/connections.
     */
    public int getLineCount() {
        return repo.getLineCount();
    }
}
