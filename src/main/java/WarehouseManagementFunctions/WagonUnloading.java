package WarehouseManagementFunctions;

import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class WagonUnloading implements Runnable{

    public static Warehouse warehouse;
    public static List<Items> products;
    public static Map<String, Box> perishable;
    public static Map<String, Box> nonPerishable;


    public void run() {
        try{
            beginWarehouse();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        try{
            beginProducts();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        try{
            removeCargoFromWagon(warehouse, products);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Runnable menu = new WarehouseManagementMenu();
        menu.run();
    }

    public static void beginWarehouse() throws FileNotFoundException {
        String fileNameWarehouse = "warehouseMngt/train_station_dataset/bays.csv";

        Scanner sc = new Scanner(new File(fileNameWarehouse));
        warehouse = new Warehouse(sc.nextLine().split(";")[0]);
        String line = "";
        int aisleID = -1;

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (Integer.parseInt(line.split(";")[1]) != aisleID) {
                aisleID = Integer.parseInt(line.split(";")[1]);
                warehouse.addAisle(aisleID);
            }
            warehouse.getLastAisle().addBay(
                    new Bay(Integer.parseInt(line.split(";")[2]), Integer.parseInt(line.split(";")[3]))
            );

        }

        sc.close();
    }

    public static void beginProducts() throws FileNotFoundException {
        products = new ArrayList<Items>();

        String fileNameProducts = "warehouseMngt/train_station_dataset/items.csv";

        Scanner sc = new Scanner(new File(fileNameProducts));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] lineSplit = line.split(",");
            products.add(new Items(lineSplit[0], lineSplit[1], lineSplit[2], lineSplit[3], Float.parseFloat(lineSplit[4]), Float.parseFloat(lineSplit[5])));
        }
    }

    public static void removeCargoFromWagon(Warehouse warehouse, List<Items> itemsP) throws FileNotFoundException {
        beginProducts();
        perishable = new HashMap<>();
        nonPerishable = new HashMap<>();

        String fileNameWagons = "warehouseMngt/train_station_dataset/wagons.csv";
        fillMap(fileNameWagons);
        System.out.println("######NON PERISHABLE####");
        nonPerishable.forEach((k,v) -> System.out.printf("Arrived in: %s, %s\n", k, v.toString()));
        System.out.println("######PERISHABLE####");
        List<Map.Entry<String, Box>> list = new ArrayList<>(perishable.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey));
        for(Map.Entry<String, Box> e : list){
            System.out.printf("Key: %s, Value: %s\n", e.getKey(), e.getValue().toString());
        }
        System.out.println("########################");
        for(Map.Entry<String, Box> e : perishable.entrySet()){
            warehouse.getFirstAvailableAisle().getFirstAvailableBay().addProductBox(e.getValue());
        }
        System.out.println("#####PERISHABLES FULLY STORED####");
        System.out.println("#####CURRENT INVENTORY#####");
        for(Aisle a : warehouse.getAisles()){
            for(Bay b : a.getBays()){
                System.out.println(b.toString());
            }
        }

        list = new ArrayList<>(nonPerishable.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey));
        for(Map.Entry<String, Box> e : list){
            System.out.printf("Key: %s, Value: %s\n", e.getKey(), e.getValue().toString());
        }
        System.out.println("########################");
        for(int i = 0; i < nonPerishable.size(); i++){
            if( i < nonPerishable.size()-1 && list.get(i).getKey().equals(list.get(i+1).getKey())){
                if(list.get(i).getValue().getProduct().getSkuID().compareTo(list.get(i+1).getValue().getProduct().getSkuID()) >0){
                    Map.Entry<String, Box> temp = list.get(i);
                    list.set(i, list.get(i+1));
                    list.set(i+1, temp);
                }
            }
            warehouse.getFirstAvailableAisle().getFirstAvailableBay().addProductBox(list.get(i).getValue());

        }
        System.out.println("#####NON PERISHABLES FULLY STORED####");
        System.out.println("#####CURRENT INVENTORY#####");
        for(Aisle a : warehouse.getAisles()){
            for(Bay b : a.getBays()){
                System.out.println(b.toString());
            }
        }

        WarehouseRepository.setCurrentWarehouse(warehouse);

        System.out.println("############WAGON UNLOADING COMPLETE############");


    }

    public static void fillMap(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        String line = sc.nextLine();
        while(sc.hasNextLine()){
            line = sc.nextLine();
            String[] lineSplit = line.split(",");
            Items product = null;
            for(Items p : products){
                if(lineSplit[2].equals(p.getSkuID())){
                    product = p;
                    break;
                }
            }
            if(lineSplit[4].equals(null) || lineSplit[4].equals("") || lineSplit[4].isBlank()){
                nonPerishable.put(lineSplit[5], new Box(product, Integer.parseInt(lineSplit[3])));
            }else{
                perishable.put(lineSplit[4], new Box(product, Integer.parseInt(lineSplit[3]), lineSplit[4]));
            }
        }
        sc.close();
    }


}
