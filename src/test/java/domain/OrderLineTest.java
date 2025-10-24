package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link OrderLine}.
 * <p>
 * Each test method targets exactly one public member of {@code OrderLine}
 * and documents the scenario, preconditions, and expected outcome.
 */
class OrderLineTest {

    /**
     * Verifies that the parameterized constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - Valid line number, SKU, and quantity supplied.
     *
     * Expected:
     *  - Getters return the values passed to the constructor.
     *
     * @see OrderLine#OrderLine(int, String, int)
     * @see OrderLine#getLineNo()
     * @see OrderLine#getSku()
     * @see OrderLine#getQuantity()
     */
    @Test
    void constructor_initializesFieldsCorrectly() {
        OrderLine line = new OrderLine(1, "SKU001", 10);

        assertEquals(1, line.getLineNo());
        assertEquals("SKU001", line.getSku());
        assertEquals(10, line.getQuantity());
    }

    /**
     * Tests the default constructor initializes an empty order line.
     *
     * Expected:
     *  - All attributes are zero or null.
     *
     * @see OrderLine#OrderLine()
     */
    @Test
    void defaultConstructor_createsEmptyOrderLine() {
        OrderLine line = new OrderLine();

        assertEquals(0, line.getLineNo());
        assertNull(line.getSku());
        assertEquals(0, line.getQuantity());
    }

    /**
     * Tests getter and setter for the line number.
     *
     * Scenario:
     *  - Update the line number and retrieve it.
     *
     * Expected:
     *  - Getter returns the updated value.
     *
     * @see OrderLine#setLineNo(int)
     * @see OrderLine#getLineNo()
     */
    @Test
    void setAndGetLineNo_updatesAndReturnsValue() {
        OrderLine line = new OrderLine();
        line.setLineNo(5);
        assertEquals(5, line.getLineNo());
    }

    /**
     * Tests getter and setter for the SKU.
     *
     * Scenario:
     *  - Update the SKU code and retrieve it.
     *
     * Expected:
     *  - Getter returns the updated value.
     *
     * @see OrderLine#setSku(String)
     * @see OrderLine#getSku()
     */
    @Test
    void setAndGetSku_updatesAndReturnsValue() {
        OrderLine line = new OrderLine();
        line.setSku("SKU999");
        assertEquals("SKU999", line.getSku());
    }

    /**
     * Tests getter and setter for the quantity.
     *
     * Scenario:
     *  - Change the quantity and retrieve it.
     *
     * Expected:
     *  - Getter returns the updated value.
     *
     * @see OrderLine#setQuantity(int)
     * @see OrderLine#getQuantity()
     */
    @Test
    void setAndGetQuantity_updatesAndReturnsValue() {
        OrderLine line = new OrderLine();
        line.setQuantity(42);
        assertEquals(42, line.getQuantity());
    }

    /**
     * Tests {@link OrderLine#toString()} returns the correct formatted string.
     *
     * Scenario:
     *  - Create an order line and convert it to a string.
     *
     * Expected:
     *  - Returned string contains the line number, SKU, and quantity.
     *
     * @see OrderLine#toString()
     */
    @Test
    void toString_formatsAllFieldsCorrectly() {
        OrderLine line = new OrderLine(3, "SKU777", 20);

        String result = line.toString();

        assertTrue(result.contains("lineNo=3"));
        assertTrue(result.contains("sku='SKU777'"));
        assertTrue(result.contains("quantity=20"));
    }
}
