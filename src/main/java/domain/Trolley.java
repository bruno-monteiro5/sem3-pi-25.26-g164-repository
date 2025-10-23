package domain;

import java.util.ArrayList;
import java.util.List;

public class Trolley {
    private final int id;
    private final double weightCapacity;
    private double usedKg;
    private final List<Allocation> items;

    public Trolley(int id, double weightCapacity) {
        this.id = id;
        this.weightCapacity = weightCapacity;
        this.usedKg = 0.0;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public double getWeightCapacity() {
        return weightCapacity;
    }

    public double getUsedKg() {
        return usedKg;
    }

    public List<Allocation> getItems() {
        return items;
    }

    public boolean canFit (double weight) {
        return (usedKg + weight) <= weightCapacity;
    }

    public void addItem(Allocation allocation) {
        double weightItem = allocation.getQuantity() * allocation.getUnitWeight();
        items.add(allocation);
        usedKg += weightItem;
    }

    public double percUtilization() {
        if (weightCapacity == 0.0) {
            return 0.0;
        } else {
            return (usedKg / weightCapacity)*100.0;
        }
    }

    public double remainingCapacity() {
        return weightCapacity - usedKg;
    }



}