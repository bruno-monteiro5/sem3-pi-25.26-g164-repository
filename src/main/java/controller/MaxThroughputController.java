package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Graph;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI14 - Maximum Throughput Calculator.
 */
public class MaxThroughputController {

    private final S3_RailwayRepository repo;
    private MapGraph<Station, Double> graph;

    /**
     * Basic constructor gets any existant repository instance in order to retrieve any existance graph
     */
    public MaxThroughputController() {
        this.repo = S3_RailwayRepository.getInstance();
        graph = repo.getGraph();
    }

    /**
     * Computes maximum flow between source and target using Edmonds-Karp.
     * Uses capacity-based directed graph from the repository.
     */
    public int computeMaxFlow(Station source, Station target) {
        //Builds the graph and executed the ford-fulkerson algorythm on the graph, between the starter and end station
        return fordFulkerson(graph, source, target);
    }

    /**
     * Ford-Fulkerson algorythm because weights matter in this case, paired with a dfs search in order to ease the process
     */
    private int fordFulkerson(MapGraph<Station, Double> graph, Station source, Station target) {

        //Clones the graph as a residual
        MapGraph<Station, Double> residual = graph.clone();

        double maxFlow = 0;

        while (true) {
            //Realizes searches for a path between source and target
            Set<Station> visited = new HashSet<>();
            double pathFlow = dfsFindPath(residual, source, target, visited, Double.MAX_VALUE);

            if (pathFlow == 0) break;

            maxFlow += pathFlow;
        }

        return (int) maxFlow; // trains per day
    }

    private double dfsFindPath(MapGraph<Station, Double> residual, Station current, Station target, Set<Station> visited, double flow) {

        //Adds The station visited to a list
        visited.add(current);

        //Verifies if the search was sucessfull
        if (current.equals(target)) return flow;

        //Searches through all stations adjacent, to see what can be the next station in the path
        for (Station next : residual.adjVertices(current)) {
            if (visited.contains(next)) continue;
            Double capacity = residual.edge(current, next).getWeight();
            if (capacity > 0) {
                double bottleneck = dfsFindPath(residual, next, target, visited, Math.min(flow, capacity));

                if (bottleneck > 0) {
                    // UPDATE FORWARD EDGE
                    residual.removeEdge(current, next);  // ← Remove first!
                    residual.addEdge(current, next, capacity - bottleneck);

                    // UPDATE BACKWARD EDGE
                    Double backCap = 0.0;
                    if (residual.edge(next, current) != null) {
                        backCap = residual.edge(next, current).getWeight();
                        residual.removeEdge(next, current);  // ← Remove first!
                    }
                    residual.addEdge(next, current, backCap + bottleneck);

                    return bottleneck;
                }
            }
        }

        return 0;
    }

    /**
     * Method to verify if the graph is built or not.
     * @return boolean
     */


    public boolean isGraphBuilt() {
        return repo.isDataLoaded();
    }

    /**
     * Returns the current station count
     * @return int
     */

    public int getStationCount() {
        return repo.getStationCount();
    }

    /**
     * Returns the current line count
     * @return int
     */

    public int getLineCount() {
        return repo.getLineCount();
    }

    /**
     * Returns the station by its ID(name)
     * @param id
     * @return Station
     */

    public Station getStationById(String id) {
        return repo.getStationById(id);
    }

    /**
     * Returns the stations list
     * @return String
     */

    public String getStations(){
        String station = "";
        int i = 1;
        for(Map.Entry<Integer, Station> entry : repo.getStationsById().entrySet()) {
            station += String.format("%d - %s \n", i, entry.getValue().getName());
            i++;
        }
        return station;
    }
}