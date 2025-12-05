package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.MinimalBackboneResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinimalBackboneController {

    private MapGraph<Station, Double> directedGraph;
    private MapGraph<Station, Double> backboneGraph;
    private boolean graphBuilt;

    public MinimalBackboneController() {
        this.directedGraph = null;
        this.backboneGraph = null;
        this.graphBuilt = false;
    }

    public void buildGraphFromCsv(String stationsCsv, String linesCsv) throws IOException {
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsv);

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsv, createStationMap(stations));

        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.loadStations(stations);
        repo.loadLines(lines);

        buildGraph(stations, lines);
        graphBuilt = true;
    }

    private java.util.Map<Integer, Station> createStationMap(List<Station> stations) {
        java.util.Map<Integer, Station> map = new java.util.HashMap<>();
        for (Station s : stations) map.put(s.getId(), s);
        return map;
    }

    private void buildGraph(List<Station> stations, List<Line> lines) {
        directedGraph = new MapGraph<>(true);

        for (Station s : stations) directedGraph.addVertex(s);

        for (Line line : lines)
            directedGraph.addEdge(line.getOrigin(), line.getDestination(), line.getDistance());
    }

    public MinimalBackboneResult calculateMinimalBackbone() {
        if (!graphBuilt) throw new IllegalStateException("Graph not built");

        MapGraph<Station, Double> undirected = convertToUndirected(directedGraph);
        backboneGraph = kruskalMST(undirected);
        double totalDistance = computeTotal(backboneGraph);

        return new MinimalBackboneResult(backboneGraph, totalDistance);
    }

    private double computeTotal(MapGraph<Station, Double> graph) {
        double total = 0;
        for (Edge<Station, Double> e : graph.edges()) total += e.getWeight();
        return total;
    }

    private MapGraph<Station, Double> convertToUndirected(MapGraph<Station, Double> directed) {
        MapGraph<Station, Double> undirected = new MapGraph<>(false);

        for (Station s : directed.vertices()) undirected.addVertex(s);

        java.util.Set<String> added = new java.util.HashSet<>();

        for (Edge<Station, Double> e : directed.edges()) {
            Station u = e.getVOrig();
            Station v = e.getVDest();
            double w = e.getWeight();

            String k1 = u.getId() + "-" + v.getId();
            String k2 = v.getId() + "-" + u.getId();

            if (!added.contains(k1) && !added.contains(k2)) {
                Edge<Station, Double> rev = directed.edge(v, u);
                if (rev != null) w = Math.min(w, rev.getWeight());

                undirected.addEdge(u, v, w);
                added.add(k1);
                added.add(k2);
            }
        }
        return undirected;
    }

    private MapGraph<Station, Double> kruskalMST(MapGraph<Station, Double> graph) {
        MapGraph<Station, Double> mst = new MapGraph<>(false);

        for (Station s : graph.vertices()) mst.addVertex(s);

        List<Edge<Station, Double>> edges = new ArrayList<>(graph.edges());
        bubbleSortEdges(edges);

        UnionFind uf = new UnionFind(graph.vertices());

        for (Edge<Station, Double> e : edges) {
            Station u = e.getVOrig();
            Station v = e.getVDest();

            if (!uf.connected(u, v)) {
                mst.addEdge(u, v, e.getWeight());
                uf.union(u, v);
            }
        }
        return mst;
    }

    private void bubbleSortEdges(List<Edge<Station, Double>> edges) {
        for (int i = 0; i < edges.size() - 1; i++) {
            for (int j = 0; j < edges.size() - i - 1; j++) {
                if (edges.get(j).getWeight() > edges.get(j + 1).getWeight()) {
                    Edge<Station, Double> tmp = edges.get(j);
                    edges.set(j, edges.get(j + 1));
                    edges.set(j + 1, tmp);
                }
            }
        }
    }

    private static class UnionFind {
        private final java.util.Map<Station, Station> parent = new java.util.HashMap<>();
        private final java.util.Map<Station, Integer> rank = new java.util.HashMap<>();

        public UnionFind(List<Station> stations) {
            for (Station s : stations) {
                parent.put(s, s);
                rank.put(s, 0);
            }
        }

        public Station find(Station s) {
            if (!parent.get(s).equals(s)) parent.put(s, find(parent.get(s)));
            return parent.get(s);
        }

        public boolean connected(Station a, Station b) {
            return find(a).equals(find(b));
        }

        public void union(Station a, Station b) {
            Station ra = find(a);
            Station rb = find(b);
            if (ra.equals(rb)) return;

            int r1 = rank.get(ra);
            int r2 = rank.get(rb);

            if (r1 < r2) parent.put(ra, rb);
            else if (r1 > r2) parent.put(rb, ra);
            else {
                parent.put(rb, ra);
                rank.put(ra, r1 + 1);
            }
        }
    }
}
