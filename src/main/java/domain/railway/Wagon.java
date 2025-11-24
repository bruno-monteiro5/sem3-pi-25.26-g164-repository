package domain.railway;

import domain.railway.WagonModel;

/**
 * Represents a wagon
 */
public class Wagon {
    private int id;
    private int serviceYear;
    private WagonModel model;
    private String operatorVat;

    public Wagon(int id, int serviceYear, WagonModel model, String operatorVat) {
        this.id = id;
        this.serviceYear = serviceYear;
        this.model = model;
        this.operatorVat = operatorVat;
    }

    public int getId() { return id; }
    public int getServiceYear() { return serviceYear; }
    public WagonModel getModel() { return model; }
    public String getOperatorVat() { return operatorVat; }

    /**
     * Calculates total weight (tara + payload)
     */
    public int getTotalWeight() {
        return (int) (model.getWeight() + model.getPayload());
    }

    @Override
    public String toString() {
        return String.format("Wagon %d (%s)", id, model.getType());
    }
}
