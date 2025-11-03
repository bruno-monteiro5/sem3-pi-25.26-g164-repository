package domain;

public abstract class Endpoint {
    protected String name;
    protected Location location;


    public Endpoint(String name, Location location) {
        this.name = name;
        this.location = location;

    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }




}
