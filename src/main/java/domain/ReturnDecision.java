package domain;

import java.time.LocalDateTime;

/**
 * Represents the decision taken during the inspection of a returned product.
 */
public class ReturnDecision {

    private final String returnId;
    private final String sku;
    private final ReturnAction action;
    private final int qtyRestocked;
    private final int qtyDiscarded;
    private final LocalDateTime timestamp;

    /**
     * Constructs a decision made during return inspection.
     *
     * @param entry         the original return entry
     * @param action        the action taken (restock/discard/partial)
     * @param restocked     quantity restocked
     * @param discarded     quantity discarded
     */
    public ReturnDecision(ReturnEntry entry, ReturnAction action, int restocked, int discarded) {
        this.returnId = entry.getReturnId();
        this.sku = entry.getSku();
        this.action = action;
        this.qtyRestocked = restocked;
        this.qtyDiscarded = discarded;
        this.timestamp = LocalDateTime.now();
    }

    public String getReturnId() {
        return returnId;
    }

    public String getSku() {
        return sku;
    }

    public ReturnAction getAction() {
        return action;
    }

    public int getQtyRestocked() {
        return qtyRestocked;
    }

    public int getQtyDiscarded() {
        return qtyDiscarded;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " | returnId=" + returnId + " | sku=" + sku + " | action=" + action +
                " | qtyRestocked=" + qtyRestocked + " | qtyDiscarded=" + qtyDiscarded;
    }
}
