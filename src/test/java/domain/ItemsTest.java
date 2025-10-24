package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Items}.
 * <p>
 * Each test method targets exactly one public member of {@code Items}
 * and documents the scenario, preconditions, and expected outcome.
 */
class ItemsTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - Valid SKU, name, category, pack, volume, and weight supplied.
     *
     * Expected:
     *  - Each getter returns the value provided to the constructor.
     *
     * @see Items#Items(String, String, String, String, float, float)
     */
    @Test
    void constructor_initializesAllFields() {
        Items items = new Items("SKU1", "Prod1", "CategoryA", "Pack1", 1.5f, 2.3f);

        assertEquals("SKU1", items.getSkuID());
        assertEquals("Prod1", items.getName());
        assertEquals("CategoryA", items.getCategory());
        assertEquals("Pack1", items.getPack());
        assertEquals(1.5f, items.getVolume());
        assertEquals(2.3f, items.getWeight());
    }

    /**
     * Tests the getter and setter for SKU ID.
     *
     * Scenario:
     *  - Change SKU ID and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setSkuID(String)
     * @see Items#getSkuID()
     */
    @Test
    void setAndGetSkuID_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 1.0f, 1.0f);
        items.setSkuID("B2");
        assertEquals("B2", items.getSkuID());
    }

    /**
     * Tests the getter and setter for product name.
     *
     * Scenario:
     *  - Change name and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setName(String)
     * @see Items#getName()
     */
    @Test
    void setAndGetName_updatesAndReturnsValue() {
        Items items = new Items("A1", "OldName", "Cat", "Pack", 1.0f, 1.0f);
        items.setName("NewName");
        assertEquals("NewName", items.getName());
    }

    /**
     * Tests the getter and setter for category.
     *
     * Scenario:
     *  - Change category and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setCategory(String)
     * @see Items#getCategory()
     */
    @Test
    void setAndGetCategory_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "OldCat", "Pack", 1.0f, 1.0f);
        items.setCategory("NewCat");
        assertEquals("NewCat", items.getCategory());
    }

    /**
     * Tests the getter and setter for packaging type.
     *
     * Scenario:
     *  - Change pack type and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setPack(String)
     * @see Items#getPack()
     */
    @Test
    void setAndGetPack_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Box", 1.0f, 1.0f);
        items.setPack("Bag");
        assertEquals("Bag", items.getPack());
    }

    /**
     * Tests the getter and setter for volume.
     *
     * Scenario:
     *  - Change volume and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setVolume(float)
     * @see Items#getVolume()
     */
    @Test
    void setAndGetVolume_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 2.0f, 1.0f);
        items.setVolume(5.5f);
        assertEquals(5.5f, items.getVolume());
    }

    /**
     * Tests the getter and setter for weight.
     *
     * Scenario:
     *  - Change weight and retrieve it.
     *
     * Expected:
     *  - The getter returns the updated value.
     *
     * @see Items#setWeight(float)
     * @see Items#getWeight()
     */
    @Test
    void setAndGetWeight_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 1.0f, 3.0f);
        items.setWeight(7.2f);
        assertEquals(7.2f, items.getWeight());
    }

    /**
     * Tests the string representation of an {@link Items} object.
     *
     * Scenario:
     *  - Create a product and call {@link Items#toString()}.
     *
     * Expected:
     *  - Returns the exact formatted string using dot (.) as decimal separator.
     *
     * @see Items#toString()
     */
    @Test
    void toString_formatsAllFieldsCorrectly() {
        Items items = new Items("SKU-X", "Prod-X", "Category-X", "Pack-X", 1.23f, 4.56f);

        String expected = "Product Prod-X, SKU: SKU-X, Category: Category-X, Pack: Pack-X, Volume: 1.23, Weight: 4.56";
        assertEquals(expected, items.toString());
    }
}
