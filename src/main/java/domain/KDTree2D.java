package domain;

import java.util.*;

/**
 * 2D-Tree (KD-Tree with k=2) for efficient spatial queries on railway stations.
 * Built using balanced construction from pre-sorted AVL indexes.
 */
public class KDTree2D {

    private KDNode root;
    private int size;

    public KDTree2D() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Builds a balanced 2D-Tree from latitude and longitude AVL indexes.
     * Uses the in-order traversal (sorted) from both indexes to efficiently construct the tree.
     *
     * @param latitudeIndex AVL tree sorted by (latitude, name)
     * @param longitudeIndex AVL tree sorted by (longitude, name)
     */
    public void buildBalanced(AVL<LatKey> latitudeIndex, AVL<LonKey> longitudeIndex) {
        // Extract all stations from latitude index (arbitrary choice)
        List<Station> allStations = new ArrayList<>();
        for (LatKey key : latitudeIndex.inOrder()) {
            allStations.add(key.getStation());
        }

        if (allStations.isEmpty()) {
            this.root = null;
            this.size = 0;
            return;
        }

        // Build balanced tree starting with latitude axis (0)
        this.root = buildBalancedRecursive(allStations, 0);
        this.size = allStations.size();
    }

    /**
     * Recursively builds a balanced 2D-Tree.
     *
     * @param stations list of stations to include in this subtree
     * @param depth current depth (determines axis: depth % 2)
     * @return root node of the subtree
     */
    private KDNode buildBalancedRecursive(List<Station> stations, int depth) {
        if (stations == null || stations.isEmpty()) {
            return null;
        }

        // Determine axis based on depth (alternates between lat and lon)
        int axis = depth % 2; // 0 = latitude, 1 = longitude

        // Sort stations by the current axis
        final int currentAxis = axis;
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(Station s1, Station s2) {
                double coord1 = currentAxis == 0
                        ? s1.getLocation().getLatitude()
                        : s1.getLocation().getLongitude();
                double coord2 = currentAxis == 0
                        ? s2.getLocation().getLatitude()
                        : s2.getLocation().getLongitude();

                int cmp = Double.compare(coord1, coord2);
                if (cmp != 0) return cmp;
                return s1.getName().compareTo(s2.getName());
            }
        });

        // Find median index
        int medianIdx = stations.size() / 2;

        // Group stations with same coordinates as the median (bucket)
        List<Station> bucket = new ArrayList<>();
        double medianCoord = axis == 0
                ? stations.get(medianIdx).getLocation().getLatitude()
                : stations.get(medianIdx).getLocation().getLongitude();

        List<Station> leftStations = new ArrayList<>();
        List<Station> rightStations = new ArrayList<>();

        for (Station station : stations) {
            double coord = axis == 0
                    ? station.getLocation().getLatitude()
                    : station.getLocation().getLongitude();

            if (Math.abs(coord - medianCoord) < 1e-9) {
                // Same coordinate - add to bucket
                bucket.add(station);
            } else if (coord < medianCoord) {
                leftStations.add(station);
            } else {
                rightStations.add(station);
            }
        }

        // Sort bucket by name
        Collections.sort(bucket, Comparator.comparing(Station::getName));

        // Create node with bucket
        KDNode node = new KDNode(axis, bucket);

        // Recursively build left and right subtrees
        node.setLeft(buildBalancedRecursive(leftStations, depth + 1));
        node.setRight(buildBalancedRecursive(rightStations, depth + 1));

        return node;
    }

    /**
     * Returns the root node of the tree.
     */
    public KDNode getRoot() {
        return root;
    }

    /**
     * Returns the total number of stations in the tree.
     */
    public int size() {
        return size;
    }

    /**
     * Calculates the height of the tree.
     */
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(KDNode node) {
        if (node == null) return -1;
        return 1 + Math.max(heightRecursive(node.getLeft()), heightRecursive(node.getRight()));
    }

    /**
     * Returns a map of bucket sizes (how many stations share the same coordinates).
     * Key: bucket size, Value: count of buckets with that size
     */
    public Map<Integer, Integer> getBucketSizeDistribution() {
        Map<Integer, Integer> distribution = new TreeMap<>();
        collectBucketSizes(root, distribution);
        return distribution;
    }

    private void collectBucketSizes(KDNode node, Map<Integer, Integer> distribution) {
        if (node == null) return;

        int bucketSize = node.getStations().size();
        distribution.put(bucketSize, distribution.getOrDefault(bucketSize, 0) + 1);

        collectBucketSizes(node.getLeft(), distribution);
        collectBucketSizes(node.getRight(), distribution);
    }

    /**
     * Returns statistics about the tree structure.
     */
    public KDTreeStats getStatistics() {
        return new KDTreeStats(size(), height(), getBucketSizeDistribution());
    }

    /**
     * Value object containing 2D-Tree statistics.
     */
    public static class KDTreeStats {
        private final int size;
        private final int height;
        private final Map<Integer, Integer> bucketDistribution;

        public KDTreeStats(int size, int height, Map<Integer, Integer> bucketDistribution) {
            this.size = size;
            this.height = height;
            this.bucketDistribution = new TreeMap<>(bucketDistribution);
        }

        public int getSize() {
            return size;
        }

        public int getHeight() {
            return height;
        }

        public Map<Integer, Integer> getBucketDistribution() {
            return bucketDistribution;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("2D-Tree Statistics:\n");
            sb.append(String.format("  Total stations: %d%n", size));
            sb.append(String.format("  Tree height: %d%n", height));
            sb.append("  Bucket distribution:\n");

            for (Map.Entry<Integer, Integer> entry : bucketDistribution.entrySet()) {
                sb.append(String.format("    %d station(s) per coordinate: %d bucket(s)%n",
                        entry.getKey(), entry.getValue()));
            }

            return sb.toString();
        }
    }
}