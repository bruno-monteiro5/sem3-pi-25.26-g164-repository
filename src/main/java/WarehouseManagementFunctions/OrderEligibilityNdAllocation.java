package WarehouseManagementFunctions;

import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

public class OrderEligibilityNdAllocation implements Runnable {

    Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
    List<Order> orders = new ArrayList<>();
    String filenameorders = "warehouseMngt/train_station_dataset/orders.csv";
    String filenameorderlines = "warehouseMngt/train_station_dataset/order_lines.csv";
    List<Allocation> allocation = new ArrayList<>();

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    public void run() {

        banner("ORDER ELIGIBILITY & ALLOCATION");

        //fills the warehouse if it senses it is null
        if (warehouse == null) {
            System.out.println(RED + "Warehouse is null." + RESET + "Beginning warehouse initialization...");
            Runnable run = new WagonUnloading();
            run.run();
            warehouse = WarehouseRepository.getCurrentWarehouse();
            System.out.println(GREEN + "Warehouse initialized." + RESET);
            System.out.println(line(72));
        }

        try {
            fillOrders();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // writes the list's contents if it is filled with success
        System.out.println(GREEN + "Orders loaded:" + RESET);
        System.out.println(line(72));
        for (Order o : orders) {
            System.out.println(o.toString());
        }
        System.out.println(line(72));

        // verifies which boxes can be allocated to the requests
        allocateBoxesChoice();

        // writes all the allocations with the requests
        for (Allocation a : allocation) {
            System.out.println(GREEN + "ALLOCATION RESULT" + RESET);
            System.out.println(line(72));
            System.out.println(a.toString());
            System.out.println(line(72));
        }

        WarehouseRepository.setAllocation(allocation);

        System.out.println(GREEN + "Allocation workflow finished." + RESET);
        System.out.println(line(72));

        Runnable WarehouseManagementMenu = new WarehouseManagementMenu();
        WarehouseManagementMenu.run();
    }

    public void fillOrders() throws FileNotFoundException {
        validateCsvHeaders(filenameorders, new String[]{"orderId", "dueDate", "priority"});
        validateCsvHeaders(filenameorderlines, new String[]{"orderId", "lineNo", "sku", "qty"});

        Scanner sc = new Scanner(new File(filenameorders));
        String line = sc.nextLine(); // Skip header
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] linesplit = line.split(",");
            if (linesplit.length != 3 || !isValidOrderLine(linesplit)) {
                System.err.println(RED + "Invalid line in orders.csv: " + line + RESET);
                continue;
            }
            orders.add(new Order(linesplit[0], Integer.parseInt(linesplit[2]), linesplit[1]));
        }
        sc = new Scanner(new File(filenameorderlines));
        line = sc.nextLine(); // Skip header
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            String[] linesplit = line.split(",");
            if (linesplit.length != 4 || !isValidOrderLineDetails(linesplit)) {
                System.err.println(RED + "Invalid line in order_lines.csv: " + line + RESET);
                continue;
            }
            Order order = null;
            for (Order o : orders) {
                if (o.getOrderID().equals(linesplit[0])) {
                    order = o;
                    break;
                }
            }
            if (order != null) {
                order.addLine(new OrderLine(
                        Integer.parseInt(linesplit[1]),
                        linesplit[2],
                        Integer.parseInt(linesplit[3])
                ));
            } else {
                System.err.println(RED + "Order ID not found for line: " + line + RESET);
            }
        }
        sc.close();
        orders.sort(Comparator.comparing(Order::getPriority));
    }

    private void validateCsvHeaders(String filename, String[] expectedHeaders) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));
        if (!sc.hasNextLine()) {
            throw new IllegalArgumentException("CSV file is empty: " + filename);
        }
        String[] headers = sc.nextLine().split(",");
        if (!Arrays.equals(headers, expectedHeaders)) {
            throw new IllegalArgumentException("Invalid CSV headers in file " + filename + ". Expected: " +
                    String.join(",", expectedHeaders) + ", but got: " + String.join(",", headers));
        }
    }

    private boolean isValidOrderLine(String[] linesplit) {
        try {
            // Validate orderId: should be alphanumeric
            if (!linesplit[0].matches("^ORD\\d+$")) return false;

            // Validate dueDate: should be a valid LocalDateTime
            LocalDateTime.parse(linesplit[1]);

            // Validate priority: should be an integer (e.g., 1, 2, 3)
            int priority = Integer.parseInt(linesplit[2]);
            return priority > 0; // Only positive priorities are valid
        } catch (Exception e) {
            System.err.println(RED + "[ERROR] Validation failed for order line: " + Arrays.toString(linesplit) + RESET);
            return false;
        }
    }

    private boolean isValidOrderLineDetails(String[] linesplit) {
        try {
            // Validate orderId: should be alphanumeric
            if (!linesplit[0].matches("^ORD\\d+$")) return false;

            // Validate lineNo: should be a positive integer
            int lineNo = Integer.parseInt(linesplit[1]);
            if (lineNo <= 0) return false;

            // Validate SKU format (alphanumeric starting with SKU)
            if (!linesplit[2].matches("^SKU\\d+$")) return false;

            // Validate quantity: should be a positive integer
            int qty = Integer.parseInt(linesplit[3]);
            return qty > 0;
        } catch (Exception e) {
            System.err.println(RED + "[ERROR] Validation failed for order line details: " + Arrays.toString(linesplit) + RESET);
            return false;
        }
    }

    public void allocateBoxesChoice(){
        System.out.println(GREEN + "ALLOCATION MODE" + RESET);
        System.out.println(line(72));
        System.out.println("Do you wish to perform strict allocation or partial? [DEFAULT: Strict]");
        System.out.println("1. Strict");
        System.out.println("2. Partial");
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();

        while (choice != 1 && choice != 2) {
            System.out.println(RED + "[ERROR] Invalid input. Please enter 1 or 2" + RESET);
            choice = sc.nextInt();
        }
        if (choice == 1) {
            System.out.println(GREEN + "Strict allocation selected." + RESET);
            System.out.println(line(72));
            allocateBoxes(true);
        } else {
            System.out.println(YELLOW + "Partial allocation selected." + RESET);
            System.out.println(line(72));
            allocateBoxes(false);
        }
    }

    public static final double MAX_ALLOCATION_KG = 400.0;

    private int maxUnitsPerAllocation(double unitWeight) {
        if (unitWeight <= 0.0) return 0;
        return (int) Math.floor(MAX_ALLOCATION_KG / unitWeight);
    }

    private void addSlicedAllocations(Order o, OrderLine ol, Aisle a, Bay b, Box sourceBox, int takeQty, List<Allocation> out) {
        double unitW = sourceBox.getProduct().getWeight();
        int maxPerAlloc = maxUnitsPerAllocation(unitW);
        if (maxPerAlloc <= 0) {
            System.out.println(sourceBox + " - " + RED + "UNDISPATCHBLE" + RESET + " (invalid unit weight)");
            return;
        }

        int remaining = takeQty;
        while (remaining > 0) {
            int chunk = Math.min(remaining, maxPerAlloc);
            Allocation part = new Allocation(
                    o, ol, ol.getSku(),
                    new Box(sourceBox.getBoxID(), sourceBox.getProduct(), chunk, sourceBox.getExpDate()),
                    a, b,
                    chunk,
                    unitW
            );
            out.add(part);
            remaining -= chunk;
        }
    }

    public void allocateBoxes(boolean strict) {
        if (!strict) {
            for (Order o : orders) {
                System.out.println(CYAN + "ORDER: " + RESET + o);
                for (OrderLine ol : o.getLines()) {
                    System.out.println(CYAN + "ORDERLINE: " + RESET + ol);
                    int toAllocate = ol.getQuantity();

                    for (Aisle a : warehouse.getAisles()) {
                        if (toAllocate == 0) break;
                        for (Bay b : a.getBays()) {
                            if (toAllocate == 0) break;
                            for (Box bo : b.getInventory()) {
                                if (toAllocate == 0) break;

                                boolean skuOk = bo.getProduct().getSkuID().equals(ol.getSku());
                                boolean dateOk = (bo.getExpDate() == null) || (o.getDueDate().compareTo(bo.getExpDate()) < 0);
                                boolean hasQty = bo.getQuantity() > 0;

                                if (!skuOk || !dateOk || !hasQty) {
                                    System.out.println(bo + " - " + RED + "UNDISPATCHBLE" + RESET);
                                    continue;
                                }

                                System.out.println(bo + " - " + YELLOW + "PARTIAL" + RESET);

                                int canTake = Math.min(toAllocate, bo.getQuantity());
                                if (canTake <= 0) continue;

                                addSlicedAllocations(o, ol, a, b, bo, canTake, allocation);

                                bo.setQuantity(bo.getQuantity() - canTake);
                                toAllocate -= canTake;
                            }
                        }
                    }
                    System.out.println(GREEN + "ORDER LINE FINISHED" + RESET);
                    System.out.println(line(72));
                }
                System.out.println(GREEN + "ORDER FINISHED: " + RESET + o);
                System.out.println(line(72));
            }
        } else {
            for (Order o : orders) {
                System.out.println(GREEN + "ORDER: " + RESET + o);
                for (OrderLine ol : o.getLines()) {
                    System.out.println(GREEN + "ORDERLINE: " + RESET + ol);

                    boolean allocated = false;

                    outer:
                    for (Aisle a : warehouse.getAisles()) {
                        for (Bay b : a.getBays()) {
                            for (Box bo : b.getInventory()) {
                                boolean skuOk = bo.getProduct().getSkuID().equals(ol.getSku());
                                boolean dateOk = (bo.getExpDate() == null) || (bo.getExpDate().compareTo(o.getDueDate()) > 0);
                                boolean exactQty = bo.getQuantity() == ol.getQuantity();

                                if (skuOk && dateOk && exactQty) {
                                    System.out.println(GREEN + bo + " - ELIGIBLE" + RESET);

                                    addSlicedAllocations(o, ol, a, b, bo, bo.getQuantity(), allocation);

                                    bo.setQuantity(0);
                                    allocated = true;
                                    break outer;
                                } else {
                                    System.out.println(bo + " - " + RED + "UNDISPATCHBLE" + RESET);
                                }
                            }
                        }
                    }

                    if (!allocated) {
                        System.out.println(RED + "No exact box found for STRICT allocation of " + ol + RESET);
                    }

                    System.out.println(GREEN + "ORDER LINE FINISHED" + RESET);
                    System.out.println(line(72));
                }
                System.out.println(GREEN + "ORDER FINISHED: " + RESET + o);
                System.out.println(line(72));
            }
        }
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }

    private static void banner(String title) {
        System.out.println(CYAN + line(72));
        System.out.println(center(title, 72));
        System.out.println(line(72) + RESET);
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < pad; i++) left.append(' ');
        return left + s;
    }
}
