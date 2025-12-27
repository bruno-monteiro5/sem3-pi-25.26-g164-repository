package domain.railway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a planned route to accomplish one or more freight shipments.
 * <p>
 * A RoutePlan consists of an ordered sequence of stations (endpoints) where the train will stop,
 * along with the cargo operations (loading or unloading freights) at each stop.
 * <p>
 * For USLP08: This class holds the result of route planning, showing:
 * - The sequence of stations to visit
 * - Which freights to load/unload at each station
 */
public class RoutePlan {
    private final List<RouteOperation> operations;

    /**
     * Constructs an empty RoutePlan.
     */
    public RoutePlan() {
        this.operations = new ArrayList<RouteOperation>();
    }

    /**
     * Adds a route operation (load or unload) to this plan.
     * Operations are added in execution order.
     *
     * @param operation the RouteOperation to add
     */
    public void addOperation(RouteOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
        operations.add(operation);
    }

    /**
     * Returns the list of operations in this route plan, in execution order.
     * <p>
     * The returned list is unmodifiable to protect the internal state.
     *
     * @return ordered list of RouteOperation objects
     */
    public List<RouteOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    /**
     * Returns the number of operations in this route plan.
     *
     * @return number of operations
     */
    public int getOperationCount() {
        return operations.size();
    }

    /**
     * Checks if this route plan is empty (has no operations).
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    /**
     * Gets the first station in the route (origin of the first freight).
     *
     * @return the first station, or null if route is empty
     */
    public Endpoint getFirstStation() {
        if (operations.isEmpty()) {
            return null;
        }
        return operations.get(0).getStation();
    }

    /**
     * Gets the last station in the route (destination of the last freight).
     *
     * @return the last station, or null if route is empty
     */
    public Endpoint getLastStation() {
        if (operations.isEmpty()) {
            return null;
        }
        return operations.get(operations.size() - 1).getStation();
    }

    @Override
    public String toString() {
        if (operations.isEmpty()) {
            return "RoutePlan: empty";
        }

        Endpoint first = getFirstStation();
        Endpoint last = getLastStation();

        return String.format("RoutePlan: %d operations (%s → %s)",
                operations.size(),
                first != null ? first.getName() : "?",
                last != null ? last.getName() : "?");
    }
}