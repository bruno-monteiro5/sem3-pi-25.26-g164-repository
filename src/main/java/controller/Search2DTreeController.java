package controller;

import domain.KDNode;
import domain.KDTree2D;
import domain.Station;
import repository.KDTreeRepository;

public class Search2DTreeController {
    public KDTreeRepository kdTreeRepository = KDTreeRepository.getInstance();

    public Search2DTreeController() {
        this.kdTreeRepository = KDTreeRepository.getInstance();
    }

    /**
     * Returns the requested geographical search
     * Complexity: O(√n + k) average case with pruning, where k is the number of results
     */
    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        KDTree2D kdTree = kdTreeRepository.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            return "2D-Tree not built yet. Please build it first.";
        }

        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, kdTree.getRoot(), s, 0);
    }

    /**
     * Recursive search with KD-Tree pruning
     * @param depth Current depth in tree (0 = latitude split, 1 = longitude split, alternating)
     */
    private String searchInArea(double minLatitude, double maxLatitude,
                                double minLongitude, double maxLongitude,
                                KDNode node, String s, int depth) {
        if (node == null) {
            return s;
        }

        // Check all stations in current node
        for(Station station : node.getStations()){
            if(station.getLocation().getLatitude() >= minLatitude &&
                    station.getLocation().getLatitude() <= maxLatitude &&
                    station.getLocation().getLongitude() >= minLongitude &&
                    station.getLocation().getLongitude() <= maxLongitude){
                s += station.toString();
                System.out.println(station.toString());
            }
        }

        // Determine split dimension (even depth = latitude, odd depth = longitude)
        boolean splitByLatitude = (depth % 2 == 0);

        // Get split value from first station in node
        if (node.getStations().isEmpty()) {
            // If no stations, just recurse both sides
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, node.getLeft(), s, depth + 1);
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, node.getRight(), s, depth + 1);
            return s;
        }

        double splitValue;
        if (splitByLatitude) {
            splitValue = node.getStations().get(0).getLocation().getLatitude();
        } else {
            splitValue = node.getStations().get(0).getLocation().getLongitude();
        }

        // PRUNING LOGIC: Only visit subtrees that can contain points in the search region
        if (splitByLatitude) {
            // Split by latitude
            if (minLatitude <= splitValue && node.getLeft() != null) {
                // Left subtree can contain points in range
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        node.getLeft(), s, depth + 1);
            }
            if (maxLatitude >= splitValue && node.getRight() != null) {
                // Right subtree can contain points in range
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        node.getRight(), s, depth + 1);
            }
        } else {
            // Split by longitude
            if (minLongitude <= splitValue && node.getLeft() != null) {
                // Left subtree can contain points in range
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        node.getLeft(), s, depth + 1);
            }
            if (maxLongitude >= splitValue && node.getRight() != null) {
                // Right subtree can contain points in range
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        node.getRight(), s, depth + 1);
            }
        }

        return s;
    }

    /**
     * Return the requested geographical search with the extra filters
     * Complexity: O(√n + k) average case with pruning, where k is the number of results
     */
    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude,
                               double maxLongitude, int isCity, int isMain, String country) {
        KDTree2D kdTree = kdTreeRepository.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            return "2D-Tree not built yet. Please build it first.";
        }

        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                isCity, isMain, country, s, kdTree.getRoot(), 0);
    }

    private String searchInArea(double minLatitude, double maxLatitude, double minLongitude,
                                double maxLongitude, int isCityNum, int isMainNum, String country,
                                String s, KDNode node, int depth){
        if (node == null) {
            return s;
        }

        // Determine filter requirements
        boolean filterCity = (isCityNum == 0 || isCityNum == 1);
        boolean filterMain = (isMainNum == 0 || isMainNum == 1);
        boolean filterCountry = !country.equals("all");

        boolean expectedCity = (isCityNum == 1); // 1 = true (is city), 0 = false (not city)
        boolean expectedMain = (isMainNum == 1); // 1 = true (is main), 0 = false (not main)

        // Check all stations in current node
        for(Station station : node.getStations()) {
            // Check geographic bounds
            if (station.getLocation().getLatitude() < minLatitude ||
                    station.getLocation().getLatitude() > maxLatitude ||
                    station.getLocation().getLongitude() < minLongitude ||
                    station.getLocation().getLongitude() > maxLongitude) {
                continue; // Skip this station
            }

            // Apply optional filters
            if (filterCity && station.isCity() != expectedCity) {
                continue;
            }
            if (filterMain && station.isMainStation() != expectedMain) {
                continue;
            }
            if (filterCountry && !station.getCountry().toLowerCase().equals(country.toLowerCase())) {
                continue;
            }

            // Station passes all filters
            s += station.toString();
            System.out.println(station.toString());
        }

        // PRUNING LOGIC: Same as above
        boolean splitByLatitude = (depth % 2 == 0);

        if (node.getStations().isEmpty()) {
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                    isCityNum, isMainNum, country, s, node.getLeft(), depth + 1);
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                    isCityNum, isMainNum, country, s, node.getRight(), depth + 1);
            return s;
        }

        double splitValue;
        if (splitByLatitude) {
            splitValue = node.getStations().get(0).getLocation().getLatitude();
        } else {
            splitValue = node.getStations().get(0).getLocation().getLongitude();
        }

        if (splitByLatitude) {
            if (minLatitude <= splitValue && node.getLeft() != null) {
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        isCityNum, isMainNum, country, s, node.getLeft(), depth + 1);
            }
            if (maxLatitude >= splitValue && node.getRight() != null) {
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        isCityNum, isMainNum, country, s, node.getRight(), depth + 1);
            }
        } else {
            if (minLongitude <= splitValue && node.getLeft() != null) {
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        isCityNum, isMainNum, country, s, node.getLeft(), depth + 1);
            }
            if (maxLongitude >= splitValue && node.getRight() != null) {
                s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                        isCityNum, isMainNum, country, s, node.getRight(), depth + 1);
            }
        }

        return s;
    }
}