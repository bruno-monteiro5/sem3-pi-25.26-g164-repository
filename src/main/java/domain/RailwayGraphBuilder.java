package domain;

import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;


/**
 * Helper class for building graph structures from railway repository data.
 * <p>
 * This class follows the Single Responsibility Principle by separating
 * graph construction logic from data storage (repository).
 */
public class RailwayGraphBuilder {

    private final S3_RailwayRepository repo;

    /**
     * Creates a builder using the singleton repository instance.
     */
    public RailwayGraphBuilder() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Creates a builder using a specific repository instance.
     * Useful for testing.
     */
    public RailwayGraphBuilder(S3_RailwayRepository repo) {
        this.repo = repo;
    }

    /**
     * Builds a directed graph using line distances as edge weights.
     * <p>
     * This graph is suitable for:
     * - Shortest path calculations
     * - Topological sorting (USEI11)
     * - Minimum spanning tree algorithms (USEI12)
     *
     * @return directed graph with distance weights
     * @throws IllegalStateException if repository data is not loaded
     */
    public MapGraph<Station, Double> buildDirectedGraphWithDistance() {
        validateDataLoaded();

        MapGraph<Station, Double> graph = new MapGraph<>(true);

        for (Station s : repo.getStationsById().values()) {
            graph.addVertex(s);
        }

        for (Line line : repo.getLines()) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double distance = line.getDistance();

            domain.graph.Edge<Station, Double> existing = graph.edge(origin, dest);

            if (existing == null) {
                graph.addEdge(origin, dest, distance);
            } else if (distance < existing.getWeight()) {
                graph.removeEdge(origin, dest);
                graph.addEdge(origin, dest, distance);
            }
        }

        return graph;
    }


    /**
     * Builds a directed graph using line capacities as edge weights.
     * <p>
     * This graph is suitable for:
     * - Maximum flow problems (USEI14)
     * - Network capacity analysis
     *
     * @return directed graph with capacity weights
     * @throws IllegalStateException if repository data is not loaded
     */
    public MapGraph<Station, Double> buildDirectedGraphWithCapacity() {
        validateDataLoaded();

        MapGraph<Station, Double> graph = new MapGraph<>(true);

        for (Station s : repo.getStationsById().values()) {
            graph.addVertex(s);
        }

        for (Line line : repo.getLines()) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double capacity = line.getCapacity();
            graph.addEdge(origin, dest, capacity);
        }

        return graph;
    }

    /**
     * Builds a directed graph using line costs as edge weights.
     * <p>
     * This graph is suitable for risk-aware shortest path calculations
     * such as those needed in USEI15, where costs represent penalties,
     * risks, or economic implications.
     *
     * @return directed graph with cost weights
     * @throws IllegalStateException if repository data is not loaded
     */
    public MapGraph<Station, Double> buildDirectedGraphWithCost() {
        validateDataLoaded();

        MapGraph<Station, Double> graph = new MapGraph<>(true);

        for (Station s : repo.getStationsById().values()) {
            graph.addVertex(s);
        }

        for (Line line : repo.getLines()) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double cost = line.getCost();
            graph.addEdge(origin, dest, cost);
        }

        return graph;
    }

    /**
     * Builds an undirected graph using line distances as edge weights.
     * <p>
     * This graph is suitable for:
     * - Centrality analysis (USEI13)
     * - Minimum spanning tree (USEI12)
     * - Connected components analysis
     *
     * @return undirected graph with distance weights
     * @throws IllegalStateException if repository data is not loaded
     */
    public MapGraph<Station, Double> buildUndirectedGraph() {
        validateDataLoaded();

        MapGraph<Station, Double> graph = new MapGraph<>(false);

        for (Station s : repo.getStationsById().values()) {
            graph.addVertex(s);
        }

        for (Line line : repo.getLines()) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double distance = line.getDistance();
            graph.addEdge(origin, dest, distance);
        }

        return graph;
    }



    /**
     * Validates that the repository has loaded data.
     *
     * @throws IllegalStateException if no data is loaded
     */
    private void validateDataLoaded() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException(
                    "Repository data not loaded. Load CSV files first.");
        }
    }


}