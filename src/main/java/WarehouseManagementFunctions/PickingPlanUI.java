package WarehouseManagementFunctions;

import controller.PickingPlanController;
import domain.*;
import repository.WarehouseRepository;
import ui.WarehouseManagementMenu;
import java.util.List;

public class PickingPlanUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

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

        PackingMode mode = PackingMode.BFD;               // ou FF / FFD
        AllocationPolicy policy = AllocationPolicy.SPLIT_LINE;// ou DEFER_LINE
        double capacity = OrderEligibilityNdAllocation.MAX_ALLOCATION_KG;

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
}
