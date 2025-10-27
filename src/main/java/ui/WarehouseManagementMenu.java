package ui;

import WarehouseManagementFunctions.PickPathUI;
import WarehouseManagementFunctions.PickingPlanUI;
import WarehouseManagementFunctions.OrderEligibilityNdAllocation;
import WarehouseManagementFunctions.WagonUnloading;
import WarehouseManagementFunctions.ReturnsInspectionUI;
import domain.Option;
import domain.Utils;


public class WarehouseManagementMenu implements Runnable{

    @Override
    public void run() {
        menu();
    }



    public static void menu() {


        System.out.println("####WAREHOUSE MANAGEMENT MENU####");

        Option[] options = new Option[7];
        Runnable unl = new WagonUnloading();
        Runnable all = new OrderEligibilityNdAllocation();
        Runnable picking = new PickingPlanUI();
        Runnable pick = new PickPathUI();
        Runnable returns = new ReturnsInspectionUI(); // New line
        Runnable stats = new StatsPrinterUI();
        Runnable main = new MainMenu();

        options[0] = new Option("Unload Wagons into warehouse", unl);
        options[1] = new Option("Elect products and allocate them for current orders", all);
        options[2] = new Option("Generate Picking Plan", picking);
        options[3] = new Option("Pick Path", pick);
        options[4] = new Option("Inspect Returned Goods", returns);
        options[5] = new Option("View Warehouse Statistics", stats);
        options[6] = new Option("Go Back", main);
        Utils.listAndSelect(options);
    }
}
