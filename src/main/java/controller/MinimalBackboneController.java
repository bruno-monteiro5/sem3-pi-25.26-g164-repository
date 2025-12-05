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

/**
 * Controller for USEI12 - Minimal Backbone Network.
 * <p>
 * Computes a minimum spanning tree over the railway network using Kruskal's algorithm.
 * The resulting network connects all reachable stations with minimum total track length.
 * <p>
 * The backbone is computed treating the directed graph as undirected, as the goal is
 * to identify a minimal physical infrastructure that connects all stations.
 */
public class MinimalBackboneController {

    private MapGraph<Station, Double> directedGraph;
    private MapGraph<Station, Double> backboneGraph;
    private boolean graphBuilt;

    /**
     * Creates a new controller with no graph loaded.
     */
    public MinimalBackboneController() {
        this.directedGraph = null;
        this.backboneGraph = null;
        this.graphBuilt = false;
    }

    /**
     * Builds the railway network graph from CSV files.
     * <p>
     * Loads stations and lines from the provided CSV paths and constructs
     * a directed graph representation of the railway network.
     *
     * @param stationsCsv path to the stations CSV file
     * @param linesCsv    path to the lines CSV file
     * @throws IOException if file reading fails
     */
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

    /**
     * Creates a map of station ID to Station object.
     *
     * @param stations list of stations
     * @return map with station IDs as keys
     */
    private java.util.Map<Integer, Station> createStationMap(List<Station> stations) {
        java.util.Map<Integer, Station> map = new java.util.HashMap<>();
        for (Station s : stations) {
            map.put(s.getId(), s);
        }
        return map;
    }

    /**
     * Builds the internal graph representation from stations and lines.
     *
     * @param stations list of all stations
     * @param lines    list of all railway lines
     */
    private void buildGraph(List<Station> stations, List<Line> lines) {
        directedGraph = new MapGraph<>(true);

        for (Station s : stations) {
            directedGraph.addVertex(s);
        }

        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double distance = line.getDistance();

            directedGraph.addEdge(origin, dest, distance);
        }
    }

    /**
     * Computes the minimal backbone network using Kruskal's algorithm.
     * <p>
     * The algorithm treats the directed graph as undirected and finds the
     * minimum spanning tree that connects all reachable stations with
     * minimum total distance.
     *
     * @return result object containing the backbone graph and total distance
     * @throws IllegalStateException if graph has not been built
     */
    public MinimalBackboneResult calculateMinimalBackbone() {
        if (!graphBuilt) {
            throw new IllegalStateException("Graph not built. Call buildGraphFromCsv first.");
        }

        MapGraph<Station, Double> undirectedGraph = convertToUndirected(directedGraph);
        backboneGraph = kruskallMST(undirectedGraph);
        double totalDistance = calculateTotalDistance(backboneGraph);

        return new MinimalBackboneResult(backboneGraph, totalDistance);
    }

    /**
     * Converts a directed graph to an undirected representation.
     *
     * @param directed the directed graph
     * @return undirected graph representation
     */
    private MapGraph<Station, Double> convertToUndirected(MapGraph<Station, Double> directed) {
        MapGraph<Station, Double> undirected = new MapGraph<>(false);

        for (Station s : directed.vertices()) {
            undirected.addVertex(s);
        }

        java.util.Set<String> addedEdges = new java.util.HashSet<>();

        for (Edge<Station, Double> edge : directed.edges()) {
            Station u = edge.getVOrig();
            Station v = edge.getVDest();
            Double weight = edge.getWeight();

            String edgeKey1 = createEdgeKey(u, v);
            String edgeKey2 = createEdgeKey(v, u);

            if (!addedEdges.contains(edgeKey1) && !addedEdges.contains(edgeKey2)) {
                Edge<Station, Double> reverseEdge = directed.edge(v, u);
                Double finalWeight = weight;

                if (reverseEdge != null) {
                    finalWeight = Math.min(weight, reverseEdge.getWeight());
                }

                undirected.addEdge(u, v, finalWeight);
                addedEdges.add(edgeKey1);
                addedEdges.add(edgeKey2);
            }
        }
        return undirected;
    }

    /**
     * Creates a unique key for an edge between two stations.
     *
     * @param s1 first station
     * @param s2 second station
     * @return string key representing the edge
     */
    private String createEdgeKey(Station s1, Station s2) {
        return s1.getId() + "-" + s2.getId();
    }

    /**
     * Implements Kruskal's algorithm to find the minimum spanning tree.
     *
     * @param graph undirected graph
     * @return minimum spanning tree as a graph
     */
    private MapGraph<Station, Double> kruskallMST(MapGraph<Station, Double> graph) {
        MapGraph<Station, Double> mst = new MapGraph<>(false);

        for (Station v : graph.vertices()) {
            mst.addVertex(v);
        }

        List<Edge<Station, Double>> edges = new ArrayList<>();
        for (Edge<Station, Double> e : graph.edges()) {
            edges.add(e);
        }

        sortEdgesByWeight(edges);

        UnionFind uf = new UnionFind(graph.vertices());

        for (Edge<Station, Double> edge : edges) {
            Station u = edge.getVOrig();
            Station v = edge.getVDest();

            if (!uf.connected(u, v)) {
                mst.addEdge(u, v, edge.getWeight());
                uf.union(u, v);
            }
        }
        return mst;
    }

    /**
     * Sorts edges by weight in ascending order using bubble sort (to avoid lambdas/comparators).
     *
     * @param edges list of edges to sort (modified in place)
     */
    private void sortEdgesByWeight(List<Edge<Station, Double>> edges) {
        int n = edges.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (edges.get(j).getWeight() > edges.get(j + 1).getWeight()) {
                    Edge<Station, Double> temp = edges.get(j);
                    edges.set(j, edges.get(j + 1));
                    edges.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * Calculates the total distance (sum of all edge weights) in the graph.
     *
     * @param graph the graph to measure
     * @return total sum of edge weights
     */
    private double calculateTotalDistance(MapGraph<Station, Double> graph) {
        double total = 0.0;
        for (Edge<Station, Double> edge : graph.edges()) {
            total += edge.getWeight();
        }
        return total;
    }

    /**
     * Union-Find (Disjoint Set Union) data structure for cycle detection.
     */
    private static class UnionFind {
        private java.util.Map<Station, Station> parent;
        private java.util.Map<Station, Integer> rank;

        public UnionFind(List<Station> stations) {
            parent = new java.util.HashMap<>();
            rank = new java.util.HashMap<>();
            for (Station s : stations) {
                parent.put(s, s);
                rank.put(s, 0);
            }
        }

        public Station find(Station s) {
            if (!parent.get(s).equals(s)) {
                parent.put(s, find(parent.get(s)));
            }
            return parent.get(s);
        }

        public void union(Station s1, Station s2) {
            Station root1 = find(s1);
            Station root2 = find(s2);

            if (root1.equals(root2)) {
                return;
            }

            int rank1 = rank.get(root1);
            int rank2 = rank.get(root2);

            if (rank1 < rank2) {
                parent.put(root1, root2);
            } else if (rank1 > rank2) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank1 + 1);
            }
        }

        public boolean connected(Station s1, Station s2) {
            return find(s1).equals(find(s2));
        }
    }
}