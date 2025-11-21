package domain.railway;

public class Line {
    private int id;
    private String name;
    private String owner;
    private int endpointA;
    private int endpointB;
    private int gauge;
    private String endpointAName;
    private String endpointBName;

    public Line(int id, String name, String owner, int endpointA, int endpointB,
                int gauge, String endpointAName, String endpointBName) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.endpointA = endpointA;
        this.endpointB = endpointB;
        this.gauge = gauge;
        this.endpointAName = endpointAName;
        this.endpointBName = endpointBName;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getGauge() { return gauge; }
    public String getEndpointAName() { return endpointAName; }
    public String getEndpointBName() { return endpointBName; }
    public int getEndpointA() { return endpointA; }
    public int getEndpointB() { return endpointB; }

    @Override
    public String toString() {
        return String.format("%s: %s → %s (Gauge: %dmm)",
                name, endpointAName, endpointBName, gauge);
    }
}