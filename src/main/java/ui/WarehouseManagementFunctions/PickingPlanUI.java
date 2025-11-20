package ui.WarehouseManagementFunctions;

import controller.OrderEligibilityNdAllocationController;
import controller.PickingPlanController;
import domain.*;
import repository.WarehouseRepository;
import ui.WarehouseManagementMenu;
import java.util.List;
import java.util.Scanner;

public class PickingPlanUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";


    Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println();
        System.out.println(CYAN + line(72));
        System.out.println(center("GENERATE PICKING PLAN (USEI03)", 72));
        System.out.println(line(72) + RESET);

        List<Allocation> allocations = WarehouseRepository.getAllocation();

        if (allocations == null || allocations.isEmpty()) {
            System.out.println(RED + "[ERROR] There are no allocations available." + RESET);
            System.out.println(line(72));
            Runnable war = new WarehouseManagementMenu();
            war.run();
        }

        PackingMode mode = askPackingMode();
        AllocationPolicy policy = askAllocationPolicy();
        double capacity = OrderEligibilityNdAllocationController.MAX_ALLOCATION_KG;

        PickingPlanController controller = new PickingPlanController();
        PickingPlan plan = controller.generatePlan(allocations, mode, capacity, policy);

        printPickingPlan(plan);

        System.out.println(GREEN + "\nPicking plan generation complete." + RESET);
        System.out.println(line(72));

        Runnable war = new WarehouseManagementMenu();
        war.run();
    }

    private void printPickingPlan(PickingPlan plan) {
        System.out.println(CYAN + "\n==== PICKING PLAN ====" + RESET);
        System.out.println("Mode: " + plan.getMode());
        System.out.println("Total trolleys: " + plan.getTrolleys().size());
        System.out.println(line(72));

        for (Trolley t : plan.getTrolleys()) {
            System.out.printf(GREEN + "\nTrolley #%d" + RESET + " | %.2f / %.2f kg (%.1f%% used)\n",
                    t.getId(), t.getUsedKg(), t.getWeightCapacity(), t.percUtilization());
            for (Allocation a : t.getItems()) {
                System.out.printf("  Order=%s | Line=%d | Aisle=%d | Bay=%d | Box=%s | SKU=%s | Qty=%.0f\n",
                        a.getOrderId(), a.getLineNo().getLineNo(), a.getAisle().getAisleID(),
                        a.getBay().getBayID(), a.getBoxId(), a.getSku(), a.getQuantity());
            }
        }

        printPartialAllocations(plan.getPartial());
        printDeferredAllocations(plan.getDeferred());
    }

    private void printPartialAllocations(List<PartialAllocationLog> partials) {
        if (partials == null || partials.isEmpty()) return;
        System.out.println(CYAN + "\n-- PARTIAL ALLOCATIONS --" + RESET);
        System.out.println(line(72));
        for (PartialAllocationLog p : partials) {
            System.out.printf("Order=%s | Line=%d | SKU=%s | AllocatedNow=%.0f | Deferred=%.0f\n",
                    p.getOrderId(), p.getLineNo(), p.getSku(),
                    p.getQuantityNow(), p.getQuantityDeferred());
        }
    }

    private void printDeferredAllocations(List<Allocation> deferred) {
        if (deferred == null || deferred.isEmpty()) return;
        System.out.println(RED + "\n-- SKIPPED DUE TO CAPACITY --" + RESET);
        System.out.println(line(72));
        for (Allocation a : deferred) {
            System.out.printf("Order=%s | Line=%d | SKU=%s | Qty=%.0f\n",
                    a.getOrderId(), a.getLineNo(), a.getSku(), a.getQuantity());
        }
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

    private PackingMode askPackingMode() {
        System.out.println(GREEN + "\nSelect packing heuristic:" + RESET);
        System.out.println("  1) FF  - First Fit");
        System.out.println("  2) FFD - First Fit Decreasing");
        System.out.println("  3) BFD - Best Fit Decreasing");

        String option;
        boolean valid = false;
        PackingMode mode = null;

        do {
            System.out.print("Option (1-3): ");
            option = scanner.nextLine().trim();

            if (option.equals("1")) {
                mode = PackingMode.FF;
                valid = true;
            } else if (option.equals("2")) {
                mode = PackingMode.FFD;
                valid = true;
            } else if (option.equals("3")) {
                mode = PackingMode.BFD;
                valid = true;
            } else {
                System.out.println(RED + "Invalid option" + RESET);
            }
        } while (!valid);

        return mode;
    }


    private AllocationPolicy askAllocationPolicy() {
        System.out.println(GREEN + "\nIf a single line doesn't fit in remaining capacity:" + RESET);
        System.out.println("  1) SPLIT_LINE ");
        System.out.println("  2) DEFER_LINE ");

        String option;
        boolean valid = false;
        AllocationPolicy policy = null;

        do {
            System.out.print("Option (1-2): ");
            option = scanner.nextLine().trim();

            if (option.equals("1")) {
                policy = AllocationPolicy.SPLIT_LINE;
                valid = true;
            } else if (option.equals("2")) {
                policy = AllocationPolicy.DEFER_LINE;
                valid = true;
            } else {
                System.out.println(RED + "Invalid option" + RESET);
            }
        } while (!valid);

        return policy;
    }

}
