package ui;

import WarehouseManagementFunctions.PickPathUI;
import WarehouseManagementFunctions.PickingPlanUI;
import WarehouseManagementFunctions.OrderEligibilityNdAllocation;
import WarehouseManagementFunctions.WagonUnloading;
import WarehouseManagementFunctions.ReturnsInspectionUI;
import domain.Option;
import domain.Utils;

public class WarehouseManagementMenu implements Runnable {

    // design comentado
    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN  = COLOR_ON ? "\u001B[36m" : "";
    private static final String RED   = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP   = "─";

    @Override
    public void run() {
        menu();
    }

    public static void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                        WAREHOUSE MANAGEMENT MENU");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[7];
        Runnable unl     = new WagonUnloading();
        Runnable all     = new OrderEligibilityNdAllocation();
        Runnable picking = new PickingPlanUI();
        Runnable pick    = new PickPathUI();
        Runnable returns = new ReturnsInspectionUI();
        Runnable stats   = new StatsPrinterUI();
        Runnable main    = new MainMenu();

        options[0] = new Option("Unload Wagons into Warehouse", unl);
        options[1] = new Option("Select Products & Allocate Orders", all);
        options[2] = new Option("Generate Picking Plan", picking);
        options[3] = new Option("Compute Pick Path (Sequencing)", pick);
        options[4] = new Option("Inspect Returned Goods", returns);
        options[5] = new Option("View Warehouse Statistics"+ RED, stats);
        options[6] = new Option( "Go Back" + RESET, main);

        Utils.listAndSelect(options);
    }

    private static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
