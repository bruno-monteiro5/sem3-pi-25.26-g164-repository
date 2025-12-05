package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.List;

/**
 * Controller for USEI12 - Minimal Backbone Network.
 * Step #242: Build directed graph from CSV and create undirected version.
 */
public class MinimalBackboneController {

    private MapGraph<Station, Double> directedGraph;
    private boolean graphBuilt;

    public MinimalBackboneController() {
        this.directedGraph = null;
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

        for (Line line : lines) {
            directedGraph.addEdge(
                    line.getOrigin(),
                    line.getDestination(),
                    line.getDistance()
            );
        }
    }

    public MapGraph<Station, Double> getDirectedGraph() {
        if (!graphBuilt) throw new IllegalStateException("Graph not built yet.");
        return directedGraph;
    }

    public MapGraph<Station, Double> buildUndirectedGraph() {
        if (!graphBuilt) throw new IllegalStateException("Graph not built yet.");
        return convertToUndirected(directedGraph);
    }

    private MapGraph<Station, Double> convertToUndirected(MapGraph<Station, Double> directed) {
        MapGraph<Station, Double> undirected = new MapGraph<>(false);

        for (Station s : directed.vertices()) undirected.addVertex(s);

        java.util.Set<String> addedEdges = new java.util.HashSet<>();

        for (Edge<Station, Double> e : directed.edges()) {
            Station u = e.getVOrig();
            Station v = e.getVDest();
            double w = e.getWeight();

            String k1 = u.getId() + "-" + v.getId();
            String k2 = v.getId() + "-" + u.getId();

            if (!addedEdges.contains(k1) && !addedEdges.contains(k2)) {
                Edge<Station, Double> reverse = directed.edge(v, u);
                if (reverse != null) w = Math.min(w, reverse.getWeight());

                undirected.addEdge(u, v, w);
                addedEdges.add(k1);
                addedEdges.add(k2);
            }
        }
        return undirected;
    }
}
