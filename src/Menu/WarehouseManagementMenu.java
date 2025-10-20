package Menu;

import Classes.Option;
import Classes.Utils;
import WarehouseManagementFunctions.WagonUnloading;

import java.sql.SQLOutput;

public class WarehouseManagementMenu implements Runnable{

    @Override
    public void run() {
        menu();
    }



    public static void menu() {


        System.out.println("####WAREHOUSE MANAGEMENT MENU####");

        Option[] options = new Option[2];
        Runnable unl = new WagonUnloading();
        Runnable main = new MainMenu();
        options[0] = new Option("Unload Wagons into warehouse", unl);
        options[1] = new Option("Go Back", main);
        Utils.listAndSelect(options);
    }
}
