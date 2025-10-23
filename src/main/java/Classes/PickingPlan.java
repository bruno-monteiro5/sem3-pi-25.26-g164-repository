
package Classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickingPlan {
    private final PackingMode mode;
    private final List<Trolley> trolleys;

    public PickingPlan(PackingMode mode, List<Trolley> trolleys) {
        this.mode = mode;
        this.trolleys = trolleys;
    }

    public PackingMode getMode() {
        return mode;
    }

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    private static double weightLine (Allocation allocation) {
        return allocation.getQuantity() * allocation.getUnitWeight();
    }

    public List<Trolley> planFirstFit(List<Allocation> allocations, double trolleyCapacity) {
        List<Trolley> trolleys = new ArrayList<Trolley>();
        int nextTrolleyId = 1;

        for (Allocation allocation : allocations) {
            double w = weightLine(allocation);
            boolean placed = false;
            for (int i = 0; i < trolleys.size() && !placed; i++) {
                Trolley trolley = trolleys.get(i);
                if (trolley.canFit(w)) {
                    trolley.addItem(allocation);
                    placed = true;
                }
            }
            if (!placed) {
                Trolley newTrolley = new Trolley(nextTrolleyId, trolleyCapacity);
                nextTrolleyId++;
                newTrolley.addItem(allocation);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
    }

    public List<Trolley> planFirstFitDecreasing(List<Allocation> allocations, double trolleyCapacity) {
        List<Allocation> sorted = new ArrayList<>(allocations);
        Collections.sort(sorted);

        return planFirstFit(sorted, trolleyCapacity);
    }

    public List<Trolley> planBestFitDecreasing(List<Allocation> allocations, double trolleyCapacity) {
        List<Allocation> sorted = new ArrayList<>(allocations);
        Collections.sort(sorted);

        List<Trolley> trolleys = new ArrayList<Trolley>();
        int nextTrolleyId = 1;

        for (Allocation allocation : sorted) {
            double w = weightLine(allocation);
            Trolley bestTrolley = null;
            double maxRemainingCapacity = -1.0;

            for (Trolley trolley : trolleys) {
                if (trolley.canFit(w)) {
                    double remaining = trolley.remainingCapacity() - w;
                    if (remaining >= 0 && remaining < maxRemainingCapacity) {
                        maxRemainingCapacity = remaining;
                        bestTrolley = trolley;
                    }
                }
            }

            if (bestTrolley != null) {
                bestTrolley.addItem(allocation);
            } else {
                Trolley newTrolley = new Trolley(nextTrolleyId, trolleyCapacity);
                nextTrolleyId++;
                newTrolley.addItem(allocation);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
    }

}
