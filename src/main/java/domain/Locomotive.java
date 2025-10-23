package domain;

public class Locomotive {
    private int entryYear;
    private int power;
    private int acceleration;
    private int weight;


    public Locomotive() {}

    public Locomotive(int entryYear, int power, int acceleration, int weight) {
        this.entryYear = entryYear;
        this.power = power;
        this.acceleration = acceleration;
        this.weight = weight;
    }

    public int getEntryYear() {
        return entryYear;
    }

    public void setEntryYear(int entryYear) {
        this.entryYear = entryYear;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }



}
