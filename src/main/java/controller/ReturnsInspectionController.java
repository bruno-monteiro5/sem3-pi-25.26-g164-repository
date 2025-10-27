package controller;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;

import java.time.LocalDateTime;

/**
 * Orchestrates the inspection of returned goods (USEI05).
 * Connects quarantine and inventory systems. Logging is handled externally by the UI.
 */
public class ReturnsInspectionController {

    private final QuarantineController quarantine;
    private final InventoryPort inventory;

    public ReturnsInspectionController(QuarantineController quarantine,
                                       InventoryPort inventory,
                                       Object unusedLog) { // left here just for constructor compatibility
        this.quarantine = quarantine;
        this.inventory = inventory;
    }

    public ReturnEntry previewNext() {
        return quarantine.peekNext();
    }

    public ReturnDecision discardAllNext() {
        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        return new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
    }

    public ReturnDecision restockAllNext() throws Exception {
        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        if (e.getReason() == ReturnReason.EXPIRED) {
            return new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
        }

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

        return new ReturnDecision(e, ReturnAction.RESTOCK_ALL, e.getQuantity(), 0);
    }

    public ReturnDecision partialRestockNext(int qtyToRestock) throws Exception {
        if (qtyToRestock < 0) throw new IllegalArgumentException("qtyToRestock must be >= 0");

        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        if (e.getReason() == ReturnReason.EXPIRED || qtyToRestock <= 0) {
            return new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
        }

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
            return new ReturnDecision(e, ReturnAction.RESTOCK_ALL, e.getQuantity(), 0);
        }

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
        return new ReturnDecision(e, ReturnAction.PARTIAL, qtyToRestock, qtyDiscarded);
    }
}
