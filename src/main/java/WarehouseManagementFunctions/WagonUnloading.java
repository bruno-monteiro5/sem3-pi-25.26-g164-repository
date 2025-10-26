package WarehouseManagementFunctions;

import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class WagonUnloading implements Runnable {

    public static Warehouse warehouse;
    public static List<Items> products;
    public static Map<String, Box> perishable;
    public static Map<String, Box> nonPerishable;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void run() {
        try {
            beginWarehouse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            beginProducts();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            removeCargoFromWagon(warehouse, products);
        } catch (FileNotFoundException e) {
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
                    new Bay(
                            Integer.parseInt(line.split(";")[2]),
                            Integer.parseInt(line.split(";")[3])
                    )
            );
        }

        sc.close();
    }

    public static void beginProducts() throws FileNotFoundException {
        products = new ArrayList<>();

        String fileNameProducts = "warehouseMngt/train_station_dataset/items.csv";

        Scanner sc = new Scanner(new File(fileNameProducts));
        // Skip header
        String line = sc.nextLine();
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] lineSplit = line.split(",");
            products.add(new Items(
                    lineSplit[0],
                    lineSplit[1],
                    lineSplit[2],
                    lineSplit[3],
                    Float.parseFloat(lineSplit[4]),
                    Float.parseFloat(lineSplit[5])
            ));
        }
    }

    public static void removeCargoFromWagon(Warehouse warehouse, List<Items> itemsP) throws FileNotFoundException {
        beginProducts();
        perishable = new HashMap<>();
        nonPerishable = new HashMap<>();

        String fileNameWagons = "warehouseMngt/train_station_dataset/wagons.csv";
        fillMap(fileNameWagons);

        // Process perishable items
        List<Map.Entry<String, Box>> list = new ArrayList<>(perishable.entrySet());
        list.sort(Comparator.comparing(entry -> LocalDate.parse(entry.getKey(), DATE_FORMATTER))); // Sort by expiry date
        for (Map.Entry<String, Box> e : list) {
            warehouse.getFirstAvailableAisle().getFirstAvailableBay().addProductBox(e.getValue());
        }

        // Process non-perishable items
        List<Map.Entry<String, Box>> nonList = new ArrayList<>(nonPerishable.entrySet());
        nonList.sort(Comparator.comparing(Map.Entry::getKey)); // Sort by receivedAt
        for (Map.Entry<String, Box> e : nonList) {
            warehouse.getFirstAvailableAisle().getFirstAvailableBay().addProductBox(e.getValue());
        }

        System.out.println("#####CURRENT INVENTORY#####");
        for (Aisle a : warehouse.getAisles()) {
            for (Bay b : a.getBays()) {
                System.out.println(b.toString());
            }
        }

        WarehouseRepository.setCurrentWarehouse(warehouse);

        System.out.println("############WAGON UNLOADING COMPLETE############");
    }

    public static void fillMap(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        // Skip header
        String line = sc.nextLine();
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] lineSplit = line.split(",");

            // Handle product lookup
            Items product = null;
            for (Items p : products) {
                if (lineSplit[2].equals(p.getSkuID())) {
                    product = p;
                    break;
                }
            }

            // Skip this entry if the SKU is invalid
            if (product == null) {
                System.err.println("Product with SKU " + lineSplit[2] + " not found.");
                continue;
            }

            // Parse expiry date (if exists)
            String expiryDate = lineSplit[4];
            if (expiryDate == null || expiryDate.isBlank()) {
                // Non-perishable item
                String receivedAt = lineSplit[5];
                nonPerishable.put(receivedAt, new Box(
                        product,
                        Integer.parseInt(lineSplit[3])
                ));
            } else {
                try {
                    // Validate date format
                    LocalDate.parse(expiryDate, DATE_FORMATTER);
                    perishable.put(expiryDate, new Box(
                            product,
                            Integer.parseInt(lineSplit[3]),
                            expiryDate
                    ));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid expiry date for box ID " + lineSplit[1] + ": " + expiryDate);
                }
            }
        }
        sc.close();
    }
}