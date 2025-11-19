package controller;

import domain.*;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import repository.KDTreeRepository;


/**
 * Controller for proximity search queries (USEI09).
 * <p>
 * Finds the N nearest stations to a target coordinate using the 2D-Tree
 * and Haversine distance, with optional time zone filtering.
 */
public class ProximitySearchController {

    private final StationRepository repository;
    private final KDTreeRepository kdTreeRepository;

    /**
     * Creates a controller using the singleton repository.
     */
    public ProximitySearchController(Build2DTreeController treeController) {
        this.repository = StationRepository.getInstance();
        this.kdTreeRepository = KDTreeRepository.getInstance();
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
    public ProximitySearchResult findNearestN(double targetLat, double targetLon,
                                              int n, TimeZoneFilter filter) {
        if (!kdTreeRepository.isBuilt()) {
            throw new IllegalStateException("2D-Tree must be built first (run USEI07)");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        if (filter == null) {
            filter = new TimeZoneFilter();
        }

        long startNanos = System.nanoTime();

        ProximitySearchResult result = new ProximitySearchResult(targetLat, targetLon, n, filter);
        KDTree2D tree = kdTreeRepository.getKDTree();

        List<Station> candidates = new ArrayList<>();
        collectAllStations(tree.getRoot(), candidates);

        List<StationWithDistance> distances = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Station station = candidates.get(i);
            if (filter.matches(station)) {
                double distance = HaversineDistance.distanceToStation(targetLat, targetLon, station);
                distances.add(new StationWithDistance(station, distance));
            }
        }

        if (distances.isEmpty()) {
            long endNanos = System.nanoTime();
            double elapsedSeconds = (endNanos - startNanos) / 1e9;
            System.out.printf(
                    "Proximity search finished. Target=(%.4f, %.4f), Found=0/%d, Time=%.3f s%n",
                    targetLat, targetLon, n, elapsedSeconds
            );
            return result;
        }

        Collections.sort(distances, new Comparator<StationWithDistance>() {
            @Override
            public int compare(StationWithDistance sd1, StationWithDistance sd2) {
                int distCompare = Double.compare(sd1.distance, sd2.distance);
                if (distCompare != 0) {
                    return distCompare;
                }
                return sd1.station.getName().compareTo(sd2.station.getName());
            }
        });

        int baseCount = Math.min(n, distances.size());

        for (int i = 0; i < baseCount; i++) {
            StationWithDistance sd = distances.get(i);
            result.addResult(sd.station, sd.distance);
        }

        if (baseCount > 0 && baseCount < distances.size()) {
            StationWithDistance boundary = distances.get(baseCount - 1);
            double boundaryDistance = boundary.distance;
            Location boundaryLoc = boundary.station.getLocation();
            double boundaryLat = boundaryLoc.getLatitude();
            double boundaryLon = boundaryLoc.getLongitude();

            for (int i = baseCount; i < distances.size(); i++) {
                StationWithDistance sd = distances.get(i);

                if (Double.compare(sd.distance, boundaryDistance) != 0) {
                    break;
                }

                Location loc = sd.station.getLocation();
                if (loc != null &&
                        loc.getLatitude() == boundaryLat &&
                        loc.getLongitude() == boundaryLon) {
                    result.addResult(sd.station, sd.distance);
                }
            }
        }

        result.sortResults();

        long endNanos = System.nanoTime();
        double elapsedSeconds = (endNanos - startNanos) / 1e9;

        System.out.printf(
                "Proximity search finished. Target=(%.4f, %.4f), Found=%d/%d, Time=%.3f s%n",
                targetLat, targetLon, result.getResultCount(), n, elapsedSeconds
        );

        return result;
    }


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
