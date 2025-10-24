package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PartialAllocationLog}.
 * <p>
 * Each test method targets exactly one public member of {@code PartialAllocationLog}
 * and documents the scenario, preconditions, and expected outcome.
 */
class PartialAllocationLogTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - Valid parameters provided for order ID, line number, SKU, and quantities.
     *
     * Expected:
     *  - All getter methods return the exact values passed to the constructor.
     *
     * @see PartialAllocationLog#PartialAllocationLog(String, int, String, double, double)
     * @see PartialAllocationLog#getOrderId()
     * @see PartialAllocationLog#getLineNo()
     * @see PartialAllocationLog#getSku()
     * @see PartialAllocationLog#getQuantityNow()
     * @see PartialAllocationLog#getQuantityDeferred()
     */
    @Test
    void constructor_initializesAllFieldsCorrectly() {
        PartialAllocationLog log =
                new PartialAllocationLog("ORD123", 7, "SKU777", 15.5, 3.0);

        assertEquals("ORD123", log.getOrderId());
        assertEquals(7, log.getLineNo());
        assertEquals("SKU777", log.getSku());
        assertEquals(15.5, log.getQuantityNow());
        assertEquals(3.0, log.getQuantityDeferred());
    }

    /**
     * Tests {@link PartialAllocationLog#getOrderId()}.
     *
     * Scenario:
     *  - Retrieve order ID from a known instance.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getOrderId_returnsCurrentValue() {
        PartialAllocationLog log = new PartialAllocationLog("ORD999", 1, "SKU1", 5, 2);
        assertEquals("ORD999", log.getOrderId());
    }

    /**
     * Tests {@link PartialAllocationLog#getLineNo()}.
     *
     * Scenario:
     *  - Retrieve line number from a known instance.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getLineNo_returnsCurrentValue() {
        PartialAllocationLog log = new PartialAllocationLog("ORD888", 4, "SKU2", 10, 1);
        assertEquals(4, log.getLineNo());
    }

    /**
     * Tests {@link PartialAllocationLog#getSku()}.
     *
     * Scenario:
     *  - Retrieve SKU from a known instance.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getSku_returnsCurrentValue() {
        PartialAllocationLog log = new PartialAllocationLog("ORD777", 3, "ABC123", 6, 0);
        assertEquals("ABC123", log.getSku());
    }

    /**
     * Tests {@link PartialAllocationLog#getQuantityNow()}.
     *
     * Scenario:
     *  - Retrieve the immediately allocated quantity.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getQuantityNow_returnsCurrentValue() {
        PartialAllocationLog log = new PartialAllocationLog("ORD555", 2, "SKU5", 7.25, 1.75);
        assertEquals(7.25, log.getQuantityNow());
    }

    /**
     * Tests {@link PartialAllocationLog#getQuantityDeferred()}.
     *
     * Scenario:
     *  - Retrieve the deferred quantity.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getQuantityDeferred_returnsCurrentValue() {
        PartialAllocationLog log = new PartialAllocationLog("ORD444", 8, "SKU8", 4.5, 2.5);
        assertEquals(2.5, log.getQuantityDeferred());
    }
}
