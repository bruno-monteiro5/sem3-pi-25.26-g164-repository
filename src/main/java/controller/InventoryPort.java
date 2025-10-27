package controller;

import java.time.LocalDateTime;

/**
 * Integration port for USEI05 to update warehouse stock after an inspection.
 * <p>
 * Provide a concrete implementation that delegates to your existing warehouse model
 * (e.g., repository and domain entities) and applies FEFO/FIFO insertion rules.
 */
public interface InventoryPort {

    /**
     * Restocks a returned box into the warehouse according to FEFO/FIFO rules.
     * <p>
     * Implementations should validate SKU existence, available capacity, and
     * consistency of perishable metadata. When present, {@code expiryDate} should
     * follow ISO-8601 date format (e.g., {@code 2025-11-14}); otherwise it may be {@code null}.
     *
     * @param boxId      identifier of the returned box (e.g., {@code RET-<returnId>})
     * @param sku        product SKU to restock
     * @param quantity   number of units to add to stock
     * @param expiryDate optional expiration date in ISO-8601 (may be {@code null})
     * @param receivedAt timestamp when the box was received, as {@link LocalDateTime}
     * @throws Exception if domain validation fails or capacity constraints are violated
     */
    void restockReturnedBox(String boxId,
                            String sku,
                            int quantity,
                            String expiryDate,
                            LocalDateTime receivedAt) throws Exception;
}
