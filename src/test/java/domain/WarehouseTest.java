package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test suite for {@link Warehouse}.
 * <p>
 * Each test method targets exactly one public member of {@code Warehouse}
 * and documents the scenario, preconditions, and expected outcome.
 */
class WarehouseTest {

    /**
     * Verifies that the constructor initializes the ID and aisle list correctly.
     *
     * Preconditions:
     *  - Valid warehouse ID provided.
     *
     * Expected:
     *  - {@link Warehouse#getWarehouseID()} returns the ID;
     *  - {@link Warehouse#getAisles()} is initialized as an empty list.
     *
     * @see Warehouse#Warehouse(String)
     * @see Warehouse#getWarehouseID()
     * @see Warehouse#getAisles()
     */
    @Test
    void constructor_initializesFieldsCorrectly() {
        Warehouse wh = new Warehouse("WH1");

        assertEquals("WH1", wh.getWarehouseID());
        assertNotNull(wh.getAisles());
        assertTrue(wh.getAisles().isEmpty());
    }

    /**
     * Tests {@link Warehouse#getAisles()} and {@link Warehouse#setAisles(List)}.
     *
     * Scenario:
     *  - Replace the aisles list with a predefined one.
     *
     * Expected:
     *  - Getter returns the same reference and same content.
     */
    @Test
    void setAndGetAisles_replacesAndReturnsSameList() {
        Warehouse wh = new Warehouse("WH2");

        List<Aisle> list = new ArrayList<>();
        list.add(new Aisle(1));
        list.add(new Aisle(2));

        wh.setAisles(list);

        assertSame(list, wh.getAisles());
        assertEquals(2, wh.getAisles().size());
    }

    /**
     * Tests {@link Warehouse#getWarehouseID()} and {@link Warehouse#setWarehouseID(String)}.
     *
     * Scenario:
     *  - Change the ID and verify update.
     *
     * Expected:
     *  - Getter returns new value.
     */
    @Test
    void setAndGetWarehouseID_updatesAndReturnsValue() {
        Warehouse wh = new Warehouse("OldID");
        wh.setWarehouseID("NewID");
        assertEquals("NewID", wh.getWarehouseID());
    }

    /**
     * Tests {@link Warehouse#addAisle(int)} adds a new aisle correctly.
     *
     * Expected:
     *  - List size increases by one and contains the new aisle.
     */
    @Test
    void addAisle_addsNewAisleToList() {
        Warehouse wh = new Warehouse("WH3");

        wh.addAisle(10);

        assertEquals(1, wh.getAisles().size());
        assertEquals(10, wh.getAisles().get(0).getAisleID());
    }

    /**
     * Tests {@link Warehouse#getLastAisle()} returns the last aisle.
     *
     * Scenario:
     *  - Add multiple aisles and request the last one.
     *
     * Expected:
     *  - Returned aisle matches the last added.
     */
    @Test
    void getLastAisle_returnsMostRecent() {
        Warehouse wh = new Warehouse("WH4");
        wh.addAisle(1);
        wh.addAisle(2);
        wh.addAisle(3);

        Aisle last = wh.getLastAisle();

        assertEquals(3, last.getAisleID());
    }

    /**
     * Tests {@link Warehouse#getFirstAvailableAisle()} returns the first with available bays.
     *
     * Scenario:
     *  - First aisle full, second with available bay, third empty.
     *
     * Expected:
     *  - Returns the second aisle.
     */
    @Test
    void getFirstAvailableAisle_returnsFirstNonFull() {
        Warehouse wh = new Warehouse("WH5");

        Aisle a1 = new Aisle(1);
        Aisle a2 = new Aisle(2);
        Aisle a3 = new Aisle(3);

        // fill a1 with bays at capacity
        Bay fullBay = new Bay(1, 1);
        fullBay.addProductBox(new Box(new Items("SKU1", "Prod", "Cat", "Pack", 1f, 1f), 1));
        a1.addBay(fullBay);

        // a2 has one bay partially available
        Bay bay = new Bay(2, 2);
        bay.addProductBox(new Box(new Items("SKU2", "Prod", "Cat", "Pack", 1f, 1f), 1)); // not full
        a2.addBay(bay);

        a3.addBay(new Bay(3, 5)); // empty but later

        wh.setAisles(List.of(a1, a2, a3));

        Aisle result = wh.getFirstAvailableAisle();
        assertSame(a2, result);
    }

    /**
     * Tests {@link Warehouse#getFirstAvailableAisle()} returns null when all aisles full.
     */
    @Test
    void getFirstAvailableAisle_returnsNullWhenNoneAvailable() {
        Warehouse wh = new Warehouse("WH6");
        Aisle a1 = new Aisle(1);
        Bay full = new Bay(1, 1);
        full.addProductBox(new Box(new Items("SKU1", "Prod", "Cat", "Pack", 1f, 1f), 1));
        a1.addBay(full);
        wh.addAisle(a1.getAisleID());
        wh.setAisles(List.of(a1));

        assertNull(wh.getFirstAvailableAisle());
    }
}
