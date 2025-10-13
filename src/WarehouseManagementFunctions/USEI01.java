package WarehouseManagementFunctions;

import Classes.Aisle;
import Classes.Bay;
import Classes.Warehouse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class USEI01 {

    public static Warehouse warehouse;

    public static Warehouse beginWarehouse() throws FileNotFoundException {
        String fileNameWarehouse = "warehouseMngt/train_station_dataset/bays.csv";


        Scanner sc = new Scanner(new File(fileNameWarehouse));
        String line = sc.nextLine();
        warehouse = new Warehouse(sc.nextLine().split(";")[0]);
        int aisleID = -1;
        while(sc.hasNextLine()){
            line = sc.nextLine();
            if(Integer.parseInt(line.split(";")[1]) != aisleID){
                aisleID = Integer.parseInt(line.split(";")[1]);
                warehouse.addAisle(aisleID);
            }
            warehouse.getLastAisle().addBay(new Bay(Integer.parseInt(line.split(";")[2]), Integer.parseInt(line.split(";")[3]) ));
        }
        sc.close();
        return warehouse;
    }





}
