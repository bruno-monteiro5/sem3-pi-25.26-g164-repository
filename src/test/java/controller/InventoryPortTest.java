package controller;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for {@link InventoryPort}.
 * <p>
 * These tests validate the expected method signature and behavioral contract
 * described in the Javadoc of the interface, using a local stub implementation.
 * Since {@code InventoryPort} is an interface with no default methods,
 * coverage is achieved via exercising a conforming implementation.
 */
class InventoryPortTest {

    /**
     * Minimal stub that enforces the documented preconditions:
     * <ul>
     *   <li>SKU must be non-null/non-blank;</li>
     *   <li>Quantity must be &gt; 0;</li>
     *   <li>If {@code expiryDate} is provided, it must be ISO-8601 (yyyy-MM-dd);</li>
     *   <li>{@code boxId} and {@code receivedAt} must be non-null.</li>
     * </ul>
     * This stub also captures the last call to allow verification of parameter passing.
     */
    private static final class StubInventoryPort implements InventoryPort {
        String lastBoxId;
        String lastSku;
        int lastQuantity;
        String lastExpiryDate;
        LocalDateTime lastReceivedAt;

        @Override
        public void restockReturnedBox(String boxId,
                                       String sku,
                                       int quantity,
                                       String expiryDate,
                                       LocalDateTime receivedAt) throws Exception {
            if (boxId == null) throw new Exception("boxId must not be null");
            if (sku == null || sku.isBlank()) throw new Exception("sku must not be blank");
            if (quantity <= 0) throw new Exception("quantity must be > 0");
            if (receivedAt == null) throw new Exception("receivedAt must not be null");
            if (expiryDate != null) {
                // Must be ISO-8601 yyyy-MM-dd
                // If parsing fails, let it throw to comply with the contract.
                java.time.LocalDate.parse(expiryDate);
            }
            // capture for verification
            this.lastBoxId = boxId;
            this.lastSku = sku;
            this.lastQuantity = quantity;
            this.lastExpiryDate = expiryDate;
            this.lastReceivedAt = receivedAt;
        }
    }

    /**
     * Verifies that a valid call with ISO-8601 expiry date succeeds and parameters are captured.
     *
     * @throws Exception if the stub detects a contract violation (should not happen here)
     */
    @Test
    void restock_withIsoExpiry_succeeds_andCapturesParameters() throws Exception {
        StubInventoryPort port = new StubInventoryPort();
        LocalDateTime now = LocalDateTime.now();

        assertDoesNotThrow(() ->
                port.restockReturnedBox("RET-123", "SKU001", 10, "2025-11-14", now));

        assertEquals("RET-123", port.lastBoxId);
        assertEquals("SKU001", port.lastSku);
        assertEquals(10, port.lastQuantity);
        assertEquals("2025-11-14", port.lastExpiryDate);
        assertEquals(now, port.lastReceivedAt);
    }

    /**
     * Verifies that a valid call with {@code null} expiry date succeeds.
     */
    @Test
    void restock_withNullExpiry_succeeds() throws Exception {
        StubInventoryPort port = new StubInventoryPort();
        assertDoesNotThrow(() ->
                port.restockReturnedBox("RET-9", "SKU9", 1, null, LocalDateTime.now()));
    }

    /**
     * Verifies that a non-positive quantity is rejected with an exception.
     */
    @Test
    void restock_withNonPositiveQuantity_throws() {
        StubInventoryPort port = new StubInventoryPort();
        Exception ex = assertThrows(Exception.class, () ->
                port.restockReturnedBox("RET-X", "SKU-X", 0, null, LocalDateTime.now()));
        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
    }

    /**
     * Verifies that an invalid (non-ISO) expiry date triggers an exception.
     */
    @Test
    void restock_withInvalidExpiryFormat_throws() {
        StubInventoryPort port = new StubInventoryPort();
        assertThrows(Exception.class, () ->
                port.restockReturnedBox("RET-X", "SKU-X", 5, "14/11/2025", LocalDateTime.now()));
    }

    /**
     * Verifies that a blank SKU triggers an exception.
     */
    @Test
    void restock_withBlankSku_throws() {
        StubInventoryPort port = new StubInventoryPort();
        assertThrows(Exception.class, () ->
                port.restockReturnedBox("RET-X", "   ", 3, null, LocalDateTime.now()));
    }

    /**
     * Verifies that a null receivedAt triggers an exception.
     */
    @Test
    void restock_withNullReceivedAt_throws() {
        StubInventoryPort port = new StubInventoryPort();
        assertThrows(Exception.class, () ->
                port.restockReturnedBox("RET-X", "SKU-X", 3, null, null));
    }

    /**
     * Verifies that a null boxId triggers an exception.
     */
    @Test
    void restock_withNullBoxId_throws() {
        StubInventoryPort port = new StubInventoryPort();
        assertThrows(Exception.class, () ->
                port.restockReturnedBox(null, "SKU-X", 3, null, LocalDateTime.now()));
    }
}
