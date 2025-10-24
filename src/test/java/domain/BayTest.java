package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Bay}.
 * <p>
 * One test per public method. Tests use the real domain classes
 * {@link Items} and {@link Box} to validate behavior exactly as specified.
 */
class BayTest {

    /**
     * Verifies that the constructor sets ID, capacity and starts with an empty inventory.
     *
     * @see Bay#Bay(int, int)
     */
    @Test
    void constructor_initializesFields() {
        Bay bay = new Bay(1, 10);
        assertEquals(1, bay.getBayID());
        assertEquals(10, bay.getCapacity());
        assertTrue(bay.getInventory().isEmpty());
    }

    /**
     * Verifies {@link Bay#getBayID()} returns the current ID.
     *
     * @see Bay#getBayID()
     */
    @Test
    void getBayID_returnsCurrentId() {
        Bay bay = new Bay(2, 5);
        assertEquals(2, bay.getBayID());
    }

    /**
     * Verifies {@link Bay#setBayID(int)} persists the new value.
     *
     * @see Bay#setBayID(int)
     */
    @Test
    void setBayID_updatesValue() {
        Bay bay = new Bay(3, 5);
        bay.setBayID(7);
        assertEquals(7, bay.getBayID());
    }

    /**
     * Verifies {@link Bay#getInventory()} returns a live (mutable) list and is initially empty.
     *
     * @see Bay#getInventory()
     */
    @Test
    void getInventory_initiallyEmptyAndMutable() {
        Bay bay = new Bay(4, 2);
        assertTrue(bay.getInventory().isEmpty());

        // Mutate the returned list to ensure we have a live reference
        Items items = new Items("SKU1", "Prod1", "Cat", "Pack", 1.0f, 2.0f);
        bay.getInventory().add(new Box(items, 1));
        assertEquals(1, bay.getInventory().size());
    }

    /**
     * Verifies {@link Bay#setInventory(List)} replaces the internal reference and preserves content.
     *
     * @see Bay#setInventory(List)
     */
    @Test
    void setInventory_replacesReference() {
        Bay bay = new Bay(5, 3);
        List<Box> custom = new ArrayList<>();
        custom.add(new Box(new Items("S1", "N1", "C1", "P1", 1.0f, 1.0f), 2));
        custom.add(new Box(new Items("S2", "N2", "C2", "P2", 2.0f, 3.0f), 4, "2030-12-31"));

        bay.setInventory(custom);

        assertSame(custom, bay.getInventory());
        assertEquals(2, bay.getInventory().size());
    }

    /**
     * Verifies {@link Bay#getCapacity()} returns the current capacity.
     *
     * @see Bay#getCapacity()
     */
    @Test
    void getCapacity_returnsCurrentValue() {
        Bay bay = new Bay(6, 8);
        assertEquals(8, bay.getCapacity());
    }

    /**
     * Verifies {@link Bay#setCapacity(int)} persists the new capacity.
     *
     * @see Bay#setCapacity(int)
     */
    @Test
    void setCapacity_updatesValue() {
        Bay bay = new Bay(7, 1);
        bay.setCapacity(4);
        assertEquals(4, bay.getCapacity());
    }

    /**
     * Verifies {@link Bay#addProductBox(Box)} behavior for both states:
     * success when there is free capacity and failure once the bay is full.
     * (Both branches covered within this single test method.)
     *
     * @see Bay#addProductBox(Box)
     */
    @Test
    void addProductBox_handlesAvailableAndFull() {
        Bay bay = new Bay(8, 2);

        Items itemsA = new Items("A", "NameA", "CatA", "PackA", 0.5f, 0.2f);
        Items itemsB = new Items("B", "NameB", "CatB", "PackB", 0.7f, 0.3f);
        Items itemsC = new Items("C", "NameC", "CatC", "PackC", 0.9f, 0.4f);

        // 1) Success while capacity not reached
        assertTrue(bay.addProductBox(new Box(itemsA, 1)));
        assertTrue(bay.addProductBox(new Box(itemsB, 2)));
        assertEquals(2, bay.getInventory().size());

        // 2) Failure when full
        assertFalse(bay.addProductBox(new Box(itemsC, 3)));
        assertEquals(2, bay.getInventory().size());
    }

    /**
     * Verifies {@link Bay#toString()}:
     * - on empty inventory it prints only the header;
     * - on non-empty inventory it prints the header plus each box line.
     * (Both branches covered within this single test method.)
     *
     * @see Bay#toString()
     */
    @Test
    void toString_formatsEmptyAndNonEmptyInventory() {
        // Empty case
        Bay empty = new Bay(9, 3);
        String expectedEmpty = "Bay ID: 9, with 0 boxes, and 3 capacity";
        assertEquals(expectedEmpty, empty.toString());

        // Non-empty case
        Bay nonEmpty = new Bay(10, 5);
        Items i1 = new Items("X1", "NameX1", "CatX", "PackX", 1.23f, 4.56f);
        Items i2 = new Items("X2", "NameX2", "CatY", "PackY", 2.34f, 5.67f);

        // One box without expiry, one with expiry to reflect both Box.toString() variants
        Box b1 = new Box(i1, 2);
        Box b2 = new Box(i2, 3, "2029-01-01");
        nonEmpty.addProductBox(b1);
        nonEmpty.addProductBox(b2);

        String s = nonEmpty.toString();

        assertTrue(s.startsWith("Bay ID: 10, with 2 boxes, and 5 capacity"));
        // It should contain each Box.toString() on its own line
        assertTrue(s.contains(b1.toString() + "\n"));
        assertTrue(s.contains(b2.toString() + "\n"));
    }
}
