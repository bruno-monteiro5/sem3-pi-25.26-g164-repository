package domain;

public class Station extends Endpoint {

    private String country;
    private String timeZoneGroup;
    private boolean isCity;
    private boolean isMainStation;
    private boolean isAirport;

    public Station(String name,
                   String country,
                   Location location,
                   String timeZoneGroup,
                   boolean isCity,
                   boolean isMainStation,
                   boolean isAirport) {
        super(normalizeName(name), location);
        this.country = normalizeCountry(country);
        this.timeZoneGroup = normalizeTzGroup(timeZoneGroup);
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
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



    public String timeZoneGroup() {
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


}
