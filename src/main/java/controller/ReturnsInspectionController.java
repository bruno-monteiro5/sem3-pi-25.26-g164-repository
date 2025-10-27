package controller;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import repository.ReturnsAuditLog;
import controller.InventoryPort;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Orchestrates the inspection of returned goods (USEI05).
 * <p>
 * Connects the quarantine queue, the audit log, and the inventory system.
 * <ul>
 *   <li>Retrieves the next {@link ReturnEntry} from the quarantine;</li>
 *   <li>Applies the chosen action (Restock, Discard, or Partial);</li>
 *   <li>Updates inventory through {@link InventoryPort};</li>
 *   <li>Creates a {@link ReturnDecision} and records it via {@link ReturnsAuditLog}.</li>
 * </ul>
 * <p>
 * This class does not depend directly on the existing warehouse model;
 * instead, it uses an {@link InventoryPort} interface to connect to your
 * current {@code WarehouseRepository} and stock management logic.
 */
public class ReturnsInspectionController {

    private final QuarantineController quarantine;
    private final InventoryPort inventory;
    private final ReturnsAuditLog auditLog;

    /**
     * Builds a new inspection controller.
     *
     * @param quarantine quarantine storage controller
     * @param inventory  integration port to the existing inventory system
     * @param auditLog   audit log for persisting inspection decisions
     */
    public ReturnsInspectionController(QuarantineController quarantine,
                                       InventoryPort inventory,
                                       ReturnsAuditLog auditLog) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
    }

    /** @return the next entry waiting for inspection, or {@code null} if none exist. */
    public ReturnEntry previewNext() {
        return quarantine.peekNext();
    }

    /**
     * Applies a "discard all" decision to the next entry in quarantine.
     * Removes it from the queue and writes to the audit log.
     */
    public ReturnDecision discardAllNext() throws IOException {
        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        ReturnDecision d =  new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
        auditLog.append(d);
        return d;
    }

    /**
     * Applies a "restock all" decision to the next entry in quarantine.
     * Creates a returned box ("RET-&lt;returnId&gt;") and inserts it through the inventory port.
     * EXPIRED items are automatically discarded.
     */
    public ReturnDecision restockAllNext() throws Exception {
        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        if (e.getReason() == ReturnReason.EXPIRED) {
            // Expired items cannot be restocked → discard instead
            ReturnDecision d = new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
            auditLog.append(d);
            return d;
        }

        // Prepare new box ID for restock (avoid double "RET" prefix)
        String boxId = e.getReturnId().startsWith("RET")
                ? "RET-" + e.getReturnId().substring(3)
                : "RET-" + e.getReturnId();
        LocalDateTime receivedAt = LocalDateTime.now();

        // Delegate to existing warehouse logic through InventoryPort
        inventory.restockReturnedBox(
                boxId,
                e.getSku(),
                e.getQuantity(),
                e.getExpiryDate(),
                receivedAt
        );

        ReturnDecision d = new ReturnDecision(e, ReturnAction.RESTOCK_ALL, e.getQuantity(), 0);
        auditLog.append(d);
        return d;
    }

    /**
     * Applies a "partial restock" decision to the next entry in quarantine.
     * Restocks only {@code qtyToRestock} units; the rest are discarded.
     * Automatically discards expired items.
     */
    public ReturnDecision partialRestockNext(int qtyToRestock) throws Exception {
        if (qtyToRestock < 0) throw new IllegalArgumentException("qtyToRestock must be >= 0");

        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        // If expired or no quantity to restock, discard everything
        if (e.getReason() == ReturnReason.EXPIRED || qtyToRestock <= 0) {
            ReturnDecision d = new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
            auditLog.append(d);
            return d;
        }

        // If requested restock >= available, treat as full restock
        if (qtyToRestock >= e.getQuantity()) {
            String boxId = e.getReturnId().startsWith("RET")
                    ? "RET-" + e.getReturnId().substring(3)
                    : "RET-" + e.getReturnId();
            LocalDateTime receivedAt = LocalDateTime.now();
            inventory.restockReturnedBox(
                    boxId,
                    e.getSku(),
                    e.getQuantity(),
                    e.getExpiryDate(),
                    receivedAt
            );
            ReturnDecision d = new ReturnDecision(e, ReturnAction.RESTOCK_ALL, e.getQuantity(), 0);
            auditLog.append(d);
            return d;
        }

        // Otherwise, do a partial restock
        int qtyDiscarded = e.getQuantity() - qtyToRestock;
        String boxId = e.getReturnId().startsWith("RET")
                ? "RET-" + e.getReturnId().substring(3)
                : "RET-" + e.getReturnId();
        LocalDateTime receivedAt = LocalDateTime.now();
        inventory.restockReturnedBox(
                boxId,
                e.getSku(),
                qtyToRestock,
                e.getExpiryDate(),
                receivedAt
        );
        ReturnDecision d = new ReturnDecision(e, ReturnAction.PARTIAL, qtyToRestock, qtyDiscarded);
        auditLog.append(d);
        return d;
    }
}
