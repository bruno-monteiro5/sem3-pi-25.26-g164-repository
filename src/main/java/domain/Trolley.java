package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trolley used to transport allocated products during the picking process.
 * <p>
 * Each trolley has a fixed weight capacity and stores a list of {@link Allocation} objects
 * assigned to it according to the selected packing strategy.
 */
public class Trolley {
    /** Unique identifier of the trolley. */
    private int id;

    /** Maximum weight capacity of the trolley (in kilograms). */
    private double weightCapacity;

    /** Total weight currently used by the allocations. */
    private double usedKg;

    /** List of allocations placed in this trolley. */
    private List<Allocation> items;


    /**
     * Creates a new trolley with a specific ID and capacity.
     *
     * @param id             unique trolley identifier
     * @param weightCapacity maximum load capacity (in kilograms)
     */
    public Trolley(int id, double weightCapacity) {
        this.id = id;
        this.weightCapacity = weightCapacity;
        this.usedKg = 0.0;
        this.items = new ArrayList<>();
    }

    /** @return the trolley identifier */
    public int getId() {
        return id;
    }

    /** @return the maximum capacity of the trolley (kg) */
    public double getWeightCapacity() {
        return weightCapacity;
    }

    /** @return the total weight currently used (kg) */
    public double getUsedKg() {
        return usedKg;
    }

    /** @return the list of allocations stored in this trolley */
    public List<Allocation> getItems() {
        return items;
    }

    /**
     * Checks if the trolley can accommodate a new allocation of the given weight.
     *
     * @param weight total weight of the allocation to add
     * @return {@code true} if it fits within the remaining capacity, otherwise {@code false}
     */
    public boolean canFit (double weight) {
        return (usedKg + weight) <= weightCapacity;
    }

    /**
     * Adds an allocation to this trolley and updates its used weight.
     *
     * @param allocation the allocation to add
     */
    public void addItem(Allocation allocation) {
        double weightItem = allocation.getQuantity() * allocation.getUnitWeight();
        items.add(allocation);
        usedKg += weightItem;
    }

    /**
     * Calculates the utilization percentage of this trolley.
     *
     * @return percentage of used capacity in the range [0, 100]
     */
    public double percUtilization() {
        if (weightCapacity == 0.0) {
            return 0.0;
        } else {
            return (usedKg / weightCapacity)*100.0;
        }
    }


    /**
     * Computes how much capacity remains available in the trolley.
     *
     * @return remaining capacity (kg)
     */
    public double remainingCapacity() {
        return weightCapacity - usedKg;
    }



}