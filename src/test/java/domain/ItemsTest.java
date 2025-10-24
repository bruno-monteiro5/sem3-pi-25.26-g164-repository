package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class ItemsTest {


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


    @Test
    void setAndGetSkuID_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 1.0f, 1.0f);
        items.setSkuID("B2");
        assertEquals("B2", items.getSkuID());
    }

    @Test
    void setAndGetName_updatesAndReturnsValue() {
        Items items = new Items("A1", "OldName", "Cat", "Pack", 1.0f, 1.0f);
        items.setName("NewName");
        assertEquals("NewName", items.getName());
    }


    @Test
    void setAndGetCategory_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "OldCat", "Pack", 1.0f, 1.0f);
        items.setCategory("NewCat");
        assertEquals("NewCat", items.getCategory());
    }


    @Test
    void setAndGetPack_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Box", 1.0f, 1.0f);
        items.setPack("Bag");
        assertEquals("Bag", items.getPack());
    }


    @Test
    void setAndGetVolume_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 2.0f, 1.0f);
        items.setVolume(5.5f);
        assertEquals(5.5f, items.getVolume());
    }


    @Test
    void setAndGetWeight_updatesAndReturnsValue() {
        Items items = new Items("A1", "Name", "Cat", "Pack", 1.0f, 3.0f);
        items.setWeight(7.2f);
        assertEquals(7.2f, items.getWeight());
    }

    @Test
    void toString_formatsAllFieldsCorrectly() {
        Items items = new Items("SKU-X", "Prod-X", "Category-X", "Pack-X", 1.23f, 4.56f);

        String expected = "Product Prod-X, SKU: SKU-X, Category: Category-X, Pack: Pack-X, Volume: 1.23, Weight: 4.56";
        assertEquals(expected, items.toString());
    }
}
