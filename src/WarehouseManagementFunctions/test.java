package WarehouseManagementFunctions;

import Classes.Aisle;
import Classes.Bay;
import Classes.Warehouse;

import java.io.FileNotFoundException;

public class test {
    public static void main(String[] args) throws FileNotFoundException {
        Warehouse w = USEI01.beginWarehouse();
        for(Aisle a : w.getAisles()){
            System.out.println(a.toString());
            for(Bay b: a.getBays()){
                System.out.println(b.toString());
            }
        }
    }
}
