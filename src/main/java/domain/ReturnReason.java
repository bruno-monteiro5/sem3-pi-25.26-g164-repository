package domain;

/**
 * Enumerates the valid reasons for a product return (USEI05).
 * <p>
 * Each value corresponds exactly to one of the expected codes
 * that may appear in the {@code returns.csv} file.
 */
public enum ReturnReason {

    /** The customer changed their mind or refused delivery. */
    CUSTOMER_REMORSE,

    /** The product was damaged or defective. */
    DAMAGED,

    /** The product has reached or passed its expiry date. */
    EXPIRED,

    /** Returned due to a stock cycle count or audit. */
    CYCLE_COUNT;

    /**
     * Converts a text value (case-insensitive, hyphens allowed)
     * into the corresponding {@link ReturnReason}.
     * <p>
     * For example, "customer-remorse" → {@link #CUSTOMER_REMORSE}.
     *
     * @param text raw string from CSV (not null)
     * @return matching {@code ReturnReason}
     * @throws IllegalArgumentException if the text does not match any constant
     */
    public static ReturnReason fromString(String text) {
        String normalized = text.trim().toUpperCase().replace('-', '_');
        return ReturnReason.valueOf(normalized);
    }
}
