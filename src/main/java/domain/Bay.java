package domain;

import java.util.ArrayList;
import java.util.List;

public class Bay {
    private int bayID;
    private List<Box> inventory;
    private int capacity;

    public Bay(int bayID, int capacity){
        this.bayID = bayID;
        this.capacity = capacity;
        this.inventory = new ArrayList<Box>();
    }

    public int getBayID() {
        return bayID;
    }
    public void setBayID(int bayID) {
        this.bayID = bayID;
    }
    public List<Box> getInventory() {
        return inventory;
    }
    public void setInventory(List<Box> inventory) {
        this.inventory = inventory;
    }
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public boolean addProductBox(Box box){
        if(this.inventory.size() < this.capacity){
            this.inventory.add(box);
            return true;
        }else{
            return false;
        }
    }

    public String toString(){
        if(this.inventory.size() == 0) {
            return String.format("Bay ID: %d, with %d boxes, and %d capacity", this.bayID, this.inventory.size(), this.capacity);
        }else{
            String result = String.format("Bay ID: %d, with %d boxes, and %d capacity", this.bayID, this.inventory.size(), this.capacity);
            for(Box e : this.inventory){
                result += String.format("%s\n", e.toString());
            }
            return result;
        }
    }
}
