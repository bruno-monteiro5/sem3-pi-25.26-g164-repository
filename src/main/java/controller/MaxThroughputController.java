package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI14 - Maximum Throughput Calculator.
 */
public class MaxThroughputController {

    private final S3_RailwayRepository repo;

    public MaxThroughputController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Loads CSVs and updates the repository.
     */
    public void buildGraphFromCsv(String stationsCsv, String linesCsv) throws IOException {
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsv);

        Map<Integer, Station> stationsById = new HashMap<>();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsv, stationsById);

        repo.loadStations(stations);
        repo.loadLines(lines);
    }

    /**
     * Computes maximum flow between source and target using Edmonds-Karp.
     * Uses capacity-based directed graph from the repository.
     */
    public int computeMaxFlow(Station source, Station target) {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildDirectedGraphWithCapacity();

        Map<Station, Map<Station, Integer>> residual = buildResidualGraph(graph);
        return edmondsKarp(residual, source, target);
    }

    /**
     * Builds residual capacity map from the graph.
     */
    private Map<Station, Map<Station, Integer>> buildResidualGraph(MapGraph<Station, Double> graph) {
        Map<Station, Map<Station, Integer>> residual = new HashMap<>();

        for (Station u : graph.vertices()) {
            residual.put(u, new HashMap<>());
        }

        for (Station u : graph.vertices()) {
            for (Station v : graph.adjVertices(u)) {
                double w = graph.edge(u, v).getWeight();
                int cap = (int) Math.round(w);
                if (cap < 0) cap = 0;
                residual.get(u).put(v, cap);
            }
        }

        return residual;
    }

    /**
     * Edmonds-Karp algorithm (Ford-Fulkerson with BFS).
     */
    private int edmondsKarp(Map<Station, Map<Station, Integer>> residual,
                            Station source,
                            Station target) {
        int maxFlow = 0;

        while (true) {
            Map<Station, Station> parent = new HashMap<>();
            int pathFlow = bfsFindAugmentingPath(source, target, parent, residual);
            if (pathFlow == 0) break;

            maxFlow += pathFlow;

            Station cur = target;
            while (!cur.equals(source)) {
                Station prev = parent.get(cur);

                int prevToCur = residual.get(prev).getOrDefault(cur, 0) - pathFlow;
                residual.get(prev).put(cur, prevToCur);

                int curToPrev = residual.get(cur).getOrDefault(prev, 0) + pathFlow;
                residual.get(cur).put(prev, curToPrev);

                cur = prev;
            }
        }

        return maxFlow;
    }

    /**
     * BFS to find augmenting path. Returns path flow or 0.
     */
    private int bfsFindAugmentingPath(Station source,
                                      Station target,
                                      Map<Station, Station> parent,
                                      Map<Station, Map<Station, Integer>> residual) {
        Queue<Station> q = new LinkedList<>();
        q.add(source);
        parent.clear();
        parent.put(source, null);

        while (!q.isEmpty()) {
            Station u = q.poll();
            for (Map.Entry<Station, Integer> entry : residual.get(u).entrySet()) {
                Station v = entry.getKey();
                int cap = entry.getValue();
                if (cap > 0 && !parent.containsKey(v)) {
                    parent.put(v, u);
                    if (v.equals(target)) {
                        return computePathFlow(source, target, parent, residual);
                    }
                    q.add(v);
                }
            }
        }
        return 0;
    }

    private int computePathFlow(Station source,
                                Station target,
                                Map<Station, Station> parent,
                                Map<Station, Map<Station, Integer>> residual) {
        int flow = Integer.MAX_VALUE;
        Station cur = target;
        while (!cur.equals(source)) {
            Station prev = parent.get(cur);
            int cap = residual.get(prev).getOrDefault(cur, 0);
            flow = Math.min(flow, cap);
            cur = prev;
        }
        return flow == Integer.MAX_VALUE ? 0 : flow;
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

    public Station getStationById(String id) {
        return repo.getStationById(id);
    }
}