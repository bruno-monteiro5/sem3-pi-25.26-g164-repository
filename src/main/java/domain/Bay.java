package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a storage bay within an aisle of the warehouse.
 * <p>
 * A {@code Bay} holds multiple {@link Box} objects up to its defined capacity.
 * Each bay has a unique identifier, a maximum capacity,
 * and an internal list representing the stored boxes (inventory).
 * <p>
 * The class provides operations to manage the bay inventory,
 * including adding boxes, retrieving capacity and inventory information,
 * and generating a formatted description of the bay’s current state.
 */
public class Bay {

    /** Unique identifier of the bay. */
    private int bayID;

    /** List of boxes currently stored in this bay. */
    private List<Box> inventory;

    /** Maximum number of boxes this bay can hold. */
    private int capacity;

    /**
     * Constructs a new {@code Bay} with the specified identifier and capacity.
     * <p>
     * The inventory list is initialized as empty.
     *
     * @param bayID    the unique identifier of the bay
     * @param capacity the maximum number of boxes the bay can hold
     */
    public Bay(int bayID, int capacity) {
        this.bayID = bayID;
        this.capacity = capacity;
        this.inventory = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of this bay.
     *
     * @return the bay identifier
     */
    public int getBayID() {
        return bayID;
    }

    /**
     * Updates the unique identifier of this bay.
     *
     * @param bayID the new bay identifier
     */
    public void setBayID(int bayID) {
        this.bayID = bayID;
    }

    /**
     * Returns the list of boxes currently stored in this bay.
     * <p>
     * The returned list is live — modifications to it will affect
     * the internal inventory directly.
     *
     * @return the current list of boxes in the bay
     */
    public List<Box> getInventory() {
        return inventory;
    }

    /**
     * Replaces the internal inventory list with a new one.
     * <p>
     * Typically used when restoring or resetting bay data from an external source.
     *
     * @param inventory the new list of boxes to assign to this bay
     */
    public void setInventory(List<Box> inventory) {
        this.inventory = inventory;
    }

    /**
     * Returns the maximum capacity (number of boxes) that this bay can hold.
     *
     * @return the capacity of the bay
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Updates the maximum capacity (number of boxes) that this bay can hold.
     *
     * @param capacity the new capacity of the bay
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Attempts to add a {@link Box} to this bay’s inventory.
     * <p>
     * A box can only be added if the current number of stored boxes
     * is less than the bay’s defined capacity.
     *
     * @param box the box to add
     * @return {@code true} if the box was successfully added;
     *         {@code false} if the bay is already full
     */
    public boolean addProductBox(Box box) {
        if (this.inventory.size() < this.capacity) {
            this.inventory.add(box);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a formatted string describing the bay.
     * <p>
     * If the inventory is empty, only the header with ID, box count, and capacity
     * is printed. Otherwise, the string also lists each box on a new line
     * using the {@link Box#toString()} representation.
     *
     * @return a string describing the bay’s current state
     */
    @Override
    public String toString() {
        if (this.inventory.size() == 0) {
            return String.format("Bay ID: %d, with %d boxes, and %d capacity",
                    this.bayID, this.inventory.size(), this.capacity);
        } else {
            String result = String.format("Bay ID: %d, with %d boxes, and %d capacity",
                    this.bayID, this.inventory.size(), this.capacity);
            for (Box e : this.inventory) {
                result += String.format("%s\n", e.toString());
            }
            return result;
        }
    }
}
