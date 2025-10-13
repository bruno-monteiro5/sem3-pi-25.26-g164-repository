package Classes;

import java.util.HashMap;
import java.util.Map;

public class Bay {
    private int bayID;
    private Map<Products, Integer> inventory;
    private int capacity;

    public Bay(int bayID, int capacity){
        this.bayID = bayID;
        this.capacity = capacity;
        this.inventory = new HashMap<Products, Integer>();
    }

    public int getBayID() {
        return bayID;
    }
    public void setBayID(int bayID) {
        this.bayID = bayID;
    }
    public Map<Products, Integer> getInventory() {
        return inventory;
    }
    public void setInventory(Map<Products, Integer> inventory) {
        this.inventory = inventory;
    }
    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public boolean addProductBox(Products product, int quantity){
        if(this.inventory.size() < this.capacity){
            this.inventory.put(product, quantity);
            return true;
        }else{
            return false;
        }
    }

    public String toString(){
        return String.format("Bay ID: %d, with %d boxes, and %d capacity", this.bayID, this.inventory.size(), this.capacity);
    }
}
