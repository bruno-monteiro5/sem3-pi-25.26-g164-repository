package controller;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class WagonUnloadingControllerTest {

    @Test
    void beginWarehouse() throws FileNotFoundException {
        WagonUnloadingController.beginWarehouse();
        assertFalse(WagonUnloadingController.warehouse == null);
        assertTrue(WagonUnloadingController.warehouse.getFirstAvailableAisle().getFirstAvailableBay().getInventory().isEmpty());
        assertFalse(WagonUnloadingController.products.isEmpty());
    }

    @Test
    void beginProducts() throws FileNotFoundException {
        WagonUnloadingController.beginProducts();
        assertFalse(WagonUnloadingController.products.isEmpty());
    }

    @Test
    void removeCargoFromWagon() throws FileNotFoundException {
        WagonUnloadingController.beginWarehouse();
        WagonUnloadingController.beginProducts();
        WagonUnloadingController.removeCargoFromWagon(WagonUnloadingController.warehouse, WagonUnloadingController.products);
        assertFalse(WagonUnloadingController.warehouse.getFirstAvailableAisle().getFirstAvailableBay().getInventory().isEmpty());
        assertFalse(WagonUnloadingController.products.isEmpty());
    }

    @Test
    void fillMap() {
        assertFalse(WagonUnloadingController.nonPerishable.isEmpty());
        assertFalse(WagonUnloadingController.perishable.isEmpty());
    }
}