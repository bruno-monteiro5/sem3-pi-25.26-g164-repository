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
    private MapGraph<Station, Double> graph= null;

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
        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        graph = builder.buildDirectedGraphWithCapacity();

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
        graph = builder.buildDirectedGraphWithCapacity();
        return fordFulkerson(graph, source, target);
    }

    /**
     * Ford-Fulkerson algorythm because weights matter in this case, paired with a dfs search in order to ease the process
     */
    private int fordFulkerson(MapGraph<Station, Double> graph, Station source, Station target) {

        MapGraph<Station, Double> residual = graph.clone();

        double maxFlow = 0;

        while (true) {

            Set<Station> visited = new HashSet<>();
            double pathFlow = dfsFindPath(residual, source, target, visited, Double.MAX_VALUE);

            if (pathFlow == 0) break;

            maxFlow += pathFlow;
        }

        return (int) maxFlow; // trains per day
    }

    private double dfsFindPath(MapGraph<Station, Double> residual, Station current, Station target, Set<Station> visited, double flow) {

        visited.add(current);

        if (current.equals(target)) return flow;

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