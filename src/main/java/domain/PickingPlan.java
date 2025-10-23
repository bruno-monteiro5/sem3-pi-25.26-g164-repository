package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickingPlan {
    private PackingMode mode;
    private List<Trolley> trolleys;
    private List<PartialAllocationLog> partial;
    private List<Allocation> deferred;

    public PickingPlan(PackingMode mode, List<Trolley> trolleys) {
        this.mode = mode;
        this.trolleys = new ArrayList<>(trolleys);
        this.partial = new ArrayList<>();
        this.deferred = new ArrayList<>();
    }

    public PackingMode getMode() {
        return mode;
    }

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    public List<PartialAllocationLog> getPartial() {
        return partial;
    }

    public List<Allocation> getDeferred() {
        return deferred;
    }

    public int getNumberOfTrolleys() {
        return trolleys.size();
    }

    public double getTotalUsedKg() {
        double sum = 0.0;
        for (Trolley trolley : trolleys) {
            sum += trolley.getUsedKg();
        }
        return sum;
    }

    public double getTotalCapacityKg() {
        double sum = 0.0;
        for (Trolley trolley : trolleys) {
            sum += trolley.getWeightCapacity();
        }
        return sum;
    }

    public double getGlobalUtilizationPercent() {
        double totalCapacity = getTotalCapacityKg();
        double totalUsed = getTotalUsedKg();

        if (totalCapacity == 0.0) {
            return 0.0;
        }

        double utilization = totalUsed / totalCapacity;
        double utilizationPercent = utilization * 100.0;

        return utilizationPercent;
    }

}
