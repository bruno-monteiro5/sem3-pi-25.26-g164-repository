package domain;

import java.util.ArrayList;
import java.util.List;

public class Aisle {
    private int aisleID;
    private List<Bay> bays;

    public Aisle(int aisleID){
        this.aisleID = aisleID;
        this.bays = new ArrayList<>();
    }

    public int getAisleID() {
        return aisleID;
    }
    public void setAisleID(int aisleID) {
        this.aisleID = aisleID;
    }
    public List<Bay> getBays() {
        return bays;
    }
    public void setBays(List<Bay> bays) {
        this.bays = bays;
    }
    public void addBay(Bay bay){
        this.bays.add(bay);
    }

    public String toString(){
        return String.format("Aisle ID: %d, with %d bays", this.aisleID, this.bays.size());
    }

    public Bay getFirstAvailableBay(){
        for(Bay b: bays){
            if(b.getInventory().size() < b.getCapacity()){
                return b;
            }
        }
        return null;
    }


}
