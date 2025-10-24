package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PickStop}.
 * <p>
 * Each test method targets exactly one public member of {@code PickStop}
 * and documents the scenario, preconditions, and expected outcome.
 */
class PickStopTest {

    /**
     * Verifies that the constructor correctly initializes all fields.
     *
     * Preconditions:
     *  - Valid aisle, bay, and quantity supplied.
     *
     * Expected:
     *  - All getter methods return the provided values.
     *
     * @see PickStop#PickStop(int, int, double)
     * @see PickStop#getAisle()
     * @see PickStop#getBay()
     * @see PickStop#getTotalQty()
     */
    @Test
    void constructor_initializesFieldsCorrectly() {
        PickStop stop = new PickStop(2, 5, 10.5);

        assertEquals(2, stop.getAisle());
        assertEquals(5, stop.getBay());
        assertEquals(10.5, stop.getTotalQty());
    }

    /**
     * Tests {@link PickStop#getAisle()}.
     *
     * Scenario:
     *  - Retrieve the aisle number from a known instance.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getAisle_returnsCorrectValue() {
        PickStop stop = new PickStop(4, 1, 3.0);
        assertEquals(4, stop.getAisle());
    }

    /**
     * Tests {@link PickStop#getBay()}.
     *
     * Scenario:
     *  - Retrieve the bay number from a known instance.
     *
     * Expected:
     *  - Returned value matches the constructor argument.
     */
    @Test
    void getBay_returnsCorrectValue() {
        PickStop stop = new PickStop(4, 7, 8.0);
        assertEquals(7, stop.getBay());
    }

    /**
     * Tests {@link PickStop#getTotalQty()}.
     *
     * Scenario:
     *  - Retrieve total quantity after construction.
     *
     * Expected:
     *  - Returned value equals the constructor argument.
     */
    @Test
    void getTotalQty_returnsCorrectValue() {
        PickStop stop = new PickStop(1, 2, 6.5);
        assertEquals(6.5, stop.getTotalQty());
    }

    /**
     * Tests {@link PickStop#addQty(double)} increases the total quantity correctly.
     *
     * Scenario:
     *  - Add a positive quantity to an existing pick stop.
     *
     * Expected:
     *  - The total quantity increases by the added amount.
     */
    @Test
    void addQty_incrementsTotalQuantity() {
        PickStop stop = new PickStop(2, 3, 5.0);
        stop.addQty(4.5);
        assertEquals(9.5, stop.getTotalQty());
    }

    /**
     * Tests {@link PickStop#toString()} returns the formatted representation.
     *
     * Expected:
     *  - String includes aisle, bay, and quantity in the expected format.
     */
    @Test
    void toString_formatsAllFieldsCorrectly() {
        PickStop stop = new PickStop(3, 9, 12.75);
        String result = stop.toString();

        assertTrue(result.contains("(3,9)"));
        assertTrue(result.contains("qty=12.75"));
    }

    /**
     * Tests {@link PickStop#equals(Object)} returns true for same aisle and bay,
     * and false for different coordinates or different types.
     *
     * @see PickStop#equals(Object)
     */
    @Test
    void equals_comparesBasedOnAisleAndBay() {
        PickStop stop1 = new PickStop(1, 2, 10.0);
        PickStop stop2 = new PickStop(1, 2, 99.0); // same coordinates, different qty
        PickStop stop3 = new PickStop(1, 3, 10.0);

        assertEquals(stop1, stop2);   // same aisle and bay
        assertNotEquals(stop1, stop3);
        assertNotEquals(stop1, null);
        assertNotEquals(stop1, "not a PickStop");
    }

    /**
     * Tests {@link PickStop#hashCode()} produces identical values
     * for equal objects and different values for distinct coordinates.
     *
     * @see PickStop#hashCode()
     */
    @Test
    void hashCode_consistentWithEquals() {
        PickStop a = new PickStop(1, 1, 5.0);
        PickStop b = new PickStop(1, 1, 7.0);
        PickStop c = new PickStop(2, 1, 5.0);

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }
}
