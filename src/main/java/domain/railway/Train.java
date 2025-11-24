package domain.railway;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Train {
    private int id;
    private String operatorVat;
    private LocalTime departureTime;
    private LocalDate date;
    private Path path;
    private Endpoint startEndpoint;
    private Endpoint endEndpoint;
    private List<Locomotive> locomotives;
    private List<Freight> freights;

    public Train(int id, String operatorVat, LocalTime departureTime,
                 LocalDate date, Path path, Endpoint startEndpoint,
                 Endpoint endEndpoint) {
        this.id = id;
        this.operatorVat = operatorVat;
        this.departureTime = departureTime;
        this.date = date;
        this.path = path;
        this.startEndpoint = startEndpoint;
        this.endEndpoint = endEndpoint;
        this.locomotives = new ArrayList<>();
        this.freights = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getOperatorVat() { return operatorVat; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalDate getDate() { return date; }
    public Path getPath() { return path; }
    public Endpoint getStartEndpoint() { return startEndpoint; }
    public Endpoint getEndEndpoint() { return endEndpoint; }
    public List<Locomotive> getLocomotives() { return locomotives; }
    public List<Freight> getFreights() { return freights; }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setStartEndpoint(Endpoint startEndpoint) {
        this.startEndpoint = startEndpoint;
    }

    public void setEndEndpoint(Endpoint endEndpoint) {
        this.endEndpoint = endEndpoint;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }


    public void addLocomotive(Locomotive locomotive) {
        this.locomotives.add(locomotive);
    }

    public void addFreight(Freight freight) {
        this.freights.add(freight);
    }

    /**
     * Calcula the total power of all locomotives
     */
    public int getTotalPower() {
        return locomotives.stream()
                .mapToInt(Locomotive::getPower)
                .sum();
    }

    /**
     * Calculates the total weight of the train (locomotive + loaded wagons)
     */
    public int getTotalWeight() {
        int locomotiveWeight = locomotives.stream()
                .mapToInt(Locomotive::getTotalWeight)
                .sum();

        int wagonWeight = freights.stream()
                .flatMap(f -> f.getWagons().stream())
                .mapToInt(Wagon::getTotalWeight)
                .sum();

        return locomotiveWeight + wagonWeight;
    }

    /**
     * Verifies if the train has at least one electric locomotive
     */
    public boolean hasElectricLocomotive() {
        return locomotives.stream().anyMatch(Locomotive::isElectric);
    }

    /**
     * Verifies if all locomotives are electric
     */
    public boolean isFullyElectric() {
        return !locomotives.isEmpty() &&
                locomotives.stream().allMatch(Locomotive::isElectric);
    }

    @Override
    public String toString() {
        return String.format("Train %d: %s → %s at %s on %s (Locos: %d, Freights: %d)",
                id,
                startEndpoint.getName(),
                endEndpoint.getName(),
                departureTime,
                date,
                locomotives.size(),
                freights.size());
    }
}