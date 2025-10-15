package Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if(this.inventory.size() == 0) {
            return String.format("Bay ID: %d, with %d boxes, and %d capacity", this.bayID, this.inventory.size(), this.capacity);
        }else{
            String result = String.format("Bay ID: %d, with %d boxes, and %d capacity", this.bayID, this.inventory.size(), this.capacity);
            List<Map.Entry<Products, Integer>> list = new ArrayList<Map.Entry<Products, Integer>>(this.inventory.entrySet());
            for(Map.Entry<Products, Integer> e : list){
                result += String.format("Product %s in quantity %d\n", e.getKey().toString(), e.getValue());
            }
            return result;
        }
    }
}
