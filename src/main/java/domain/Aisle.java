package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an aisle within a warehouse.
 * <p>
 * Each {@code Aisle} has a unique identifier and contains a list of {@link Bay} objects.
 * Bays represent storage locations or compartments within the aisle.
 * <p>
 * This class provides methods to manage the bays in an aisle,
 * including adding bays, retrieving them, and searching for the first bay
 * that still has available storage capacity.
 */
public class Aisle {

    /** Unique identifier of this aisle. */
    private int aisleID;

    /** List of bays contained in this aisle. */
    private List<Bay> bays;

    /**
     * Constructs a new {@code Aisle} with the given identifier.
     * <p>
     * The list of bays is initialized as empty.
     *
     * @param aisleID the identifier of the aisle
     */
    public Aisle(int aisleID){
        this.aisleID = aisleID;
        this.bays = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of this aisle.
     *
     * @return the aisle identifier
     */
    public int getAisleID() {
        return aisleID;
    }

    /**
     * Updates the identifier of this aisle.
     *
     * @param aisleID the new aisle identifier
     */
    public void setAisleID(int aisleID) {
        this.aisleID = aisleID;
    }

    /**
     * Returns the list of bays currently associated with this aisle.
     *
     * @return the list of {@link Bay} objects in this aisle
     */
    public List<Bay> getBays() {
        return bays;
    }

    /**
     * Replaces the current list of bays with a new one.
     * <p>
     * Typically used when importing or resetting the aisle configuration.
     *
     * @param bays the new list of bays
     */
    public void setBays(List<Bay> bays) {
        this.bays = bays;
    }

    /**
     * Adds a new bay to this aisle.
     * <p>
     * The new bay is appended at the end of the internal list.
     *
     * @param bay the bay to be added
     */
    public void addBay(Bay bay){
        this.bays.add(bay);
    }

    /**
     * Finds and returns the first bay in this aisle that still has available capacity.
     * <p>
     * The method iterates over all bays and checks if the number of stored boxes
     * (inventory size) is smaller than the bay’s capacity.
     * The first bay meeting this condition is returned.
     * <p>
     * If no bay has available capacity, the method returns {@code null}.
     *
     * @return the first available {@link Bay}, or {@code null} if all are full
     */
    public Bay getFirstAvailableBay(){
        for(Bay b: bays){
            if(b.getInventory().size() < b.getCapacity()){
                return b;
            }
        }
        return null;
    }

    /**
     * Returns a formatted textual representation of this aisle,
     * including its identifier and the current number of bays.
     *
     * @return a string describing the aisle
     */
    public String toString(){
        String s = String.format("Aisle ID: %d, with %d bays", this.aisleID, this.bays.size());
        for(Bay b: this.bays){
            s += String.format("%s\n", b.toString());
        }
        return s;
    }
}
