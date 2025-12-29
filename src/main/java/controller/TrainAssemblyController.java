package controller.AssemblyController;

import domain.railway.*;
import repository.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for USLP09 - Train Assembly.
 *
 * Responsibilities:
 * - List available locomotives and wagons by location
 * - Filter by status (Free vs In Transit)
 * - Sort by distance from route start point
 * - Assign rolling stock to trains
 *
 */
public class TrainAssemblyController {

    private final LocationRepository locationRepo;
    private final RailwayRepository railwayRepo;

    public TrainAssemblyController() {
        this.railwayRepo = new RailwayRepository();
        this.locationRepo = new LocationRepository(railwayRepo);
    }

    // ==================== LOCOMOTIVES ====================

    /**
     * Gets all locomotives with their current location and status.
     */
    public List<LocomotiveWithDetails> getAvailableLocomotives() throws SQLException {
        List<LocomotiveLocation> locations = locationRepo.getAllLocomotiveLocations();
        List<Locomotive> allLocos = railwayRepo.getAllLocomotives();

        List<LocomotiveWithDetails> result = new ArrayList<>();

        for (LocomotiveLocation loc : locations) {
            Locomotive loco = findLocomotiveById(allLocos, loc.getLocomotiveId());
            if (loco != null) {
                result.add(new LocomotiveWithDetails(loco, loc));
            }
        }

        return result;
    }

    /**
     * Gets locomotives sorted by distance from route start, showing status.
     *
     * Format per USLP09:
     * - FREE locomotives: sorted by distance ascending
     * - IN TRANSIT locomotives: show final destination
     */
    public List<LocomotiveWithDetails> getLocomotivesByProximity(Endpoint routeStart)
            throws SQLException {

        List<LocomotiveWithDetails> all = getAvailableLocomotives();

        // Separate into free and in-transit
        List<LocomotiveWithDetails> free = all.stream()
                .filter(ld -> ld.location.isFree())
                .collect(Collectors.toList());

        List<LocomotiveWithDetails> inTransit = all.stream()
                .filter(ld -> ld.location.isInTransit())
                .collect(Collectors.toList());

        // Sort free by distance
        free.sort(Comparator.comparingDouble(
                ld -> ld.location.distanceFrom(routeStart)
        ));

        // Combine: free first, then in-transit
        List<LocomotiveWithDetails> result = new ArrayList<>();
        result.addAll(free);
        result.addAll(inTransit);

        return result;
    }

    // ==================== WAGONS ====================

    /**
     * Gets all wagons with their current location and status.
     */
    public List<WagonWithDetails> getAvailableWagons() throws SQLException {
        List<WagonLocation> locations = locationRepo.getAllWagonLocations();

        List<WagonWithDetails> result = new ArrayList<>();

        for (WagonLocation loc : locations) {
            // Load full wagon details from railway repository
            // (You'll need to add this method to RailwayRepository)
            Wagon wagon = railwayRepo.getWagonById(loc.getWagonId());
            if (wagon != null) {
                result.add(new WagonWithDetails(wagon, loc));
            }
        }

        return result;
    }

    /**
     * Gets wagons sorted by distance from route start.
     */
    public List<WagonWithDetails> getWagonsByProximity(Endpoint routeStart)
            throws SQLException {

        List<WagonWithDetails> all = getAvailableWagons();

        List<WagonWithDetails> free = all.stream()
                .filter(wd -> wd.location.isFree())
                .collect(Collectors.toList());

        List<WagonWithDetails> inTransit = all.stream()
                .filter(wd -> wd.location.isInTransit())
                .collect(Collectors.toList());

        free.sort(Comparator.comparingDouble(
                wd -> wd.location.distanceFrom(routeStart)
        ));

        List<WagonWithDetails> result = new ArrayList<>();
        result.addAll(free);
        result.addAll(inTransit);

        return result;
    }

    // ==================== HELPER CLASSES ====================

    /**
     * Combines locomotive details with current location.
     */
    public static class LocomotiveWithDetails {
        public final Locomotive locomotive;
        public final LocomotiveLocation location;

        public LocomotiveWithDetails(Locomotive locomotive, LocomotiveLocation location) {
            this.locomotive = locomotive;
            this.location = location;
        }

        @Override
        public String toString() {
            String status = location.isFree() ? "FREE" : "IN TRANSIT";
            String trainInfo = location.getTrainId() != null
                    ? " (Train " + location.getTrainId() + ")"
                    : "";

            return String.format("%s | %s @ %s%s",
                    locomotive.getName(),
                    status,
                    location.getCurrentEndpoint().getName(),
                    trainInfo);
        }
    }

    /**
     * Combines wagon details with current location.
     */
    public static class WagonWithDetails {
        public final Wagon wagon;
        public final WagonLocation location;

        public WagonWithDetails(Wagon wagon, WagonLocation location) {
            this.wagon = wagon;
            this.location = location;
        }

        @Override
        public String toString() {
            String status = location.isFree() ? "FREE" : "IN TRANSIT";
            String trainInfo = location.getTrainId() != null
                    ? " (Train " + location.getTrainId() + ")"
                    : "";

            return String.format("Wagon %d (%s) | %s @ %s%s",
                    wagon.getId(),
                    wagon.getModel().getType(),
                    status,
                    location.getCurrentEndpoint().getName(),
                    trainInfo);
        }
    }

    // ==================== HELPERS ====================

    private Locomotive findLocomotiveById(List<Locomotive> locos, int id) {
        return locos.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
    }
}