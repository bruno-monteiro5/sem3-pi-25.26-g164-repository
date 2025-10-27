package ui;

import domain.Option;
import domain.Utils;

public class MainMenu implements Runnable{

    @Override
    public void run() {
        menu();
    }

    public static void menu(){
        Option[] options = new Option[3];
        Runnable warehouse = new WarehouseManagementMenu();
        Runnable travelTime = new TravelTimeCalculatorUI();

        System.out.println("####MAIN MENU####");

        options[0] = new Option("Warehouse Management Functions", warehouse);
        options[1] = new Option("Calculate Travel Time (USLP03)", travelTime);
        options[2] = new Option("Exit", () -> System.exit(0));
        Utils.listAndSelect(options);
    }
}
