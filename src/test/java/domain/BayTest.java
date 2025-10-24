package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for {@link Bay}.
 * <p>
 * Each test method targets exactly one public member of {@code Bay}
 * and documents the scenario, preconditions, and expected outcome.
 */
class BayTest {

    /**
     * Verifies that the constructor initializes the bay ID, capacity,
     * and creates an empty inventory list.
     *
     * Preconditions:
     *  - Valid integer ID and capacity supplied.
     *
     * Expected:
     *  - {@link Bay#getBayID()} returns the supplied ID;
     *  - {@link Bay#getCapacity()} returns the supplied capacity;
     *  - {@link Bay#getInventory()} is empty.
     *
     * @see Bay#Bay(int, int)
     * @see Bay#getBayID()
     * @see Bay#getCapacity()
     * @see Bay#getInventory()
     */
    @Test
    void constructor_initializesFields() {
        Bay bay = new Bay(1, 10);
        assertEquals(1, bay.getBayID());
        assertEquals(10, bay.getCapacity());
        assertTrue(bay.getInventory().isEmpty());
    }

    /**
     * Tests the getter for the bay identifier.
     *
     * Scenario:
     *  - Create a bay with a known ID and read it back.
     *
     * Expected:
     *  - Returned ID matches the constructor value.
     *
     * @see Bay#getBayID()
     */
    @Test
    void getBayID_returnsCurrentId() {
        Bay bay = new Bay(2, 5);
        assertEquals(2, bay.getBayID());
    }

    /**
     * Tests the setter for the bay identifier.
     *
     * Scenario:
     *  - Start with one ID, change it, and read again.
     *
     * Expected:
     *  - The new ID is persisted and returned by the getter.
     *
     * @see Bay#setBayID(int)
     * @see Bay#getBayID()
     */
    @Test
    void setBayID_updatesValue() {
        Bay bay = new Bay(3, 5);
        bay.setBayID(9);
        assertEquals(9, bay.getBayID());
    }

    /**
     * Tests the inventory getter.
     *
     * Scenario:
     *  - A newly created bay should start with an empty list.
     *  - Add one {@link Box} directly to verify that the returned list is live.
     *
     * Expected:
     *  - Inventory is initially empty;
     *  - Adding to the returned list affects the internal inventory.
     *
     * @see Bay#getInventory()
     */
    @Test
    void getInventory_returnsLiveList() {
        Bay bay = new Bay(4, 2);
        assertTrue(bay.getInventory().isEmpty());

        Items product = new Items("S1", "Item1", "Food", "Pack1", 1.2f, 0.8f);
        bay.getInventory().add(new Box(product, 1));

        assertEquals(1, bay.getInventory().size());
    }

    /**
     * Tests the inventory setter.
     *
     * Scenario:
     *  - Replace the initial list with a new predefined list.
     *
     * Expected:
     *  - The new list is stored and visible through the getter;
     *  - The reference equality is preserved.
     *
     * @see Bay#setInventory(List)
     * @see Bay#getInventory()
     */
    @Test
    void setInventory_replacesCollection() {
        Bay bay = new Bay(5, 3);
        List<Box> boxes = new ArrayList<>();

        Items items = new Items("S2", "Item2", "Tools", "Pack2", 1.0f, 2.5f);
        boxes.add(new Box(items, 2, "2030-01-01"));
        boxes.add(new Box(items, 1));

        bay.setInventory(boxes);

        assertSame(boxes, bay.getInventory());
        assertEquals(2, bay.getInventory().size());
    }

    /**
     * Tests the getter for the bay capacity.
     *
     * Scenario:
     *  - Create a bay and query its capacity.
     *
     * Expected:
     *  - Returned capacity matches the constructor value.
     *
     * @see Bay#getCapacity()
     */
    @Test
    void getCapacity_returnsCurrentValue() {
        Bay bay = new Bay(6, 8);
        assertEquals(8, bay.getCapacity());
    }

    /**
     * Tests the setter for the bay capacity.
     *
     * Scenario:
     *  - Change the capacity and query it again.
     *
     * Expected:
     *  - The new capacity value is stored and returned correctly.
     *
     * @see Bay#setCapacity(int)
     * @see Bay#getCapacity()
     */
    @Test
    void setCapacity_updatesValue() {
        Bay bay = new Bay(7, 1);
        bay.setCapacity(5);
        assertEquals(5, bay.getCapacity());
    }

    /**
     * Tests adding product boxes to the bay.
     *
     * Scenario:
     *  1) Add boxes while capacity allows it (should succeed);
     *  2) Attempt to add one more when full (should fail).
     *
     * Expected:
     *  - Step 1: {@link Bay#addProductBox(Box)} returns true and adds the box;
     *  - Step 2: returns false when capacity is exceeded.
     *
     * @see Bay#addProductBox(Box)
     */
    @Test
    void addProductBox_handlesAvailableAndFull() {
        Bay bay = new Bay(8, 2);

        Items i1 = new Items("A", "ItemA", "CatA", "PackA", 0.5f, 0.2f);
        Items i2 = new Items("B", "ItemB", "CatB", "PackB", 0.7f, 0.3f);
        Items i3 = new Items("C", "ItemC", "CatC", "PackC", 0.9f, 0.4f);

        assertTrue(bay.addProductBox(new Box(i1, 1)));
        assertTrue(bay.addProductBox(new Box(i2, 2)));
        assertFalse(bay.addProductBox(new Box(i3, 3)));

        assertEquals(2, bay.getInventory().size());
    }

    /**
     * Tests the string representation of a bay.
     *
     * Scenario (covers both branches of the method):
     *  1) Empty inventory → should display only the header;
     *  2) Non-empty inventory → should list each box on a new line.
     *
     * Expected:
     *  - Step 1: correct format with zero boxes;
     *  - Step 2: header includes correct ID/count, followed by each box line.
     *
     * @see Bay#toString()
     */
    @Test
    void toString_formatsEmptyAndNonEmptyInventory() {
        // Step 1: empty inventory
        Bay empty = new Bay(9, 3);
        String expectedEmpty = "Bay ID: 9, with 0 boxes, and 3 capacity";
        assertEquals(expectedEmpty, empty.toString());

        // Step 2: non-empty inventory
        Bay nonEmpty = new Bay(10, 5);
        Items i1 = new Items("X1", "ProdX1", "CatX", "PackX", 1.23f, 4.56f);
        Items i2 = new Items("X2", "ProdX2", "CatY", "PackY", 2.34f, 5.67f);

        Box b1 = new Box(i1, 2);
        Box b2 = new Box(i2, 3, "2029-12-31");

        nonEmpty.addProductBox(b1);
        nonEmpty.addProductBox(b2);

        String result = nonEmpty.toString();

        assertTrue(result.startsWith("Bay ID: 10, with 2 boxes, and 5 capacity"));
        assertTrue(result.contains(b1.toString() + "\n"));
        assertTrue(result.contains(b2.toString() + "\n"));
    }
}
