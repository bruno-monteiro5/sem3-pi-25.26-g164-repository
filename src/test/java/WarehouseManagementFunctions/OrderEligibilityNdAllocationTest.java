package WarehouseManagementFunctions;

import domain.Aisle;
import domain.Bay;
import org.junit.jupiter.api.Test;
import repository.WarehouseRepository;

import static org.junit.jupiter.api.Assertions.*;

class OrderEligibilityNdAllocationTest {

    @Test
    void run() {

        if(WarehouseRepository.getCurrentWarehouse() == null){
            Runnable run = new WagonUnloading();
            run.run();
        }

        assertEquals(4, WarehouseRepository.getCurrentWarehouse().getAisles());
        assertEquals(20, WarehouseRepository.getCurrentWarehouse().getFirstAvailableAisle().getBays().size());

        for(Aisle aisle : WarehouseRepository.getCurrentWarehouse().getAisles()){
            for(Bay bay : aisle.getBays()){
                assertTrue(bay.getInventory().size() <= bay.getCapacity());
            }
        }

    }
}