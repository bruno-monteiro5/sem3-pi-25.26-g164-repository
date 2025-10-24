package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for {@link Aisle}.
 * <p>
 * Each test method targets exactly one public member of {@code Aisle}
 * and documents the scenario, preconditions and expected outcome.
 */
class AisleTest {

    /**
     * Verifies that the constructor initializes the aisle id and creates
     * an empty list of bays.
     *
     * Preconditions:
     *  - valid integer id supplied.
     *
     * Expected:
     *  - {@link Aisle#getAisleID()} returns the supplied id;
     *  - {@link Aisle#getBays()} is empty.
     *
     * @see Aisle#Aisle(int)
     * @see Aisle#getAisleID()
     * @see Aisle#getBays()
     */
    @Test
    void constructor_initializesFields() {
        Aisle aisle = new Aisle(10);
        assertEquals(10, aisle.getAisleID());
        assertTrue(aisle.getBays().isEmpty());
    }

    /**
     * Tests the getter for the aisle identifier.
     *
     * Scenario:
     *  - Create an aisle with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Aisle#getAisleID()
     */
    @Test
    void getAisleID_returnsCurrentId() {
        Aisle aisle = new Aisle(3);
        assertEquals(3, aisle.getAisleID());
    }

    /**
     * Tests the setter for the aisle identifier.
     *
     * Scenario:
     *  - Start with an id, change it, and read again.
     *
     * Expected:
     *  - the new id is persisted and returned by the getter.
     *
     * @see Aisle#setAisleID(int)
     * @see Aisle#getAisleID()
     */
    @Test
    void setAisleID_updatesValue() {
        Aisle aisle = new Aisle(1);
        aisle.setAisleID(7);
        assertEquals(7, aisle.getAisleID());
    }

    /**
     * Tests the bays getter.
     *
     * Scenario:
     *  - Provide a predefined list through {@link Aisle#setBays(List)}
     *    and then read it via {@link Aisle#getBays()}.
     *
     * Expected:
     *  - the returned reference is the same list and preserves contents.
     *
     * @see Aisle#getBays()
     * @see Aisle#setBays(List)
     */
    @Test
    void getBays_returnsInternalList() {
        Aisle aisle = new Aisle(2);
        List<Bay> list = new ArrayList<>();
        Bay b1 = new Bay(100, 0);
        Bay b2 = new Bay(101, 1);
        list.add(b1);
        list.add(b2);

        aisle.setBays(list);

        assertSame(list, aisle.getBays());
        assertEquals(2, aisle.getBays().size());
        assertSame(b1, aisle.getBays().get(0));
        assertSame(b2, aisle.getBays().get(1));
    }

    /**
     * Tests the bays setter replaces the internal collection.
     *
     * Scenario:
     *  - Replace the (initially empty) list by a custom list.
     *
     * Expected:
     *  - the new list is stored and visible via {@link Aisle#getBays()}.
     *
     * @see Aisle#setBays(List)
     */
    @Test
    void setBays_replacesCollection() {
        Aisle aisle = new Aisle(20);
        List<Bay> newList = new ArrayList<>();
        newList.add(new Bay(200, 1));
        aisle.setBays(newList);

        assertSame(newList, aisle.getBays());
        assertEquals(1, aisle.getBays().size());
    }

    /**
     * Tests adding a single bay to the aisle.
     *
     * Scenario:
     *  - Call {@link Aisle#addBay(Bay)} once.
     *
     * Expected:
     *  - the bay appears in the internal list.
     *
     * @see Aisle#addBay(Bay)
     */
    @Test
    void addBay_appendsBay() {
        Aisle aisle = new Aisle(3);
        Bay bay = new Bay(300, 1);

        aisle.addBay(bay);

        assertEquals(1, aisle.getBays().size());
        assertTrue(aisle.getBays().contains(bay));
    }

    /**
     * Tests the string representation of an aisle.
     *
     * Scenario:
     *  - Aisle with one bay to check the count formatting.
     *
     * Expected:
     *  - exact match to the documented format.
     *
     * @see Aisle#toString()
     */
    @Test
    void toString_formatsIdAndCount() {
        Aisle aisle = new Aisle(4);
        aisle.addBay(new Bay(400, 1));

        String expected = "Aisle ID: 4, with 1 bays";
        assertEquals(expected, aisle.toString());
    }

    /**
     * Tests the search for the first available bay (capacity not reached).
     *
     * Scenario (covers both branches of the method):
     *  1) Two bays: first has capacity 0 (always full), second has capacity 1 (available).
     *     Expect the second bay to be returned.
     *  2) Make the second bay also full (capacity 0) and query again.
     *     Expect {@code null}.
     *
     * Expected:
     *  - Step 1: returns the first bay with free capacity;
     *  - Step 2: returns {@code null} when all bays are full.
     *
     * @see Aisle#getFirstAvailableBay()
     */
    @Test
    void getFirstAvailableBay_handlesAvailableAndAllFull() {
        Aisle aisle = new Aisle(5);

        Bay fullBay = new Bay(500, 0);
        Bay availableBay = new Bay(501, 1);

        aisle.addBay(fullBay);
        aisle.addBay(availableBay);

        Bay firstFree = aisle.getFirstAvailableBay();
        assertNotNull(firstFree);
        assertSame(availableBay, firstFree);

        availableBay.setCapacity(0);
        Bay none = aisle.getFirstAvailableBay();
        assertNull(none);
    }
}