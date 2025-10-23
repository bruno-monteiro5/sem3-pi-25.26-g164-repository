package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickingPlan {
    private PackingMode mode;
    private List<Trolley> trolleys;

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

    private static double weightLine (Items item) {
        return item.getQuantity() * item.getUnitWeight();
    }

    public List<Trolley> planFirstFit(List<Items> items, double trolleyCapacity) {
        List<Trolley> trolleys = new ArrayList<Trolley>();
        int nextTrolleyId = 1;

        for (Items item : items) {
            double w = weightLine(item);
            boolean placed = false;
            for (int i = 0; i < trolleys.size() && !placed; i++) {
                Trolley trolley = trolleys.get(i);
                if (trolley.canFit(w)) {
                    trolley.addItem(item);
                    placed = true;
                }
            }
            if (!placed) {
                Trolley newTrolley = new Trolley(nextTrolleyId, trolleyCapacity);
                nextTrolleyId++;
                newTrolley.addItem(item);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
    }

    public List<Trolley> planFirstFitDecreasing(List<Items> items, double trolleyCapacity) {
        List<Items> sorted = new ArrayList<>(items);
        Collections.sort(sorted);

        return planFirstFit(sorted, trolleyCapacity);
    }

    public List<Trolley> planBestFitDecreasing(List<Items> items, double trolleyCapacity) {
        List<Items> sorted = new ArrayList<>(items);
        Collections.sort(sorted);

        List<Trolley> trolleys = new ArrayList<Trolley>();
        int nextTrolleyId = 1;

        for (Items item : sorted) {
            double w = weightLine(item);
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
                bestTrolley.addItem(item);
            } else {
                Trolley newTrolley = new Trolley(nextTrolleyId, trolleyCapacity);
                nextTrolleyId++;
                newTrolley.addItem(item);
                trolleys.add(newTrolley);
            }
        }
        return trolleys;
    }


}
