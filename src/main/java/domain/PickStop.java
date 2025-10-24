package domain;

import java.util.Objects;

/**
 * Represents a stop in a picking route inside the warehouse.
 * <p>
 * Each {@code PickStop} identifies a physical location composed of
 * an aisle and a bay, where a certain total quantity of items
 * needs to be collected. It may also accumulate quantities if the
 * same aisle and bay appear multiple times in a plan.
 * <p>
 * Equality and hash code are based solely on aisle and bay values,
 * meaning that two {@code PickStop} objects representing the same
 * coordinates are considered equal regardless of quantity.
 */
public class PickStop {

    /** The aisle number for this pick stop. */
    private final int aisle;

    /** The bay number for this pick stop. */
    private final int bay;

    /** The total quantity of items to pick at this location. */
    private double totalQty;

    /**
     * Constructs a new {@code PickStop} with the given aisle, bay, and quantity.
     *
     * @param aisle     the aisle number
     * @param bay       the bay number
     * @param totalQty  the total quantity of items to pick at this stop
     */
    public PickStop(int aisle, int bay, double totalQty) {
        this.aisle = aisle;
        this.bay = bay;
        this.totalQty = totalQty;
    }

    /**
     * Returns the aisle number of this pick stop.
     *
     * @return the aisle number
     */
    public int getAisle() { return aisle; }

    /**
     * Returns the bay number of this pick stop.
     *
     * @return the bay number
     */
    public int getBay() { return bay; }

    /**
     * Returns the total quantity of items to be picked at this stop.
     *
     * @return the total quantity
     */
    public double getTotalQty() { return totalQty; }

    /**
     * Adds an additional quantity to this pick stop.
     *
     * @param q the quantity to add
     */
    public void addQty(double q) { this.totalQty += q; }

    /**
     * Returns a formatted string representation of this pick stop.
     * <p>
     * Example: {@code (3,5) qty=42.0}
     *
     * @return a string describing this pick stop
     */
    @Override
    public String toString() {
        return "(" + aisle + "," + bay + ") qty=" + totalQty;
    }

    /**
     * Compares this pick stop with another object for equality.
     * <p>
     * Two pick stops are equal if and only if they have the same
     * aisle and bay values, regardless of quantity.
     *
     * @param o the object to compare to
     * @return {@code true} if both represent the same location; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickStop)) return false;
        PickStop that = (PickStop) o;
        return aisle == that.aisle && bay == that.bay;
    }

    /**
     * Returns a hash code value consistent with {@link #equals(Object)}.
     *
     * @return a hash code based on aisle and bay
     */
    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }
}
