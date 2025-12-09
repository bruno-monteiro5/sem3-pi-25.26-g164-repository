package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import domain.graph.Algorithms;
import domain.graph.map.MapGraph;
import domain.UpgradeOrderResult;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.*;

/**
 * Controller para USEI11 - Directed Line Upgrade Plan
 *
 * Calcula a ordem de upgrade de estações baseada em dependências direcionadas.
 * Se existirem ciclos, identifica as estações envolvidas.
 */
public class UpgradeOrderController {

    private MapGraph<Station, Double> graph;
    private boolean graphBuilt = false;

    public UpgradeOrderController() {
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
        if (!repo.getStationsById().isEmpty() && !repo.getLines().isEmpty()) {
            List<Station> stations = new ArrayList<>(repo.getStationsById().values());
            List<Line> lines = repo.getLines();
            buildGraph(stations, lines);
            graphBuilt = true;
        }
    }

    /**
     * Carrega os dados dos CSVs e constrói o grafo direcionado
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
     * Constrói o grafo direcionado a partir das estações e linhas
     */
    private void buildGraph(List<Station> stations, List<Line> lines) {
        graph = new MapGraph<>(true); // grafo DIRECIONADO

        // Adicionar vértices
        for (Station s : stations) {
            graph.addVertex(s);
        }

        // Adicionar arestas direcionadas
        // A→B significa: "B depende de A" (A deve ser upgraded antes de B)
        for (Line line : lines) {
            Station origin = line.getOrigin();
            Station dest = line.getDestination();
            double distance = line.getDistance();

            graph.addEdge(origin, dest, distance);
        }
    }

    /**
     * Calcula a ordem de upgrade usando ordenação topológica
     */
    public UpgradeOrderResult calculateUpgradeOrder() {
        if (!graphBuilt) {
            throw new IllegalStateException("Graph not built. Call buildGraphFromCsv first.");
        }

        // Primeiro, detectar ciclos
        List<List<Station>> cycles = detectCycles();

        if (!cycles.isEmpty()) {
            // Grafo tem ciclos - não é possível ordenação topológica
            return new UpgradeOrderResult(cycles);
        }

        // Grafo é acíclico - calcular ordenação topológica
        LinkedList<Station> topoOrder = Algorithms.topologicalSort(graph);

        return new UpgradeOrderResult(topoOrder);
    }

    /**
     * Detecta ciclos no grafo usando DFS com coloração
     * Retorna lista de ciclos encontrados
     */
    private List<List<Station>> detectCycles() {
        List<List<Station>> cycles = new ArrayList<>();

        Map<Station, Integer> color = new HashMap<>();
        Map<Station, Station> parent = new HashMap<>();

        // 0 = branco (não visitado)
        // 1 = cinza (em processamento)
        // 2 = preto (processado)

        for (Station s : graph.vertices()) {
            color.put(s, 0);
            parent.put(s, null);
        }

        for (Station s : graph.vertices()) {
            if (color.get(s) == 0) {
                List<Station> cycle = dfsCycleDetection(s, color, parent);
                if (cycle != null) {
                    cycles.add(cycle);
                }
            }
        }

        return cycles;
    }

    /**
     * DFS auxiliar para detecção de ciclos
     */
    private List<Station> dfsCycleDetection(Station u,
                                            Map<Station, Integer> color,
                                            Map<Station, Station> parent) {
        color.put(u, 1); // cinza

        for (Station v : graph.adjVertices(u)) {
            parent.put(v, u);

            if (color.get(v) == 1) {
                // Back edge - ciclo encontrado
                return reconstructCycle(u, v, parent);
            }

            if (color.get(v) == 0) {
                List<Station> cycle = dfsCycleDetection(v, color, parent);
                if (cycle != null) {
                    return cycle;
                }
            }
        }

        color.put(u, 2); // preto
        return null;
    }

    /**
     * Reconstrói o ciclo a partir dos predecessores
     */
    private List<Station> reconstructCycle(Station start, Station end,
                                           Map<Station, Station> parent) {
        List<Station> cycle = new ArrayList<>();
        cycle.add(end);

        Station current = start;
        while (current != null && !current.equals(end)) {
            cycle.add(0, current);
            current = parent.get(current);
        }

        cycle.add(end); // fechar o ciclo
        return cycle;
    }

    public boolean isGraphBuilt() {
        return graphBuilt;
    }

    public int getStationCount() {
        return graphBuilt ? graph.numVertices() : 0;
    }

    public int getLineCount() {
        return graphBuilt ? graph.numEdges() : 0;
    }
}