package domain;

public class LatKey implements Comparable<LatKey> {

    private double latitude;
    private String name;
    private Station station;

    public LatKey(Station station) {
        this.latitude = station.getLocation().getLatitude();
        this.name = station.getName();
        this.station = station;
    }

    @Override
    public int compareTo(LatKey other) {
        int compare = Double.compare(this.latitude, other.latitude);
        if (compare != 0) return compare;
        return this.name.compareTo(other.name);
    }

    public Station getStation() {
        return station;
    }
}
