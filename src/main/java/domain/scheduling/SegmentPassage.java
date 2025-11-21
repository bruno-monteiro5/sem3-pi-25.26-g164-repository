package domain.scheduling;

import domain.railway.Endpoint;
import domain.railway.LineSegment;

import java.time.LocalDateTime;

/**
 * Representa a passagem de um comboio por um segmento de linha
 */
public class SegmentPassage {
    private LineSegment segment;
    private Endpoint departureEndpoint;
    private Endpoint arrivalEndpoint;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int speedUsed; // km/h
    private boolean requiresStop;

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

    public LineSegment getSegment() { return segment; }
    public Endpoint getDepartureEndpoint() { return departureEndpoint; }
    public Endpoint getArrivalEndpoint() { return arrivalEndpoint; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public int getSpeedUsed() { return speedUsed; }
    public boolean requiresStop() { return requiresStop; }

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