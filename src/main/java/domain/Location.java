package domain;

public class Location {

    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Invalid longitude: " + longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
