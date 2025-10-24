package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object that represents a single product return waiting for quarantine inspection (USEI05).
 * <p>
 * It captures exactly the fields coming from {@code returns.csv}:
 * <ul>
 *   <li>{@code returnId} – unique identifier for the return (e.g., "RET-000123")</li>
 *   <li>{@code sku} – product SKU code</li>
 *   <li>{@code quantity} – returned quantity (units)</li>
 *   <li>{@code reason} – {@link ReturnReason} parsed from CSV</li>
 *   <li>{@code timestamp} – return registration timestamp (ISO-8601 local date-time)</li>
 *   <li>{@code expiryDate} – optional expiry date string as provided in the CSV (may be {@code null})</li>
 * </ul>
 * <p>
 * Natural ordering matches the quarantine processing policy:
 * most recent first (timestamp descending), then by {@code returnId} ascending as tie-breaker.
 */
public final class ReturnEntry implements Comparable<ReturnEntry> {

    /** Unique identifier for the return entry (e.g., "RET-000123"). */
    private final String returnId;

    /** Product SKU code associated with this return. */
    private final String sku;

    /** Returned quantity in units. Must be positive. */
    private final int quantity;

    /** Parsed reason for the return (must be one of the allowed values). */
    private final ReturnReason reason;

    /** Return registration timestamp (ISO local date-time). */
    private final LocalDateTime timestamp;

    /** Optional expiry date string as provided in CSV (may be {@code null}). */
    private final String expiryDate;

    /**
     * Creates a new immutable {@code ReturnEntry}.
     *
     * @param returnId  unique return identifier (non-blank)
     * @param sku       product SKU (non-blank)
     * @param quantity  returned quantity (must be {@code > 0})
     * @param reason    parsed {@link ReturnReason} (non-null)
     * @param timestamp return timestamp (non-null)
     * @param expiryDate optional expiry date string (may be null or blank; blank will be normalized to {@code null})
     * @throws IllegalArgumentException if any required argument is invalid
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

    public String getReturnId() {
        return returnId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReturnReason getReason() {
        return reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * Natural ordering for quarantine processing:
     * <ul>
     *   <li>Newer returns (greater timestamp) come first.</li>
     *   <li>When timestamps are equal, lower {@code returnId} (lexicographically) comes first.</li>
     * </ul>
     */
    @Override
    public int compareTo(ReturnEntry other) {
        int byTimeDesc = other.timestamp.compareTo(this.timestamp); // newer first
        if (byTimeDesc != 0) return byTimeDesc;
        return this.returnId.compareTo(other.returnId);              // tie-breaker: id ascending
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnEntry)) return false;
        ReturnEntry that = (ReturnEntry) o;
        // Identity by unique returnId is sufficient for set/map usage
        return Objects.equals(returnId, that.returnId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnId);
    }
}
