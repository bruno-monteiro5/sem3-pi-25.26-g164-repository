package controller.FreightController;

import domain.railway.Endpoint;
import domain.railway.Freight;
import domain.railway.RouteOperation;
import domain.railway.RoutePlan;

import java.util.List;

/**
 * Controller responsible for planning routes to fulfill a list of freight shipments.
 * <p>
 * Provides functionality to define a route that covers multiple freights (simple or composite route),
 * producing a RoutePlan with the sequence of station stops and cargo operations.
 * <p>
 * USLP08 - As Freight Manager, I want to use a route planner that allows me to define/assign routes
 * in order to accomplish a list of pending freights.
 * <p>
 * Acceptance Criteria:
 * - A list with the sequence of stations and the cargos to be loaded/unloaded should be presented
 */
public class RoutePlannerController {

    /**
     * Creates a RoutePlan for the given sequence of freights.
     * <p>
     * Logic:
     * - If a single freight is provided, creates a simple route (load at origin, unload at destination)
     * - If multiple freights are provided, creates a composite route with proper chaining
     * <p>
     * For composite routes:
     * - The destination of each freight must be the origin of the next freight (continuous chain)
     * - At intermediate stops, the current freight is unloaded and the next freight is loaded
     * - The first freight is loaded at its origin
     * - The last freight is unloaded at its destination
     * <p>
     * Example for 2 freights: A→B and B→C
     * 1. Station A: Load Freight 1
     * 2. Station B: Unload Freight 1, Load Freight 2
     * 3. Station C: Unload Freight 2
     *
     * @param freights the list of freights (in the desired transport order)
     * @return a RoutePlan covering all given freights
     * @throws IllegalArgumentException if the freights list is empty or not chainable as a single route
     */
    public RoutePlan planRoute(List<Freight> freights) {
        // Validation: check if freights list is valid
        if (freights == null || freights.isEmpty()) {
            throw new IllegalArgumentException("No freights provided for route planning");
        }

        // Validation: check continuous chaining of freights
        // (each next freight's origin must match the previous freight's destination)
        for (int i = 1; i < freights.size(); i++) {
            Freight previous = freights.get(i - 1);
            Freight current = freights.get(i);

            Endpoint prevDest = previous.getDestination();
            Endpoint currOrig = current.getOrigin();

            if (!prevDest.equals(currOrig)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Freights do not form a continuous route: Freight %d ends at '%s' but Freight %d starts at '%s'",
                                previous.getId(),
                                prevDest.getName(),
                                current.getId(),
                                currOrig.getName()
                        )
                );
            }
        }

        // Create the route plan
        RoutePlan routePlan = new RoutePlan();

        // Step 1: Load the first freight at its origin
        Freight firstFreight = freights.get(0);
        RouteOperation loadFirst = new RouteOperation(
                firstFreight.getOrigin(),
                firstFreight,
                RouteOperation.Type.LOAD
        );
        routePlan.addOperation(loadFirst);

        // Step 2: Process each freight in sequence
        for (int index = 0; index < freights.size(); index++) {
            Freight currentFreight = freights.get(index);

            // Unload current freight at its destination
            RouteOperation unloadCurrent = new RouteOperation(
                    currentFreight.getDestination(),
                    currentFreight,
                    RouteOperation.Type.UNLOAD
            );
            routePlan.addOperation(unloadCurrent);

            // If there is a next freight, load it at the same station
            // (current destination == next origin, already validated)
            if (index < freights.size() - 1) {
                Freight nextFreight = freights.get(index + 1);
                RouteOperation loadNext = new RouteOperation(
                        currentFreight.getDestination(), // same as nextFreight.getOrigin()
                        nextFreight,
                        RouteOperation.Type.LOAD
                );
                routePlan.addOperation(loadNext);
            }
        }

        return routePlan;
    }

    /**
     * Validates if a list of freights can form a continuous route.
     * <p>
     * A continuous route means each freight's destination is the next freight's origin.
     *
     * @param freights the list of freights to validate
     * @return true if freights form a continuous route, false otherwise
     */
    public boolean validateContinuousRoute(List<Freight> freights) {
        if (freights == null || freights.isEmpty()) {
            return false;
        }

        if (freights.size() == 1) {
            return true; // Single freight is always valid
        }

        // Check chaining
        for (int i = 1; i < freights.size(); i++) {
            Freight previous = freights.get(i - 1);
            Freight current = freights.get(i);

            if (!previous.getDestination().equals(current.getOrigin())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Counts the total number of station stops in a route plan.
     * <p>
     * Station stops are unique locations where operations occur.
     *
     * @param plan the route plan to analyze
     * @return number of unique station stops
     */
    public int countStationStops(RoutePlan plan) {
        if (plan == null || plan.getOperations().isEmpty()) {
            return 0;
        }

        java.util.Set<Endpoint> uniqueStations = new java.util.HashSet<Endpoint>();

        for (RouteOperation op : plan.getOperations()) {
            uniqueStations.add(op.getStation());
        }

        return uniqueStations.size();
    }
}