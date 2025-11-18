package controller;

import domain.*;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for proximity search queries (USEI09).
 * <p>
 * Finds the N nearest stations to a target coordinate using the 2D-Tree
 * and Haversine distance, with optional time zone filtering.
 */
public class ProximitySearchController {

    private final StationRepository repository;
    private final Build2DTreeController treeController;

    /**
     * Creates a controller using the singleton repository.
     */
    public ProximitySearchController(Build2DTreeController treeController) {
        this.repository = StationRepository.getInstance();
        this.treeController = treeController;
    }

    /**
     * Finds the N nearest stations to a target point without filtering.
     *
     * @param targetLat target latitude in decimal degrees
     * @param targetLon target longitude in decimal degrees
     * @param n number of nearest neighbors to find (must be > 0)
     * @return proximity search result
     * @throws IllegalStateException if 2D-Tree hasn't been built
     * @throws IllegalArgumentException if n <= 0
     */
    public ProximitySearchResult findNearestN(double targetLat, double targetLon, int n) {
        return findNearestN(targetLat, targetLon, n, new TimeZoneFilter());
    }

    /**
     * Finds the N nearest stations to a target point with optional time zone filtering.
     * <p>
     * Uses a greedy approach traversing the 2D-Tree to find candidates,
     * then calculates exact Haversine distances and returns the N closest matches.
     *
     * @param targetLat target latitude in decimal degrees
     * @param targetLon target longitude in decimal degrees
     * @param n number of nearest neighbors to find (must be > 0)
     * @param filter time zone filter (use disabled filter for no filtering)
     * @return proximity search result with up to N stations
     * @throws IllegalStateException if 2D-Tree hasn't been built
     * @throws IllegalArgumentException if n <= 0
     */


    /**
     * Recursively collects all stations from the 2D-Tree.
     *
     * @param node current node
     * @param accumulator list to accumulate stations
     */
    private void collectAllStations(KDNode node, List<Station> accumulator) {
        if (node == null) {
            return;
        }

        List<Station> bucket = node.getStations();
        for (int i = 0; i < bucket.size(); i++) {
            accumulator.add(bucket.get(i));
        }

        collectAllStations(node.getLeft(), accumulator);
        collectAllStations(node.getRight(), accumulator);
    }

    /**
     * Helper class to pair a station with its calculated distance.
     */
    private static class StationWithDistance {
        final Station station;
        final double distance;

        StationWithDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }
    }
}
