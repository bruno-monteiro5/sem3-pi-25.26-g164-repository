package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI13 - Rail Hub Centrality Analysis.
 *
 * Computes centrality metrics for railway stations:
 * - Degree: number of direct connections
 * - Strength: sum of edge weights (distances)
 * - Betweenness: fraction of shortest paths passing through a station
 * - Harmonic Closeness: accessibility to all other stations
 * - HubScore: weighted composite metric (0.35*betweenness + 0.35*harmonic + 0.30*strength_norm)
 *
 * All "normalized" metrics are normalized to [0,1] per connected component.
 */
public class HubCentralityController {

    private final S3_RailwayRepository repo;

    public HubCentralityController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Loads railway network from CSV files and updates the repository.
     */
    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath)
            throws IOException {

        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        Map<Integer, Station> stationsById = new HashMap<Integer, Station>();
        for (int i = 0; i < stations.size(); i++) {
            Station s = stations.get(i);
            stationsById.put(Integer.valueOf(s.getId()), s);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        repo.loadStations(stations);
        repo.loadLines(lines);
    }

    /**
     * Checks if the railway network has been loaded.
     */
    public boolean isGraphBuilt() {
        return repo.isDataLoaded();
    }

    /**
     * Returns the number of stations in the railway network.
     */
    public int getStationCount() {
        return repo.getStationCount();
    }

    /**
     * Returns the number of railway connections.
     */
    public int getLineCount() {
        return repo.getLineCount();
    }

    /**
     * Computes all hub centrality metrics and returns a ranked list.
     * Uses the undirected graph from the repository.
     * All normalized metrics are normalized to [0,1] per connected component.
     */
    public List<HubScore> calculateHubCentrality() {
        if (!repo.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded. Call buildGraphFromCsv first.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repo);
        MapGraph<Station, Double> graph = builder.buildUndirectedGraph();

        List<Station> vertices = new ArrayList<Station>(graph.vertices());

        // Index map to avoid vertices.indexOf(...) inside loops
        Map<Station, Integer> indexMap = new HashMap<Station, Integer>();
        for (int i = 0; i < vertices.size(); i++) {
            indexMap.put(vertices.get(i), Integer.valueOf(i));
        }

        // Connected components
        List<List<Station>> components = getConnectedComponents(graph, vertices);
        Map<Station, Integer> componentId = buildComponentIdMap(components);

        Map<Station, Integer> degreeMap = computeDegree(graph, vertices);
        Map<Station, Double> strengthMap = computeStrength(graph, vertices);

        Map<Station, Double> betweennessMap = computeBetweenness(graph, vertices, indexMap);
        Map<Station, Double> harmonicMap = computeHarmonicCloseness(graph, vertices, indexMap);

        // Normalize per component
        Map<Station, Double> strengthNormMap = normalizeStrengthPerComponent(strengthMap, components);
        normalizeBetweennessPerComponent(betweennessMap, components);
        normalizeHarmonicPerComponent(harmonicMap, componentId, components);

        return buildHubScores(vertices, degreeMap, strengthMap,
                strengthNormMap, betweennessMap, harmonicMap);
    }

    /**
     * Computes the degree (number of adjacent vertices) for each station.
     */
    private Map<Station, Integer> computeDegree(MapGraph<Station, Double> graph,
                                                List<Station> vertices) {
        Map<Station, Integer> degreeMap = new HashMap<Station, Integer>();

        for (int i = 0; i < vertices.size(); i++) {
            Station v = vertices.get(i);
            degreeMap.put(v, Integer.valueOf(graph.outDegree(v)));
        }

        return degreeMap;
    }

    /**
     * Computes the strength (sum of edge weights) for each station.
     * Strength represents total distance to all direct neighbors.
     */
    private Map<Station, Double> computeStrength(MapGraph<Station, Double> graph,
                                                 List<Station> vertices) {
        Map<Station, Double> strengthMap = new HashMap<Station, Double>();

        for (int i = 0; i < vertices.size(); i++) {
            Station v = vertices.get(i);
            double strength = 0.0;

            Collection<Edge<Station, Double>> edges = graph.outgoingEdges(v);
            Iterator<Edge<Station, Double>> it = edges.iterator();
            while (it.hasNext()) {
                Edge<Station, Double> edge = it.next();
                strength = strength + edge.getWeight().doubleValue();
            }

            strengthMap.put(v, Double.valueOf(strength));
        }

        return strengthMap;
    }

    /**
     * Computes betweenness centrality for all stations using Dijkstra's algorithm (O(n^2)).
     * For undirected graphs, raw betweenness is divided by 2 at normalization time
     * because each pair (s,t) is counted twice (s->t and t->s).
     */
    private Map<Station, Double> computeBetweenness(MapGraph<Station, Double> graph,
                                                    List<Station> vertices,
                                                    Map<Station, Integer> indexMap) {
        Map<Station, Double> betweennessMap = new HashMap<Station, Double>();
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            betweennessMap.put(vertices.get(i), Double.valueOf(0.0));
        }

        for (int s = 0; s < n; s++) {
            Station source = vertices.get(s);

            Map<Station, List<Station>> predecessors = new HashMap<Station, List<Station>>();
            Map<Station, Integer> pathCount = new HashMap<Station, Integer>();
            Map<Station, Double> distances = new HashMap<Station, Double>();

            for (int i = 0; i < n; i++) {
                Station v = vertices.get(i);
                predecessors.put(v, new ArrayList<Station>());
                pathCount.put(v, Integer.valueOf(0));
                distances.put(v, Double.valueOf(Double.POSITIVE_INFINITY));
            }

            distances.put(source, Double.valueOf(0.0));
            pathCount.put(source, Integer.valueOf(1));

            List<Station> stack = new ArrayList<Station>();
            boolean[] visited = new boolean[n];

            while (true) {
                Station current = null;
                double minDist = Double.POSITIVE_INFINITY;

                for (int i = 0; i < n; i++) {
                    Station v = vertices.get(i);
                    if (!visited[i]) {
                        double d = distances.get(v).doubleValue();
                        if (d < minDist) {
                            minDist = d;
                            current = v;
                        }
                    }
                }

                if (current == null || minDist == Double.POSITIVE_INFINITY) {
                    break;
                }

                int currentIdx = indexMap.get(current).intValue();
                visited[currentIdx] = true;
                stack.add(current);

                Collection<Station> neighbors = graph.adjVertices(current);
                Iterator<Station> it = neighbors.iterator();
                while (it.hasNext()) {
                    Station neighbor = it.next();
                    Edge<Station, Double> edge = graph.edge(current, neighbor);

                    double currentDist = distances.get(current).doubleValue();
                    double edgeWeight = edge.getWeight().doubleValue();
                    double newDist = currentDist + edgeWeight;
                    double neighborDist = distances.get(neighbor).doubleValue();

                    if (newDist < neighborDist) {
                        distances.put(neighbor, Double.valueOf(newDist));
                        pathCount.put(neighbor, pathCount.get(current));
                        predecessors.get(neighbor).clear();
                        predecessors.get(neighbor).add(current);

                    } else if (Math.abs(newDist - neighborDist) < 1e-9) {
                        int neighborCount = pathCount.get(neighbor).intValue();
                        int currentCount = pathCount.get(current).intValue();
                        pathCount.put(neighbor, Integer.valueOf(neighborCount + currentCount));
                        predecessors.get(neighbor).add(current);
                    }
                }
            }

            Map<Station, Double> dependency = new HashMap<Station, Double>();
            for (int i = 0; i < n; i++) {
                dependency.put(vertices.get(i), Double.valueOf(0.0));
            }

            for (int i = stack.size() - 1; i >= 0; i--) {
                Station w = stack.get(i);

                List<Station> preds = predecessors.get(w);
                for (int j = 0; j < preds.size(); j++) {
                    Station pred = preds.get(j);

                    int predCount = pathCount.get(pred).intValue();
                    int wCount = pathCount.get(w).intValue();
                    double wDep = dependency.get(w).doubleValue();

                    double contribution = ((double) predCount / (double) wCount) * (1.0 + wDep);

                    double predDep = dependency.get(pred).doubleValue();
                    dependency.put(pred, Double.valueOf(predDep + contribution));
                }

                if (!w.equals(source)) {
                    double wDep = dependency.get(w).doubleValue();
                    double currentBetw = betweennessMap.get(w).doubleValue();
                    betweennessMap.put(w, Double.valueOf(currentBetw + wDep));
                }
            }
        }

        return betweennessMap;
    }

    /**
     * Computes harmonic closeness centrality for all stations using Dijkstra's algorithm (O(n^2)).
     *
     * IMPORTANT: harmonic closeness of a station "source" is:
     *   (1/(k-1)) * sum_{target != source, reachable} 1/d(source,target)
     *
     * So the accumulation must add to the SOURCE (not to the target).
     */
    private Map<Station, Double> computeHarmonicCloseness(MapGraph<Station, Double> graph,
                                                          List<Station> vertices,
                                                          Map<Station, Integer> indexMap) {
        Map<Station, Double> harmonicMap = new HashMap<Station, Double>();
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            harmonicMap.put(vertices.get(i), Double.valueOf(0.0));
        }

        for (int s = 0; s < n; s++) {
            Station source = vertices.get(s);

            Map<Station, Double> distances = new HashMap<Station, Double>();
            boolean[] visited = new boolean[n];

            for (int i = 0; i < n; i++) {
                distances.put(vertices.get(i), Double.valueOf(Double.POSITIVE_INFINITY));
            }
            distances.put(source, Double.valueOf(0.0));

            // Dijkstra O(n^2)
            while (true) {
                Station current = null;
                double minDist = Double.POSITIVE_INFINITY;

                for (int i = 0; i < n; i++) {
                    Station v = vertices.get(i);
                    if (!visited[i]) {
                        double d = distances.get(v).doubleValue();
                        if (d < minDist) {
                            minDist = d;
                            current = v;
                        }
                    }
                }

                if (current == null || minDist == Double.POSITIVE_INFINITY) {
                    break;
                }

                int currentIdx = indexMap.get(current).intValue();
                visited[currentIdx] = true;

                Collection<Station> neighbors = graph.adjVertices(current);
                Iterator<Station> it = neighbors.iterator();
                while (it.hasNext()) {
                    Station neighbor = it.next();
                    Edge<Station, Double> edge = graph.edge(current, neighbor);

                    double currentDist = distances.get(current).doubleValue();
                    double edgeWeight = edge.getWeight().doubleValue();
                    double newDist = currentDist + edgeWeight;
                    double neighborDist = distances.get(neighbor).doubleValue();

                    if (newDist < neighborDist) {
                        distances.put(neighbor, Double.valueOf(newDist));
                    }
                }
            }

            // Accumulate harmonic closeness FOR THE SOURCE
            double acc = 0.0;

            for (int t = 0; t < n; t++) {
                Station target = vertices.get(t);

                if (!source.equals(target)) {
                    double dist = distances.get(target).doubleValue();

                    if (dist > 0 && dist < Double.POSITIVE_INFINITY) {
                        acc = acc + (1.0 / dist);
                    }
                }
            }

            double currentSourceH = harmonicMap.get(source).doubleValue();
            harmonicMap.put(source, Double.valueOf(currentSourceH + acc));
        }

        return harmonicMap;
    }

    /**
     * Builds HubScore objects combining all metrics and sorts them by hubScore descending.
     */
    private List<HubScore> buildHubScores(List<Station> vertices,
                                          Map<Station, Integer> degreeMap,
                                          Map<Station, Double> strengthMap,
                                          Map<Station, Double> strengthNormMap,
                                          Map<Station, Double> betweennessMap,
                                          Map<Station, Double> harmonicMap) {
        List<HubScore> results = new ArrayList<HubScore>();

        for (int i = 0; i < vertices.size(); i++) {
            Station v = vertices.get(i);

            double betw = betweennessMap.get(v).doubleValue();
            double harm = harmonicMap.get(v).doubleValue();
            double strNorm = strengthNormMap.get(v).doubleValue();
            double hubScore = 0.35 * betw + 0.35 * harm + 0.30 * strNorm;

            results.add(new HubScore(
                    v,
                    degreeMap.get(v).intValue(),
                    strengthMap.get(v).doubleValue(),
                    betw,
                    harm,
                    hubScore
            ));
        }

        sortHubScores(results);

        return results;
    }

    /**
     * Sorts HubScore list by hubScore in descending order using insertion sort.
     */
    private void sortHubScores(List<HubScore> list) {
        for (int i = 1; i < list.size(); i++) {
            HubScore key = list.get(i);
            int j = i - 1;

            while (j >= 0 && list.get(j).getHubScore() < key.getHubScore()) {
                list.set(j + 1, list.get(j));
                j = j - 1;
            }
            list.set(j + 1, key);
        }
    }

    /**
     * Finds all connected components in the undirected graph.
     */
    private List<List<Station>> getConnectedComponents(MapGraph<Station, Double> graph,
                                                       List<Station> vertices) {
        List<List<Station>> components = new ArrayList<List<Station>>();
        Set<Station> visited = new HashSet<Station>();

        for (int i = 0; i < vertices.size(); i++) {
            Station start = vertices.get(i);
            if (visited.contains(start)) {
                continue;
            }

            List<Station> component = new ArrayList<Station>();
            Queue<Station> q = new LinkedList<Station>();

            visited.add(start);
            q.add(start);

            while (!q.isEmpty()) {
                Station current = q.remove();
                component.add(current);

                Collection<Station> neighbors = graph.adjVertices(current);
                Iterator<Station> it = neighbors.iterator();
                while (it.hasNext()) {
                    Station nb = it.next();
                    if (!visited.contains(nb)) {
                        visited.add(nb);
                        q.add(nb);
                    }
                }
            }

            components.add(component);
        }

        return components;
    }

    private Map<Station, Integer> buildComponentIdMap(List<List<Station>> components) {
        Map<Station, Integer> map = new HashMap<Station, Integer>();

        for (int c = 0; c < components.size(); c++) {
            List<Station> comp = components.get(c);
            for (int i = 0; i < comp.size(); i++) {
                map.put(comp.get(i), Integer.valueOf(c));
            }
        }

        return map;
    }

    /**
     * Normalizes strength values per connected component to [0,1] using max strength of that component.
     */
    private Map<Station, Double> normalizeStrengthPerComponent(Map<Station, Double> strengthMap,
                                                               List<List<Station>> components) {
        Map<Station, Double> norm = new HashMap<Station, Double>();

        for (int c = 0; c < components.size(); c++) {
            List<Station> comp = components.get(c);

            double max = 0.0;
            for (int i = 0; i < comp.size(); i++) {
                Station v = comp.get(i);
                double s = strengthMap.get(v).doubleValue();
                if (s > max) {
                    max = s;
                }
            }

            for (int i = 0; i < comp.size(); i++) {
                Station v = comp.get(i);
                double s = strengthMap.get(v).doubleValue();
                double vNorm = (max > 0) ? (s / max) : 0.0;
                norm.put(v, Double.valueOf(vNorm));
            }
        }

        return norm;
    }

    /**
     * Normalizes betweenness per connected component to [0,1] for undirected graphs.
     *
     * For undirected graphs:
     *  - divide raw betweenness by 2 (double counting of pairs)
     *  - then divide by ((k-1)(k-2))/2 where k is component size
     */
    private void normalizeBetweennessPerComponent(Map<Station, Double> betweennessMap,
                                                  List<List<Station>> components) {

        for (int c = 0; c < components.size(); c++) {
            List<Station> comp = components.get(c);
            int k = comp.size();

            // First: undirected adjustment (/2)
            for (int i = 0; i < k; i++) {
                Station v = comp.get(i);
                double raw = betweennessMap.get(v).doubleValue();
                betweennessMap.put(v, Double.valueOf(raw / 2.0));
            }

            // Then: normalization divisor per component
            double divisor = (k - 1) * (k - 2) / 2.0;

            if (divisor > 0) {
                for (int i = 0; i < k; i++) {
                    Station v = comp.get(i);
                    double b = betweennessMap.get(v).doubleValue();
                    betweennessMap.put(v, Double.valueOf(b / divisor));
                }
            } else {
                // components of size 1 or 2 -> betweenness is 0
                for (int i = 0; i < k; i++) {
                    betweennessMap.put(comp.get(i), Double.valueOf(0.0));
                }
            }
        }
    }

    /**
     * Normalizes harmonic closeness per connected component:
     * harmonic(source) = (1/(k-1)) * sum_{target != source} 1/d(source,target) within the component.
     */
    private void normalizeHarmonicPerComponent(Map<Station, Double> harmonicMap,
                                               Map<Station, Integer> componentId,
                                               List<List<Station>> components) {

        for (int i = 0; i < components.size(); i++) {
            List<Station> comp = components.get(i);
            int k = comp.size();

            double divisor = (k - 1.0);

            for (int j = 0; j < k; j++) {
                Station v = comp.get(j);
                double h = harmonicMap.get(v).doubleValue();

                if (divisor > 0) {
                    harmonicMap.put(v, Double.valueOf(h / divisor));
                } else {
                    harmonicMap.put(v, Double.valueOf(0.0));
                }
            }
        }
    }
}
