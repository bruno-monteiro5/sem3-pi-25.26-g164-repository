package domain;

public abstract class Endpoint {
    private final String name;
    private final String location;


    public Endpoint(String name, String location) {
        this.name = name;
        this.location = location;

    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }
}
