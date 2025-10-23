package domain;

import java.util.ArrayList;
import java.util.List;

public class Trolley {
    private  int id;
    private  double weightCapacity;
    private double usedKg;
    private List<Items> items;

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

    public List<Items> getItems() {
        return items;
    }

    public boolean canFit (double weight) {
        return (usedKg + weight) <= weightCapacity;
    }

    public void addItem(Items item) {
        double weightItem = item.getQuantity() * item.getUnitWeight();
        items.add(item);
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
