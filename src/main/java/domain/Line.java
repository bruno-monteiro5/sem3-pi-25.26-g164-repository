package domain;

public class Line {

    private final Station origin;
    private final Station destination;
    private final double distance;
    private final int capacity;
    private final double cost;

    public Line(Station origin,
                Station destination,
                double distance,
                int capacity,
                double cost) {

        if (origin == null || destination == null)
            throw new IllegalArgumentException("origin/destination is null");

        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.capacity = capacity;
        this.cost = cost;
    }

    public Station getOrigin() {
        return origin;
    }

    public Station getDestination() {
        return destination;
    }

    public double getDistance() {
        return distance;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("Line %s -> %s (dist=%.2f, cap=%d, cost=%.2f)",
                origin.getName(), destination.getName(), distance, capacity, cost);
    }
}
