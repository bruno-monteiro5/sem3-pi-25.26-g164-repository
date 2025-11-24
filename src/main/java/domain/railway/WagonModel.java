package domain.railway;

/**
 * Represents a model wagon
 */
public class WagonModel {
    private int id;
    private String model;
    private String maker;
    private int numberBogies;
    private int length;
    private int width;
    private int height;
    private double weight;      // tara
    private int maxSpeed;
    private double payload;     // carga máxima
    private double volume;
    private String type;
    private int gauge;

    public WagonModel(int id, String model, String maker, int numberBogies,
                      int length, int width, int height, double weight,
                      int maxSpeed, double payload, double volume,
                      String type, int gauge) {
        this.id = id;
        this.model = model;
        this.maker = maker;
        this.numberBogies = numberBogies;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.payload = payload;
        this.volume = volume;
        this.type = type;
        this.gauge = gauge;
    }

    public int getId() { return id; }
    public String getModel() { return model; }
    public String getMaker() { return maker; }
    public int getNumberBogies() { return numberBogies; }
    public int getLength() { return length; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getWeight() { return weight; }
    public int getMaxSpeed() { return maxSpeed; }
    public double getPayload() { return payload; }
    public double getVolume() { return volume; }
    public String getType() { return type; }
    public int getGauge() { return gauge; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", model, maker, type);
    }
}