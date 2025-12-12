package domain;

/**
 * Represents the centrality metrics for a railway station hub.
 * <p>
 * This class stores normalized metrics used to identify the most
 * important stations (hubs) in a railway network.
 * All scores are normalized to [0,1] per connected component.
 */
public class HubScore {

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

    public Station getStation() {
        return station;
    }

    public int getDegree() {
        return degree;
    }

    public double getStrength() {
        return strength;
    }

    public double getBetweenness() {
        return betweenness;
    }

    public double getHarmonicCloseness() {
        return harmonicCloseness;
    }

    public double getHubScore() {
        return hubScore;
    }

    public String toString() {
        return "Station: " + station.getName() + " (ID: " + station.getId() + ")" +
                " | Degree: " + degree +
                " | Strength: " + String.format("%.2f", strength) +
                " | Betweenness: " + String.format("%.4f", betweenness) +
                " | Harmonic: " + String.format("%.4f", harmonicCloseness) +
                " | HubScore: " + String.format("%.4f", hubScore);
    }
}