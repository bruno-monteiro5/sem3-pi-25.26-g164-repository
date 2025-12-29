package ui;

import domain.Option;
import domain.Utils;
import ui.AssemblyUI.TrainAssemblyUI;
import ui.FreightUI.RoutePlannerUI;
import ui.RailwayUI.TravelTimeCalculatorUI;
import ui.SchedulingUI.SchedulerUI;

public class MainMenu implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    @Override
    public void run() {
        menu();
    }

    public static void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                                MAIN MENU");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[8];
        Runnable warehouse = new WarehouseManagementMenu();
        Runnable travelTime = new TravelTimeCalculatorUI();
        Runnable infraIndex  = new InfraestructureIndexMenu();
        Runnable scheduler = new SchedulerUI();
        Runnable routePlanner = new RoutePlannerUI();
        Runnable optimizer = new RailwayNetworkMenu();
        Runnable assembler = new TrainAssemblyUI();
        Runnable exit = new Runnable() {
            public void run() {
                System.out.println(CYAN + "\n✓ Thank you for using Logistics On Rails!" + RESET);
                System.exit(0);
            }
        };

        options[0] = new Option("Warehouse Management Functions (Sprint 1 ESINF)", warehouse);
        options[1] = new Option("Infrastructure Index (Sprint 2 ESINF)", infraIndex);
        options[2] = new Option("Railway Network Optimization (Sprint 3 ESINF)", optimizer);
        options[3] = new Option("Calculate Travel Time (USLP03)", travelTime);
        options[4] = new Option("Train Dispatch Scheduler (USLP07 & USLP10)", scheduler);
        options[5] = new Option("Route Planner for Freights (USLP08)", routePlanner);
        options[6] = new Option("Assemble and assign trains (USLP09)", assembler);
        options[7] = new Option(RED + "Exit" + RESET, exit);

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