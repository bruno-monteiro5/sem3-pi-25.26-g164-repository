package domain.scheduling;

import domain.railway.Endpoint;
import domain.railway.LineSegment;

import java.time.LocalDateTime;

/**
 * Represents an operation of crossing between two trains on a simple track
 */
public class CrossingOperation {

    public enum CrossingType {
        AT_STATION,     // Crossing in a station
        AT_SIDING       // Crossing in a siding
    }

    private int waitingTrainId;
    private int passingTrainId;
    private LineSegment segment;
    private Endpoint location;  // Station or position of the siding
    private CrossingType type;
    private LocalDateTime waitStartTime;
    private LocalDateTime waitEndTime;
    private int waitDurationMinutes;

    public CrossingOperation(int waitingTrainId,
                             int passingTrainId,
                             LineSegment segment,
                             Endpoint location,
                             CrossingType type,
                             LocalDateTime waitStartTime,
                             LocalDateTime waitEndTime) {
        this.waitingTrainId = waitingTrainId;
        this.passingTrainId = passingTrainId;
        this.segment = segment;
        this.location = location;
        this.type = type;
        this.waitStartTime = waitStartTime;
        this.waitEndTime = waitEndTime;
        this.waitDurationMinutes = (int) java.time.Duration.between(
                waitStartTime, waitEndTime).toMinutes();
    }

    public int getWaitingTrainId() { return waitingTrainId; }
    public int getPassingTrainId() { return passingTrainId; }
    public LineSegment getSegment() { return segment; }
    public Endpoint getLocation() { return location; }
    public CrossingType getType() { return type; }
    public LocalDateTime getWaitStartTime() { return waitStartTime; }
    public LocalDateTime getWaitEndTime() { return waitEndTime; }
    public int getWaitDurationMinutes() { return waitDurationMinutes; }

    @Override
    public String toString() {
        return String.format("CROSSING: Train %d waits at %s (%s) for Train %d | Wait: %d min (%s to %s)",
                waitingTrainId,
                location.getName(),
                type == CrossingType.AT_STATION ? "Station" : "Siding",
                passingTrainId,
                waitDurationMinutes,
                waitStartTime.toLocalTime(),
                waitEndTime.toLocalTime());
    }
}