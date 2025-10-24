package WarehouseManagementFunctions;

import controller.PickPathController;
import controller.PickPathController.PickPathResult;
import domain.*;

import java.util.ArrayList;
import java.util.List;

public class PickPathUI implements Runnable {

    private final List<Allocation> allocations;

    public PickPathUI(List<Allocation> allocations) {
        this.allocations = allocations;
    }

    @Override
    public void run() {
        System.out.println("\n=== PICK PATH SEQUENCING (USEI04) ===");

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
    }

    private void printPath(PickPathResult res) {
        System.out.print("Path: (0,0) ");
        for (PickStop s : res.getPath()) {
            System.out.print("-> (" + s.getAisle() + "," + s.getBay() + ")");
        }
        System.out.println("\nTotal distance = " + res.getTotalDistance());
    }

    // Exemplo standalone (podes remover e integrar no teu menu principal)
    public static void main(String[] args) {
        List<Allocation> allocations = new ArrayList<>();
        // Exemplo do enunciado: bays (1,8), (2,2), (3,4)
        allocations.add(new Allocation("ORD", 1, "SKU", "B1", 1, 8, 1, 1.0));
        allocations.add(new Allocation("ORD", 2, "SKU", "B2", 2, 2, 1, 1.0));
        allocations.add(new Allocation("ORD", 3, "SKU", "B3", 3, 4, 1, 1.0));

        new PickPathUI(allocations).run();
    }
}