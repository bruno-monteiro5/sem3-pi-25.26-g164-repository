package ui.WarehouseManagementFunctions;

import controller.PickPathController;
import controller.PickPathController.PickPathResult;
import domain.*;
import repository.WarehouseRepository;
import ui.WarehouseManagementMenu;

import java.util.List;

public class PickPathUI implements Runnable {

    private List<Allocation> allocations = null;

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    @Override
    public void run() {

        allocations = WarehouseRepository.getAllocation();

        System.out.println();
        System.out.println(CYAN + line(72));
        System.out.println(center("PICK PATH SEQUENCING (USEI04)", 72));
        System.out.println(line(72) + RESET);

        if (allocations == null || allocations.isEmpty()) {
            System.err.println(RED + "[ERROR] Allocations not generated yet. Please generate the picking plan first." + RESET);
            System.out.println("Going to the main menu...\n");
            new WarehouseManagementMenu().run();
            return;
        }

        PickPathController controller = new PickPathController();

        // 1) Fundir bays duplicadas (somando qty)
        List<PickStop> stops = controller.mergeStops(allocations);

        // 2) Estratégia A
        PickPathResult A = controller.sequenceStrategyA(stops);

        // 3) Estratégia B
        PickPathResult B = controller.sequenceStrategyB(stops);

        // 4) Output
        System.out.println(CYAN + "\nStrategy A  — Ascending by aisle, then bay" + RESET);
        System.out.println(line(72));
        printPath(A);

        System.out.println(CYAN + "\nStrategy B  — Nearest-Neighbour" + RESET);
        System.out.println(line(72));
        printPath(B);

        System.out.println(GREEN + "\nPick path sequencing complete." + RESET);
        System.out.println(line(72));

        Runnable war = new WarehouseManagementMenu();
        war.run();
    }

    private void printPath(PickPathResult res) {
        System.out.println("Path:");
        System.out.print("  (0,0) ");
        for (PickStop s : res.getPath()) {
            System.out.print("-> (" + s.getAisle() + "," + s.getBay() + ")");
        }
        System.out.println();
        System.out.println("Total distance: " + res.getTotalDistance());
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
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