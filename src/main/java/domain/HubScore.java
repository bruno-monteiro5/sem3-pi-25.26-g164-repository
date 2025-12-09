package domain;

/**
 * Represents the centrality metrics for a railway station hub.
 * <p>
 * This class stores normalized metrics used to identify the most
 * important stations (hubs) in a railway network.
 * All scores are normalized to [0,1] per connected component.
 */
public class HubScore implements Comparable<HubScore> {

    private final Station station;
    private final int degree;
    private final double strength;
    private final double betweenness;
    private final double harmonicCloseness;
    private final double hubScore;

    /**
     * Creates a new HubScore with all centrality metrics.
     *
     * @param station the railway station
     * @param degree number of direct connections
     * @param strength sum of edge weights
     * @param betweenness normalized betweenness centrality [0,1]
     * @param harmonicCloseness normalized harmonic closeness [0,1]
     * @param hubScore combined weighted score [0,1]
     */
    public HubScore(Station station, int degree, double strength,
                    double betweenness, double harmonicCloseness,
                    double hubScore) {
        this.station = station;
        this.degree = degree;
        this.strength = strength;
        this.betweenness = betweenness;
        this.harmonicCloseness = harmonicCloseness;
        this.hubScore = hubScore;
    }

    public Station getStation() { return station; }
    public int getDegree() { return degree; }
    public double getStrength() { return strength; }
    public double getBetweenness() { return betweenness; }
    public double getHarmonicCloseness() { return harmonicCloseness; }
    public double getHubScore() { return hubScore; }

    /**
     * Compares by hubScore descending (higher scores first).
     */
    @Override
    public int compareTo(HubScore other) {
        return Double.compare(other.hubScore, this.hubScore);
    }

    @Override
    public String toString() {
        return String.format(
                "Station: %s (ID: %d) | Degree: %d | Strength: %.2f | " +
                        "Betweenness: %.4f | Harmonic: %.4f | HubScore: %.4f",
                station.getName(), station.getId(), degree, strength,
                betweenness, harmonicCloseness, hubScore
        );
    }
}
