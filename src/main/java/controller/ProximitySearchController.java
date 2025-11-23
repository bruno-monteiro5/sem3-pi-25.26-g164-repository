package controller;

import domain.*;
import repository.KDTreeRepository;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        ProximitySearchResult result = new ProximitySearchResult(targetLat, targetLon, n, filter);
        KDTree2D tree = kdTreeRepository.getKDTree();

        List<Station> candidates = KDTreeUtil.collectAllStations(tree);

        List<StationWithDistance> distances = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Station station = candidates.get(i);
            if (filter.matches(station)) {
                double distance = HaversineDistance.distanceToStation(targetLat, targetLon, station);
                distances.add(new StationWithDistance(station, distance));
            }
        }

        if (distances.isEmpty()) {
            System.out.printf(
                    "Proximity search finished. Target=(%.4f, %.4f), Found=0/%d%n",
                    targetLat, targetLon, n
            );
            return result;
        }

        Collections.sort(distances);

        int baseCount = Math.min(n, distances.size());
        for (int i = 0; i < baseCount; i++) {
            StationWithDistance sd = distances.get(i);
            result.addResult(sd.station, sd.distance);
        }

        // 5) Incluir empates à distância N se tiverem as mesmas coordenadas
        if (baseCount > 0 && baseCount < distances.size()) {
            StationWithDistance boundary = distances.get(baseCount - 1);
            double boundaryDistance = boundary.distance;
            Location boundaryLoc = boundary.station.getLocation();
            double boundaryLat = boundaryLoc.getLatitude();
            double boundaryLon = boundaryLoc.getLongitude();

            for (int i = baseCount; i < distances.size(); i++) {
                StationWithDistance sd = distances.get(i);

                if (Double.compare(sd.distance, boundaryDistance) == 0) {
                    Location loc = sd.station.getLocation();
                    if (loc != null &&
                            loc.getLatitude() == boundaryLat &&
                            loc.getLongitude() == boundaryLon) {
                        result.addResult(sd.station, sd.distance);
                    }
                }
            }
        }

        result.sortResults();

        System.out.printf(
                "Proximity search finished. Target=(%.4f, %.4f), Found=%d/%d%n",
                targetLat, targetLon, result.getResultCount(), n
        );

        return result;
    }



    /**
     * Helper class to pair a station with its calculated distance.
     * Natural ordering: distance ASC, then station name ASC.
     */
    private static class StationWithDistance implements Comparable<StationWithDistance> {
        final Station station;
        final double distance;

        StationWithDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }

        @Override
        public int compareTo(StationWithDistance other) {
            int distCompare = Double.compare(this.distance, other.distance);
            if (distCompare != 0) {
                return distCompare;
            }

            String name1 = this.station.getName();
            String name2 = other.station.getName();
            return name1.compareTo(name2);
        }
    }
}
