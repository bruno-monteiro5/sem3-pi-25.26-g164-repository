package WarehouseManagementFunctions;

import Classes.Aisle;
import Classes.Bay;
import Classes.Products;
import Classes.Warehouse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) throws FileNotFoundException {
        Warehouse w = USEI01.beginWarehouse();
        List<Products> p = new ArrayList<>();
        for(Aisle a : w.getAisles()){
            System.out.println(a.toString());
            for(Bay b: a.getBays()){
                System.out.println(b.toString());
            }
        }
        USEI01.removeCargoFromWagon(w, p);
    }
}
