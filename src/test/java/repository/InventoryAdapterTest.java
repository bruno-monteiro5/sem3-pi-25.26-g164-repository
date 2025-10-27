package repository;

import controller.InventoryPort;
import domain.Aisle;
import domain.Bay;
import domain.Box;
import domain.Items;
import domain.Warehouse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link InventoryAdapter}.
 * <p>
 * This test class ensures full branch coverage of
 * {@link InventoryPort#restockReturnedBox(String, String, int, String, LocalDateTime)},
 * verifying the correct interaction with {@link WarehouseRepository} and
 * the underlying warehouse domain model.
 * <p>
 * Covered scenarios:
 * <ul>
 *   <li>Warehouse not initialized (throws {@link IllegalStateException});</li>
 *   <li>Reuse of existing {@link Items} by matching SKU;</li>
 *   <li>Creation of a minimal {@link Items} when SKU not found;</li>
 *   <li>Placement of boxes in the first available bay with capacity;</li>
 *   <li>No capacity available (throws generic {@link Exception});</li>
 *   <li>Box creation with and without an expiry date.</li>
 * </ul>
 */
class InventoryAdapterTest {

    /**
     * Cleans up static state after each test to prevent side effects
     * between consecutive test executions.
     */
    @AfterEach
    void cleanup() {
        WarehouseRepository.setCurrentWarehouse(null);
    }

    /**
     * Creates a temporary in-memory warehouse with a single aisle and one bay
     * of the specified capacity.
     * <p>
     * The constructed warehouse is set as the active instance
     * in {@link WarehouseRepository}.
     *
     * @param capacity the number of boxes that the single bay can hold
     * @return the created {@link Bay} instance for direct inspection
     */
    private Bay newWarehouseWithSingleBay(int capacity) {
        Warehouse w = new Warehouse("W1");
        w.addAisle(1);
        Aisle a = w.getAisles().get(0);
        Bay b = new Bay(1, capacity);
        a.addBay(b);
        WarehouseRepository.setCurrentWarehouse(w);
        return b;
    }

    /**
     * Verifies that calling {@code restockReturnedBox} without initializing
     * a warehouse triggers an {@link IllegalStateException}.
     */
    @Test
    void restock_warehouseNotInitialized_throwsIllegalState() {
        WarehouseRepository.setCurrentWarehouse(null);
        InventoryAdapter adapter = new InventoryAdapter();

        Exception ex = assertThrows(IllegalStateException.class, () ->
                adapter.restockReturnedBox("RET-1", "SKU-X", 1, null, LocalDateTime.now()));
        assertTrue(ex.getMessage().toLowerCase().contains("warehouse"));
    }

    /**
     * Verifies that when a box with a matching SKU already exists in stock,
     * the adapter reuses the same {@link Items} instance rather than creating a new one.
     */
    @Test
    void restock_reusesExistingItemsBySku_inSameBay() throws Exception {
        InventoryAdapter adapter = new InventoryAdapter();
        Bay bay = newWarehouseWithSingleBay(2);

        Items existing = new Items("SKU1", "SKU1", "", "", 0.0f, 0.0f);
        Box existingBox = new Box("BOX-OLD", existing, 3, null);
        assertTrue(bay.addProductBox(existingBox));

        int before = bay.getInventory().size();
        adapter.restockReturnedBox("RET-123", "SKU1", 5, null, LocalDateTime.now());

        List<Box> inv = bay.getInventory();
        assertEquals(before + 1, inv.size());
        Box added = inv.get(inv.size() - 1);

        assertSame(existing, added.getProduct());
        assertEquals("SKU1", added.getProduct().getSkuID());
        assertEquals(5, added.getQuantity());
        assertNull(added.getExpDate());
    }

    /**
     * Verifies that if the SKU is not present in the warehouse,
     * the adapter creates a minimal {@link Items} instance and places the box
     * in the first bay with available capacity.
     */
    @Test
    void restock_createsNewItemsWhenSkuNotFound_andPlacesInFirstBay() throws Exception {
        InventoryAdapter adapter = new InventoryAdapter();
        Bay bay = newWarehouseWithSingleBay(2);

        Items other = new Items("OTHER", "OTHER", "", "", 0.0f, 0.0f);
        assertTrue(bay.addProductBox(new Box("BOX-OTHER", other, 1)));

        int before = bay.getInventory().size();
        adapter.restockReturnedBox("RET-9", "SKU-NEW", 2, "2025-12-31", LocalDateTime.now());

        List<Box> inv = bay.getInventory();
        assertEquals(before + 1, inv.size());
        Box added = inv.get(inv.size() - 1);

        assertEquals("SKU-NEW", added.getProduct().getSkuID());
        assertEquals(2, added.getQuantity());
        assertEquals("2025-12-31", added.getExpDate());
        assertNotSame(other, added.getProduct());
    }

    /**
     * Verifies that when no bay has capacity available, the adapter
     * throws a generic {@link Exception} indicating the condition.
     */
    @Test
    void restock_noCapacityAnywhere_throwsException() {
        InventoryAdapter adapter = new InventoryAdapter();
        Bay bay = newWarehouseWithSingleBay(1);

        Items existing = new Items("SKU1", "SKU1", "", "", 0.0f, 0.0f);
        assertTrue(bay.addProductBox(new Box("BOX-OLD", existing, 1)));

        Exception ex = assertThrows(Exception.class, () ->
                adapter.restockReturnedBox("RET-2", "SKU2", 1, null, LocalDateTime.now()));
        assertTrue(ex.getMessage().toLowerCase().contains("no available capacity"));
    }
}
