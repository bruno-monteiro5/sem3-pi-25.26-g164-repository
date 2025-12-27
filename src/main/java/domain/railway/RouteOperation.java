package domain.railway;

/**
 * Represents a single route operation (loading or unloading a freight) at a specific station.
 * <p>
 * For USLP08: Each RouteOperation specifies:
 * - Where the operation occurs (station/endpoint)
 * - What cargo is involved (freight)
 * - What action is taken (load or unload)
 */
public class RouteOperation {
    /**
     * Type of operation: loading or unloading cargo.
     */
    public enum Type {
        /**
         * Loading a freight onto the train.
         */
        LOAD,

        /**
         * Unloading a freight from the train.
         */
        UNLOAD
    }

    private final Endpoint station;
    private final Freight freight;
    private final Type type;

    /**
     * Creates a new RouteOperation for a given station, freight, and operation type.
     *
     * @param station the station where the operation occurs
     * @param freight the freight (cargo) being loaded or unloaded
     * @param type    the type of operation (load or unload)
     * @throws IllegalArgumentException if any parameter is null
     */
    public RouteOperation(Endpoint station, Freight freight, Type type) {
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }
        if (freight == null) {
            throw new IllegalArgumentException("Freight cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        this.station = station;
        this.freight = freight;
        this.type = type;
    }

    /**
     * Gets the station where this operation occurs.
     *
     * @return the station/endpoint
     */
    public Endpoint getStation() {
        return station;
    }

    /**
     * Gets the freight involved in this operation.
     *
     * @return the freight being loaded or unloaded
     */
    public Freight getFreight() {
        return freight;
    }

    /**
     * Gets the type of operation (load or unload).
     *
     * @return the operation type
     */
    public Type getType() {
        return type;
    }

    /**
     * Checks if this is a load operation.
     *
     * @return true if loading, false if unloading
     */
    public boolean isLoad() {
        return type == Type.LOAD;
    }

    /**
     * Checks if this is an unload operation.
     *
     * @return true if unloading, false if loading
     */
    public boolean isUnload() {
        return type == Type.UNLOAD;
    }

    @Override
    public String toString() {
        String action = (type == Type.LOAD ? "Load" : "Unload");
        return String.format("%s Freight %d at %s", action, freight.getId(), station.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteOperation that = (RouteOperation) o;

        if (!station.equals(that.station)) return false;
        if (!freight.equals(that.freight)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = station.hashCode();
        result = 31 * result + freight.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}