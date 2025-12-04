package domain;

import domain.Station;
import java.util.LinkedList;
import java.util.List;

/**
 * Resultado da análise de ordenação de upgrade de estações.
 * Contém ou uma ordem válida, ou ciclos detectados.
 */
public class UpgradeOrderResult {

    private final boolean hasCycle;
    private final LinkedList<Station> upgradeOrder;
    private final List<List<Station>> cycles;

    /**
     * Construtor para caso sem ciclos (ordenação válida)
     */
    public UpgradeOrderResult(LinkedList<Station> upgradeOrder) {
        this.hasCycle = false;
        this.upgradeOrder = upgradeOrder;
        this.cycles = null;
    }

    /**
     * Construtor para caso com ciclos
     */
    public UpgradeOrderResult(List<List<Station>> cycles) {
        this.hasCycle = true;
        this.upgradeOrder = null;
        this.cycles = cycles;
    }

    public boolean hasCycle() {
        return hasCycle;
    }

    public LinkedList<Station> getUpgradeOrder() {
        return upgradeOrder;
    }

    public List<List<Station>> getCycles() {
        return cycles;
    }

    public int getStationCount() {
        if (hasCycle) {
            return cycles.stream()
                    .flatMap(List::stream)
                    .mapToInt(s -> 1)
                    .sum();
        }
        return upgradeOrder != null ? upgradeOrder.size() : 0;
    }
}