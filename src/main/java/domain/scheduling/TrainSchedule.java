package domain.scheduling;

import domain.railway.Train;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o agendamento completo de um comboio específico
 */
public class TrainSchedule {
    private Train train;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedArrival;
    private List<SegmentPassage> passages;
    private List<CrossingOperation> crossingOperations;
    private long totalTravelTimeMinutes;
    private long totalWaitTimeMinutes;

    public TrainSchedule(Train train, LocalDateTime scheduledDeparture) {
        this.train = train;
        this.scheduledDeparture = scheduledDeparture;
        this.passages = new ArrayList<>();
        this.crossingOperations = new ArrayList<>();
        this.totalTravelTimeMinutes = 0;
        this.totalWaitTimeMinutes = 0;
    }

    public void addPassage(SegmentPassage passage) {
        this.passages.add(passage);
        updateEstimatedArrival();
    }

    public void addCrossingOperation(CrossingOperation crossing) {
        this.crossingOperations.add(crossing);
        this.totalWaitTimeMinutes += crossing.getWaitDurationMinutes();
    }

    private void updateEstimatedArrival() {
        if (!passages.isEmpty()) {
            SegmentPassage lastPassage = passages.get(passages.size() - 1);
            this.estimatedArrival = lastPassage.getArrivalTime();
            this.totalTravelTimeMinutes = java.time.Duration.between(
                    scheduledDeparture, estimatedArrival).toMinutes();
        }
    }

    public Train getTrain() { return train; }
    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public List<SegmentPassage> getPassages() { return passages; }
    public List<CrossingOperation> getCrossingOperations() { return crossingOperations; }
    public long getTotalTravelTimeMinutes() { return totalTravelTimeMinutes; }
    public long getTotalWaitTimeMinutes() { return totalWaitTimeMinutes; }

    public long getEffectiveTravelTimeMinutes() {
        return totalTravelTimeMinutes - totalWaitTimeMinutes;
    }

    @Override
    public String toString() {
        return String.format("""
                ═══════════════════════════════════════════════════════════════
                TRAIN SCHEDULE: %s
                ═══════════════════════════════════════════════════════════════
                Route: %s → %s
                Scheduled Departure: %s
                Estimated Arrival: %s
                Total Travel Time: %d min (Effective: %d min, Wait: %d min)
                Crossings: %d
                ═══════════════════════════════════════════════════════════════
                """,
                train.getId(),
                train.getStartEndpoint().getName(),
                train.getEndEndpoint().getName(),
                scheduledDeparture,
                estimatedArrival,
                totalTravelTimeMinutes,
                getEffectiveTravelTimeMinutes(),
                totalWaitTimeMinutes,
                crossingOperations.size());
    }

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
            for (CrossingOperation co : crossingOperations) {
                sb.append(co).append("\n");
            }
        }

        return sb.toString();
    }
}