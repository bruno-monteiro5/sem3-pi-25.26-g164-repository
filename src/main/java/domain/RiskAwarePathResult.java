package domain;

import java.util.List;

/**
 * Result object for USEI15 - Risk-Aware Shortest Paths.
 * <p>
 * Contains either a valid shortest path with its cost, or information
 * about a negative cycle that was detected in the network.
 */
public class RiskAwarePathResult {

    private final boolean hasNegativeCycle;
    private final Station source;
    private final Station target;
    private final List<Station> path;
    private final double totalCost;
    private final List<Station> negativeCycle;

    /**
     * Constructor for a valid path result (no negative cycle).
     *
     * @param source the source station
     * @param target the target station
     * @param path the shortest path from source to target
     * @param totalCost the total cost of the path
     */
    public RiskAwarePathResult(Station source, Station target,
                               List<Station> path, double totalCost) {
        this.hasNegativeCycle = false;
        this.source = source;
        this.target = target;
        this.path = path;
        this.totalCost = totalCost;
        this.negativeCycle = null;
    }

    /**
     * Constructor for a negative cycle result.
     *
     * @param negativeCycle the list of stations forming the negative cycle
     */
    public RiskAwarePathResult(List<Station> negativeCycle) {
        this.hasNegativeCycle = true;
        this.source = null;
        this.target = null;
        this.path = null;
        this.totalCost = 0.0;
        this.negativeCycle = negativeCycle;
    }

    /**
     * Returns whether a negative cycle was detected.
     *
     * @return true if negative cycle exists, false otherwise
     */
    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    /**
     * Returns the source station of the query.
     *
     * @return source station, or null if negative cycle detected
     */
    public Station getSource() {
        return source;
    }

    /**
     * Returns the target station of the query.
     *
     * @return target station, or null if negative cycle detected
     */
    public Station getTarget() {
        return target;
    }

    /**
     * Returns the shortest path from source to target.
     *
     * @return list of stations in the path, or null if no path exists or negative cycle
     */
    public List<Station> getPath() {
        return path;
    }

    /**
     * Returns the total cost of the path.
     * Can be negative if the path uses highly preferred links.
     *
     * @return total cost, or 0.0 if negative cycle or no path
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Returns the stations involved in the negative cycle.
     *
     * @return list of stations in the cycle, or null if no cycle
     */
    public List<Station> getNegativeCycle() {
        return negativeCycle;
    }

    /**
     * Checks if the target is reachable from the source.
     *
     * @return true if a valid path exists, false otherwise
     */
    public boolean isReachable() {
        return !hasNegativeCycle && path != null && !path.isEmpty();
    }

    @Override
    public String toString() {
        if (hasNegativeCycle) {
            return "RiskAwarePathResult{negative cycle detected with " +
                    negativeCycle.size() + " stations}";
        } else if (path == null || path.isEmpty()) {
            return "RiskAwarePathResult{no path found from " + source.getName() +
                    " to " + target.getName() + "}";
        } else {
            return "RiskAwarePathResult{path length=" + path.size() +
                    ", total cost=" + totalCost + "}";
        }
    }
}