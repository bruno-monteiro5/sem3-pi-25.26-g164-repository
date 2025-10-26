package controller;

import domain.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PickingPlanController}.
 * <p>
 * Each test verifies a distinct scenario of the single public method
 * {@link PickingPlanController#generatePlan(List, PackingMode, double, AllocationPolicy)}.
 */
class PickingPlanControllerTest {

    private Allocation alloc(String orderId, int lineNo, String sku,
                             int aisleId, int bayId,
                             int boxQty, double allocQty, double unitWeight) {
        Order order = new Order(orderId, 1, "2025-12-31");
        OrderLine line = new OrderLine(lineNo, sku, boxQty);
        Items items = new Items(sku, "Prod-" + sku, "Cat", "Pack", 1.0f, (float) unitWeight);
        Box box = new Box(items, boxQty);
        Aisle aisle = new Aisle(aisleId);
        Bay bay = new Bay(bayId, 10);
        return new Allocation(order, line, sku, box, aisle, bay, allocQty, unitWeight);
    }

    private double usedKgOf(Trolley t) {
        return t.getUsedKg();
    }

    /**
     * Verifies that null allocations are rejected.
     *
     * Expected:
     *  - {@link IllegalArgumentException} with message "allocations == null".
     */
    @Test
    void generatePlan_throwsWhenAllocationsNull() {
        PickingPlanController ctrl = new PickingPlanController();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ctrl.generatePlan(null, PackingMode.FF, 100.0, AllocationPolicy.SPLIT_LINE));
        assertEquals("allocations == null", ex.getMessage());
    }

    /**
     * Verifies that non-positive capacity is rejected.
     *
     * Expected:
     *  - {@link IllegalArgumentException} with message "trolleyCapacityKg must be > 0".
     */
    @Test
    void generatePlan_throwsWhenCapacityInvalid() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ctrl.generatePlan(list, PackingMode.FF, 0.0, AllocationPolicy.SPLIT_LINE));
        assertEquals("trolleyCapacityKg must be > 0", ex.getMessage());
    }

    /**
     * Verifies that null mode is rejected.
     *
     * Expected:
     *  - {@link IllegalArgumentException} with message "mode == null".
     */
    @Test
    void generatePlan_throwsWhenModeNull() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ctrl.generatePlan(list, null, 100.0, AllocationPolicy.SPLIT_LINE));
        assertEquals("mode == null", ex.getMessage());
    }

    /**
     * Verifies that null allocation policy is rejected.
     *
     * Expected:
     *  - {@link IllegalArgumentException} with message "allocationPolicy == null".
     */
    @Test
    void generatePlan_throwsWhenPolicyNull() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ctrl.generatePlan(list, PackingMode.FF, 100.0, null));
        assertEquals("allocationPolicy == null", ex.getMessage());
    }

    /**
     * FF: Single allocation that fits entirely in a single trolley.
     *
     * Expected:
     *  - One trolley with usedKg == totalWeight of the allocation.
     */
    @Test
    void generatePlan_FF_singleFitInOneTrolley() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of(
                alloc("O1", 1, "S1", 1, 1, 10, 5.0, 2.0) // 10 kg
        );

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.FF, 100.0, AllocationPolicy.SPLIT_LINE);

        assertEquals(PackingMode.FF, plan.getMode());
        assertEquals(1, plan.getNumberOfTrolleys());
        assertEquals(10.0, plan.getTrolleys().get(0).getUsedKg(), 1e-9);
    }

    /**
     * FF + SPLIT_LINE: The last trolley has some remaining capacity; next allocation
     * partially fits (split) and the remainder goes to a new trolley.
     *
     * Construction:
     *  - T1 gets first allocation (40 kg of 100 kg capacity).
     *  - Next allocation weighs 30 kg (10 units × 3 kg): 60 kg remain in T1, so all fits → no split.
     *  - To force split, make the second allocation heavier per-unit and quantity such that only part fits.
     *
     * Here:
     *  - First alloc: 20×2.0 kg = 40 kg (opens T1 with 60 kg free)
     *  - Second alloc: 30×3.0 kg = 90 kg → only 20 units fit (20×3=60 kg), 10 units spill to T2.
     *
     * Expected:
     *  - Two trolleys: T1 used ≈ 100 kg, T2 used ≈ 30 kg.
     */
    @Test
    void generatePlan_FF_splitLineWhenPartiallyFitsLastTrolley() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = new ArrayList<>();
        list.add(alloc("O1", 1, "S1", 1, 1, 50, 20.0, 2.0)); // 40 kg -> T1
        list.add(alloc("O1", 2, "S2", 1, 1, 50, 30.0, 3.0)); // 90 kg total

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.FF, 100.0, AllocationPolicy.SPLIT_LINE);

        assertEquals(2, plan.getNumberOfTrolleys());
        // After split: T1 has 40 + 60 = 100, T2 has 30
        assertEquals(100.0, usedKgOf(plan.getTrolleys().get(0)), 1e-9);
        assertEquals(30.0, usedKgOf(plan.getTrolleys().get(1)), 1e-9);
    }

    /**
     * FF + DEFER_LINE: When not fitting and trolleys already exist, allocation is deferred
     * (controller also creates a new trolley and places the allocation there).
     *
     * Expected:
     *  - New trolley created; total trolleys increases by one; weights reflect placement.
     */
    @Test
    void generatePlan_FF_deferLineCreatesNewTrolley() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = new ArrayList<>();
        // First allocation opens T1 with 90/100 used
        list.add(alloc("O1", 1, "S1", 1, 1, 50, 30.0, 3.0)); // 90 kg
        // Next one is 30 kg and won't fit in T1; DEFER_LINE path taken
        list.add(alloc("O1", 2, "S2", 1, 1, 50, 10.0, 3.0)); // 30 kg

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.FF, 100.0, AllocationPolicy.DEFER_LINE);

        assertEquals(2, plan.getNumberOfTrolleys());
        assertEquals(90.0, usedKgOf(plan.getTrolleys().get(0)), 1e-9);
        assertEquals(30.0, usedKgOf(plan.getTrolleys().get(1)), 1e-9);
    }
    /**
     * FFD + DEFER_LINE: no splitting.
     * Esperated: T1=60, T2=100.
     */
    @Test
    void generatePlan_FFD_noSplit_deferLine() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of(
                alloc("O", 1, "A", 1, 1, 100, 20.0, 3.0), // 60 kg
                alloc("O", 2, "B", 1, 1, 100, 25.0, 2.0), // 50 kg
                alloc("O", 3, "C", 1, 1, 100, 25.0, 2.0)  // 50 kg
        );

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.FFD, 100.0, AllocationPolicy.DEFER_LINE);

        assertEquals(2, plan.getNumberOfTrolleys());
        assertEquals(60.0, usedKgOf(plan.getTrolleys().get(0)), 1e-9);
        assertEquals(100.0, usedKgOf(plan.getTrolleys().get(1)), 1e-9);
    }

    /**
     * FFD + SPLIT_LINE: splitting allowed.
     * Esperated: T1=100 (60 + 40), T2=60 (10 + 50).
     */
    @Test
    void generatePlan_FFD_withSplit_splitLine() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of(
                alloc("O", 1, "A", 1, 1, 100, 20.0, 3.0), // 60 kg
                alloc("O", 2, "B", 1, 1, 100, 25.0, 2.0), // 50 kg
                alloc("O", 3, "C", 1, 1, 100, 25.0, 2.0)  // 50 kg
        );

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.FFD, 100.0, AllocationPolicy.SPLIT_LINE);

        assertEquals(2, plan.getNumberOfTrolleys());
        assertEquals(100.0, usedKgOf(plan.getTrolleys().get(0)), 1e-9);
        assertEquals(60.0,  usedKgOf(plan.getTrolleys().get(1)), 1e-9);
    }



    /**
     * BFD: Chooses the trolley that leaves the least remaining space after placement.
     *
     * Scenario:
     *  - Capacity 100, weights 70, 40, 40 (after sort: 70, 40, 40).
     *  - BFD tends to pack into 2 trolleys: T1: 70+30 (part of 40 if split allowed) or 70 alone;
     *    With SPLIT_LINE off we still expect 2 trolleys because 40+40 fits in T2.
     *
     * Expected:
     *  - Two trolleys with used weights 70 and 80.
     */
    @Test
    void generatePlan_BFD_bestFitLeadsToTwoTrolleys() {
        PickingPlanController ctrl = new PickingPlanController();
        List<Allocation> list = List.of(
                alloc("O", 1, "A", 1, 1, 100, 35.0, 2.0), // 70 kg
                alloc("O", 2, "B", 1, 1, 100, 20.0, 2.0), // 40 kg
                alloc("O", 3, "C", 1, 1, 100, 20.0, 2.0)  // 40 kg
        );

        PickingPlan plan = ctrl.generatePlan(list, PackingMode.BFD, 100.0, AllocationPolicy.DEFER_LINE);

        assertEquals(2, plan.getNumberOfTrolleys());
        assertEquals(70.0, usedKgOf(plan.getTrolleys().get(0)), 1e-9);
        assertEquals(80.0, usedKgOf(plan.getTrolleys().get(1)), 1e-9);
    }
}
