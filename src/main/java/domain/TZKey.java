package domain;

public class TZKey implements Comparable<TZKey> {
    private String group;
    private String country;
    private String name;
    private Station station;


    public TZKey(Station station) {
        this.group = station.getTimeZoneGroup();
        this.country = station.getCountry();
        this.name = station.getName();
        this.station = station;
    }

    @Override
    public int compareTo(TZKey other) {
        int compare = this.group.compareTo(other.group);
        if (compare != 0) return compare;
        compare = this.country.compareTo(other.country);
        if (compare != 0) return compare;
        return this.name.compareTo(other.name);
    }

    public Station getStation() {
        return station;
    }


}
