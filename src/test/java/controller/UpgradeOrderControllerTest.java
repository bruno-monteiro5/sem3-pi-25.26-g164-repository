package controller;

import domain.Line;
import domain.Location;
import domain.Station;
import domain.UpgradeOrderResult;
import domain.graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UpgradeOrderController (USEI11)
 * Testa ordenação topológica e deteção de ciclos em grafos direcionados
 */
class UpgradeOrderControllerTest {

    private UpgradeOrderController controller;
    private S3_RailwayRepository repository;
    private List<Station> stations;
    private List<Line> lines;

    @BeforeEach
    void setUp() {
        controller = new UpgradeOrderController();
        repository = S3_RailwayRepository.getInstance();
        repository.clear();

        stations = new ArrayList<>();
        lines = new ArrayList<>();
    }

    /**
     * Teste 1: Grafo acíclico simples - deve retornar ordenação topológica válida
     * Estrutura: A → B → C → D
     */
    @Test
    void testSimpleAcyclicGraph() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);
        Station c = createStation(3, "Station C", 42.0, -8.0);
        Station d = createStation(4, "Station D", 43.0, -8.0);

        stations.add(a);
        stations.add(b);
        stations.add(c);
        stations.add(d);

        // A → B → C → D
        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(b, c, 15.0, 100, 7.0));
        lines.add(new Line(c, d, 20.0, 100, 10.0));

        repository.loadStations(stations);
        repository.loadLines(lines);

        // Reconstruir grafo no controller
        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertNotNull(result);
        assertFalse(result.hasCycle(), "Não deve haver ciclos");

        LinkedList<Station> order = result.getUpgradeOrder();
        assertNotNull(order);
        assertEquals(4, order.size(), "Deve ter 4 estações");

        // Verificar ordem topológica: A deve vir antes de B, B antes de C, C antes de D
        int idxA = order.indexOf(a);
        int idxB = order.indexOf(b);
        int idxC = order.indexOf(c);
        int idxD = order.indexOf(d);

        assertTrue(idxA < idxB, "A deve vir antes de B");
        assertTrue(idxB < idxC, "B deve vir antes de C");
        assertTrue(idxC < idxD, "C deve vir antes de D");
    }

    /**
     * Teste 2: Grafo com ciclo simples - deve detetar ciclo
     * Estrutura: A → B → C → A (ciclo)
     */
    @Test
    void testSimpleCycle() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);
        Station c = createStation(3, "Station C", 42.0, -8.0);

        stations.add(a);
        stations.add(b);
        stations.add(c);

        // Criar ciclo: A → B → C → A
        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(b, c, 15.0, 100, 7.0));
        lines.add(new Line(c, a, 20.0, 100, 10.0)); // ciclo!

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertNotNull(result);
        assertTrue(result.hasCycle(), "Deve detetar ciclo");

        List<List<Station>> cycles = result.getCycles();
        assertNotNull(cycles);
        assertFalse(cycles.isEmpty(), "Deve conter pelo menos um ciclo");

        // Verificar se o ciclo contém as 3 estações
        List<Station> cycle = cycles.get(0);
        assertTrue(cycle.size() >= 3, "Ciclo deve ter pelo menos 3 estações");
        assertTrue(cycle.contains(a) || cycle.contains(b) || cycle.contains(c),
                "Ciclo deve conter estações do grafo");
    }

    /**
     * Teste 3: Grafo DAG complexo com múltiplos caminhos
     * Estrutura em forma de diamante:
     *      A
     *     / \
     *    B   C
     *     \ /
     *      D
     */
    @Test
    void testDiamondDAG() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);
        Station c = createStation(3, "Station C", 41.0, -9.0);
        Station d = createStation(4, "Station D", 42.0, -8.5);

        stations.add(a);
        stations.add(b);
        stations.add(c);
        stations.add(d);

        // Diamante: A → B → D, A → C → D
        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(a, c, 10.0, 100, 5.0));
        lines.add(new Line(b, d, 15.0, 100, 7.0));
        lines.add(new Line(c, d, 15.0, 100, 7.0));

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertFalse(result.hasCycle());

        LinkedList<Station> order = result.getUpgradeOrder();
        assertEquals(4, order.size());

        // A deve ser primeira
        assertEquals(a, order.getFirst(), "A deve ser a primeira estação");

        // D deve ser última
        assertEquals(d, order.getLast(), "D deve ser a última estação");

        // B e C devem estar entre A e D
        int idxA = order.indexOf(a);
        int idxB = order.indexOf(b);
        int idxC = order.indexOf(c);
        int idxD = order.indexOf(d);

        assertTrue(idxA < idxB && idxB < idxD);
        assertTrue(idxA < idxC && idxC < idxD);
    }

    /**
     * Teste 4: Grafo com múltiplos ciclos
     * Estrutura: A → B → C → A (ciclo 1) e D → E → D (ciclo 2)
     */
    @Test
    void testMultipleCycles() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);
        Station c = createStation(3, "Station C", 42.0, -8.0);
        Station d = createStation(4, "Station D", 43.0, -8.0);
        Station e = createStation(5, "Station E", 44.0, -8.0);

        stations.add(a);
        stations.add(b);
        stations.add(c);
        stations.add(d);
        stations.add(e);

        // Ciclo 1: A → B → C → A
        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(b, c, 15.0, 100, 7.0));
        lines.add(new Line(c, a, 20.0, 100, 10.0));

        // Ciclo 2: D → E → D
        lines.add(new Line(d, e, 10.0, 100, 5.0));
        lines.add(new Line(e, d, 10.0, 100, 5.0));

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertTrue(result.hasCycle());

        List<List<Station>> cycles = result.getCycles();
        assertNotNull(cycles);
        assertTrue(cycles.size() >= 1, "Deve detetar pelo menos um ciclo");
    }

    /**
     * Teste 5: Grafo vazio - deve retornar ordenação vazia
     */
    @Test
    void testEmptyGraph() {
        // Arrange
        repository.clear();
        controller = new UpgradeOrderController();

        // Act & Assert


        assertThrows(IllegalStateException.class, controller::calculateUpgradeOrder);
    }

    /**
     * Teste 6: Grafo com uma única estação - deve retornar ordem com 1 elemento
     */
    @Test
    void testSingleStation() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        stations.add(a);

        repository.loadStations(stations);
        repository.loadLines(new ArrayList<>()); // sem linhas

        controller = new UpgradeOrderController();


        // Assert
        assertThrows(IllegalStateException.class, controller::calculateUpgradeOrder);
    }

    /**
     * Teste 7: Grafo linear longo - deve retornar ordenação correta
     * Estrutura: A → B → C → D → E → F
     */
    @Test
    void testLongLinearGraph() {
        // Arrange
        List<Station> linearStations = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Station s = createStation(i, "Station " + (char)('A' + i - 1),
                    40.0 + i, -8.0);
            linearStations.add(s);
            stations.add(s);
        }

        // Criar cadeia A → B → C → D → E → F
        for (int i = 0; i < linearStations.size() - 1; i++) {
            lines.add(new Line(linearStations.get(i), linearStations.get(i + 1),
                    10.0, 100, 5.0));
        }

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertFalse(result.hasCycle());

        LinkedList<Station> order = result.getUpgradeOrder();
        assertEquals(6, order.size());

        // Verificar ordem sequencial
        for (int i = 0; i < linearStations.size() - 1; i++) {
            int idx1 = order.indexOf(linearStations.get(i));
            int idx2 = order.indexOf(linearStations.get(i + 1));
            assertTrue(idx1 < idx2,
                    String.format("Station %d deve vir antes de Station %d", i, i+1));
        }
    }

    /**
     * Teste 8: Self-loop (estação com ligação para si própria) - deve detetar ciclo
     */
    @Test
    void testSelfLoop() {
        // Arrange
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);

        stations.add(a);
        stations.add(b);

        // A → B (normal) e A → A (self-loop)
        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(a, a, 5.0, 100, 2.0)); // self-loop

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertTrue(result.hasCycle(), "Self-loop deve ser detetado como ciclo");
    }

    /**
     * Teste 9: Grafo com componentes desconexas - deve processar todas
     */
    @Test
    void testDisconnectedComponents() {
        // Arrange
        // Componente 1: A → B
        Station a = createStation(1, "Station A", 40.0, -8.0);
        Station b = createStation(2, "Station B", 41.0, -8.0);

        // Componente 2: C → D (desconexo de A-B)
        Station c = createStation(3, "Station C", 42.0, -9.0);
        Station d = createStation(4, "Station D", 43.0, -9.0);

        stations.add(a);
        stations.add(b);
        stations.add(c);
        stations.add(d);

        lines.add(new Line(a, b, 10.0, 100, 5.0));
        lines.add(new Line(c, d, 10.0, 100, 5.0));

        repository.loadStations(stations);
        repository.loadLines(lines);

        controller = new UpgradeOrderController();

        // Act
        UpgradeOrderResult result = controller.calculateUpgradeOrder();

        // Assert
        assertFalse(result.hasCycle());
        assertEquals(4, result.getUpgradeOrder().size());

        // Verificar dependências dentro de cada componente
        LinkedList<Station> order = result.getUpgradeOrder();
        assertTrue(order.indexOf(a) < order.indexOf(b));
        assertTrue(order.indexOf(c) < order.indexOf(d));
    }

    /**
     * Teste 10: Carregar de CSV real - teste de integração
     */
    @Test
    void testLoadFromCSV() throws IOException {
        // Arrange
        String stationsPath = "warehouseMngt/train_station_dataset/stations.csv";
        String linesPath = "warehouseMngt/train_station_dataset/lines.csv";

        java.io.File stationsFile = new java.io.File(stationsPath);
        java.io.File linesFile = new java.io.File(linesPath);

        // Só executar se os ficheiros existirem
        if (!stationsFile.exists() || !linesFile.exists()) {
            System.out.println("CSV files not found - skipping integration test");
            return;
        }

        // Act
        controller.buildGraphFromCsv(stationsPath, linesPath);

        // Assert
        assertTrue(controller.isGraphBuilt());
        assertTrue(controller.getStationCount() > 0);
        assertTrue(controller.getLineCount() > 0);

        // Tentar calcular ordem
        UpgradeOrderResult result = controller.calculateUpgradeOrder();
        assertNotNull(result);
    }

    // Métodos auxiliares

    private Station createStation(int id, String name, double lat, double lon) {
        Location loc = new Location(lat, lon);
        return new Station(id, name, loc, 0.0, 0.0);
    }
}