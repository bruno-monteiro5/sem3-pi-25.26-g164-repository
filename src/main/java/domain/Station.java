package domain;

import java.util.Objects;

public class Station  {

    private String name;
    private Location location;
    private String country;
    private String timeZoneGroup;
    private boolean isCity;
    private boolean isMainStation;
    private boolean isAirport;

    //for sprint 3
    private int id;
    private double cordX;
    private double cordY;

    public Station(String name,
                   String country,
                   Location location,
                   String timeZoneGroup,
                   boolean isCity,
                   boolean isMainStation,
                   boolean isAirport) {

        this.name = normalizeName(name);
        this.country = normalizeCountry(country);
        this.timeZoneGroup = normalizeTzGroup(timeZoneGroup);
        this.location = location;

        if (this.name == null || this.name.isEmpty())
            throw new IllegalArgumentException("name is blank");
        if (this.country == null || this.country.isEmpty())
            throw new IllegalArgumentException("country is blank");
        if (this.timeZoneGroup == null || this.timeZoneGroup.isEmpty())
            throw new IllegalArgumentException("timeZoneGroup is blank");
        if (this.location == null)
            throw new IllegalArgumentException("location is null");

        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    //new constructor for sprint 3
    public Station(int id, String name, Location location, double cordX, double cordY){
        this.id = id;
        this.name = normalizeName(name);
        this.location = location;
        this.cordX = cordX;
        this.cordY = cordY;
    }


    static String normalizeName(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeTzGroup(String s) {
        if (s == null) return "";
        String t = s.trim();
        t = t.replace("(", "").replace(")", "").replace("'", "").replace(",", "");
        return t.trim();
    }

    static String normalizeCountry(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= 3) return t.toUpperCase();
        return t;
    }


    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    public boolean isCity() {
        return isCity;
    }

    public boolean isMainStation() {
        return isMainStation;
    }

    public boolean isAirport() {
        return isAirport;
    }

    public String getCountry() {
        return country;
    }


    public int getId() { return id; }
    public double getCordX() { return cordX; }
    public double getCordY() { return cordY; }


    public String toString(){
        String s = String.format("Station: %s, Time Zone: %s, Country: %s, Latitude: %f, Longitude %f", name, timeZoneGroup, country, location.getLatitude(), location.getLongitude());
        if(isCity){
            s += ", Is city";
        }else{
            s += ", Is not city";
        }
        if(isMainStation){
            s += ", Is main station";
        }else{
            s += ", Is not main station";
        }
        if(isAirport){
            s += ", Is airport\n";
        }else{
            s += ", Is not airport\n";
        }

        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id == station.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
