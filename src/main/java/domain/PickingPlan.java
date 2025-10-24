package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object that represents the result of a picking plan execution.
 * <p>
 * It contains the packing mode used, the list of generated {@link Trolley} objects,
 * and optional logs for partial and deferred allocations.
 */
public class PickingPlan {
    /** The packing mode used to create this plan. */
    private PackingMode mode;

    /** The list of trolleys that compose this plan. */
    private List<Trolley> trolleys;

    /** Partial-allocation logs produced during packing (may be empty). */
    private List<PartialAllocationLog> partial;

    /** Allocations deferred due to capacity (may be empty). */
    private List<Allocation> deferred;


    /**
     * Builds a picking plan with the given mode and resulting trolleys.
     * <p>
     * Partial and deferred logs are initialized as empty lists.
     *
     * @param mode     the packing mode applied (FF, FFD, BFD)
     * @param trolleys the resulting list of trolleys
     */
    public PickingPlan(PackingMode mode, List<Trolley> trolleys) {
        this.mode = mode;
        this.trolleys = new ArrayList<>(trolleys);
        this.partial = new ArrayList<>();
        this.deferred = new ArrayList<>();
    }

    /** @return the packing mode used to generate this plan */
    public PackingMode getMode() {
        return mode;
    }

    /** @return the list of trolleys produced by the plan */
    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    /** @return the list of partial-allocation logs */
    public List<PartialAllocationLog> getPartial() {
        return partial;
    }

    /** @return the list of deferred allocations */
    public List<Allocation> getDeferred() {
        return deferred;
    }

    /** @return the total number of trolleys in this plan */
    public int getNumberOfTrolleys() {
        return trolleys.size();
    }

    /**
     * Computes the total used weight across all trolleys.
     *
     * @return the sum of used kilograms in all trolleys
     */
    public double getTotalUsedKg() {
        double sum = 0.0;
        for (Trolley trolley : trolleys) {
            sum += trolley.getUsedKg();
        }
        return sum;
    }


    /**
     * Computes the total capacity across all trolleys.
     *
     * @return the sum of capacity kilograms in all trolleys
     */
    public double getTotalCapacityKg() {
        double sum = 0.0;
        for (Trolley trolley : trolleys) {
            sum += trolley.getWeightCapacity();
        }
        return sum;
    }


    /**
     * Computes the global utilization (percentage) across all trolleys.
     *
     * @return utilization percentage in the range [0, 100]
     */
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
