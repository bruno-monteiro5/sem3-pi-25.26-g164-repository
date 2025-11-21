package domain.railway;

public class Endpoint {
    private int id;
    private String name;
    private int metafacilityId;

    public Endpoint(int id, String name, int metafacilityId) {
        this.id = id;
        this.name = name;
        this.metafacilityId = metafacilityId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMetafacilityId() { return metafacilityId; }

    @Override
    public String toString() {
        return String.format("Endpoint[id=%d, name=%s]", id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return id == endpoint.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}