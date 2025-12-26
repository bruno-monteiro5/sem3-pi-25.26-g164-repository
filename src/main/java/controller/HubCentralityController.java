package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.HubScore;
import domain.Line;
import domain.RailwayGraphBuilder;
import domain.Station;
import domain.graph.Algorithms;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import domain.graph.priorityqueue.Entry;
import domain.graph.priorityqueue.HeapPriorityQueue;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

public class HubCentralityController {

    private final S3_RailwayRepository repository;

    public HubCentralityController() {
        this.repository = S3_RailwayRepository.getInstance();
    }

    public void buildGraphFromCsv(String stationsCsvPath, String linesCsvPath) throws IOException {
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsCsvPath);

        Map<Integer, Station> stationsById = new HashMap<>();
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            stationsById.put(station.getId(), station);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesCsvPath, stationsById);

        repository.loadStations(stations);
        repository.loadLines(lines);
    }

    public boolean isGraphBuilt() {
        return repository.isDataLoaded();
    }

    /**
     * USEI13 - Hub Centrality.
     *
     * Shortest path metrics (betweenness + harmonic) use DISTANCE as edge weight
     * because Dijkstra requires non-negative weights.
     *
     * Strength uses CAPACITY from the repository lines (separate metric).
     */
    public List<HubScore> calculateHubCentrality(boolean directedGraph) {

        if (!repository.isDataLoaded()) {
            throw new IllegalStateException("Data not loaded.");
        }

        RailwayGraphBuilder builder = new RailwayGraphBuilder(repository);

        MapGraph<Station, Double> graph;
        if (directedGraph) {
            graph = builder.buildDirectedGraphWithDistance();
        } else {
            graph = builder.buildUndirectedGraph();
        }

        List<Station> stationList = new ArrayList<>(graph.vertices());
        int numberOfStations = stationList.size();

        Map<Station, Integer> stationIndexMap = new HashMap<>();
        for (int i = 0; i < numberOfStations; i++) {
            stationIndexMap.put(stationList.get(i), i);
        }

        List<int[]> components;
        if (graph.isDirected()) {
            components = computeWeakComponentsUsingEdges(graph, stationList, stationIndexMap);
        } else {
            components = computeComponentsBFS(graph, stationList, stationIndexMap);
        }

        Map<Station, Integer> degreeMap = computeDegree(graph, stationList);

        Map<Station, Double> strengthMap = computeStrengthFromLinesCapacity(stationList, directedGraph);

        Map<Station, Double> betweennessMap =
                computeBetweenness(graph, stationList, stationIndexMap, components, graph.isDirected());

        Map<Station, Double> harmonicMap =
                computeHarmonicCloseness(graph, stationList, stationIndexMap, components);

        Map<Station, Double> normalizedStrength =
                normalizeStrengthPerComponent(strengthMap, stationList, components);

        return buildHubScoreList(
                stationList,
                degreeMap,
                strengthMap,
                normalizedStrength,
                betweennessMap,
                harmonicMap
        );
    }


    private Map<Station, Integer> computeDegree(MapGraph<Station, Double> graph,
                                                List<Station> stationList) {

        Map<Station, Integer> degreeMap = new HashMap<>();

        for (int i = 0; i < stationList.size(); i++) {
            Station station = stationList.get(i);
            int degree;

            if (graph.isDirected()) {
                degree = graph.inDegree(station) + graph.outDegree(station);
            } else {
                degree = graph.outDegree(station);
            }

            degreeMap.put(station, degree);
        }
        return degreeMap;
    }


    /**
     * Strength for USEI13:
     * - uses line CAPACITY (not distance)
     * - undirected: adds capacity to both endpoints
     * - directed: outgoing strength (adds capacity only to origin)
     */
    private Map<Station, Double> computeStrengthFromLinesCapacity(List<Station> stationList,
                                                                  boolean directedGraph) {

        Map<Station, Double> strengthMap = new HashMap<>();

        for (int i = 0; i < stationList.size(); i++) {
            strengthMap.put(stationList.get(i), 0.0);
        }

        List<Line> lines = repository.getLines();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);

            Station origin = line.getOrigin();
            Station destination = line.getDestination();
            double capacity = line.getCapacity();

            if (directedGraph) {
                strengthMap.put(origin, strengthMap.get(origin) + capacity);
            } else {
                strengthMap.put(origin, strengthMap.get(origin) + capacity);
                strengthMap.put(destination, strengthMap.get(destination) + capacity);
            }
        }

        return strengthMap;
    }


    private Map<Station, Double> computeHarmonicCloseness(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap,
            List<int[]> components) {

        int n = stationList.size();
        double[] harmonicValues = new double[n];

        for (int sourceIndex = 0; sourceIndex < n; sourceIndex++) {

            double[] distances = dijkstraDistances(graph, stationList, stationIndexMap, sourceIndex);
            double sum = 0.0;

            for (int targetIndex = 0; targetIndex < n; targetIndex++) {
                if (targetIndex == sourceIndex) continue;

                double distance = distances[targetIndex];
                if (distance > 0.0 && distance < Double.POSITIVE_INFINITY) {
                    sum += 1.0 / distance;
                }
            }
            harmonicValues[sourceIndex] = sum;
        }

        normalizeHarmonicPerComponent(harmonicValues, components);

        Map<Station, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(stationList.get(i), harmonicValues[i]);
        }
        return result;
    }

    /**
     * Guarantees [0,1] per component:
     * 1) divide by (k-1) (size normalization)
     * 2) rescale by max in component (forces [0,1])
     */
    private void normalizeHarmonicPerComponent(double[] harmonic,
                                               List<int[]> components) {

        for (int c = 0; c < components.size(); c++) {
            int[] component = components.get(c);

            int size = component.length;
            double divisor;
            if (size > 1) {
                divisor = size - 1.0;
            } else {
                divisor = 0.0;
            }


            double maxValue = 0.0;

            for (int i = 0; i < component.length; i++) {
                int index = component[i];

                double sizeNormalized;
                if (divisor > 0.0) {
                    sizeNormalized = harmonic[index] / divisor;
                } else {
                    sizeNormalized = 0.0;
                }

                harmonic[index] = sizeNormalized;

                if (sizeNormalized > maxValue) {
                    maxValue = sizeNormalized;
                }
            }

            for (int i = 0; i < component.length; i++) {
                int index = component[i];
                if (maxValue > 0.0) {
                    harmonic[index] = harmonic[index] / maxValue;
                } else {
                    harmonic[index] = 0.0;
                }

            }
        }
    }


    private Map<Station, Double> computeBetweenness(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap,
            List<int[]> components,
            boolean directedGraph) {

        int n = stationList.size();
        double[] betweenness = new double[n];

        for (int sourceIndex = 0; sourceIndex < n; sourceIndex++) {

            DijkstraResult dijkstraResult =
                    dijkstraWithPathCounting(graph, stationList, stationIndexMap, sourceIndex);

            double[] dependency = new double[n];

            for (int i = dijkstraResult.stackSize - 1; i >= 0; i--) {
                int w = dijkstraResult.stack[i];

                int[] predecessorArray = dijkstraResult.predecessors[w];
                for (int p = 0; p < predecessorArray.length; p++) {
                    int predecessor = predecessorArray[p];

                    if (dijkstraResult.sigma[w] > 0) {
                        dependency[predecessor] +=
                                (dijkstraResult.sigma[predecessor] / (double) dijkstraResult.sigma[w]) *
                                        (1.0 + dependency[w]);
                    }
                }

                if (w != sourceIndex) {
                    betweenness[w] += dependency[w];
                }
            }
        }

        normalizeBetweennessPerComponent(betweenness, components, directedGraph);

        Map<Station, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(stationList.get(i), betweenness[i]);
        }
        return result;
    }

    /**
     * - undirected: divide by 2, then by (k-1)(k-2)/2
     * - directed: by (k-1)(k-2)
     */
    private void normalizeBetweennessPerComponent(double[] betweenness,
                                                  List<int[]> components,
                                                  boolean directedGraph) {

        for (int c = 0; c < components.size(); c++) {
            int[] component = components.get(c);

            int k = component.length;
            double divisor;

            if (directedGraph) {
                divisor = (k - 1.0) * (k - 2.0);
            } else {
                for (int i = 0; i < component.length; i++) {
                    int index = component[i];
                    betweenness[index] = betweenness[index] / 2.0;
                }
                divisor = (k - 1.0) * (k - 2.0) / 2.0;
            }

            for (int i = 0; i < component.length; i++) {
                int index = component[i];
                if (divisor > 0.0) {
                    betweenness[index] = betweenness[index] / divisor;
                } else {
                    betweenness[index] = 0.0;
                }

            }
        }
    }


    private Map<Station, Double> normalizeStrengthPerComponent(
            Map<Station, Double> strengthMap,
            List<Station> stationList,
            List<int[]> components) {

        Map<Station, Double> normalizedStrength = new HashMap<>();

        for (int c = 0; c < components.size(); c++) {
            int[] component = components.get(c);

            double maxValue = 0.0;

            for (int i = 0; i < component.length; i++) {
                int index = component[i];
                Station station = stationList.get(index);
                double value = strengthMap.get(station);
                if (value > maxValue) {
                    maxValue = value;
                }
            }

            for (int i = 0; i < component.length; i++) {
                int index = component[i];
                Station station = stationList.get(index);

                double normalized;
                if (maxValue > 0.0) {
                    normalized = strengthMap.get(station) / maxValue;
                } else {
                    normalized = 0.0;
                }

                normalizedStrength.put(station, normalized);
            }
        }

        return normalizedStrength;
    }


    private List<HubScore> buildHubScoreList(
            List<Station> stationList,
            Map<Station, Integer> degreeMap,
            Map<Station, Double> strengthMap,
            Map<Station, Double> normalizedStrength,
            Map<Station, Double> betweennessMap,
            Map<Station, Double> harmonicMap) {

        List<HubScore> resultList = new ArrayList<>();

        for (int i = 0; i < stationList.size(); i++) {
            Station station = stationList.get(i);

            double hubScore =
                    0.35 * betweennessMap.get(station)
                            + 0.35 * harmonicMap.get(station)
                            + 0.30 * normalizedStrength.get(station);

            resultList.add(new HubScore(
                    station,
                    degreeMap.get(station),
                    strengthMap.get(station),
                    betweennessMap.get(station),
                    harmonicMap.get(station),
                    hubScore
            ));
        }

        // insertion sort descending
        for (int i = 1; i < resultList.size(); i++) {
            HubScore key = resultList.get(i);
            int j = i - 1;

            while (j >= 0 && resultList.get(j).getHubScore() < key.getHubScore()) {
                resultList.set(j + 1, resultList.get(j));
                j--;
            }
            resultList.set(j + 1, key);
        }

        return resultList;
    }


    private List<int[]> computeComponentsBFS(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap) {

        boolean[] visited = new boolean[stationList.size()];
        List<int[]> components = new ArrayList<>();

        for (int i = 0; i < stationList.size(); i++) {
            Station startStation = stationList.get(i);
            int startIndex = stationIndexMap.get(startStation);

            if (visited[startIndex]) continue;

            LinkedList<Station> bfsResult =
                    Algorithms.BreadthFirstSearch(graph, startStation);

            int[] temp = new int[bfsResult.size()];
            int count = 0;

            for (int j = 0; j < bfsResult.size(); j++) {
                Station station = bfsResult.get(j);
                int index = stationIndexMap.get(station);

                if (!visited[index]) {
                    visited[index] = true;
                    temp[count++] = index;
                }
            }

            components.add(Arrays.copyOf(temp, count));
        }

        return components;
    }

    /**
     * Weak components for directed graphs:
     * Uses graph.edges() to connect origin<->destination (ignores orientation).
     */
    private List<int[]> computeWeakComponentsUsingEdges(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap) {

        Map<Station, List<Station>> neighborsMap = new HashMap<>();

        for (int i = 0; i < stationList.size(); i++) {
            neighborsMap.put(stationList.get(i), new ArrayList<Station>());
        }

        for (Edge<Station, Double> edge : graph.edges()) {
            Station origin = edge.getVOrig();
            Station destination = edge.getVDest();

            neighborsMap.get(origin).add(destination);
            neighborsMap.get(destination).add(origin);
        }

        boolean[] visited = new boolean[stationList.size()];
        List<int[]> components = new ArrayList<>();

        for (int i = 0; i < stationList.size(); i++) {
            Station startStation = stationList.get(i);
            int startIndex = stationIndexMap.get(startStation);

            if (visited[startIndex]) continue;

            Stack<Station> stack = new Stack<>();
            stack.push(startStation);
            visited[startIndex] = true;

            int[] temp = new int[stationList.size()];
            int count = 0;

            while (!stack.isEmpty()) {
                Station currentStation = stack.pop();
                int currentIndex = stationIndexMap.get(currentStation);

                temp[count++] = currentIndex;

                List<Station> neighborList = neighborsMap.get(currentStation);
                for (int j = 0; j < neighborList.size(); j++) {
                    Station neighbor = neighborList.get(j);
                    int neighborIndex = stationIndexMap.get(neighbor);

                    if (!visited[neighborIndex]) {
                        visited[neighborIndex] = true;
                        stack.push(neighbor);
                    }
                }
            }

            components.add(Arrays.copyOf(temp, count));
        }

        return components;
    }





    private double[] dijkstraDistances(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap,
            int sourceIndex) {

        int n = stationList.size();
        double[] distances = new double[n];
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            distances[i] = Double.POSITIVE_INFINITY;
        }
        distances[sourceIndex] = 0.0;

        HeapPriorityQueue<Double, Integer> priorityQueue = new HeapPriorityQueue<>();
        priorityQueue.insert(0.0, sourceIndex);

        while (!priorityQueue.isEmpty()) {

            Entry<Double, Integer> entry = priorityQueue.removeMin();
            int currentIndex = entry.getValue();

            if (visited[currentIndex]) continue;
            visited[currentIndex] = true;

            Station currentStation = stationList.get(currentIndex);

            for (Station neighbor : graph.adjVertices(currentStation)) {

                int neighborIndex = stationIndexMap.get(neighbor);
                if (visited[neighborIndex]) continue;

                Edge<Station, Double> edge = graph.edge(currentStation, neighbor);
                if (edge == null || edge.getWeight() == null) continue;

                double newDistance = distances[currentIndex] + edge.getWeight();
                if (newDistance < distances[neighborIndex]) {
                    distances[neighborIndex] = newDistance;
                    priorityQueue.insert(newDistance, neighborIndex);
                }
            }
        }

        return distances;
    }





    private static class DijkstraResult {
        final double[] distances;
        final long[] sigma;
        final int[][] predecessors;
        final int[] stack;
        int stackSize;

        DijkstraResult(int n) {
            distances = new double[n];
            sigma = new long[n];
            predecessors = new int[n][];
            stack = new int[n];
        }
    }

    private DijkstraResult dijkstraWithPathCounting(
            MapGraph<Station, Double> graph,
            List<Station> stationList,
            Map<Station, Integer> stationIndexMap,
            int sourceIndex) {

        int n = stationList.size();
        DijkstraResult result = new DijkstraResult(n);

        boolean[] visited = new boolean[n];
        List<List<Integer>> predecessorLists = new ArrayList<List<Integer>>();

        for (int i = 0; i < n; i++) {
            result.distances[i] = Double.POSITIVE_INFINITY;
            result.sigma[i] = 0;
            predecessorLists.add(new ArrayList<Integer>());
        }

        result.distances[sourceIndex] = 0.0;
        result.sigma[sourceIndex] = 1;

        HeapPriorityQueue<Double, Integer> priorityQueue = new HeapPriorityQueue<>();
        priorityQueue.insert(0.0, sourceIndex);

        while (!priorityQueue.isEmpty()) {

            Entry<Double, Integer> entry = priorityQueue.removeMin();
            int currentIndex = entry.getValue();

            if (visited[currentIndex]) continue;
            visited[currentIndex] = true;

            result.stack[result.stackSize++] = currentIndex;

            Station currentStation = stationList.get(currentIndex);

            for (Station neighbor : graph.adjVertices(currentStation)) {

                int neighborIndex = stationIndexMap.get(neighbor);

                Edge<Station, Double> edge = graph.edge(currentStation, neighbor);
                if (edge == null || edge.getWeight() == null) continue;

                double newDistance = result.distances[currentIndex] + edge.getWeight();

                if (newDistance < result.distances[neighborIndex]) {

                    result.distances[neighborIndex] = newDistance;
                    result.sigma[neighborIndex] = result.sigma[currentIndex];

                    predecessorLists.get(neighborIndex).clear();
                    predecessorLists.get(neighborIndex).add(currentIndex);

                    priorityQueue.insert(newDistance, neighborIndex);

                } else if (Math.abs(newDistance - result.distances[neighborIndex]) < 1e-9) {

                    result.sigma[neighborIndex] += result.sigma[currentIndex];
                    predecessorLists.get(neighborIndex).add(currentIndex);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            List<Integer> list = predecessorLists.get(i);
            int[] predecessorArray = new int[list.size()];

            for (int j = 0; j < list.size(); j++) {
                predecessorArray[j] = list.get(j);
            }

            result.predecessors[i] = predecessorArray;
        }

        return result;
    }

    public int getStationCount() {
        return repository.getStationCount();
    }

    public int getLineCount() {
        return repository.getLineCount();
    }
}
