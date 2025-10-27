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
            System.out.println(a.toString());
        }

        WarehouseRepository.setCurrentWarehouse(warehouse);

        System.out.println("############WAGON UNLOADING COMPLETE############");
    }

    public static void fillMap(String filename) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        Set<String> seenBoxIds = new HashSet<>();

        // Skip header
        if (sc.hasNextLine()) sc.nextLine();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] lineSplit = line.split(",");

            if (lineSplit.length != 6) {
                System.err.println("[ERROR] Invalid CSV format: incorrect number of columns.");
                continue;
            }

            String wagonId = lineSplit[0];
            String boxId = lineSplit[1];
            String sku = lineSplit[2];
            String qtyStr = lineSplit[3];
            String expiryDate = lineSplit[4];
            String receivedAt = lineSplit[5];

            // Check box ID uniqueness
            if (!seenBoxIds.add(boxId)) {
                System.err.println("[ERROR] Duplicate box ID: " + boxId);
                continue;
            }

            // Find matching product
            Items product = null;
            for (Items p : products) {
                if (sku.equals(p.getSkuID())) {
                    product = p;
                    break;
                }
            }
            if (product == null) {
                System.err.println("[ERROR] Unknown SKU: " + sku);
                continue;
            }

            // Validate quantity
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    System.err.println("[ERROR] Invalid quantity (<= 0) for box ID: " + boxId);
                    continue;
                }
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] Non-numeric quantity for box ID: " + boxId);
                continue;
            }

            // Validate receivedAt
            if (receivedAt == null || receivedAt.isBlank()) {
                System.err.println("[ERROR] Missing receivedAt for box ID: " + boxId);
                continue;
            }

            // Parse expiry date (optional)
            if (expiryDate == null || expiryDate.isBlank()) {
                // Non-perishable
                nonPerishable.put(receivedAt, new Box(boxId, product, qty));
            } else {
                try {
                    LocalDate.parse(expiryDate, DATE_FORMATTER);
                    perishable.put(expiryDate, new Box(boxId, product, qty, expiryDate));
                } catch (DateTimeParseException e) {
                    System.err.println("[ERROR] Invalid expiry date for box ID: " + boxId + ": " + expiryDate);
                }
            }
        }
        sc.close();
    }

}