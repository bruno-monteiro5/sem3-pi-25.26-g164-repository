package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Node for a 2D-Tree (KD-Tree with k=2).
 * Each node splits on either latitude (axis=0) or longitude (axis=1).
 * Supports multiple stations at the same coordinates (bucket).
 */
public class KDNode {

    /** Splitting axis: 0 = latitude, 1 = longitude */
    private int axis;

    /** List of stations at this node (bucket for duplicate coordinates) */
    private List<Station> stations;

    /** Left child (values < split) */
    private KDNode left;

    /** Right child (values >= split) */
    private KDNode right;

    /**
     * Creates a new KD-Tree node.
     *
     * @param axis splitting axis (0=latitude, 1=longitude)
     * @param stations list of stations at this coordinate
     */
    public KDNode(int axis, List<Station> stations) {
        this.axis = axis;
        this.stations = new ArrayList<>(stations);
        // Ensure stations are sorted by name
        Collections.sort(this.stations, Comparator.comparing(Station::getName));
        this.left = null;
        this.right = null;
    }

    public int getAxis() {
        return axis;
    }

    public List<Station> getStations() {
        return stations;
    }

    public KDNode getLeft() {
        return left;
    }

    public void setLeft(KDNode left) {
        this.left = left;
    }

    public KDNode getRight() {
        return right;
    }

    public void setRight(KDNode right) {
        this.right = right;
    }

    /**
     * Returns the coordinate value for this node's axis.
     */
    public double getCoordinate() {
        if (stations.isEmpty()) return 0.0;
        Station first = stations.get(0);
        return axis == 0
                ? first.getLocation().getLatitude()
                : first.getLocation().getLongitude();
    }
}