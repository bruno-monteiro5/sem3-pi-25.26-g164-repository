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

        List<int[]> components = getConnectedComponents(graph, vertices, indexMap);

        Map<Station, Integer> degreeMap = computeDegree(graph, vertices);
        Map<Station, Double> strengthMap = computeStrength(graph, vertices);

        Map<Station, Double> betweennessMap = computeBetweenness(graph, vertices, indexMap, components);
        Map<Station, Double> harmonicMap = computeHarmonicCloseness(graph, vertices, indexMap, components);

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
            for (Edge<Station, Double> e : graph.outgoingEdges(v)) {
                strength += e.getWeight();
            }
            strengthMap.put(v, strength);
        }
        return strengthMap;
    }

    /**
     * Betweenness (Brandes-style accumulation) with Dijkstra O(n^2) per source.
     * Normalized per connected component to [0,1] (undirected).
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

            // dependency accumulation
            double[] delta = new double[n];

            // process vertices in non-increasing distance order (stack order from Dijkstra)
            for (int i = r.stackSize - 1; i >= 0; i--) {
                int w = r.stack[i];
                for (int pred : r.preds[w]) {
                    if (r.sigma[w] > 0) {
                        delta[pred] += ((double) r.sigma[pred] / (double) r.sigma[w]) * (1.0 + delta[w]);
                    }
                }
                if (w != s) {
                    betw[w] += delta[w];
                }
            }
        }

        // normalize per component (undirected: divide by 2 first, then by component divisor)
        normalizeBetweennessPerComponent(betw, components);

        Map<Station, Double> betweennessMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            betweennessMap.put(vertices.get(i), betw[i]);
        }
        return betweennessMap;
    }

    /**
     * Harmonic closeness per station:
     * harmonic(s) = (1/(k-1)) * sum_{t != s, reachable} 1/d(s,t) inside its component.
     */
    private Map<Station, Double> computeHarmonicCloseness(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            List<int[]> components) {

        int n = vertices.size();
        double[] harmonic = new double[n];

        // compute raw sum(1/dist) for each source
        for (int s = 0; s < n; s++) {
            double[] dist = dijkstraDistances(graph, vertices, indexMap, s);

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

        // normalize per component by (k-1)
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
     * Connected components for an undirected graph, returned as arrays of vertex indices.
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

            // BFS using indices
            int[] queue = new int[n];
            int qh = 0;
            int qt = 0;

            int[] temp = new int[n];
            int size = 0;

            visited[i] = true;
            queue[qt++] = i;

            while (qh < qt) {
                int cur = queue[qh++];
                temp[size++] = cur;

                Station curS = vertices.get(cur);
                for (Station nb : graph.adjVertices(curS)) {
                    Integer nbIdxObj = indexMap.get(nb);
                    if (nbIdxObj == null) continue;
                    int nbIdx = nbIdxObj;

                    if (!visited[nbIdx]) {
                        visited[nbIdx] = true;
                        queue[qt++] = nbIdx;
                    }
                }
            }

            int[] comp = new int[size];
            System.arraycopy(temp, 0, comp, 0, size);
            comps.add(comp);
        }

        return comps;
    }


    private static class DijkstraResult {
        final double[] dist;
        final int[] sigma;
        final int[][] preds;
        final int[] stack;
        int stackSize;

        DijkstraResult(int n) {
            this.dist = new double[n];
            this.sigma = new int[n];
            this.preds = new int[n][];
            this.stack = new int[n];
            this.stackSize = 0;
        }
    }

    /**
     * Dijkstra with:
     * - dist[]
     * - sigma[] (number of shortest paths)
     * - preds[] as int-lists (predecessor indices)
     * - stack[] in order of "settled" vertices
     */
    private DijkstraResult dijkstraWithPathCounting(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            int sourceIdx) {

        int n = vertices.size();
        DijkstraResult resultD = new DijkstraResult(n);

        boolean[] visited = new boolean[n];
        @SuppressWarnings("unchecked")
        ArrayList<Integer>[] predLists = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            resultD.dist[i] = Double.POSITIVE_INFINITY;
            resultD.sigma[i] = 0;
            predLists[i] = new ArrayList<>();
        }

        resultD.dist[sourceIdx] = 0.0;
        resultD.sigma[sourceIdx] = 1;

        while (true) {
            int u = selectMinUnvisited(resultD.dist, visited);
            if (u == -1) break;

            visited[u] = true;
            resultD.stack[resultD.stackSize++] = u;

            Station uS = vertices.get(u);
            for (Station nb : graph.adjVertices(uS)) {
                Integer vObj = indexMap.get(nb);
                if (vObj == null) continue;
                int v = vObj;

                if (visited[v]) {
                }

                Edge<Station, Double> e = graph.edge(uS, nb);
                if (e == null) continue;

                double w = e.getWeight();
                double alt = resultD.dist[u] + w;

                if (alt < resultD.dist[v]) {
                    resultD.dist[v] = alt;
                    resultD.sigma[v] = resultD.sigma[u];
                    predLists[v].clear();
                    predLists[v].add(u);
                } else if (Math.abs(alt - resultD.dist[v]) < 1e-9) {
                    resultD.sigma[v] += resultD.sigma[u];
                    predLists[v].add(u);
                }
            }
        }

        // convert predecessor lists to int arrays
        for (int i = 0; i < n; i++) {
            ArrayList<Integer> list = predLists[i];
            int[] arr = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
                arr[j] = list.get(j);
            }
            resultD.preds[i] = arr;
        }

        return resultD;
    }

    private double[] dijkstraDistances(
            MapGraph<Station, Double> graph,
            List<Station> vertices,
            Map<Station, Integer> indexMap,
            int sourceIdx) {

        int n = vertices.size();
        double[] dist = new double[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) dist[i] = Double.POSITIVE_INFINITY;
        dist[sourceIdx] = 0.0;

        while (true) {
            int u = selectMinUnvisited(dist, visited);
            if (u == -1) break;

            visited[u] = true;

            Station uS = vertices.get(u);
            for (Station nb : graph.adjVertices(uS)) {
                Integer vObj = indexMap.get(nb);
                if (vObj == null) continue;
                int v = vObj;

                Edge<Station, Double> e = graph.edge(uS, nb);
                if (e == null) continue;

                double alt = dist[u] + e.getWeight();
                if (alt < dist[v]) {
                    dist[v] = alt;
                }
            }
        }

        return dist;
    }

    /**
     * Returns index of unvisited vertex with smallest dist, or -1 if none reachable.
     * This removes the "minDist == POSITIVE_INFINITY always false" warning.
     */
    private int selectMinUnvisited(double[] dist, boolean[] visited) {
        int best = -1;
        double bestDist = Double.POSITIVE_INFINITY;

        for (int i = 0; i < dist.length; i++) {
            if (!visited[i] && dist[i] < bestDist) {
                bestDist = dist[i];
                best = i;
            }
        }

        return best;
    }
}
