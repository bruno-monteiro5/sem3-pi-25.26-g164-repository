package domain;

/**
 * Enumerates the possible inspection outcomes for a returned item (USEI05).
 * <p>
 * Represents the action taken after evaluating a {@link ReturnEntry}, used across
 * {@link ReturnDecision}, controllers, and audit logs.
 */
public enum ReturnAction {

    /** The entire quantity is restocked into the warehouse. */
    RESTOCK_ALL,

    /** The entire quantity is discarded due to damage, expiration, or policy. */
    DISCARD_ALL,

    /** A portion of the quantity is restocked, and the remainder is discarded. */
    PARTIAL;
}
