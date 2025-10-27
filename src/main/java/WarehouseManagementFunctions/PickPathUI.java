package WarehouseManagementFunctions;

import controller.PickPathController;
import controller.PickPathController.PickPathResult;
import domain.*;
import repository.WarehouseRepository;
import ui.WarehouseManagementMenu;

import java.util.ArrayList;
import java.util.List;

public class PickPathUI implements Runnable {

    private List<Allocation> allocations = null;

    @Override
    public void run() {

        allocations = WarehouseRepository.getAllocation();

            System.out.println("\n=== PICK PATH SEQUENCING (USEI04) ===");

            if (allocations == null || allocations.isEmpty()) {
                System.out.println("\n[ERROR] Allocations not generated yet. Please generate the picking plan first.");
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
        System.out.println("\nStrategy A (Ascending by aisle, then bay):");
        printPath(A);

        System.out.println("\nStrategy B (Nearest-Neighbour):");
        printPath(B);

        Runnable war = new WarehouseManagementMenu();
        war.run();
    }

    private void printPath(PickPathResult res) {
        System.out.print("Path: (0,0) ");
        for (PickStop s : res.getPath()) {
            System.out.print("-> (" + s.getAisle() + "," + s.getBay() + ")");
        }
        System.out.println("\nTotal distance = " + res.getTotalDistance());
    }

    // Exemplo standalone (podes remover e integrar no teu menu principal
}