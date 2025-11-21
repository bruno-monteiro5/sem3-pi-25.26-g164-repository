package domain.railway;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um Freight (carga) com origem e destino
 */
public class Freight {
    private int id;
    private LocalDate date;
    private Endpoint origin;
    private Endpoint destination;
    private List<Wagon> wagons;

    public Freight(int id, LocalDate date, Endpoint origin, Endpoint destination) {
        this.id = id;
        this.date = date;
        this.origin = origin;
        this.destination = destination;
        this.wagons = new ArrayList<>();
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public Endpoint getOrigin() { return origin; }
    public Endpoint getDestination() { return destination; }
    public List<Wagon> getWagons() { return wagons; }

    public void addWagon(Wagon wagon) {
        this.wagons.add(wagon);
    }

    /**
     * Calcula o peso total dos vagões deste freight
     */
    public int getTotalWeight() {
        return wagons.stream()
                .mapToInt(Wagon::getTotalWeight)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("Freight %d: %s → %s (%d wagons)",
                id, origin.getName(), destination.getName(), wagons.size());
    }
}
