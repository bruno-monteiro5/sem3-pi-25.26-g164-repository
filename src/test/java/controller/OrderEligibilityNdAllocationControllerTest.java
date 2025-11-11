package controller;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class OrderEligibilityNdAllocationControllerTest {

    @Test
    void fillOrders() throws FileNotFoundException {
        OrderEligibilityNdAllocationController.fillOrders();
        assertFalse(OrderEligibilityNdAllocationController.orders.isEmpty());
    }

    @Test
    void allocateBoxes() throws FileNotFoundException {
        WagonUnloadingController.beginWarehouse();
        WagonUnloadingController.beginProducts();
        WagonUnloadingController.removeCargoFromWagon(WagonUnloadingController.warehouse, WagonUnloadingController.products);
        OrderEligibilityNdAllocationController.fillOrders();
        OrderEligibilityNdAllocationController.allocateBoxes(true);
        assertFalse(OrderEligibilityNdAllocationController.allocation.isEmpty());
        WagonUnloadingController.beginWarehouse();
        WagonUnloadingController.beginProducts();
        WagonUnloadingController.removeCargoFromWagon(WagonUnloadingController.warehouse, WagonUnloadingController.products);
        OrderEligibilityNdAllocationController.fillOrders();
        OrderEligibilityNdAllocationController.allocateBoxes(false);
        assertFalse(OrderEligibilityNdAllocationController.allocation.isEmpty());
    }
}