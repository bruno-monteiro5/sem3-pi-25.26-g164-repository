package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import domain.graph.Graph;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaxThroughputController {

    /**
     * A controller for USEI14 and it's required functions
     */

    public Graph graph = null;
    public boolean graphBuilt = false;

    /**
     * Gets the data from the CSV files and builds directed graphs
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
     * Constructs the directed graph from the given Station and Line classes
     */
    private void buildGraph(List<Station> stations, List<Line> lines) {
        graph = new MapGraph<>(true);

        for (Station s : stations) {
            graph.addVertex(s);
        }

        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double distance = line.getDistance();

            graph.addEdge(origin, dest, distance);
        }
    }
}
