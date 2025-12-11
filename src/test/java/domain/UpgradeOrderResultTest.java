package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        assertNull(result.getCycles(), "Cycles deve ser null quando não há ciclos");
        assertEquals(3, result.getStationCount());
    }

    /**
     * Teste 2: Construtor com ciclos detectados
     */
    @Test
    void testCycleConstructor() {
        // Arrange
        List<List<Station>> cycles = new ArrayList<>();

        List<Station> cycle1 = new ArrayList<>();
        cycle1.add(createStation(1, "Station A"));
        cycle1.add(createStation(2, "Station B"));
        cycle1.add(createStation(3, "Station C"));
        cycle1.add(createStation(1, "Station A")); // fecha o ciclo

        cycles.add(cycle1);

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycles);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasCycle(), "Deve ter ciclos");
        assertNull(result.getUpgradeOrder(), "Order deve ser null quando há ciclos");
        assertNotNull(result.getCycles());
        assertEquals(1, result.getCycles().size());
        assertEquals(4, result.getStationCount()); // 4 elementos no ciclo (incluindo repetição)
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
     * Teste 4: Lista de ciclos vazia
     */
    @Test
    void testEmptyCycles() {
        // Arrange
        List<List<Station>> emptyCycles = new ArrayList<>();

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(emptyCycles);

        // Assert
        assertTrue(result.hasCycle());
        assertNotNull(result.getCycles());
        assertTrue(result.getCycles().isEmpty());
        assertEquals(0, result.getStationCount());
    }

    /**
     * Teste 5: Múltiplos ciclos
     */
    @Test
    void testMultipleCycles() {
        // Arrange
        List<List<Station>> cycles = new ArrayList<>();

        // Ciclo 1: A → B → C → A
        List<Station> cycle1 = new ArrayList<>();
        cycle1.add(createStation(1, "Station A"));
        cycle1.add(createStation(2, "Station B"));
        cycle1.add(createStation(3, "Station C"));
        cycle1.add(createStation(1, "Station A"));

        // Ciclo 2: D → E → D
        List<Station> cycle2 = new ArrayList<>();
        cycle2.add(createStation(4, "Station D"));
        cycle2.add(createStation(5, "Station E"));
        cycle2.add(createStation(4, "Station D"));

        cycles.add(cycle1);
        cycles.add(cycle2);

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycles);

        // Assert
        assertTrue(result.hasCycle());
        assertEquals(2, result.getCycles().size());
        assertEquals(7, result.getStationCount()); // 4 + 3 elementos nos ciclos
    }

    /**
     * Teste 6: Ordenação com uma única estação
     */
    @Test
    void testSingleStationOrder() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        order.add(createStation(1, "Station A"));

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Assert
        assertFalse(result.hasCycle());
        assertEquals(1, result.getStationCount());
        assertEquals(createStation(1, "Station A"), result.getUpgradeOrder().getFirst());
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
     * Teste 8: Imutabilidade - modificar lista original não deve afetar resultado
     *
    //@Test
    void testImmutability() {
        // Arrange
        LinkedList<Station> order = new LinkedList<>();
        Station s1 = createStation(1, "Station A");
        Station s2 = createStation(2, "Station B");
        order.add(s1);
        order.add(s2);

        UpgradeOrderResult result = new UpgradeOrderResult(order);

        // Act - modificar lista original
        order.add(createStation(3, "Station C"));

        // Assert - resultado não deve ser afetado
        assertEquals(2, result.getStationCount(),
                "Modificar lista original não deve afetar resultado");
    }
    **/
    /**
     * Teste 9: Verificar contagem de estações em ciclos complexos
     */
    @Test
    void testStationCountInComplexCycles() {
        // Arrange
        List<List<Station>> cycles = new ArrayList<>();

        // Ciclo com 5 estações
        List<Station> largeCycle = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            largeCycle.add(createStation(i, "Station " + i));
        }
        largeCycle.add(createStation(1, "Station 1")); // fecha ciclo

        cycles.add(largeCycle);

        // Act
        UpgradeOrderResult result = new UpgradeOrderResult(cycles);

        // Assert
        assertEquals(6, result.getStationCount()); // 5 estações + 1 repetição
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