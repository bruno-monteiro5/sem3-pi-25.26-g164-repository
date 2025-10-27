package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Box}.
 * <p>
 * Each test method targets exactly one public member of {@code Box}
 * and documents the scenario, preconditions, and expected outcome.
 */
class BoxTest {

    /**
     * Verifies that the constructor with expiry initializes all fields.
     *
     * Preconditions:
     *  - Valid {@link Items}, positive quantity, and non-null expiry date supplied.
     *
     * Expected:
     *  - All getters return the values passed to the constructor.
     *
     * @see Box#Box(Items, int, String)
     * @see Box#getProduct()
     * @see Box#getQuantity()
     * @see Box#getExpDate()
     */
    @Test
    void constructor_withExpiry_initializesFields() {
        Items items = new Items("SKU1", "Name1", "CatA", "PackA", 1.23f, 4.56f);
        Box box = new Box(items, 5, "2029-12-31");

        assertSame(items, box.getProduct());
        assertEquals(5, box.getQuantity());
        assertEquals("2029-12-31", box.getExpDate());
    }

    /**
     * Verifies that the constructor without expiry initializes fields
     * and sets {@code expDate} to {@code null}.
     *
     * Preconditions:
     *  - Valid {@link Items} and positive quantity supplied.
     *
     * Expected:
     *  - Product and quantity set; expiry is {@code null}.
     *
     * @see Box#Box(Items, int)
     * @see Box#getProduct()
     * @see Box#getQuantity()
     * @see Box#getExpDate()
     */
    @Test
    void constructor_withoutExpiry_initializesFieldsAndNullExpiry() {
        Items items = new Items("SKU2", "Name2", "CatB", "PackB", 2.00f, 3.00f);
        Box box = new Box(items, 3);

        assertSame(items, box.getProduct());
        assertEquals(3, box.getQuantity());
        assertNull(box.getExpDate());
    }

    /**
     * Tests the getter for the product (items).
     *
     * Scenario:
     *  - Create a box and retrieve its product.
     *
     * Expected:
     *  - Returned reference equals the one given at construction.
     *
     * @see Box#getProduct()
     */
    @Test
    void getProduct_returnsCurrentItems() {
        Items items = new Items("S3", "N3", "C3", "P3", 0.50f, 0.75f);
        Box box = new Box(items, 1);
        assertSame(items, box.getProduct());
    }

    /**
     * Tests the setter for the product (items).
     *
     * Scenario:
     *  - Replace the current product with a new instance.
     *
     * Expected:
     *  - Getter reflects the new reference.
     *
     * @see Box#setProduct(Items)
     * @see Box#getProduct()
     */
    @Test
    void setProduct_updatesReference() {
        Items initial = new Items("S4", "N4", "C4", "P4", 1.00f, 2.00f);
        Items updated = new Items("S5", "N5", "C5", "P5", 1.10f, 2.10f);
        Box box = new Box(initial, 2);

        box.setProduct(updated);

        assertSame(updated, box.getProduct());
    }

    /**
     * Tests the getter for quantity.
     *
     * Scenario:
     *  - Create a box and query its quantity.
     *
     * Expected:
     *  - Returned value matches the constructor value.
     *
     * @see Box#getQuantity()
     */
    @Test
    void getQuantity_returnsCurrentValue() {
        Box box = new Box(new Items("S6", "N6", "C6", "P6", 3.33f, 6.66f), 7);
        assertEquals(7, box.getQuantity());
    }

    /**
     * Tests the setter for quantity.
     *
     * Scenario:
     *  - Change the quantity and read it back.
     *
     * Expected:
     *  - Getter returns the updated value.
     *
     * @see Box#setQuantity(int)
     * @see Box#getQuantity()
     */
    @Test
    void setQuantity_updatesValue() {
        Box box = new Box(new Items("S7", "N7", "C7", "P7", 2.22f, 4.44f), 1);
        box.setQuantity(9);
        assertEquals(9, box.getQuantity());
    }

    /**
     * Tests the getter for expiry date.
     *
     * Scenario:
     *  - Construct with a known expiry and retrieve it.
     *
     * Expected:
     *  - Returned value matches the constructor value.
     *
     * @see Box#getExpDate()
     */
    @Test
    void getExpDate_returnsCurrentValue() {
        Box box = new Box(new Items("S8", "N8", "C8", "P8", 1.11f, 2.22f), 2, "2031-01-01");
        assertEquals("2031-01-01", box.getExpDate());
    }

    /**
     * Tests the setter for expiry date.
     *
     * Scenario:
     *  - Update expiry date and read it back.
     *
     * Expected:
     *  - Getter returns the updated value.
     *
     * @see Box#setExpDate(String)
     * @see Box#getExpDate()
     */
    @Test
    void setExpDate_updatesValue() {
        Box box = new Box(new Items("S9", "N9", "C9", "P9", 5.55f, 6.66f), 4);
        assertNull(box.getExpDate());

        box.setExpDate("2030-06-30");
        assertEquals("2030-06-30", box.getExpDate());
    }

