package repository;

import controller.InventoryPort;
import domain.Aisle;
import domain.Bay;
import domain.Box;
import domain.Items;
import domain.Warehouse;

import java.time.LocalDateTime;

/**
 * Concrete adapter that bridges USEI05 return inspections to the warehouse model.
 * <p>
 * Implements {@link InventoryPort} by inserting returned boxes into the current
 * {@link Warehouse}, reusing an existing {@link Items} instance when the SKU is
 * already present in stock. If none is found, a minimal {@link Items} is created
 * so the flow can proceed.
 * <p>
 * Notes:
 * <ul>
 *   <li>Accesses the active warehouse via {@link WarehouseRepository#getCurrentWarehouse()}.</li>
 *   <li>Searches aisles → bays → bay inventory (see {@link Bay#getInventory()}) to locate an existing SKU.</li>
 *   <li>Places the new box into the first bay that still has capacity (see {@link Bay#addProductBox(Box)}).</li>
 * </ul>
 */
public class InventoryAdapter implements InventoryPort {

    /**
     * Restocks a returned box back into the warehouse.
     * <p>
     * Attempts to reuse an existing {@link Items} instance for the given SKU; if not available,
     * a minimal {@link Items} is created. The returned {@link Box} is then placed into the first
     * bay with available capacity.
     *
     * @param boxId      identifier of the returned box (e.g., {@code RET-000123})
     * @param sku        product SKU
     * @param quantity   number of units to restock
     * @param expiryDate optional expiry date string (may be {@code null})
     * @param receivedAt timestamp when the returned box was received
     * @throws Exception if the warehouse is not initialized or no bay has available capacity
     */
    @Override
    public void restockReturnedBox(String boxId,
                                   String sku,
                                   int quantity,
                                   String expiryDate,
                                   LocalDateTime receivedAt) throws Exception {

        Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
        if (warehouse == null) {
            throw new IllegalStateException("Warehouse not initialized.");
        }

        Items product = null;
        for (Aisle aisle : warehouse.getAisles()) {
            for (Bay bay : aisle.getBays()) {
                for (Box box : bay.getInventory()) {
                    if (box.getProduct().getSkuID().equals(sku)) {
                        product = box.getProduct();
                        break;
                    }
                }
                if (product != null) break;
            }
            if (product != null) break;
        }

        if (product == null) {
            product = new Items(sku, sku, "", "", 0.0f, 0.0f);
        }

        Box newBox = (expiryDate != null)
                ? new Box(boxId, product, quantity, expiryDate)
                : new Box(boxId, product, quantity);

        boolean placed = false;
        for (Aisle aisle : warehouse.getAisles()) {
            for (Bay bay : aisle.getBays()) {
                if (bay.addProductBox(newBox)) {
                    placed = true;
                    break;
                }
            }
            if (placed) break;
        }

        if (!placed) {
            throw new Exception("No available capacity in the warehouse for the returned box (SKU=" + sku + ").");
        }
    }
}
