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

        assertEquals(4, WarehouseRepository.getCurrentWarehouse().getAisles());
        assertEquals(20, WarehouseRepository.getCurrentWarehouse().getFirstAvailableAisle().getBays().size());

        for(Aisle aisle : WarehouseRepository.getCurrentWarehouse().getAisles()){
            for(Bay bay : aisle.getBays()){
                assertTrue(bay.getInventory().size() <= bay.getCapacity());
            }
        }

    }
}