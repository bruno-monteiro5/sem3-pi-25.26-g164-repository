package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import domain.graph.Graph;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Class used for the backbone operations in the calculation of the Maximum throughput in a line between two stations.
 */
public class MaxThroughputController {

    public Graph<Station, Double> graph = null;
    public boolean graphBuilt = false;

    /**
     * Loads CSVs and builds directed graph.
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

        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.loadStations(stations);
        repo.loadLines(lines);

        buildGraph(stations, lines);
        graphBuilt = true;
    }

    /**
     * Builds a directed MapGraph using line.capacity as the edge weight (capacity).
     * This change is required because the max-flow problem uses capacities (c(E)), not distances.
     */
    private void buildGraph(List<Station> stations, List<Line> lines) {
        graph = new MapGraph<>(true);

        for (Station s : stations) {
            graph.addVertex(s);
        }

        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double capacity = line.getCapacity();
            graph.addEdge(origin, dest, capacity);
        }
    }

    /**
     * Computes maximum flow between source and target.
     *
     * Implementation notes:
     * - Builds an integer residual capacity map from graph edge weights (casts to int).
     * - Uses BFS to find the shortest augmenting paths.
     */
    public int computeMaxFlow(Station source, Station target) {
        if (graph == null) return 0;

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
     * BFS to find the shortest augmenting path from source to target in the residual graph.
     * Returns path flow, or 0 if none.
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
        return graphBuilt;
    }

    public int getStationCount() {
        return graph == null ? 0 : graph.numVertices();
    }

    public int getLineCount() {
        return graph == null ? 0 : graph.numEdges();
    }

    public Station getStationById(int id) {
        if (graph == null) return null;
        for (Station s : graph.vertices()) {
            if (s.getId() == id) return s;
        }
        return null;
    }
}
