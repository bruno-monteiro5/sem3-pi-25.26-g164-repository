package domain.scheduling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a complete schedule with multiple trains
 */
public class Schedule {
    private LocalDate date;
    private List<TrainSchedule> trainSchedules;

    public Schedule(LocalDate date) {
        this.date = date;
        this.trainSchedules = new ArrayList<>();
    }

    public void addTrainSchedule(TrainSchedule trainSchedule) {
        this.trainSchedules.add(trainSchedule);
    }

    public LocalDate getDate() { return date; }
    public List<TrainSchedule> getTrainSchedules() { return trainSchedules; }

    /**
     * Returns the schedules ordered by departure time
     */
    public List<TrainSchedule> getSchedulesByDeparture() {
        return trainSchedules.stream()
                .sorted(Comparator.comparing(TrainSchedule::getScheduledDeparture))
                .toList();
    }

    public int getTotalCrossings() {
        return trainSchedules.stream()
                .mapToInt(ts -> ts.getCrossingOperations().size())
                .sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(80)).append("\n");
        sb.append(String.format("║ SCHEDULE FOR %s%s║\n",
                date, " ".repeat(80 - 17 - date.toString().length())));
        sb.append(String.format("║ Total Trains: %d | Total Crossings: %d%s║\n",
                trainSchedules.size(), getTotalCrossings(),
                " ".repeat(80 - 41 - String.valueOf(trainSchedules.size()).length()
                        - String.valueOf(getTotalCrossings()).length())));
        sb.append("═".repeat(80)).append("\n");

            for (TrainSchedule ts : getSchedulesByDeparture()) {
                String startName = ts.getTrain().getStartEndpoint() != null ? ts.getTrain().getStartEndpoint().getName() : "N/A";
                String endName = ts.getTrain().getEndEndpoint() != null ? ts.getTrain().getEndEndpoint().getName() : "N/A";

                sb.append(String.format("\n🚂 Train %d: %s → %s | Depart: %s | Arrive: %s | Time: %d min\n",
                        ts.getTrain().getId(),
                        startName,
                        endName,
                        ts.getScheduledDeparture().toLocalTime(),
                        ts.getEstimatedArrival().toLocalTime(),
                        ts.getTotalTravelTimeMinutes()));
            }

            return sb.toString();
    }
}