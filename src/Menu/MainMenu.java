package Menu;

import Classes.Option;
import Classes.Utils;

public class MainMenu implements Runnable{

    @Override
    public void run() {
        menu();
    }

    public static void menu(){
        Option[] options = new Option[2];
        Runnable warehouse = new WarehouseManagementMenu();

        System.out.println("####MAIN MENU####");

        options[0] = new Option("Warehouse Management Functions", warehouse);
        options[1] = new Option("Exit", () -> System.exit(0));
        Utils.listAndSelect(options);
    }
}
