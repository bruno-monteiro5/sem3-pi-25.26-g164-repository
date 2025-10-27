package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Allocation}.
 * <p>
 * Each test method targets one public member or logical behaviour of {@code Allocation}
 * and documents the scenario, preconditions and expected outcome.
 */
class AllocationTest {

    /**
     * Builds a minimal {@link Items} instance for test data.
     *
     * @param sku the SKU code
     * @return a basic {@link Items} object
     */
    private Items items(String sku) {
        return new Items(sku, "Item-" + sku, "Category", "Pack", 1.0f, 2.0f);
    }

    /**
     * Builds a basic {@link Box} instance with explicit box ID.
     *
     * @param id   the box identifier
     * @param sku  product SKU
     * @param qty  quantity
     * @return configured {@link Box}
     */
    private Box box(String id, String sku, int qty) {
        return new Box(id, items(sku), qty);
    }

    /**
     * Builds a simple {@link OrderLine} for testing.
     *
     * @param no   line number
     * @param sku  SKU code
     * @param qty  quantity
     * @return {@link OrderLine} instance
     */
    private OrderLine line(int no, String sku, int qty) {
        return new OrderLine(no, sku, qty);
    }

    /**
     * Builds a minimal {@link Order} for use in allocation tests.
     *
     * @param id order identifier
     * @return a basic {@link Order}
     */
    private Order order(String id) {
        Order o = new Order(id, 1, "2025-10-27T10:00:00");
        o.addLine(new OrderLine(1, "SKU-Z", 1));
        return o;
    }

    /**
     * Verifies that the constructor assigns all parameters to fields and getters
     * return the exact same objects or primitive values.
     *
     * Preconditions:
     *  - valid {@link Order}, {@link OrderLine}, {@link Box}, {@link Aisle}, {@link Bay}
     *    and numeric values provided.
     *
     * Expected:
     *  - all getters return their respective constructor values.
     *
     * @see Allocation#Allocation(Order, OrderLine, String, Box, Aisle, Bay, double, double)
     */
    @Test
    void constructor_and_getters_assignFieldsCorrectly() {
        Order ord = order("ORD-1");
        OrderLine ln = line(7, "SKU-1", 3);
        Box bx = box("B-1", "SKU-1", 10);
        Aisle aisle = new Aisle(2);
        Bay bay = new Bay(5, 3);

        Allocation a = new Allocation(ord, ln, "SKU-1", bx, aisle, bay, 4.0, 1.5);

        assertSame(ord, a.getOrderId());
        assertSame(ln, a.getLineNo());
        assertEquals("SKU-1", a.getSku());
        assertSame(bx, a.getBoxId());
        assertSame(aisle, a.getAisle());
        assertSame(bay, a.getBay());
        assertEquals(4.0, a.getQuantity());
        assertEquals(1.5, a.getUnitWeight());
    }

    /**
     * Verifies that {@link Allocation#totalWeight()} correctly multiplies
     * the quantity by the unit weight.
     *
     * Scenario:
     *  - Create an allocation with known values.
     *
     * Expected:
     *  - Returned weight equals {@code quantity * unitWeight}.
     *
     * @see Allocation#totalWeight()
     */
    @Test
    void totalWeight_multipliesQuantityAndUnitWeight() {
        Allocation a = new Allocation(
                order("ORD-2"),
                line(1, "SKU-A", 1),
                "SKU-A",
                box("B-A", "SKU-A", 5),
                new Aisle(1),
                new Bay(1, 2),
                12.5,
                0.8
        );
        assertEquals(10.0, a.totalWeight(), 1e-9);
    }

    /**
     * Verifies that {@link Allocation#compareTo(Allocation)} sorts allocations
     * by total weight in descending order.
     *
     * Scenario:
     *  - Two allocations with different total weights and two with equal weight.
     *
     * Expected:
     *  - Heavier allocation precedes lighter one;
     *  - Equal weights return 0.
     *
     * @see Allocation#compareTo(Allocation)
     */
    @Test
    void compareTo_ordersByTotalWeightDescending() {
        Allocation heavy = new Allocation(
                order("ORD-H"),
                line(1, "SKU-H", 1),
                "SKU-H",
                box("B-H", "SKU-H", 5),
                new Aisle(1),
                new Bay(1, 2),
                10.0, 3.0 // 30 kg
        );

        Allocation light = new Allocation(
                order("ORD-L"),
                line(1, "SKU-L", 1),
                "SKU-L",
                box("B-L", "SKU-L", 5),
                new Aisle(1),
                new Bay(1, 2),
                4.0, 2.0 // 8 kg
        );

        assertTrue(heavy.compareTo(light) < 0);
        assertTrue(light.compareTo(heavy) > 0);

        Allocation equalA = new Allocation(order("E1"), line(1, "SKU-E", 1), "SKU-E",
                box("BE1", "SKU-E", 5), new Aisle(1), new Bay(1, 2), 5.0, 2.0);
        Allocation equalB = new Allocation(order("E2"), line(2, "SKU-E", 1), "SKU-E",
                box("BE2", "SKU-E", 5), new Aisle(1), new Bay(2, 2), 10.0, 1.0);
        assertEquals(0, equalA.compareTo(equalB));
    }

    /**
     * Verifies that {@link Allocation#toString()} contains all relevant fragments.
     *
     * Scenario:
     *  - Construct an allocation with known identifiers and check textual output.
     *
     * Expected:
     *  - Contains the box reference, line number, box quantity, order reference,
     *    aisle and bay IDs, and ends with a newline.
     *
     * @see Allocation#toString()
     */
    @Test
    void toString_containsExpectedFragments() {
        Order ord = order("ORD-9");
        OrderLine ln = line(9, "SKU-9", 2);
        Box bx = box("B-9", "SKU-9", 7);
        Aisle aisle = new Aisle(12);
        Bay bay = new Bay(34, 2);

        Allocation a = new Allocation(ord, ln, "SKU-9", bx, aisle, bay, 3.0, 2.0);

        String s = a.toString();

        assertTrue(s.startsWith("Allocation of box "));
        assertTrue(s.contains("for the line order 9"));
        assertTrue(s.contains("of quantity 7"));
        assertTrue(s.contains("for the order " + ord.toString()));
        assertTrue(s.contains("stored in Aisle 12 and Bay 34"));
        assertTrue(s.endsWith("\n"));
    }
}
