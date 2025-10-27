package domain;

/**
 * Enumerates the valid reasons for a product return (USEI05).
 * <p>
 * Each value directly maps to one of the allowed codes found in {@code returns.csv}.
 * Used throughout quarantine processing, inspection, and decision logging.
 */
public enum ReturnReason {

    /** The customer changed their mind or refused the delivery. */
    CUSTOMER_REMORSE,

    /** The product was damaged, defective, or otherwise unsellable. */
    DAMAGED,

    /** The product has reached or exceeded its expiry date. */
    EXPIRED,

    /** Returned during a stock cycle count or audit reconciliation. */
    CYCLE_COUNT;

    /**
     * Parses a textual reason (case-insensitive, hyphens allowed) into the corresponding enum constant.
     * <p>
     * Example: {@code "customer-remorse"} → {@link #CUSTOMER_REMORSE}.
     *
     * @param text the raw string value from the CSV (non-null)
     * @return the corresponding {@link ReturnReason}
     * @throws IllegalArgumentException if the text does not match any valid constant
     */
    public static ReturnReason fromString(String text) {
        String normalized = text.trim().toUpperCase().replace('-', '_');
        return ReturnReason.valueOf(normalized);
    }
}
