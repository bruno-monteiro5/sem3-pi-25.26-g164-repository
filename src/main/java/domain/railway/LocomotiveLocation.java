package domain.railway;

import java.time.LocalDateTime;

/**
 * Represents the current location of a locomotive.
 * Maps to Locomotive_Current_Location table.
 *
 * @author G164
 * @version 1.0 - USLP09/10
 */
public class LocomotiveLocation {

    public enum Status {
        /** Free - available for assignment */
        F,
        /** In Transit - currently on a train */
        T
    }

    private int locomotiveId;
    private Endpoint currentEndpoint;
    private Status status;
    private LocalDateTime arrival;
    private Integer trainId; // null if status = F

    public LocomotiveLocation(int locomotiveId, Endpoint currentEndpoint,
                              Status status, LocalDateTime arrival) {
        this.locomotiveId = locomotiveId;
        this.currentEndpoint = currentEndpoint;
        this.status = status;
        this.arrival = arrival;
    }

    // Getters
    public int getLocomotiveId() { return locomotiveId; }
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

    /**
     * Calculates distance from a given endpoint (for sorting in USLP09).
     *
     * @param target Target endpoint
     * @return Estimated distance in km (simplified - could use graph algorithms)
     */
    public double distanceFrom(Endpoint target) {
        // Simplified: in real scenario, would calculate graph distance
        // For now, just return 0 if same endpoint, else arbitrary value
        return currentEndpoint.getId() == target.getId() ? 0.0 : 100.0;
    }

    @Override
    public String toString() {
        String statusStr = status == Status.F ? "FREE" : "IN TRANSIT";
        String trainInfo = trainId != null ? " (Train " + trainId + ")" : "";
        return String.format("Locomotive %d @ %s [%s]%s - arrived %s",
                locomotiveId,
                currentEndpoint.getName(),
                statusStr,
                trainInfo,
                arrival.toLocalTime());
    }
}