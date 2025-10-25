package domain;

/**
 * Enumerates the possible inspection actions for a returned item (USEI05).
 * <p>
 * Used by {@link ReturnDecision}, controllers, and audit logs.
 */
public enum ReturnAction {
    /** Entire quantity is restocked into the warehouse. */
    RESTOCK_ALL,

    /** Entire quantity is discarded. */
    DISCARD_ALL,

    /** Part of the quantity is restocked, the rest is discarded. */
    PARTIAL;
}
