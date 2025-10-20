package Classes;

public class LineSegment {
    private final Endpoint startPoint; //ainda para ver
    private final Endpoint endPoint; //ainda para ver
    private final double length;
    private final double gaugeWidth;
    private final double maxWeight;
    private final double speedLimit;


    public LineSegment(Endpoint startPoint, Endpoint endPoint, double length, double gaugeWidth, double maxWeight, double speedLimit) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.length = length;
        this.gaugeWidth = gaugeWidth;
        this.maxWeight = maxWeight;
        this.speedLimit = speedLimit;
    }

    public Endpoint getStartPoint() {
        return startPoint;
    }

    public Endpoint getEndPoint() {
        return endPoint;
    }

    public double getLength() {
        return length;
    }

    public double getGaugeWidth() {
        return gaugeWidth;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }


}
