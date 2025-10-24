package repository;

import domain.Allocation;
import domain.Warehouse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link WarehouseRepository}.
 * <p>
 *     Each test method targets exactly one public member of {@code WarehouseRepository}
 *     and documents the scenario, preconditions, and expected outcome.
 * </p>
 */

class WarehouseRepositoryTest {

    /**
     * Tests the getter and setter for the current warehouse.
     *
     * Expected:
     *  - {@link WarehouseRepository#getCurrentWarehouse()} returns the updated warehouse.
     *
     *  @see WarehouseRepository#currentWarehouse
     */


    @Test
    void getCurrentWarehouse() {
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        warehouseRepository.setCurrentWarehouse(null);
        assertNull(warehouseRepository.getCurrentWarehouse());


        Warehouse w = new Warehouse("1");
        warehouseRepository.setCurrentWarehouse(w);
        assertEquals(w, warehouseRepository.getCurrentWarehouse());
    }

    /**
     * Tests the setter for the current warehouse.
     *
     * Expected:
     *  - {@link WarehouseRepository#setCurrentWarehouse(Warehouse)} sets the Warehouse correcly.
     *
     *
     * @see WarehouseRepository#currentWarehouse
     */

    @Test
    void setCurrentWarehouse() {
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        warehouseRepository.setCurrentWarehouse(null);
        assertNull(warehouseRepository.getCurrentWarehouse());

        Warehouse w = new Warehouse("1");
        warehouseRepository.setCurrentWarehouse(w);
        assertEquals(w, warehouseRepository.getCurrentWarehouse());
    }

    /**
     * Tests the getter for the current allocation list.
     *
     * Expected:
     *  - {@link WarehouseRepository#getAllocation()} returns the updated allocation list.
     *
     *  @see WarehouseRepository#allocation
     */

    @Test
    void getAllocation() {
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        assertTrue(warehouseRepository.getAllocation().isEmpty());

        List<Allocation> a = new ArrayList<>();
        assertEquals(a, warehouseRepository.getAllocation());
    }

    /**
     * Tests the setter for the current allocation list.
     *
     * Expected:
     *  - {@link WarehouseRepository#setAllocation(List)} sets the current allocation correcly.
     *
     *
     * @see WarehouseRepository#allocation
     */

    @Test
    void setAllocation() {
        WarehouseRepository warehouseRepository = new WarehouseRepository();
        List<Allocation> a = new ArrayList<>();
        warehouseRepository.setAllocation(a);
        assertEquals(a, warehouseRepository.getAllocation());
    }
}