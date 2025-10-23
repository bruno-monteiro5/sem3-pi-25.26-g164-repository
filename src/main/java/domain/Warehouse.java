package domain;

import java.util.ArrayList;
import java.util.List;

public class Warehouse {
    private List<Aisle> aisles;
    private String warehouseID;

    public Warehouse(String warehouseID){
        this.warehouseID = warehouseID;
        this.aisles = new ArrayList<>();
    }

    public List<Aisle> getAisles() {
        return aisles;
    }
    public void setAisles(List<Aisle> aisles) {
        this.aisles = aisles;
    }
    public String getWarehouseID() {
        return warehouseID;
    }
    public void setWarehouseID(String warehouseID) {
        this.warehouseID = warehouseID;
    }
    public void addAisle(int aisleID){
        this.aisles.add(new Aisle(aisleID));
    }
    public Aisle getLastAisle(){
        return this.aisles.get(this.aisles.size() - 1);
    }
    public Aisle getFirstAvailableAisle(){
        for(Aisle a: aisles) {
            if (a.getFirstAvailableBay() != null) return a;
        }
        return null;
    }
}
