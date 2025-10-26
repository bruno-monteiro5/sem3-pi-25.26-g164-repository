package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the inspection output for a single {@link ReturnEntry} (USEI05).
 * <p>
 * The decision can be:
 * <ul>
 *   <li>{@code RESTOCK_ALL} – the whole quantity is restocked;</li>
 *   <li>{@code DISCARD_ALL} – the whole quantity is discarded;</li>
 *   <li>{@code PARTIAL} – part of the quantity is restocked and the remainder is discarded.</li>
 * </ul>
 * An audit timestamp is recorded when this decision is created.
 */
public final class ReturnDecision {

    private final ReturnEntry entry;
    private final ReturnAction action;
    private final int qtyRestocked;
    private final int qtyDiscarded;
    private final LocalDateTime decidedAt;

    /**
     * Creates a new {@code ReturnDecision}.
     *
     * @param entry         the inspected entry (non-null)
     * @param action        chosen action (non-null)
     * @param qtyRestocked  non-negative restocked quantity
     * @param qtyDiscarded  non-negative discarded quantity
     * @throws IllegalArgumentException if arguments are invalid (e.g., negative quantities or inconsistent sum)
     */
    public ReturnDecision(ReturnEntry entry, ReturnAction action, int qtyRestocked, int qtyDiscarded) {
        if (entry == null) throw new IllegalArgumentException("entry is required.");
        if (action == null) throw new IllegalArgumentException("action is required.");
        if (qtyRestocked < 0 || qtyDiscarded < 0) {
            throw new IllegalArgumentException("quantities must be >= 0");
        }
        if (qtyRestocked + qtyDiscarded != entry.getQuantity()) {
            throw new IllegalArgumentException("restocked + discarded must equal entry.quantity");
        }
        this.entry = entry;
        this.action = action;
        this.qtyRestocked = qtyRestocked;
        this.qtyDiscarded = qtyDiscarded;
        this.decidedAt = LocalDateTime.now();
    }

    public ReturnEntry getEntry()       { return entry; }
    public ReturnAction getAction()     { return action; }
    public int getQtyRestocked()        { return qtyRestocked; }
    public int getQtyDiscarded()        { return qtyDiscarded; }
    public LocalDateTime getDecidedAt() { return decidedAt; }

    @Override
    public String toString() {
        return "ReturnDecision{" +
                "entry=" + entry.getReturnId() +
                ", action=" + action +
                ", qtyRestocked=" + qtyRestocked +
                ", qtyDiscarded=" + qtyDiscarded +
                ", decidedAt=" + decidedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnDecision)) return false;
        ReturnDecision that = (ReturnDecision) o;
        return Objects.equals(entry.getReturnId(), that.entry.getReturnId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry.getReturnId());
    }
}
