package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a warehouse composed of multiple {@link Aisle} objects.
 * <p>
 * Each {@code Warehouse} has a unique identifier and manages a list of
 * aisles, which in turn contain bays and boxes. The class provides
 * operations to access, modify, and search for available aisles.
 */
public class Warehouse {

    /** The list of aisles in this warehouse. */
    private List<Aisle> aisles;

    /** Unique identifier of the warehouse. */
    private String warehouseID;

    /**
     * Constructs a new {@code Warehouse} with a given identifier.
     * <p>
     * Initializes the internal aisle list as empty.
     *
     * @param warehouseID unique identifier of this warehouse
     */
    public Warehouse(String warehouseID) {
        this.warehouseID = warehouseID;
        this.aisles = new ArrayList<>();
    }

    /** @return the list of aisles in this warehouse */
    public List<Aisle> getAisles() { return aisles; }

    /**
     * Replaces the internal list of aisles with a new one.
     *
     * @param aisles the new list of aisles
     */
    public void setAisles(List<Aisle> aisles) { this.aisles = aisles; }

    /** @return the warehouse identifier */
    public String getWarehouseID() { return warehouseID; }

    /**
     * Updates the warehouse identifier.
     *
     * @param warehouseID the new identifier
     */
    public void setWarehouseID(String warehouseID) { this.warehouseID = warehouseID; }

    /**
     * Adds a new aisle to this warehouse using the specified ID.
     * <p>
     * A new {@link Aisle} instance is created and appended to the aisle list.
     *
     * @param aisleID the identifier of the new aisle
     */
    public void addAisle(int aisleID) {
        this.aisles.add(new Aisle(aisleID));
    }

    /**
     * Returns the most recently added aisle in this warehouse.
     *
     * @return the last {@link Aisle} in the list
     * @throws IndexOutOfBoundsException if the warehouse has no aisles
     */
    public Aisle getLastAisle() {
        return this.aisles.get(this.aisles.size() - 1);
    }

    /**
     * Finds the first aisle that still has at least one available bay.
     *
     * @return the first {@link Aisle} with an available bay, or {@code null} if none are available
     */
    public Aisle getFirstAvailableAisle() {
        for (Aisle a : aisles) {
            if (a.getFirstAvailableBay() != null) return a;
        }
        return null;
    }
}
