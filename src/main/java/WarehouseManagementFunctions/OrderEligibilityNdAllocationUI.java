package WarehouseManagementFunctions;

import controller.OrderEligibilityNdAllocationController;
import controller.WagonUnloadingController;
import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.FileNotFoundException;
import java.util.*;

public class OrderEligibilityNdAllocationUI implements Runnable {

    public static Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
    public static List<Order> orders = new ArrayList<>();
    public static String filenameorders = "warehouseMngt/train_station_dataset/orders.csv";
    public static String filenameorderlines = "warehouseMngt/train_station_dataset/order_lines.csv";
    public static List<Allocation> allocation = new ArrayList<>();

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    public void run() {

        warehouse = WarehouseRepository.getCurrentWarehouse();

        banner("ORDER ELIGIBILITY & ALLOCATION");

        //fills the warehouse if it senses it is null
        if (WagonUnloadingController.warehouse == null) {
            System.out.println(RED + "Warehouse is null." + RESET + "Beginning warehouse initialization...");
            Runnable run = new WagonUnloadingUI();
            run.run();
            warehouse = WarehouseRepository.getCurrentWarehouse();
            System.out.println(GREEN + "Warehouse initialized." + RESET);
            System.out.println(line(72));
        }else{
            WarehouseRepository.setCurrentWarehouse(WagonUnloadingController.warehouse);
            warehouse = WagonUnloadingController.warehouse;
        }

        try {
            OrderEligibilityNdAllocationController.fillOrders();
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

        System.out.println(GREEN + "Allocation workflow finished." + RESET);
        System.out.println(line(72));

        WarehouseRepository.setAllocation(OrderEligibilityNdAllocationController.allocation);

        Runnable WarehouseManagementMenu = new WarehouseManagementMenu();
        WarehouseManagementMenu.run();
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
            OrderEligibilityNdAllocationController.allocateBoxes(true);
        } else {
            System.out.println(YELLOW + "Partial allocation selected." + RESET);
            System.out.println(line(72));
            OrderEligibilityNdAllocationController.allocateBoxes(false);
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
