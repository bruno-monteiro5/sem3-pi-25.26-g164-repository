package domain;

import java.time.LocalDateTime;

/**
 * Represents the decision made during the inspection of a returned product (USEI05).
 * <p>
 * Captures the applied {@link ReturnAction}, affected quantities, and the timestamp
 * of when the decision was taken. Used by controllers and audit logs to record
 * the outcome of each inspected {@link ReturnEntry}.
 */
public class ReturnDecision {

    private final String returnId;
    private final String sku;
    private final ReturnAction action;
    private final int qtyRestocked;
    private final int qtyDiscarded;
    private final LocalDateTime timestamp;

    /**
     * Creates a new {@code ReturnDecision} based on a processed {@link ReturnEntry}.
     *
     * @param entry     the original return entry inspected
     * @param action    the action applied (restock, discard, or partial)
     * @param restocked number of units restocked
     * @param discarded number of units discarded
     */
    public ReturnDecision(ReturnEntry entry, ReturnAction action, int restocked, int discarded) {
        this.returnId = entry.getReturnId();
        this.sku = entry.getSku();
        this.action = action;
        this.qtyRestocked = restocked;
        this.qtyDiscarded = discarded;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the identifier of the inspected return.
     *
     * @return the return ID
     */
    public String getReturnId() {
        return returnId;
    }

    /**
     * Returns the SKU of the product being inspected.
     *
     * @return the product SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Returns the action taken during inspection.
     *
     * @return the applied {@link ReturnAction}
     */
    public ReturnAction getAction() {
        return action;
    }

    /**
     * Returns the number of units restocked into inventory.
     *
     * @return restocked quantity
     */
    public int getQtyRestocked() {
        return qtyRestocked;
    }

    /**
     * Returns the number of units discarded.
     *
     * @return discarded quantity
     */
    public int getQtyDiscarded() {
        return qtyDiscarded;
    }

    /**
     * Returns the timestamp when the decision was recorded.
     *
     * @return the decision timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a concise, formatted summary of this decision for logging or diagnostics.
     *
     * @return formatted string representation of the decision
     */
    @Override
    public String toString() {
        return timestamp + " | returnId=" + returnId + " | sku=" + sku + " | action=" + action +
                " | qtyRestocked=" + qtyRestocked + " | qtyDiscarded=" + qtyDiscarded;
    }
}