    /**
     * Tests the string representation of a box without expiry.
     *
     * Scenario:
     *  - Construct a box without expiry.
     *
     * Expected:
     *  - Format: "Box of {quantity} units of {items.toString()}: ID {boxID}".
     *  - {@link Items#toString()} must appear with two decimal places for volume/weight.
     *
     * @see Box#toString()
     * @see Items#toString()
     */
    @Test
    void toString_withoutExpiry_formatsCorrectly() {
        Items items = new Items("SKU-X", "Prod-X", "Category-X", "Pack-X", 1.20f, 3.40f);
        Box box = new Box(items, 2); // boxID == null

        String expectedItems = "Product Prod-X, SKU: SKU-X, Category: Category-X, Pack: Pack-X, Volume: 1.20, Weight: 3.40";
        String expected = "Box of 2 units of " + expectedItems + ": ID null";

        assertEquals(expected, box.toString());
    }

    /**
     * Tests the string representation of a box with expiry.
     *
     * Scenario:
     *  - Construct a box with an explicit expiry.
     *
     * Expected:
     *  - Format: "Box of {quantity} units of {items.toString()}, expires on {expDate}: ID {boxID}".
     *
     * @see Box#toString()
     * @see Items#toString()
     */
    @Test
    void toString_withExpiry_formatsCorrectly() {
        Items items = new Items("SKU-Y", "Prod-Y", "Category-Y", "Pack-Y", 2.34f, 5.67f);
        Box box = new Box(items, 3, "2028-09-15"); // boxID == null

        String expectedItems = "Product Prod-Y, SKU: SKU-Y, Category: Category-Y, Pack: Pack-Y, Volume: 2.34, Weight: 5.67";
        String expected = "Box of 3 units of " + expectedItems + ", expires on 2028-09-15: ID null";

        assertEquals(expected, box.toString());
    }

    /**
     * Verifies that the constructor with box ID and expiry initializes all fields.
     *
     * Preconditions:
     *  - Valid {@link Items}, positive quantity, non-null expiry; explicit box ID supplied.
     *
     * Expected:
     *  - All getters return the values passed to the constructor, including {@code boxID}.
     *
     * @see Box#Box(String, Items, int, String)
     * @see Box#getBoxID()
     * @see Box#getProduct()
     * @see Box#getQuantity()
     * @see Box#getExpDate()
     */
    @Test
    void constructor_withBoxIdAndExpiry_initializesAllFields() {
        Items items = new Items("SKU-A", "Name-A", "Cat-A", "Pack-A", 1.00f, 2.00f);
        Box box = new Box("B-A", items, 10, "2032-12-31");

        assertEquals("B-A", box.getBoxID());
        assertSame(items, box.getProduct());
        assertEquals(10, box.getQuantity());
        assertEquals("2032-12-31", box.getExpDate());
    }

    /**
     * Verifies that the constructor with box ID and without expiry sets {@code expDate} to {@code null}.
     *
     * Preconditions:
     *  - Valid {@link Items}, positive quantity; explicit box ID supplied; no expiry.
     *
     * Expected:
     *  - Product, quantity and box ID set; expiry is {@code null}.
     *
     * @see Box#Box(String, Items, int)
     * @see Box#getBoxID()
     * @see Box#getExpDate()
     */
    @Test
    void constructor_withBoxIdWithoutExpiry_setsNullExpiry() {
        Items items = new Items("SKU-B", "Name-B", "Cat-B", "Pack-B", 0.75f, 1.25f);
        Box box = new Box("B-B", items, 6);

        assertEquals("B-B", box.getBoxID());
        assertSame(items, box.getProduct());
        assertEquals(6, box.getQuantity());
        assertNull(box.getExpDate());
    }

    /**
     * Tests the getter for the box identifier.
     *
     * Scenario:
     *  - Construct a box with an explicit box ID and read it back.
     *
     * Expected:
     *  - Returned value matches the constructor value.
     *
     * @see Box#getBoxID()
     */
    @Test
    void getBoxID_returnsCurrentIdentifier() {
        Box box = new Box("B-C", new Items("S-C", "N-C", "C-C", "P-C", 1.10f, 2.10f), 3);
        assertEquals("B-C", box.getBoxID());
    }

    /**
     * Tests the setter for the box identifier.
     *
     * Scenario:
     *  - Update the box ID and read it back.
     *
     * Expected:
     *  - Getter returns the updated identifier.
     *
     * @see Box#setBoxID(String)
     * @see Box#getBoxID()
     */
    @Test
    void setBoxID_updatesIdentifier() {
        Box box = new Box(new Items("S-D", "N-D", "C-D", "P-D", 2.20f, 3.30f), 4);
        assertNull(box.getBoxID());

        box.setBoxID("B-D");
        assertEquals("B-D", box.getBoxID());
    }

}
