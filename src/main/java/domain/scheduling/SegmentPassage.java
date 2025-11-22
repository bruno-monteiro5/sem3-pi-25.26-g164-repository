package domain.scheduling;

import domain.railway.Endpoint;
import domain.railway.LineSegment;

import java.time.LocalDateTime;

/**
 * Represents the passage of a train through a line segment.
 *
 * <p>This class is now mutable to support schedule adjustments
 * when crossing operations cause delays.
 *
 * @author G164
 * @version 2.0
 */
public class SegmentPassage {
    private LineSegment segment;
    private Endpoint departureEndpoint;
    private Endpoint arrivalEndpoint;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int speedUsed; // km/h
    private boolean requiresStop;

    /**
     * Creates a new segment passage.
     *
     * @param segment The line segment
     * @param departureEndpoint Starting endpoint
     * @param arrivalEndpoint Ending endpoint
     * @param departureTime Departure time
     * @param arrivalTime Arrival time
     * @param speedUsed Speed used in km/h
     * @param requiresStop Whether train must stop at arrival
     */
    public SegmentPassage(LineSegment segment,
                          Endpoint departureEndpoint,
                          Endpoint arrivalEndpoint,
                          LocalDateTime departureTime,
                          LocalDateTime arrivalTime,
                          int speedUsed,
                          boolean requiresStop) {
        this.segment = segment;
        this.departureEndpoint = departureEndpoint;
        this.arrivalEndpoint = arrivalEndpoint;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.speedUsed = speedUsed;
        this.requiresStop = requiresStop;
    }

    // Getters
    public LineSegment getSegment() { return segment; }
    public Endpoint getDepartureEndpoint() { return departureEndpoint; }
    public Endpoint getArrivalEndpoint() { return arrivalEndpoint; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getSpeedUsed() { return speedUsed; }
    public boolean requiresStop() { return requiresStop; }

    /**
     * Sets a new departure time.
     * Used when crossing operations cause delays.
     *
     * @param newDepartureTime The new departure time
     */
    public void setDepartureTime(LocalDateTime newDepartureTime) {
        this.departureTime = newDepartureTime;
    }

    /**
     * Sets a new arrival time.
     * Used when crossing operations cause delays.
     *
     * @param newArrivalTime The new arrival time
     */
    public void setArrivalTime(LocalDateTime newArrivalTime) {
        this.arrivalTime = newArrivalTime;
    }

    /**
     * Applies a delay to both departure and arrival times.
     *
     * @param delayMinutes Delay in minutes (positive value)
     */
    public void applyDelay(long delayMinutes) {
        this.departureTime = this.departureTime.plusMinutes(delayMinutes);
        this.arrivalTime = this.arrivalTime.plusMinutes(delayMinutes);
    }

    /**
     * Calculates travel time for this passage in minutes.
     *
     * @return Travel time in minutes
     */
    public long getTravelTimeMinutes() {
        return java.time.Duration.between(departureTime, arrivalTime).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("%s → %s | Departure: %s | Arrival: %s | Speed: %d km/h%s",
                departureEndpoint.getName(),
                arrivalEndpoint.getName(),
                departureTime.toLocalTime(),
                arrivalTime.toLocalTime(),
                speedUsed,
                requiresStop ? " [STOP]" : "");
    }
}