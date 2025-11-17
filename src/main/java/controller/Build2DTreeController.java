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