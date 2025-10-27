package controller;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;

import java.time.LocalDateTime;

/**
 * Orchestrates the inspection of returned goods (USEI05).
 * <p>
 * Pulls entries from {@link QuarantineController}, applies business rules
 * (discard, restock, or partial), and delegates stock updates to {@link InventoryPort}.
 * Logging/auditing is expected to be handled by the UI layer or an external component.
 */
public class ReturnsInspectionController {

    private final QuarantineController quarantine;
    private final InventoryPort inventory;

    /**
     * Creates a controller that processes quarantine entries and updates inventory.
     *
     * @param quarantine the source queue of returns to inspect
     * @param inventory  the inventory integration port used to restock items
     * @param unusedLog  legacy parameter preserved for constructor compatibility (ignored)
     */
    public ReturnsInspectionController(QuarantineController quarantine,
                                       InventoryPort inventory,
                                       Object unusedLog) { // left here just for constructor compatibility
        this.quarantine = quarantine;
        this.inventory = inventory;
    }

    /**
     * Peeks at the next return entry without removing it from the queue.
     *
     * @return the next {@link ReturnEntry}, or {@code null} if the queue is empty
     */
    public ReturnEntry previewNext() {
        return quarantine.peekNext();
    }

    /**
     * Discards the next entry in the queue.
     * <p>
     * Removes the head of the queue and returns a {@link ReturnDecision} marking the
     * entire quantity as discarded.
     *
     * @return a decision describing the discard action, or {@code null} if the queue is empty
     */
    public ReturnDecision discardAllNext() {
        ReturnEntry e = quarantine.pollNext();
        if (e == null) return null;

        return new ReturnDecision(e, ReturnAction.DISCARD_ALL, 0, e.getQuantity());
    }

    /**
     * Restocks the entire quantity of the next entry, respecting domain rules.
     * <p>
     * If the reason is {@link ReturnReason#EXPIRED}, the entry is discarded instead.
     * Otherwise, constructs a box identifier ({@code RET-<returnId>}) and calls
     * {@link InventoryPort#restockReturnedBox(String, String, int, String, LocalDateTime)}.
     *
     * @return a decision describing the restock (or discard) action, or {@code null} if empty
     * @throws Exception if the inventory port reports validation or capacity errors
     */
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

    /**
     * Applies a partial restock for the next entry, discarding the remainder.
     * <p>
     * If the reason is {@link ReturnReason#EXPIRED} or {@code qtyToRestock &le; 0},
     * the entire quantity is discarded. If {@code qtyToRestock} is greater than or
     * equal to the entry quantity, the behavior degenerates to a full restock.
     * Otherwise, the specified quantity is restocked and the remainder is discarded.
     *
     * @param qtyToRestock number of units to restock (must be non-negative)
     * @return a decision describing the applied action, or {@code null} if the queue is empty
     * @throws IllegalArgumentException if {@code qtyToRestock} is negative
     * @throws Exception                if the inventory port reports validation or capacity errors
     */
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
