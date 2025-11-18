package controller;

import domain.*;
import repository.StationRepository;

/**
 * Controller for building and querying the 2D-Tree (USEI07).
 */
public class Build2DTreeController {

    private StationRepository stationRepository;
    private KDTree2D kdTree;

    /**
     * Creates a controller using the singleton repository.
     */
    public Build2DTreeController() {
        this.stationRepository = StationRepository.getInstance();
        this.kdTree = new KDTree2D();
    }

    /**
     * Builds a balanced 2D-Tree using the existing AVL indexes from USEI06.
     *
     * @throws IllegalStateException if AVL indexes haven't been built yet
     */
    public void build2DTree() {
        if (!stationRepository.isLoaded()) {
            throw new IllegalStateException("AVL indexes must be built first (USEI06)");
        }

        long startNanos = System.nanoTime();

        StationIndexes stationIndexes = stationRepository.getStationIndexes();
        AVL<LatKey> latIndex = stationIndexes.getLatitudeIndex();
        AVL<LonKey> lonIndex = stationIndexes.getLongitudeIndex();

        kdTree.buildBalanced(latIndex, lonIndex);

        long endNanos = System.nanoTime();
        double elapsedSeconds = (endNanos - startNanos) / 1e9;

        KDTree2D.KDTreeStats stats = kdTree.getStatistics();

        System.out.printf(
                "2D-Tree build finished. Size=%d, Height=%d, Time=%.3f s%n",
                stats.getSize(),
                stats.getHeight(),
                elapsedSeconds
        );

        System.out.println("\nBucket size distribution:");
        for (var entry : stats.getBucketDistribution().entrySet()) {
            System.out.printf("  %d station(s) per node: %d nodes%n",
                    entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the requested geographical search
     */

    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, kdTree.getRoot(), s);
    }

    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, KDNode node, String s) {
        for(Station station : node.getStations()){
            if(station.getLocation().getLatitude() >= minLatitude && station.getLocation().getLatitude() <= maxLatitude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude){
                s += station.toString();
            }
        }
        if(node.getLeft()!= null){
            return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, node.getLeft(), s);
        }
        if(node.getRight()!= null){
            return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, node.getRight(), s);
        }
        return s;
    }

    /**
     * Return the requested geographical search with the extra filters
     */

    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int isCity, int isMain, String country) {
        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, isCity, isMain, country, s, kdTree.getRoot());
    }

    public String searchInArea(double minLatitude, double maxLatiude, double minLongitude, double maxLongitude, int isCityNum, int isMainNum, String country, String s, KDNode node){
        boolean isCityDelimited = (isCityNum == 1 || isCityNum == 0);
        boolean isMainDelimited = (isMainNum == 1 || isMainNum == 0);
        boolean isCity = false;
        boolean isMain = false;
        if(isCityDelimited){
            isCity = (isCityNum == 0);
        }
        if(isMainDelimited){
            isMain = (isMainNum == 0);
        }
        for(Station station : node.getStations()) {
            if (isCityDelimited && isMainDelimited && !country.equals("all")) {
                if (station.getLocation().getLatitude() >= minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isCity() == isCity && station.isMainStation() == isMain && station.getCountry().toLowerCase().equals(country)) {
                    s += station.toString();
                }
            }else if(isCityDelimited && isMainDelimited && country.equals("all")){
                if(station.getLocation().getLatitude() >=  minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isCity() == isCity && station.isMainStation() == isMain){
                    s += station.toString();
                }
            } else if(isCityDelimited && !country.equals("all")){
                if(station.getLocation().getLatitude() >=  minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isCity() == isCity && station.getCountry().toLowerCase().equals(country)){
                    s += station.toString();
                }
            } else if(isCityDelimited && country.equals("all")){
                if(station.getLocation().getLatitude() >=  minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isCity() == isCity){
                    s += station.toString();
                }
            } else if(isMainDelimited && !country.equals("all")){
                if(station.getLocation().getLatitude() >=  minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isMainStation() == isMain && station.getCountry().toLowerCase().equals(country)){
                    s += station.toString();
                }
            } else{
                if(station.getLocation().getLatitude() >=  minLatitude && station.getLocation().getLatitude() <= maxLatiude && station.getLocation().getLongitude() >= minLongitude && station.getLocation().getLongitude() <= maxLongitude && station.isMainStation() == isMain){
                    s += station.toString();
                }
            }
        }
        if(node.getLeft()!= null){
            return searchInArea(minLatitude, maxLatiude, minLongitude, maxLongitude, isCityNum, isMainNum, country, s, node.getLeft());
        }
        if(node.getRight()!= null){
            return searchInArea(minLatitude, maxLatiude, minLongitude, maxLongitude, isCityNum, isMainNum, country, s, node.getRight());
        }
        return s;
    }

    /**
     * Returns the built 2D-Tree.
     */
    public KDTree2D getKDTree() {
        return kdTree;
    }

    /**
     * Returns statistics about the 2D-Tree.
     */
    public KDTree2D.KDTreeStats getStatistics() {
        return kdTree.getStatistics();
    }

    /**
     * Checks if the 2D-Tree has been built.
     */
    public boolean isBuilt() {
        return kdTree.size() > 0;
    }
}