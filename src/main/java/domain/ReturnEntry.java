package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object representing a single product return awaiting quarantine inspection (USEI05).
 * <p>
 * Mirrors exactly the structure of one row from {@code returns.csv}, including identifiers,
 * SKU, quantity, reason, registration timestamp, and optional expiry date.
 * <p>
 * Natural ordering follows the quarantine processing policy:
 * newer entries first (timestamp descending), then by {@code returnId} ascending to break ties.
 */
public final class ReturnEntry implements Comparable<ReturnEntry> {

    /** Unique identifier for the return entry (e.g., {@code RET-000123}). */
    private final String returnId;

    /** Product SKU code associated with this return. */
    private final String sku;

    /** Returned quantity in units (must be positive). */
    private final int quantity;

    /** Parsed reason for the return. */
    private final ReturnReason reason;

    /** Registration timestamp in ISO-8601 local date-time format. */
    private final LocalDateTime timestamp;

    /** Optional expiry date string as provided in the CSV (may be {@code null}). */
    private final String expiryDate;

    /**
     * Creates a new immutable {@code ReturnEntry}.
     *
     * @param returnId   unique return identifier (non-blank)
     * @param sku        product SKU (non-blank)
     * @param quantity   returned quantity (must be greater than zero)
     * @param reason     parsed {@link ReturnReason} (non-null)
     * @param timestamp  registration timestamp (non-null)
     * @param expiryDate optional expiry date string (may be {@code null} or blank; blank values are normalized to {@code null})
     * @throws IllegalArgumentException if any required argument is missing or invalid
     */
    public ReturnEntry(String returnId,
                       String sku,
                       int quantity,
                       ReturnReason reason,
                       LocalDateTime timestamp,
                       String expiryDate) {
        if (returnId == null || returnId.isBlank()) {
            throw new IllegalArgumentException("returnId is required.");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0.");
        }
        if (reason == null) {
            throw new IllegalArgumentException("reason is required.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp is required.");
        }

        this.returnId = returnId;
        this.sku = sku;
        this.quantity = quantity;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = (expiryDate != null && expiryDate.isBlank()) ? null : expiryDate;
    }

    /**
     * Returns the unique identifier for this return entry.
     *
     * @return the return ID
     */
    public String getReturnId() {
        return returnId;
    }

    /**
     * Returns the SKU code of the returned product.
     *
     * @return the product SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Returns the quantity of items returned.
     *
     * @return the quantity in units
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the reason for the return.
     *
     * @return the {@link ReturnReason}
     */
    public ReturnReason getReason() {
        return reason;
    }

    /**
     * Returns the timestamp when the return was registered.
     *
     * @return the registration timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the optional expiry date string, if provided.
     *
     * @return expiry date or {@code null} if not applicable
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * Defines the natural ordering for quarantine processing:
     * <ul>
     *   <li>Newer returns (greater timestamp) appear first.</li>
     *   <li>When timestamps are equal, lower {@code returnId} values come first lexicographically.</li>
     * </ul>
     *
     * @param other another {@code ReturnEntry} to compare to
     * @return a negative integer, zero, or a positive integer as this entry
     *         should appear before, at the same position, or after the other
     */
    @Override
    public int compareTo(ReturnEntry other) {
        int byTimeDesc = other.timestamp.compareTo(this.timestamp); // newer first
        if (byTimeDesc != 0) return byTimeDesc;
        return this.returnId.compareTo(other.returnId); // tie-breaker: id ascending
    }

    /**
     * Returns a human-readable summary of this entry, suitable for debugging.
     *
     * @return formatted string with field names and values
     */
    @Override
    public String toString() {
        return "ReturnEntry{" +
                "returnId='" + returnId + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", reason=" + reason +
                ", timestamp=" + timestamp +
                ", expiryDate=" + (expiryDate == null ? "null" : "'" + expiryDate + "'") +
                '}';
    }

    /**
     * Equality is based solely on {@code returnId}, which uniquely identifies a return.
     *
     * @param o the object to compare to
     * @return {@code true} if the objects refer to the same return entry
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnEntry)) return false;
        ReturnEntry that = (ReturnEntry) o;
        return Objects.equals(returnId, that.returnId);
    }

    /**
     * Computes a hash code consistent with {@link #equals(Object)}.
     *
     * @return hash code derived from {@code returnId}
     */
    @Override
    public int hashCode() {
        return Objects.hash(returnId);
    }
}
