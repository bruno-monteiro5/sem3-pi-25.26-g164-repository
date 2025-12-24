package domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe UpgradeOrderResult
 * Valida o comportamento do resultado da análise de ordenação de upgrade
 */
class UpgradeOrderResultTest {

    /**
     * Teste 1: Construtor com ordenação válida (sem ciclos)
     */
    @Test
    void testValidOrderConstructor() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        order.add(createStation(1, "Station A"));
        order.add(createStation(2, "Station B"));
        order.add(createStation(3, "Station C"));

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Assert
        assertNotNull(result);
        assertFalse(result.hasCycle(), "Não deve ter ciclos");
        assertNotNull(result.getUpgradeOrder());
        assertEquals(3, result.getUpgradeOrder().size());
        assertNull(result.getCycleStations(), "CycleStations deve ser null quando não há ciclos");
        assertEquals(3, result.getStationCount());
    }

    /**
     * Teste 2: Construtor com ciclos detectados (set de estações)
     */
    @Test
    void testCycleConstructor() {
        // Arrange
        Set<Station> cycleStations = new LinkedHashSet<>();
        cycleStations.add(createStation(1, "Station A"));
        cycleStations.add(createStation(2, "Station B"));
        cycleStations.add(createStation(3, "Station C"));

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycleStations);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasCycle(), "Deve ter ciclos");
        assertNull(result.getUpgradeOrder(), "Order deve ser null quando há ciclos");
        assertNotNull(result.getCycleStations());
        assertEquals(3, result.getCycleStations().size());
        assertEquals(3, result.getStationCount()); // únicas (sem repetição de fecho)
    }

    /**
     * Teste 3: Ordenação vazia
     */
    @Test
    void testEmptyOrder() {
        // Arrange
        LinkedList<Station> emptyOrder = new LinkedList<>();

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(emptyOrder);

        // Assert
        assertFalse(result.hasCycle());
        assertNotNull(result.getUpgradeOrder());
        assertTrue(result.getUpgradeOrder().isEmpty());
        assertEquals(0, result.getStationCount());
    }

    /**
     * Teste 4: Set de ciclos vazio
     */
    @Test
    void testEmptyCycleStations() {
        // Arrange
        Set<Station> emptyCycleStations = new LinkedHashSet<>();

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(emptyCycleStations);

        // Assert
        assertTrue(result.hasCycle());
        assertNotNull(result.getCycleStations());
        assertTrue(result.getCycleStations().isEmpty());
        assertEquals(0, result.getStationCount());
    }

    /**
     * Teste 5: Múltiplos ciclos (união das estações únicas)
     */
    @Test
    void testMultipleCyclesStationsUnion() {
        // Arrange
        Set<Station> cycleStations = new LinkedHashSet<>();

        // Ciclo 1: A, B, C
        cycleStations.add(createStation(1, "Station A"));
        cycleStations.add(createStation(2, "Station B"));
        cycleStations.add(createStation(3, "Station C"));

        // Ciclo 2: D, E
        cycleStations.add(createStation(4, "Station D"));
        cycleStations.add(createStation(5, "Station E"));

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycleStations);

        // Assert
        assertTrue(result.hasCycle());
        assertEquals(5, result.getCycleStations().size());
        assertEquals(5, result.getStationCount()); // únicas (não soma repetições)
    }

    /**
     * Teste 6: Ordenação com uma única estação
     */
    @Test
    void testSingleStationOrder() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        Station s = createStation(1, "Station A");
        order.add(s);

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Assert
        assertFalse(result.hasCycle());
        assertEquals(1, result.getStationCount());
        assertEquals(s, result.getUpgradeOrder().getFirst());
    }

    /**
     * Teste 7: Ordenação longa (muitas estações)
     */
    @Test
    void testLongOrder() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        for (int i = 1; i <= 100; i++) {
            order.add(createStation(i, "Station " + i));
        }

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Assert
        assertFalse(result.hasCycle());
        assertEquals(100, result.getStationCount());
        assertEquals(100, result.getUpgradeOrder().size());
    }

    /**
     * Teste 9: Verificar contagem de estações em ciclo "complexo" (únicas)
     */
    @Test
    void testStationCountInComplexCycles() {
        // Arrange
        Set<Station> cycleStations = new LinkedHashSet<>();

        // Ciclo com 5 estações (únicas)
        for (int i = 1; i <= 5; i++) {
            cycleStations.add(createStation(i, "Station " + i));
        }

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycleStations);

        // Assert
        assertEquals(5, result.getStationCount()); // únicas
    }

    /**
     * Teste 10: Verificar integridade dos dados retornados
     */
    @Test
    void testDataIntegrity() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        Station s1 = createStation(1, "Station A");
        Station s2 = createStation(2, "Station B");
        Station s3 = createStation(3, "Station C");

        order.add(s1);
        order.add(s2);
        order.add(s3);

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Assert - verificar que os dados estão corretos
        LinkedList<Station> returnedOrder = result.getUpgradeOrder();
        assertEquals(3, returnedOrder.size());
        assertEquals(s1, returnedOrder.get(0));
        assertEquals(s2, returnedOrder.get(1));
        assertEquals(s3, returnedOrder.get(2));
    }

    // Métodos auxiliares

    private Station createStation(int id, String name) {
        Location loc = new Location(-40.0 + id, -8.0);
        return new Station(id, name, loc, 0.0, 0.0);
    }
}
