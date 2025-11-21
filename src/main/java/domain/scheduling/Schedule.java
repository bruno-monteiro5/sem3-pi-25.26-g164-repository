package domain.scheduling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Representa um agendamento completo com múltiplos comboios
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
     * Retorna os agendamentos ordenados por hora de partida
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
            sb.append(String.format("\n🚂 Train %d: %s → %s | Depart: %s | Arrive: %s | Time: %d min\n",
                    ts.getTrain().getId(),
                    ts.getTrain().getStartEndpoint().getName(),
                    ts.getTrain().getEndEndpoint().getName(),
                    ts.getScheduledDeparture().toLocalTime(),
                    ts.getEstimatedArrival().toLocalTime(),
                    ts.getTotalTravelTimeMinutes()));
        }

        return sb.toString();
    }
}