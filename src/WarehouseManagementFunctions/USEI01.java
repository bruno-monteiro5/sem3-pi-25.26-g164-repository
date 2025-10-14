package WarehouseManagementFunctions;

import Classes.*;
import com.sun.source.tree.NewArrayTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class USEI01 {

    public static Warehouse warehouse;
    public static List<Products> products;
    public static Map<String, Box> perishable;
    public static Map<Integer, Box> nonPerishable;

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

    public static void beginProducts() throws FileNotFoundException {
        products = new ArrayList<Products>();

        String fileNameProducts = "warehouseMngt/train_station_dataset/items.csv";

        Scanner sc = new Scanner(new File(fileNameProducts));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] lineSplit = line.split(",");
            products.add(new Products(lineSplit[0], lineSplit[1], lineSplit[2], lineSplit[3], Float.parseFloat(lineSplit[4]), Float.parseFloat(lineSplit[5])));
        }
    }

    public static void removeCargoFromWagon(Warehouse warehouse, List<Products> productsP) throws FileNotFoundException {
        beginProducts();
        productsP = products;
        perishable = new HashMap<>();
        nonPerishable = new HashMap<>();

        String fileNameWagons = "warehouseMngt/train_station_dataset/wagons.csv";
        int index = 0;
        Scanner sc = new Scanner(new File(fileNameWagons));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] lineSplit = line.split(",");
            Products product = null;
            for(Products p : products){
                if(lineSplit[2].equals(p.getSkuID())){
                    product = p;
                    break;
                }
            }
            if(lineSplit[4].equals(null) || lineSplit[4].equals("") || lineSplit[4].isBlank()){
                index ++;
                nonPerishable.put(index, new Box(product, Integer.parseInt(lineSplit[3])));
            }else{
                perishable.put(lineSplit[4], new Box(product, Integer.parseInt(lineSplit[3])));
            }
        }
        sc.close();
        System.out.println("######NON PERISHABLE####");
        nonPerishable.forEach((k,v) -> System.out.printf("Index: %d, %s\n", k, v.toString()));
        System.out.println("######PERISHABLE####");
        List<Map.Entry<String, Box>> list = new ArrayList<>();
        perishable.forEach((k, v) -> list.add(new AbstractMap.SimpleEntry<>(k, v)));
        list.sort(Comparator.comparing(Map.Entry::getKey));
        for(Map.Entry<String, Box> e : list){
            System.out.printf("Key: %s, Value: %s\n", e.getKey(), e.getValue().toString());
        }
        System.out.println("########################");
        for(Map.Entry<String, Box> e : perishable.entrySet()){
            warehouse.getFirstAvailableAisle().getFirstAvailableBay().addProductBox(e.getValue().getProduct(), e.getValue().getQuantity());
        }
        System.out.println("#####PERISHABLES FULLY STORED####");
        System.out.println("#####CURRENT INVENTORY#####");
        for(Aisle a : warehouse.getAisles()){
            for(Bay b : a.getBays()){
                System.out.println(b.toString());
            }
        }


    }





}
