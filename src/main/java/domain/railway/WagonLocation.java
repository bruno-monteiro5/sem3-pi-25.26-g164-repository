package domain.railway;

import java.time.LocalDateTime;

/**
 * Represents the current location of a wagon.
 * Maps to Wagon_Current_Location table.
 *
 */
public class WagonLocation {

    public enum Status {
        /** Free - available for loading into freight */
        F,
        /** In Transit - currently on a train */
        T
    }

    private int wagonId;
    private Endpoint currentEndpoint;
    private Status status;
    private LocalDateTime arrival;
    private Integer trainId; // null if status = F

    public WagonLocation(int wagonId, Endpoint currentEndpoint,
                         Status status, LocalDateTime arrival) {
        this.wagonId = wagonId;
        this.currentEndpoint = currentEndpoint;
        this.status = status;
        this.arrival = arrival;
    }

    // Getters
    public int getWagonId() { return wagonId; }
    public Endpoint getCurrentEndpoint() { return currentEndpoint; }
    public Status getStatus() { return status; }
    public LocalDateTime getArrival() { return arrival; }
    public Integer getTrainId() { return trainId; }

    // Setters
    public void setStatus(Status status) { this.status = status; }
    public void setCurrentEndpoint(Endpoint endpoint) { this.currentEndpoint = endpoint; }
    public void setArrival(LocalDateTime arrival) { this.arrival = arrival; }
    public void setTrainId(Integer trainId) { this.trainId = trainId; }

    public boolean isFree() { return status == Status.F; }
    public boolean isInTransit() { return status == Status.T; }

    public double distanceFrom(Endpoint target) {
        return currentEndpoint.getId() == target.getId() ? 0.0 : 100.0;
    }

    @Override
    public String toString() {
        String statusStr = status == Status.F ? "FREE" : "IN TRANSIT";
        String trainInfo = trainId != null ? " (Train " + trainId + ")" : "";
        return String.format("Wagon %d @ %s [%s]%s - arrived %s",
                wagonId,
                currentEndpoint.getName(),
                statusStr,
                trainInfo,
                arrival.toLocalTime());
    }
}