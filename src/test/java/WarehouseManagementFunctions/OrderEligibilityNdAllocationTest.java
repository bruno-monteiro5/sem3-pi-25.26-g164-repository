package WarehouseManagementFunctions;

import controller.WagonUnloadingController;
import domain.Aisle;
import domain.Allocation;
import domain.Bay;
import domain.Warehouse;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class OrderEligibilityNdAllocationTest {

    @Test
    void run() throws FileNotFoundException {

        WagonUnloadingController.beginWarehouse();
        WagonUnloadingController.beginProducts();
        Warehouse w = WagonUnloadingController.warehouse;
        WagonUnloadingController.removeCargoFromWagon(w, WagonUnloadingController.products);

        assertEquals(5, w.getAisles().size());
        assertEquals(20, w.getAisles().get(0).getBays().size());

        for(Aisle aisle : w.getAisles()){
            for(Bay bay : aisle.getBays()){
                assertTrue(bay.getInventory().size() <= bay.getCapacity());
            }
        }

        assertTrue(OrderEligibilityNdAllocationUI.allocation.isEmpty());
        for(Allocation allocation : OrderEligibilityNdAllocationUI.allocation){
            assertTrue(allocation.totalWeight()<=200);
        }

    }
}