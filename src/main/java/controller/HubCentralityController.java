package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.Station;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 * <p>
 * This controller:
 * <ul>
 *   <li>Loads railway network data from CSV files</li>
 *   <li>Builds an undirected weighted graph</li>
 *   <li>Calculates centrality metrics for each station</li>
 *   <li>Computes a combined hub score</li>
 * </ul>
 */
public class HubCentralityController {

    private MapGraph<Station, Double> graph;
    private Map<Integer, Station> stationsById;

    /**
     * Creates a new controller instance.
     */
    public HubCentralityController() {
        this.graph = null;
        this.stationsById = new HashMap<>();
    }

    /**
     * Loads railway network from CSV files and builds the graph.
     *
     * @param stationsCsvPath path to stations CSV file
     * @param linesCsvPath path to lines CSV file
     * @throws IOException if file reading fails
     */
    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath)
            throws IOException {

        // Load stations
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        stationsById.clear();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        // Load lines
        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        // Store in repository
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        repo.loadStations(stations);
        repo.loadLines(lines);

        // Build undirected graph
        graph = new MapGraph<>(false); // undirected

        for (Station s : stations) {
            graph.addVertex(s);
        }

        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station destination = line.getDestination();
            double weight = line.getDistance();

            graph.addEdge(origin, destination, weight);
        }
    }

    /**
     * Checks if the railway network graph has been loaded.
     *
     * @return true if graph is built, false otherwise
     */
    public boolean isGraphBuilt() {
        return graph != null && graph.numVertices() > 0;
    }

    /**
     * Returns the number of stations in the network.
     *
     * @return station count
     */
    public int getStationCount() {
        return graph != null ? graph.numVertices() : 0;
    }

    /**
     * Returns the number of connections in the network.
     *
     * @return edge count (undirected, so each line counted once)
     */
    public int getLineCount() {
        return graph != null ? graph.numEdges() : 0;
    }


}
