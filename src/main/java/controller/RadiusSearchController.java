package controller;

import domain.*;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for USEI10 - Radius search and density summary.
 * <p>
 * Finds all stations within a given radius of a target coordinate using the 2D-Tree
 * and Haversine distance, and provides density statistics by country and city flag.
 */
public class RadiusSearchController {

    /** Repository with station data (shared singleton). */
    private final StationRepository repository;

    /** Controller responsible for building and exposing the 2D-Tree (USEI07). */
    private final Build2DTreeController treeController;

    /**
     * Creates a controller using the singleton repository and a 2D-Tree controller.
     *
     * @param treeController controller that manages the 2D-Tree (must not be null)
     * @throws NullPointerException if {@code treeController} is null
     */
    public RadiusSearchController(Build2DTreeController treeController) {
        if (treeController == null) {
            throw new NullPointerException("treeController cannot be null");
        }
        this.repository = StationRepository.getInstance();
        this.treeController = treeController;
    }

    /**
     * Searches for all stations within {@code radiusKm} of the target location.
     * <p>
     * Acceptance Criteria:
     * <ul>
     *   <li>Uses Haversine distance with Earth radius 6371 km
     *       (via {@link HaversineDistance})</li>
     *   <li>Returns an AVL tree sorted by distance ASC, station name DESC
     *       (using {@link DistanceKey})</li>
     *   <li>Provides density summary by country and {@code isCity} flag</li>
     * </ul>
     *
     * Time Complexity: O(n log n), where n is the total number of stations.
     * <ul>
     *   <li>O(n) to traverse all nodes of the 2D-Tree and collect stations</li>
     *   <li>O(log n) per insertion into the AVL index</li>
     * </ul>
     *
     * @param targetLat target latitude in decimal degrees
     * @param targetLon target longitude in decimal degrees
     * @param radiusKm  search radius in kilometers (must be &gt;= 0)
     * @return search result with sorted tree and density summary
     * @throws IllegalStateException    if the 2D-Tree has not been built (USEI07)
     * @throws IllegalArgumentException if {@code radiusKm} is negative
     *                                  or if latitude/longitude are out of bounds
     */
    public RadiusSearchResult searchWithinRadius(double targetLat, double targetLon, double radiusKm) {
        if (!treeController.isBuilt()) {
            throw new IllegalStateException("2D-Tree must be built first (run USEI07)");
        }

        KDTreeUtil.validateCoordinates(targetLat, targetLon);

        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }

        long startNanos = System.nanoTime();

        Location searchCenter = new Location(targetLat, targetLon);
        AVL<DistanceKey> sortedByDistance = new AVL<>();
        Map<String, Integer> countByCountry = new HashMap<>();
        int cityCount = 0;
        int nonCityCount = 0;

        KDTree2D tree = treeController.getKDTree();
        List<Station> candidates = KDTreeUtil.collectAllStations(tree);

        for (int i = 0; i < candidates.size(); i++) {
            Station station = candidates.get(i);

            double distance = HaversineDistance.distanceToStation(
                    targetLat, targetLon, station);

            // Only consider stations strictly within the radius
            if (distance <= radiusKm) {
                // Insert into AVL sorted by DistanceKey (distance ASC, name DESC)
                DistanceKey key = new DistanceKey(distance, station);
                sortedByDistance.insert(key);

                // Update country count
                String country = station.getCountry();
                Integer current = countByCountry.get(country);
                if (current == null) {
                    countByCountry.put(country, 1);
                } else {
                    countByCountry.put(country, current + 1);
                }

                // Update city / non-city counters
                if (station.isCity()) {
                    cityCount++;
                } else {
                    nonCityCount++;
                }
            }
        }

        RadiusSearchResult result = new RadiusSearchResult(
                sortedByDistance,
                countByCountry,
                cityCount,
                nonCityCount,
                radiusKm,
                searchCenter
        );

        long endNanos = System.nanoTime();
        double elapsedSeconds = (endNanos - startNanos) / 1e9;

        System.out.printf(
                "Radius search finished. Target=(%.4f, %.4f), Radius=%.2f km, Found=%d, Time=%.3f s%n",
                targetLat, targetLon, radiusKm, result.getTotalCount(), elapsedSeconds
        );

        return result;
    }

    /**
     * Checks if this controller is ready to answer queries.
     *
     * @return {@code true} if the 2D-Tree has been built; {@code false} otherwise
     */
    public boolean isReady() {
        return treeController.isBuilt();
    }


}
