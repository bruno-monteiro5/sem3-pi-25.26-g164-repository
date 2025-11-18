package ui;

//import WarehouseManagementFunctions.StationIndexUI;

import domain.Option;
import domain.Utils;

public class InfraestructureIndexMenu implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN  = COLOR_ON ? "\u001B[36m" : "";
    private static final String RED   = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP   = "─";

    @Override
    public void run() { menu(); }

    public static void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                       INFRASTRUCTURE INDEX MENU");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[3];
        Runnable tzIndex = new BuildIndexesUI();
        Runnable build2DTree = new Build2DTreeUI();
        Runnable main    = new MainMenu();

        options[0] = new Option("Time-Zone Index & Windowed Queries (USEI06)", tzIndex);
        options[1] = new Option("Build 2D-Tree (USEI07 && USEI08)", build2DTree);
        options[2] = new Option("Go Back", main);

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
