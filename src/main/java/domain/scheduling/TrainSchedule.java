package domain.scheduling;

import domain.railway.Train;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete schedule of a specific train.
 *
 * <p><b>Version 2.0 changes:</b>
 * <ul>
 *   <li>Added updateTotalTravelTime() to recalculate after delays</li>
 *   <li>Improved delay tracking</li>
 * </ul>
 *
 * @author G164
 * @version 2.0
 */
public class TrainSchedule {
    private Train train;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedArrival;
    private List<SegmentPassage> passages;
    private List<CrossingOperation> crossingOperations;
    private long totalTravelTimeMinutes;
    private long totalWaitTimeMinutes;

    /**
     * Creates a new train schedule.
     *
     * @param train The train
     * @param scheduledDeparture Scheduled departure time
     */
    public TrainSchedule(Train train, LocalDateTime scheduledDeparture) {
        this.train = train;
        this.scheduledDeparture = scheduledDeparture;
        this.passages = new ArrayList<>();
        this.crossingOperations = new ArrayList<>();
        this.totalTravelTimeMinutes = 0;
        this.totalWaitTimeMinutes = 0;
    }

    /**
     * Adds a passage to this schedule.
     *
     * @param passage The segment passage to add
     */
    public void addPassage(SegmentPassage passage) {
        this.passages.add(passage);
        updateEstimatedArrival();
    }

    /**
     * Adds a crossing operation to this schedule.
     * Also updates the total wait time.
     *
     * @param crossing The crossing operation
     */
    public void addCrossingOperation(CrossingOperation crossing) {
        this.crossingOperations.add(crossing);
        this.totalWaitTimeMinutes += crossing.getWaitDurationMinutes();
    }

    /**
     * Updates the estimated arrival time based on the last passage.
     * Called automatically when passages are added.
     */
    private void updateEstimatedArrival() {
        if (!passages.isEmpty()) {
            SegmentPassage lastPassage = passages.get(passages.size() - 1);
            this.estimatedArrival = lastPassage.getArrivalTime();
            this.totalTravelTimeMinutes = java.time.Duration.between(
                    scheduledDeparture, estimatedArrival).toMinutes();
        }
    }

    /**
     * Recalculates total travel time based on current passages.
     *
     * <p><b>NEW in v2.0:</b> This method should be called after applying
     * delays from crossing operations to update the total travel time.
     *
     * <p>Usage:
     * <pre>
     * schedule.getPassages().get(5).applyDelay(15);
     * schedule.updateTotalTravelTime();  // Recalculate
     * </pre>
     */
    public void updateTotalTravelTime() {
        if (passages.isEmpty()) {
            this.totalTravelTimeMinutes = 0;
            this.estimatedArrival = scheduledDeparture;
            return;
        }

        // Get actual times from first and last passage
        SegmentPassage firstPassage = passages.get(0);
        SegmentPassage lastPassage = passages.get(passages.size() - 1);

        LocalDateTime actualDeparture = firstPassage.getDepartureTime();
        LocalDateTime actualArrival = lastPassage.getArrivalTime();

        this.estimatedArrival = actualArrival;
        this.totalTravelTimeMinutes = java.time.Duration.between(
                actualDeparture, actualArrival).toMinutes();
    }

    // Getters
    public Train getTrain() { return train; }
    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public List<SegmentPassage> getPassages() { return passages; }
    public List<CrossingOperation> getCrossingOperations() { return crossingOperations; }
    public long getTotalTravelTimeMinutes() { return totalTravelTimeMinutes; }
    public long getTotalWaitTimeMinutes() { return totalWaitTimeMinutes; }

    /**
     * Calculates effective travel time (excluding waits at crossings).
     *
     * @return Effective travel time in minutes
     */
    public long getEffectiveTravelTimeMinutes() {
        return totalTravelTimeMinutes - totalWaitTimeMinutes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(63)).append("\n");
        sb.append(String.format("TRAIN SCHEDULE: %d\n", train.getId()));
        sb.append("═".repeat(63)).append("\n");
        sb.append(String.format("Route: %s → %s\n",
                train.getStartEndpoint().getName(),
                train.getEndEndpoint().getName()));
        sb.append(String.format("Scheduled Departure: %s\n", scheduledDeparture));
        sb.append(String.format("Estimated Arrival: %s\n", estimatedArrival));
        sb.append(String.format("Total Travel Time: %d min (Effective: %d min, Wait: %d min)\n",
                totalTravelTimeMinutes, getEffectiveTravelTimeMinutes(), totalWaitTimeMinutes));
        sb.append(String.format("Crossings: %d\n", crossingOperations.size()));
        sb.append("═".repeat(63)).append("\n");

        return sb.toString();
    }

    /**
     * Generates a detailed report including all passages and crossing operations.
     *
     * @return Detailed report string
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.toString());

        sb.append("\n📍 SEGMENT PASSAGES:\n");
        sb.append("─".repeat(70)).append("\n");
        for (int i = 0; i < passages.size(); i++) {
            sb.append(String.format("%2d. %s\n", i + 1, passages.get(i)));
        }

        if (!crossingOperations.isEmpty()) {
            sb.append("\n⚠️  CROSSING OPERATIONS:\n");
            sb.append("─".repeat(70)).append("\n");
            for (int i = 0; i < crossingOperations.size(); i++) {
                sb.append(crossingOperations.get(i)).append("\n");
            }
        }

        return sb.toString();
    }
}