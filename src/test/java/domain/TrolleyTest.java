package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Trolley}.
 * <p>
 * Each test method targets exactly one public member of {@code Trolley}
 * and documents the scenario, preconditions, and expected outcome.
 */
class TrolleyTest {


    private Allocation alloc(String orderId, int lineNo, String sku,
                             int aisleId, int bayId,
                             int boxQty, double allocQty, double unitWeight) {
        Order order = new Order(orderId, /*priority*/1, "2025-12-31");
        OrderLine line = new OrderLine(lineNo, sku, /*quantity (ordered)*/ boxQty);
        Items items = new Items(sku, "Prod-" + sku, "Cat", "Pack", 1.0f, (float) unitWeight);
        Box box = new Box(items, boxQty);
        Aisle aisle = new Aisle(aisleId);
        Bay bay = new Bay(bayId, /*capacity*/10);

        return new Allocation(order, line, sku, box, aisle, bay, allocQty, unitWeight);
    }


    /**
     * Verifies that the constructor initializes ID, capacity, usedKg and items.
     *
     * Preconditions:
     *  - Valid ID and capacity supplied.
     *
     * Expected:
     *  - {@link Trolley#getId()} and {@link Trolley#getWeightCapacity()} match inputs;
     *  - {@link Trolley#getUsedKg()} is 0.0;
     *  - {@link Trolley#getItems()} is empty.
     *
     * @see Trolley#Trolley(int, double)
     */
    @Test
    void constructor_initializesFieldsCorrectly() {
        Trolley trolley = new Trolley(1, 100.0);

        assertEquals(1, trolley.getId());
        assertEquals(100.0, trolley.getWeightCapacity());
        assertEquals(0.0, trolley.getUsedKg());
        assertTrue(trolley.getItems().isEmpty());
    }

    /**
     * Tests {@link Trolley#getId()} returns the assigned identifier.
     *
     * @see Trolley#getId()
     */
    @Test
    void getId_returnsIdentifier() {
        Trolley trolley = new Trolley(5, 50.0);
        assertEquals(5, trolley.getId());
    }

    /**
     * Tests {@link Trolley#getWeightCapacity()} returns the assigned capacity.
     *
     * @see Trolley#getWeightCapacity()
     */
    @Test
    void getWeightCapacity_returnsAssignedValue() {
        Trolley trolley = new Trolley(2, 200.0);
        assertEquals(200.0, trolley.getWeightCapacity());
    }

    /**
     * Tests {@link Trolley#getUsedKg()} initially returns zero.
     *
     * @see Trolley#getUsedKg()
     */
    @Test
    void getUsedKg_initiallyZero() {
        Trolley trolley = new Trolley(1, 150.0);
        assertEquals(0.0, trolley.getUsedKg());
    }

    /**
     * Tests {@link Trolley#getItems()} returns the live list.
     *
     * Scenario:
     *  - Initially empty; after {@link Trolley#addItem(Allocation)}, the item appears.
     *
     * Expected:
     *  - List is initially empty; after add, contains the allocation instance.
     *
     * @see Trolley#getItems()
     * @see Trolley#addItem(Allocation)
     */
    @Test
    void getItems_returnsLiveList() {
        Trolley trolley = new Trolley(1, 100.0);
        assertTrue(trolley.getItems().isEmpty());

        Allocation a = alloc("O1", 1, "SKU1", 1, 1, 10, 2.0, 3.0); // total 6 kg
        trolley.addItem(a);

        assertEquals(1, trolley.getItems().size());
        assertSame(a, trolley.getItems().get(0));
    }

    /**
     * Tests {@link Trolley#canFit(double)} for both outcomes.
     *
     * Scenario:
     *  - Weight that fits within remaining capacity vs weight that exceeds it.
     *
     * Expected:
     *  - Returns true when it fits, false otherwise.
     *
     * @see Trolley#canFit(double)
     */
    @Test
    void canFit_handlesFitAndOverflowCases() {
        Trolley trolley = new Trolley(1, 100.0);

        assertTrue(trolley.canFit(80.0));   // fits
        assertFalse(trolley.canFit(120.0)); // exceeds

        // After adding 60 kg, only 40 remain
        trolley.addItem(alloc("O1", 1, "S1", 1, 1, 10, 20.0, 3.0)); // 60 kg
        assertTrue(trolley.canFit(40.0));
        assertFalse(trolley.canFit(40.0000001));
    }

    /**
     * Tests {@link Trolley#addItem(Allocation)} updates usedKg using quantity × unitWeight.
     *
     * Scenario:
     *  - Add one allocation; verify items list and usedKg updated with exact total weight.
     *
     * Expected:
     *  - items size +1; usedKg increased by allocation.totalWeight().
     *
     * @see Trolley#addItem(Allocation)
     * @see Allocation#totalWeight()
     */
    @Test
    void addItem_updatesUsedWeightAndList() {
        Trolley trolley = new Trolley(1, 100.0);

        Allocation a = alloc("O1", 1, "SKU1", 1, 1, 10, 5.0, 2.0); // 10 kg
        trolley.addItem(a);

        assertEquals(1, trolley.getItems().size());
        assertSame(a, trolley.getItems().get(0));
        assertEquals(10.0, trolley.getUsedKg(), 1e-9);
    }

    /**
     * Tests {@link Trolley#percUtilization()} returns 0 when capacity is zero,
     * and computes (usedKg/capacity)*100 otherwise.
     *
     * @see Trolley#percUtilization()
     */
    @Test
    void percUtilization_handlesZeroAndNonZeroCapacity() {
        // Zero capacity
        Trolley zeroCap = new Trolley(1, 0.0);
        assertEquals(0.0, zeroCap.percUtilization(), 1e-9);

        // Normal
        Trolley trolley = new Trolley(2, 100.0);
        trolley.addItem(alloc("O2", 1, "SKU2", 1, 1, 10, 8.0, 2.5)); // 20 kg
        assertEquals(20.0, trolley.percUtilization(), 1e-6);
    }

    /**
     * Tests {@link Trolley#remainingCapacity()} returns the correct remaining kilograms.
     *
     * Scenario:
     *  - Start with capacity 80; add 30 kg; remaining = 50.
     *
     * @see Trolley#remainingCapacity()
     */
    @Test
    void remainingCapacity_returnsAvailableSpace() {
        Trolley trolley = new Trolley(3, 80.0);
        trolley.addItem(alloc("O3", 1, "SKU3", 2, 5, 10, 6.0, 5.0)); // 30 kg

        assertEquals(50.0, trolley.remainingCapacity(), 1e-9);
    }
}
