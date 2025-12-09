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
     * Detecta ciclos no grafo usando DFS com coloração.
     * Retorna lista de ciclos encontrados (cada ciclo é uma lista de estações).
     */
    private List<List<Station>> detectCycles() {
        List<List<Station>> cycles = new ArrayList<>();

        Map<Station, Integer> color = new HashMap<>();
        Map<Station, Station> parent = new HashMap<>();

        // 0 = branco (não visitado)
        // 1 = cinza (em processamento - está na pilha DFS)
        // 2 = preto (processado - já terminou)

        for (Station s : graph.vertices()) {
            color.put(s, 0);
            parent.put(s, null);
        }

        // Percorrer todos os vértices
        for (Station s : graph.vertices()) {
            if (color.get(s) == 0) {
                List<Station> cycle = dfsCycleDetection(s, color, parent, new LinkedList<>());
                if (cycle != null && !cycle.isEmpty()) {
                    cycles.add(cycle);
                }
            }
        }

        return cycles;
    }

    /**
     * DFS auxiliar para detecção de ciclos.
     * Usa uma pilha de recursão (path) para rastrear o caminho atual.
     *
     * @param u vértice atual
     * @param color mapa de cores dos vértices
     * @param parent mapa de predecessores
     * @param path pilha de recursão (caminho atual)
     * @return ciclo encontrado ou null
     */
    private List<Station> dfsCycleDetection(Station u,
                                            Map<Station, Integer> color,
                                            Map<Station, Station> parent,
                                            LinkedList<Station> path) {

        // Marca como "em processamento" (cinza)
        color.put(u, 1);
        path.addLast(u);  // adiciona à pilha de recursão

        // Explorar todos os vizinhos
        for (Station v : graph.adjVertices(u)) {

            // CASO 1: Vértice cinza → BACK EDGE → CICLO DETECTADO!
            if (color.get(v) == 1) {
                // Reconstruir ciclo desde v até u (que está no path)
                List<Station> cycle = extractCycle(path, v);
                return cycle;
            }

            // CASO 2: Vértice branco → explorar recursivamente
            if (color.get(v) == 0) {
                parent.put(v, u);  // definir predecessor apenas na primeira visita

                List<Station> cycle = dfsCycleDetection(v, color, parent, path);
                if (cycle != null) {
                    return cycle;  // propagar ciclo encontrado
                }
            }

            // CASO 3: Vértice preto → ignorar (já foi completamente processado)
        }

        // Marca como "processado" (preto)
        color.put(u, 2);
        path.removeLast();  // remove da pilha de recursão

        return null;  // nenhum ciclo encontrado neste caminho
    }

    /**
     * Extrai o ciclo do caminho atual (path) quando uma back edge é detectada.
     *
     * @param path pilha de recursão (caminho atual do DFS)
     * @param cycleStart vértice onde o ciclo começa (destino da back edge)
     * @return lista de estações que formam o ciclo
     */
    private List<Station> extractCycle(LinkedList<Station> path, Station cycleStart) {
        List<Station> cycle = new ArrayList<>();

        boolean inCycle = false;

        // Percorrer o path de trás para frente
        for (Station s : path) {
            if (s.equals(cycleStart)) {
                inCycle = true;
            }

            if (inCycle) {
                cycle.add(s);
            }
        }

        // Adicionar novamente o início do ciclo para fechar
        if (!cycle.isEmpty()) {
            cycle.add(cycleStart);
        }

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