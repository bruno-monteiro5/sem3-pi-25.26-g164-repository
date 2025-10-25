package controller;

import java.time.LocalDateTime;

/**
 * Integration port for USEI05 to update warehouse stock after an inspection.
 * <p>
 * Provide a concrete implementation that uses your existing warehouse model
 * (e.g., WarehouseRepository, Warehouse/Bay/Box) and FEFO/FIFO insertion rules.
 */
public interface InventoryPort {

    /**
     * Restocks a returned box into the warehouse according to FEFO/FIFO rules.
     *
     * @param boxId      identifier like "RET-<returnId>"
     * @param sku        product SKU
     * @param quantity   number of units to restock
     * @param expiryDate optional expiry date (may be null)
     * @param receivedAt timestamp when the returned box was received
     * @throws Exception to signal domain-specific validation or capacity issues
     */
    void restockReturnedBox(String boxId,
                            String sku,
                            int quantity,
                            String expiryDate,
                            LocalDateTime receivedAt) throws Exception;
}
