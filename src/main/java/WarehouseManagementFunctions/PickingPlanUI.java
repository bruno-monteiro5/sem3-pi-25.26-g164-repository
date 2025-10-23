package WarehouseManagementFunctions;

import controller.PickingPlanController;
import domain.*;

import java.util.ArrayList;
import java.util.List;

public class PickingPlanUI implements Runnable {

    @Override
    public void run() {
        System.out.println("\n=== GENERATE PICKING PLAN (USEI03) ===");

        List<Allocation> allocations = new ArrayList<>();
        allocations.add(new Allocation("ORD001", 1, "SKU123", "BOX01", 2, 4, 10, 2.5));
        allocations.add(new Allocation("ORD001", 2, "SKU999", "BOX02", 3, 1, 20, 1.0));
        allocations.add(new Allocation("ORD002", 1, "SKU555", "BOX03", 1, 3, 50, 0.8));
        allocations.add(new Allocation("ORD003", 1, "SKU888", "BOX05", 5, 2, 40, 3.0));

        PackingMode mode = PackingMode.BFD;               // ou FF / FFD
        AllocationPolicy policy = AllocationPolicy.SPLIT_LINE;  // ou DEFER_LINE
        double capacity = 200.0;

        PickingPlanController controller = new PickingPlanController();
        PickingPlan plan = controller.generatePlan(allocations, mode, capacity, policy);

        printPickingPlan(plan);
    }


    private void printPickingPlan(PickingPlan plan) {
        System.out.println("\n==== PICKING PLAN ====");
        System.out.println("Mode: " + plan.getMode());
        System.out.println("Total trolleys: " + plan.getTrolleys().size());

        for (Trolley t : plan.getTrolleys()) {
            System.out.printf("\nTrolley #%d | %.2f / %.2f kg (%.1f%% used)\n",
                    t.getId(), t.getUsedKg(), t.getWeightCapacity(), t.percUtilization());
            for (Allocation a : t.getItems()) {
                System.out.printf("  order=%s line=%d aisle=%d bay=%d box=%s sku=%s qty=%.0f\n",
                        a.getOrderId(), a.getLineNo(), a.getAisle(), a.getBay(),
                        a.getBoxId(), a.getSku(), a.getQuantity());
            }
        }

        printPartialAllocations(plan.getPartial());
        printDeferredAllocations(plan.getDeferred());
    }

    private void printPartialAllocations(List<PartialAllocationLog> partials) {
        if (partials == null || partials.isEmpty()) return;
        System.out.println("\n-- Partial Allocations --");
        for (PartialAllocationLog p : partials) {
            System.out.printf("Order=%s Line=%d SKU=%s | AllocatedNow=%.0f | Deferred=%.0f\n",
                    p.getOrderId(), p.getLineNo(), p.getSku(),
                    p.getQuantityNow(), p.getQuantityDeferred());
        }
    }

    private void printDeferredAllocations(List<Allocation> deferred) {
        if (deferred == null || deferred.isEmpty()) return;
        System.out.println("\n-- Skipped due to capacity --");
        for (Allocation a : deferred) {
            System.out.printf("Order=%s Line=%d SKU=%s Qty=%.0f\n",
                    a.getOrderId(), a.getLineNo(), a.getSku(), a.getQuantity());
        }
    }
}

