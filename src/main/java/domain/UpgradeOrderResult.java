package domain;

import java.util.LinkedList;
import java.util.Set;

/**
 * Resultado da análise de ordenação de upgrade de estações.
 * Contém ou uma ordem válida, ou as estações que pertencem a pelo menos 1 ciclo.
 */
public class UpgradeOrderResult {

    private final boolean hasCycle;
    private final LinkedList<Station> upgradeOrder;
    private final Set<Station> cycleStations;

    /**
     * Construtor para caso sem ciclos (ordenação válida)
     */
    public UpgradeOrderResult(LinkedList<Station> upgradeOrder) {
        this.hasCycle = false;
        this.upgradeOrder = upgradeOrder;
        this.cycleStations = null;
    }

    /**
     * Construtor para caso com ciclos
     * (set de estações que pertencem a pelo menos um ciclo)
     */
    public UpgradeOrderResult(Set<Station> cycleStations) {
        this.hasCycle = true;
        this.upgradeOrder = null;
        this.cycleStations = cycleStations;
    }

    public boolean hasCycle() {
        return hasCycle;
    }

    public LinkedList<Station> getUpgradeOrder() {
        return upgradeOrder;
    }

    public Set<Station> getCycleStations() {
        return cycleStations;
    }

    public int getStationCount() {
        if (hasCycle) {
            return cycleStations != null ? cycleStations.size() : 0;
        }
        return upgradeOrder != null ? upgradeOrder.size() : 0;
    }
}
