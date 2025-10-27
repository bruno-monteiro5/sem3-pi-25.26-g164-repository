package controller;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnsInspectionController}.
 * <p>
 * Covers all public methods and branches:
 * {@code previewNext}, {@code discardAllNext}, {@code restockAllNext},
 * and {@code partialRestockNext} (including negative, zero, >= quantity, and partial cases).
 */
class ReturnsInspectionControllerTest {

    /** Simple inventory stub to capture last call and optionally throw. */
    private static final class CapturingInventory implements InventoryPort {
        String lastBoxId;
        String lastSku;
        Integer lastQty;
        String lastExpiry;
        LocalDateTime lastReceivedAt;
        boolean throwOnCall;

        @Override
        public void restockReturnedBox(String boxId, String sku, int quantity, String expiryDate, LocalDateTime receivedAt) throws Exception {
            if (throwOnCall) throw new Exception("simulated inventory failure");
            this.lastBoxId = boxId;
            this.lastSku = sku;
            this.lastQty = quantity;
            this.lastExpiry = expiryDate;
            this.lastReceivedAt = receivedAt;
        }
    }

    private static ReturnEntry entry(String id, String sku, int qty, ReturnReason reason, String ts, String expiry) {
        return new ReturnEntry(id, sku, qty, reason, LocalDateTime.parse(ts), expiry);
    }

    /**
     * Verifies that preview delegates to quarantine.peekNext().
     */
    @Test
    void previewNext_returnsHeadOrNull() {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, /*unused*/ null);

        assertNull(ctrl.previewNext()); // empty

        ReturnEntry e = entry("RET-001", "SKU1", 2, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);
        q.enqueue(e);

