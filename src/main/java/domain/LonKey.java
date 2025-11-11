package domain;

public class LonKey implements Comparable<LonKey> {
    private double longitude;
    private String name;
    private Station station;


    public LonKey(Station station){
        this.longitude = station.getLocation().getLongitude();
        this.name = station.getName();
        this.station = station;
    }

    @Override
    public int compareTo(LonKey other) {
        int compare = Double.compare(this.longitude, other.longitude);
        if(compare !=0) return compare;
        return this.name.compareTo(other.name);
    }

    public Station getStation() {
        return station;
    }

}
