package controller;

import domain.Line;
import domain.MinimalBackboneResult;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for USEI12 - Minimal Backbone Network.
 * Computes minimum spanning tree using Kruskal's algorithm.
 */
public class MinimalBackboneController {

    private final S3_RailwayRepository repo;

    public MinimalBackboneController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Computes the minimal backbone network using Kruskal's algorithm.
     * Uses the undirected graph from the repository.
     */
    public MinimalBackboneResult calculateMinimalBackbone() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded. Load network data first.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> directedGraph = builder.buildDirectedGraphWithDistance();

        MapGraph<Station, Double> undirectedGraph = convertToUndirected(directedGraph);
        MapGraph<Station, Double> backboneGraph = kruskallMST(undirectedGraph);
        double totalDistance = calculateTotalDistance(backboneGraph);

        return new MinimalBackboneResult(backboneGraph, totalDistance);
    }

    /**
     * Converts a directed graph to undirected representation.
     */
    private MapGraph<Station, Double> convertToUndirected(MapGraph<Station, Double> directed) {
        MapGraph<Station, Double> undirected = new MapGraph<>(false);

        for (Station s : directed.vertices()) {
            undirected.addVertex(s);
        }

        Set<String> addedEdges = new HashSet<>();

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

    private String createEdgeKey(Station s1, Station s2) {
        return s1.getId() + "-" + s2.getId();
    }

    /**
     * Kruskal's algorithm for MST.
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

    private double calculateTotalDistance(MapGraph<Station, Double> graph) {
        double total = 0.0;
        for (Edge<Station, Double> edge : graph.edges()) {
            total += edge.getWeight();
        }
        return total;
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
     * Union-Find data structure.
     */
    private static class UnionFind {
        private Map<Station, Station> parent;
        private Map<Station, Integer> rank;

        public UnionFind(List<Station> stations) {
            parent = new HashMap<>();
            rank = new HashMap<>();
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

            if (root1.equals(root2)) return;

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