        assertSame(e, ctrl.previewNext());
    }

    /**
     * Verifies discardAllNext removes the head and returns a decision; returns null when empty.
     */
    @Test
    void discardAllNext_behaviour() {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        assertNull(ctrl.discardAllNext()); // empty

        ReturnEntry e = entry("R-1", "SKU", 7, ReturnReason.DAMAGED, "2025-10-27T10:00:00", null);
        q.enqueue(e);

        ReturnDecision d = ctrl.discardAllNext();
        assertNotNull(d);
        assertEquals(ReturnAction.DISCARD_ALL, d.getAction());
        assertEquals(0, d.getQtyRestocked());
        assertEquals(7, d.getQtyDiscarded());
        assertTrue(q.isEmpty());
    }

    /**
     * Verifies restockAllNext: EXPIRED entries are discarded.
     */
    @Test
    void restockAllNext_expired_discardsAll() throws Exception {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        q.enqueue(entry("X1", "SKU-X", 3, ReturnReason.EXPIRED, "2025-10-27T10:00:00", "2025-10-01"));
        ReturnDecision d = ctrl.restockAllNext();

        assertNotNull(d);
        assertEquals(ReturnAction.DISCARD_ALL, d.getAction());
        assertEquals(0, d.getQtyRestocked());
        assertEquals(3, d.getQtyDiscarded());
        // inventory must NOT be called
        assertNull(inv.lastBoxId);
    }

    /**
     * Verifies restockAllNext: normal path restocks everything and formats boxId with/without RET prefix.
     */
    @Test
    void restockAllNext_normal_callsInventory_andFormatsBoxId() throws Exception {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        // Case A: id WITHOUT RET prefix -> "RET-" + id
        ReturnEntry a = entry("123", "SKU-A", 5, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);
        q.enqueue(a);
        ReturnDecision da = ctrl.restockAllNext();
        assertEquals(ReturnAction.RESTOCK_ALL, da.getAction());
        assertEquals(5, da.getQtyRestocked());
        assertEquals(0, da.getQtyDiscarded());
        assertEquals("RET-123", inv.lastBoxId);
        assertEquals("SKU-A", inv.lastSku);
        assertEquals(5, inv.lastQty);
        assertNull(inv.lastExpiry);
        assertNotNull(inv.lastReceivedAt);

        // Case B: id WITH "RET" prefix -> "RET-" + id.substring(3)
        // Note: if id is already "RET-001", this leads to "RET--001" by the current implementation.
        ReturnEntry b = entry("RET-001", "SKU-B", 2, ReturnReason.CYCLE_COUNT, "2025-10-27T11:00:00", "2025-12-31");
        q.enqueue(b);
        ReturnDecision db = ctrl.restockAllNext();
        assertEquals(ReturnAction.RESTOCK_ALL, db.getAction());
        assertEquals("RET--001", inv.lastBoxId); // reflects current code logic
        assertEquals("SKU-B", inv.lastSku);
        assertEquals(2, inv.lastQty);
        assertEquals("2025-12-31", inv.lastExpiry);
        assertNotNull(inv.lastReceivedAt);
    }

    /**
     * Verifies that restockAllNext returns null when queue is empty.
     */
    @Test
    void restockAllNext_empty_returnsNull() throws Exception {
        ReturnsInspectionController ctrl = new ReturnsInspectionController(new QuarantineController(), new CapturingInventory(), null);
        assertNull(ctrl.restockAllNext());
    }

    /**
     * Verifies that exceptions from the inventory port propagate as-is.
     */
    @Test
    void restockAllNext_inventoryThrows_exceptionPropagates() {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        inv.throwOnCall = true;
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        q.enqueue(entry("9", "SKU-9", 1, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null));
        assertThrows(Exception.class, ctrl::restockAllNext);
    }

    /**
     * Verifies partialRestockNext: negative quantity is rejected upfront.
     */
    @Test
    void partialRestockNext_negativeQty_throwsIAE() {
        ReturnsInspectionController ctrl = new ReturnsInspectionController(new QuarantineController(), new CapturingInventory(), null);
        assertThrows(IllegalArgumentException.class, () -> ctrl.partialRestockNext(-1));
    }

    /**
     * Verifies partialRestockNext: returns null when queue is empty.
     */
    @Test
    void partialRestockNext_empty_returnsNull() throws Exception {
        ReturnsInspectionController ctrl = new ReturnsInspectionController(new QuarantineController(), new CapturingInventory(), null);
        assertNull(ctrl.partialRestockNext(1));
    }

    /**
     * Verifies partialRestockNext: EXPIRED entries are fully discarded; qty=0 also discards.
     */
    @Test
    void partialRestockNext_expiredOrZeroQty_discardsAll() throws Exception {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        // EXPIRED path
        q.enqueue(entry("E1", "SKUE", 4, ReturnReason.EXPIRED, "2025-10-27T10:00:00", "2025-10-01"));
        ReturnDecision d1 = ctrl.partialRestockNext(3);
        assertEquals(ReturnAction.DISCARD_ALL, d1.getAction());
        assertEquals(0, d1.getQtyRestocked());
        assertEquals(4, d1.getQtyDiscarded());
        assertNull(inv.lastBoxId);

        // qtyToRestock == 0 path
        q.enqueue(entry("E2", "SKUE2", 6, ReturnReason.DAMAGED, "2025-10-27T11:00:00", null));
        ReturnDecision d2 = ctrl.partialRestockNext(0);
        assertEquals(ReturnAction.DISCARD_ALL, d2.getAction());
        assertEquals(0, d2.getQtyRestocked());
        assertEquals(6, d2.getQtyDiscarded());
        assertNull(inv.lastBoxId);
    }

    /**
     * Verifies partialRestockNext: qty >= entry quantity degenerates to full restock.
     */
    @Test
    void partialRestockNext_qtyGteQuantity_becomesFullRestock() throws Exception {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        q.enqueue(entry("R100", "SKU100", 5, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null));
        ReturnDecision d = ctrl.partialRestockNext(10);

        assertEquals(ReturnAction.RESTOCK_ALL, d.getAction());
        assertEquals(5, d.getQtyRestocked());
        assertEquals(0, d.getQtyDiscarded());
        assertEquals("RET-R100", inv.lastBoxId);
        assertEquals("SKU100", inv.lastSku);
        assertEquals(5, inv.lastQty);
        assertNotNull(inv.lastReceivedAt);
    }

    /**
     * Verifies partialRestockNext: proper partial path (0 < qty < total).
     */
    @Test
    void partialRestockNext_properPartial_callsInventoryWithQty_andReturnsDecision() throws Exception {
        QuarantineController q = new QuarantineController();
        CapturingInventory inv = new CapturingInventory();
        ReturnsInspectionController ctrl = new ReturnsInspectionController(q, inv, null);

        q.enqueue(entry("RET-777", "SKU-777", 9, ReturnReason.CYCLE_COUNT, "2025-10-27T10:00:00", "2025-12-31"));
        ReturnDecision d = ctrl.partialRestockNext(4);

        assertEquals(ReturnAction.PARTIAL, d.getAction());
        assertEquals(4, d.getQtyRestocked());
        assertEquals(5, d.getQtyDiscarded());
        assertEquals("RET--777", inv.lastBoxId); // current code behavior for ids starting with "RET"
        assertEquals("SKU-777", inv.lastSku);
        assertEquals(4, inv.lastQty);
        assertEquals("2025-12-31", inv.lastExpiry);
        assertNotNull(inv.lastReceivedAt);
    }
}
