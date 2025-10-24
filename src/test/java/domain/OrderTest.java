package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Order}.
 * <p>
 * Each test method targets exactly one public member of {@code Order}
 * and documents the scenario, preconditions, and expected outcome.
 */
class OrderTest {

    /**
     * Verifies that the constructor initializes ID, priority, and due date correctly.
     *
     * Preconditions:
     *  - Valid parameters provided to constructor.
     *
     * Expected:
     *  - Getters return the values passed to the constructor.
     *  - The order line list is initialized empty.
     *
     * @see Order#Order(String, int, String)
     * @see Order#getOrderID()
     * @see Order#getPriority()
     * @see Order#getDueDate()
     * @see Order#getLines()
     */
    @Test
    void constructor_initializesFieldsCorrectly() {
        Order order = new Order("ORD001", 2, "2025-10-25");

        assertEquals("ORD001", order.getOrderID());
        assertEquals(2, order.getPriority());
        assertEquals("2025-10-25", order.getDueDate());
        assertTrue(order.getLines().isEmpty());
    }

    /**
     * Tests the default constructor initializes an empty order.
     *
     * Expected:
     *  - All attributes are null or zero, and the list is empty.
     *
     * @see Order#Order()
     */
    @Test
    void defaultConstructor_createsEmptyOrder() {
        Order order = new Order();

        assertNull(order.getOrderID());
        assertEquals(0, order.getPriority());
        assertNull(order.getDueDate());
        assertTrue(order.getLines().isEmpty());
    }

    /**
     * Tests getter and setter for order ID.
     *
     * Scenario:
     *  - Update the ID and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated ID.
     *
     * @see Order#setOrderID(String)
     * @see Order#getOrderID()
     */
    @Test
    void setAndGetOrderID_updatesAndReturnsValue() {
        Order order = new Order();
        order.setOrderID("ORD123");
        assertEquals("ORD123", order.getOrderID());
    }

    /**
     * Tests getter and setter for priority.
     *
     * Scenario:
     *  - Change priority and verify update.
     *
     * Expected:
     *  - The getter returns the new priority.
     *
     * @see Order#setPriority(int)
     * @see Order#getPriority()
     */
    @Test
    void setAndGetPriority_updatesAndReturnsValue() {
        Order order = new Order();
        order.setPriority(5);
        assertEquals(5, order.getPriority());
    }

    /**
     * Tests getter and setter for due date.
     *
     * Scenario:
     *  - Update due date and retrieve it.
     *
     * Expected:
     *  - Getter returns the new value.
     *
     * @see Order#setDueDate(String)
     * @see Order#getDueDate()
     */
    @Test
    void setAndGetDueDate_updatesAndReturnsValue() {
        Order order = new Order();
        order.setDueDate("2025-12-31");
        assertEquals("2025-12-31", order.getDueDate());
    }

    /**
     * Tests getter and setter for the list of order lines.
     *
     * Scenario:
     *  - Replace the default list with a new one.
     *
     * Expected:
     *  - The internal reference is replaced and accessible.
     *
     * @see Order#setLines(List)
     * @see Order#getLines()
     */
    @Test
    void setAndGetLines_replacesAndReturnsReference() {
        Order order = new Order();
        List<OrderLine> newLines = new ArrayList<>();
        newLines.add(new OrderLine(1, "SKU1", 3));

        order.setLines(newLines);

        assertSame(newLines, order.getLines());
        assertEquals(1, order.getLines().size());
    }

    /**
     * Tests {@link Order#addLine(OrderLine)} adds an element to the list.
     *
     * Scenario:
     *  - Add a new order line.
     *
     * Expected:
     *  - List size increases by one and contains the added line.
     *
     * @see Order#addLine(OrderLine)
     */
    @Test
    void addLine_addsNewOrderLine() {
        Order order = new Order("ORD900", 3, "2025-10-26");
        OrderLine line = new OrderLine(1, "SKU123", 5);

        order.addLine(line);

        assertEquals(1, order.getLines().size());
        assertTrue(order.getLines().contains(line));
    }

    /**
     * Tests {@link Order#toString()} returns formatted order summary.
     *
     * Scenario:
     *  - Create an order and add one line.
     *
     * Expected:
     *  - String contains ID, priority, due date, and line count.
     *
     * @see Order#toString()
     */
    @Test
    void toString_formatsOrderSummaryCorrectly() {
        Order order = new Order("ORD777", 1, "2025-11-01");
        order.addLine(new OrderLine(1, "SKU99", 2));

        String result = order.toString();

        assertTrue(result.contains("orderId='ORD777'"));
        assertTrue(result.contains("priority=1"));
        assertTrue(result.contains("dueDate='2025-11-01'"));
        assertTrue(result.contains("lines=1"));
    }
}
