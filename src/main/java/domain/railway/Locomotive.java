package domain.railway;

public class Locomotive {
    private int id;
    private String name;
    private String make;
    private String model;
    private int power;          // kW - ADICIONADO para USLP07
    private int maxSpeed;       // km/h
    private int opSpeed;        // km/h
    private String tractionType; // "ELECTRIC" or "DIESEL"
    private int totalWeight;    // toneladas
    private int gauge;          // mm

    public Locomotive(int id, String name, String make, String model,
                      int power, int maxSpeed, int opSpeed, String tractionType,
                      int totalWeight, int gauge) {
        this.id = id;
        this.name = name;
        this.make = make;
        this.model = model;
        this.power = power;
        this.maxSpeed = maxSpeed;
        this.opSpeed = opSpeed;
        this.tractionType = tractionType;
        this.totalWeight = totalWeight;
        this.gauge = gauge;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getPower() { return power; }          // NOVO
    public int getMaxSpeed() { return maxSpeed; }
    public int getOpSpeed() { return opSpeed; }
    public String getTractionType() { return tractionType; }
    public int getTotalWeight() { return totalWeight; }
    public int getGauge() { return gauge; }

    public boolean isElectric() {
        return "ELECTRIC".equalsIgnoreCase(tractionType);
    }

    @Override
    public String toString() {
        return String.format("%s (%s %s) - %s, %dkW, Max: %dkm/h, Op: %dkm/h",
                name, make, model, tractionType, power, maxSpeed, opSpeed);
    }
}