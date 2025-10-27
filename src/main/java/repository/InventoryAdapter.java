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
 * Implements {@link InventoryPort} by inserting returned boxes back into the current
 * warehouse, reusing the {@link Items} instance whenever the SKU already exists in stock.
 * Falls back to creating a basic {@code Items} if none is found (so the flow does not break).
 * <p>
 * Notes:
 * <ul>
 *   <li>Uses {@code WarehouseRepository.getCurrentWarehouse()} to access the active warehouse.</li>
 *   <li>Iterates aisles → bays → bay inventory (via {@link Bay#getInventory()}) to locate the SKU.</li>
 *   <li>Places the new box into the first bay that still has capacity (via {@link Bay#addProductBox(Box)}).</li>
 * </ul>
 */
public class InventoryAdapter implements InventoryPort {

    @Override
    public void restockReturnedBox(String boxId,
                                   String sku,
                                   int quantity,
                                   String expiryDate,
                                   LocalDateTime receivedAt) throws Exception {

        // Obtain current warehouse
        Warehouse warehouse = WarehouseRepository.getCurrentWarehouse(); // or WarehouseRepository.currentWarehouse
        if (warehouse == null) {
            throw new IllegalStateException("Warehouse not initialized.");
        }

        // Try to reuse an existing Items (same SKU) already in stock
        Items product = null;
        for (Aisle aisle : warehouse.getAisles()) {
            for (Bay bay : aisle.getBays()) {
                for (Box box : bay.getInventory()) {        // <-- use Bay#getInventory()
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
            // No existing Items found for this SKU — create a basic Items.
            // ⚠️ If your Items constructor has a different signature, adjust the next line accordingly.
            product = new Items(sku, sku, "", "", 0.0f, 0.0f);
        }

        // Build the returned box (use expiryDate variant if provided)
        Box newBox = (expiryDate != null)
                ? new Box(boxId, product, quantity, expiryDate)
                : new Box(boxId, product, quantity);

        // Place the box into the first bay with available capacity
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
            throw new Exception("Sem espaço disponível no armazém para a caixa devolvida (SKU=" + sku + ").");
        }
    }
}
