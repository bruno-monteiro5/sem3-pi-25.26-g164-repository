package controller;

import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class WagonUnloadingController {

    public static Warehouse warehouse;
    public static List<Items> products;
    public static Map<String, Box> perishable;
    public static Map<String, Box> nonPerishable;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // design comentado
    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String RED = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP = "─";

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

        System.out.println();
        System.out.println(CYAN + line(72));
        System.out.println("                    WAGON UNLOADING IN PROGRESS");
        System.out.println(line(72) + RESET);

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

        System.out.println();
        System.out.println(GREEN + "  CURRENT INVENTORY : " + RESET);

        WarehouseRepository.setCurrentWarehouse(warehouse);

        System.out.println();
        System.out.println(GREEN +"   WAGON UNLOADING COMPLETE - SUCCESS" + RESET);
        System.out.println();
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
                System.err.println(RED + "[ERROR] Invalid CSV format: incorrect number of columns." + RESET);
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
                System.err.println(RED + "[ERROR] Duplicate box ID: " + boxId + RESET);
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
                System.err.println(RED + "[ERROR] Unknown SKU: " + sku + RESET);
                continue;
            }

            // Validate quantity
            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    System.err.println(RED + "[ERROR] Invalid quantity (<= 0) for box ID: " + boxId + RESET);
                    continue;
                }
            } catch (NumberFormatException e) {
                System.err.println(RED + "[ERROR] Non-numeric quantity for box ID: " + boxId + RESET);
                continue;
            }

            // Validate receivedAt
            if (receivedAt == null || receivedAt.isBlank()) {
                System.err.println(RED + "[ERROR] Missing receivedAt for box ID: " + boxId + RESET);
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
                    System.err.println(RED + "[ERROR] Invalid expiry date for box ID: " + boxId + ": " + expiryDate + RESET);
                }
            }
        }
        sc.close();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
