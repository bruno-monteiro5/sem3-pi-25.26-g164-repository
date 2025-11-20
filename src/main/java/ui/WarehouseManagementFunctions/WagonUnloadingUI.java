package ui.WarehouseManagementFunctions;

import controller.WagonUnloadingController;
import domain.*;
import ui.WarehouseManagementMenu;
import repository.WarehouseRepository;

import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;

public class WagonUnloadingUI implements Runnable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // design comentado
    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String RED = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP = "─";

    @Override
    public void run() {
        try {
            WagonUnloadingController.beginWarehouse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            WagonUnloadingController.beginProducts();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            WagonUnloadingController.removeCargoFromWagon(WagonUnloadingController.warehouse, WagonUnloadingController.products);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(WagonUnloadingController.warehouse != null){
            System.out.println("WAREHOUSE SUCESSFULLY FILLED");
            System.out.println("CURRENT OCCUPATION");
            for(Aisle aisle : WagonUnloadingController.warehouse.getAisles()){
                    System.out.println(aisle.toString());
            }
        }else{
            System.out.println("ERROR: WAREHOUSE NOT FILLED");
        }

        WarehouseRepository.setCurrentWarehouse(WagonUnloadingController.warehouse);

        Runnable menu = new WarehouseManagementMenu();
        menu.run();
    }
}