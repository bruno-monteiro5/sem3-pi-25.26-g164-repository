package controller;

import domain.*;
import repository.StationRepository;

/**
 * Controller for building and querying the 2D-Tree (USEI07).
 */
public class Build2DTreeController {

    private StationRepository stationRepository;
    private KDTree2D kdTree;

    public Build2DTreeController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        this.kdTree = new KDTree2D();
    }

    /**
     * Builds a balanced 2D-Tree using the existing AVL indexes from USEI06.
     *
     * @param stationIndexes the indexes built in USEI06
     */
    public void build2DTree(StationIndexes stationIndexes) {
        long startNanos = System.nanoTime();

        // Access the internal AVL trees from StationIndexes
        // Note: You'll need to add getter methods to StationIndexes to expose the AVL trees
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
}