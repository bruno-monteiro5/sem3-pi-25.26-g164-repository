package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PickingPlan}.
 * <p>
 * Each test method targets exactly one public member of {@code PickingPlan}
 * and documents the scenario, preconditions, and expected outcome.
 */
class PickingPlanTest {

    // ---------------------------------------------------------------------
    // Helpers to build real domain objects (no stubs).
    // ---------------------------------------------------------------------

    private Allocation allocation(String orderId, int lineNo, String sku,
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

    private Trolley trolleyWith(int id, double capacity, Allocation... allocations) {
        Trolley t = new Trolley(id, capacity);
        for (Allocation a : allocations) t.addItem(a);
        return t;
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    /**
     * Verifies that the constructor initializes mode, defensively copies the trolleys list,
     * and initializes partial/deferred as empty lists.
     *
     * Preconditions:
     *  - Valid packing mode and trolley list.
     *
     * Expected:
     *  - {@link PickingPlan#getMode()} returns supplied mode;
     *  - {@link PickingPlan#getTrolleys()} contains same elements but is a different list;
     *  - {@link PickingPlan#getPartial()} and {@link PickingPlan#getDeferred()} are empty.
     *
     * @see PickingPlan#PickingPlan(PackingMode, List)
     */
    @Test
    void constructor_initializesFieldsAndDefensiveCopy() {
        List<Trolley> source = new ArrayList<>();
        source.add(trolleyWith(1, 100.0));
        source.add(trolleyWith(2, 50.0));

        PickingPlan plan = new PickingPlan(PackingMode.FF, source);

        assertEquals(PackingMode.FF, plan.getMode());
        assertEquals(2, plan.getTrolleys().size());
        assertNotSame(source, plan.getTrolleys());
        assertTrue(plan.getPartial().isEmpty());
        assertTrue(plan.getDeferred().isEmpty());

        // Mutating original list does not affect the plan
        source.clear();
        assertEquals(2, plan.getTrolleys().size());
    }

    /**
     * Tests {@link PickingPlan#getMode()}.
     *
     * Expected:
     *  - Returns the mode provided at construction.
     */
    @Test
    void getMode_returnsSuppliedMode() {
        PickingPlan plan = new PickingPlan(PackingMode.BFD, List.of());
        assertEquals(PackingMode.BFD, plan.getMode());
    }

    /**
     * Tests {@link PickingPlan#getTrolleys()} returns the internal list with expected content.
     *
     * Scenario:
     *  - Build a plan with two trolleys and verify order and references.
     */
    @Test
    void getTrolleys_returnsExpectedContent() {
        Trolley t1 = trolleyWith(1, 100.0);
        Trolley t2 = trolleyWith(2, 50.0);
        PickingPlan plan = new PickingPlan(PackingMode.FFD, List.of(t1, t2));

        assertEquals(2, plan.getTrolleys().size());
        assertSame(t1, plan.getTrolleys().get(0));
        assertSame(t2, plan.getTrolleys().get(1));
    }

    /**
     * Tests {@link PickingPlan#getPartial()} initially returns an empty list.
     */
    @Test
    void getPartial_initiallyEmpty() {
        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of());
        assertTrue(plan.getPartial().isEmpty());
    }

    /**
     * Tests {@link PickingPlan#getDeferred()} initially returns an empty list.
     */
    @Test
    void getDeferred_initiallyEmpty() {
        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of());
        assertTrue(plan.getDeferred().isEmpty());
    }

    /**
     * Tests {@link PickingPlan#getNumberOfTrolleys()} returns the correct count.
     */
    @Test
    void getNumberOfTrolleys_returnsCount() {
        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of(
                trolleyWith(1, 10.0),
                trolleyWith(2, 20.0),
                trolleyWith(3, 30.0)
        ));
        assertEquals(3, plan.getNumberOfTrolleys());
    }

    /**
     * Tests {@link PickingPlan#getTotalUsedKg()} sums usedKg of all trolleys.
     */
    @Test
    void getTotalUsedKg_sumsAllTrolleys() {
        Allocation a1 = allocation("O1", 1, "S1", 1, 1, 10, 5.0, 2.0);  // 10 kg
        Allocation a2 = allocation("O2", 1, "S2", 1, 1, 10, 3.0, 1.5);  // 4.5 kg
        Allocation a3 = allocation("O3", 1, "S3", 1, 1, 10, 2.0, 7.0);  // 14 kg

        Trolley t1 = trolleyWith(1, 100.0, a1, a2); // 14.5 kg
        Trolley t2 = trolleyWith(2, 50.0, a3);      // 14.0 kg

        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of(t1, t2));

        assertEquals(28.5, plan.getTotalUsedKg(), 1e-9);
    }

    /**
     * Tests {@link PickingPlan#getTotalCapacityKg()} sums capacities of all trolleys.
     */
    @Test
    void getTotalCapacityKg_sumsAllTrolleys() {
        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of(
                trolleyWith(1, 80.0),
                trolleyWith(2, 20.0),
                trolleyWith(3, 100.0)
        ));
        assertEquals(200.0, plan.getTotalCapacityKg(), 1e-9);
    }

    /**
     * Tests {@link PickingPlan#getGlobalUtilizationPercent()}:
     *  1) Zero total capacity → 0.0;
     *  2) Non-zero capacity → (used/capacity)*100.
     */
    @Test
    void getGlobalUtilizationPercent_handlesZeroAndNonZeroCapacity() {
        // 1) Zero capacity
        PickingPlan empty = new PickingPlan(PackingMode.FF, List.of());
        assertEquals(0.0, empty.getGlobalUtilizationPercent(), 1e-9);

        // 2) Non-zero capacity: used = 30 kg, capacity = 150 kg → 20%
        Allocation a1 = allocation("O1", 1, "S1", 1, 1, 10, 6.0, 2.0);  // 12 kg
        Allocation a2 = allocation("O2", 1, "S2", 1, 1, 10, 3.0, 6.0);  // 18 kg

        Trolley t1 = trolleyWith(1, 100.0, a1);
        Trolley t2 = trolleyWith(2, 50.0, a2);

        PickingPlan plan = new PickingPlan(PackingMode.FF, List.of(t1, t2));

        assertEquals(20.0, plan.getGlobalUtilizationPercent(), 1e-6);
    }
}
