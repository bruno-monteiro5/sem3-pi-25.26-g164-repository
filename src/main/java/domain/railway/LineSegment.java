package domain.railway;

public class LineSegment {
    private int id;
    private boolean isElectrified;
    private int speedLimit;
    private int maxWeight;
    private int length; // em metros
    private int lineId;

    public LineSegment(int id, boolean isElectrified, int speedLimit,
                       int maxWeight, int length, int lineId) {
        this.id = id;
        this.isElectrified = isElectrified;
        this.speedLimit = speedLimit;
        this.maxWeight = maxWeight;
        this.length = length;
        this.lineId = lineId;
    }

    // Getters
    public int getId() { return id; }
    public boolean isElectrified() { return isElectrified; }
    public int getSpeedLimit() { return speedLimit; }
    public int getMaxWeight() { return maxWeight; }
    public int getLength() { return length; }

    @Override
    public String toString() {
        return String.format("Segment %d: %dm, %dkm/h, Electrified: %s",
                id, length, speedLimit, isElectrified ? "Yes" : "No");
    }
}