package WarehouseManagementFunctions;

import domain.Aisle;
import domain.Bay;
import domain.Warehouse;
import org.junit.jupiter.api.Test;
import repository.WarehouseRepository;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class OrderEligibilityNdAllocationTest {

    @Test
    void run() throws FileNotFoundException {

        WagonUnloading.beginWarehouse();
        WagonUnloading.beginProducts();
        Warehouse w = WagonUnloading.warehouse;
        WagonUnloading.removeCargoFromWagon(w, WagonUnloading.products);

        assertEquals(5, w.getAisles().size());
        assertEquals(20, w.getAisles().get(0).getBays().size());

        for(Aisle aisle : w.getAisles()){
            for(Bay bay : aisle.getBays()){
                assertTrue(bay.getInventory().size() <= bay.getCapacity());
            }
        }

    }
}