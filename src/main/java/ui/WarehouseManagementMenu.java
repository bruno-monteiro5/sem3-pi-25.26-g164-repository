package ui;

import domain.Option;
import domain.Utils;
import WarehouseManagementFunctions.OrderEligibilityNdAllocation;
import WarehouseManagementFunctions.WagonUnloading;

public class WarehouseManagementMenu implements Runnable{

    @Override
    public void run() {
        menu();
    }



    public static void menu() {


        System.out.println("####WAREHOUSE MANAGEMENT MENU####");

        Option[] options = new Option[3];
        Runnable unl = new WagonUnloading();
        Runnable all = new OrderEligibilityNdAllocation();
        Runnable main = new MainMenu();
        options[0] = new Option("Unload Wagons into warehouse", unl);
        options[1] = new Option("Elect products and allocate them for current orders", all);
        options[2] = new Option("Go Back", main);
        Utils.listAndSelect(options);
    }
}
