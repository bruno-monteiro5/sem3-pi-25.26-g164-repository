package domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnEntry}.
 * <p>
 * Each test method documents its scenario, preconditions and expected outcome,
 * verifying constructor validation, normalization, comparison, equality and string formatting.
 */
class ReturnEntryTest {

    /**
     * Utility method to parse ISO date-time strings.
     *
     * @param iso the ISO-8601 date-time string
     * @return a {@link LocalDateTime} instance
     */
    private static LocalDateTime t(String iso) {
        return LocalDateTime.parse(iso);
    }

    /**
     * Helper to create a {@link ReturnEntry} instance with given parameters.
     *
     * @param id   the return identifier
     * @param sku  the product SKU
     * @param qty  the quantity
     * @param reason the {@link ReturnReason}
     * @param ts   the timestamp (ISO format)
     * @param exp  the expiry date (nullable)
     * @return configured {@link ReturnEntry}
     */
    private static ReturnEntry e(String id, String sku, int qty, ReturnReason reason, String ts, String exp) {
        return new ReturnEntry(id, sku, qty, reason, t(ts), exp);
    }

    /**
     * Verifies that a valid construction sets all fields and normalizes blank expiry to {@code null}.
     *
     * Preconditions:
     *  - All arguments valid and non-null except expiry (blank string).
     *
     * Expected:
     *  - All getters reflect constructor values;
     *  - Blank expiry string converted to {@code null}.
     *
     * @see ReturnEntry#ReturnEntry(String, String, int, ReturnReason, LocalDateTime, String)
     */
    @Test
    void constructor_valid_setsFields_andNormalizesBlankExpiry() {
        ReturnEntry e = new ReturnEntry(
                "RET-001",
                "SKU1",
                5,
                ReturnReason.CUSTOMER_REMORSE,
                t("2025-10-27T10:00:00"),
                "   "
        );

        assertEquals("RET-001", e.getReturnId());
        assertEquals("SKU1", e.getSku());
        assertEquals(5, e.getQuantity());
        assertEquals(ReturnReason.CUSTOMER_REMORSE, e.getReason());
        assertEquals(t("2025-10-27T10:00:00"), e.getTimestamp());
        assertNull(e.getExpiryDate());
    }

    /**
     * Verifies that invalid constructor arguments trigger {@link IllegalArgumentException}.
     *
     * Scenario:
     *  - Test each validation rule: null/blank identifiers, non-positive quantity, null reason or timestamp.
     *
     * Expected:
     *  - An {@link IllegalArgumentException} is thrown for each invalid case.
     *
     * @see ReturnEntry#ReturnEntry(String, String, int, ReturnReason, LocalDateTime, String)
     */
    @Test
    void constructor_invalidArguments_throwIAE() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry(null, "SKU", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("  ", "SKU", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", null, 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "  ", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 0, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", -5, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 1, null, t("2025-10-27T10:00:00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 1, ReturnReason.DAMAGED, null, null));
    }

    /**
     * Verifies natural ordering: newer timestamps first, then by returnId ascending.
     *
     * Scenario:
     *  - Several entries with distinct timestamps and IDs.
     *
     * Expected:
     *  - Newer entries appear first;
     *  - When timestamps match, sorting uses ascending returnId.
     *
     * @see ReturnEntry#compareTo(ReturnEntry)
     */
    @Test
    void compareTo_ordersByTimestampDesc_thenIdAsc() {
        ReturnEntry newest = e("RET-100", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T13:00:00", null);
        ReturnEntry sameTsA = e("RET-001", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T12:00:00", null);
        ReturnEntry sameTsB = e("RET-002", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T12:00:00", null);
        ReturnEntry oldest = e("RET-000", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T09:00:00", null);

        assertTrue(newest.compareTo(sameTsA) < 0);
        assertTrue(sameTsA.compareTo(newest) > 0);

        assertTrue(sameTsA.compareTo(sameTsB) < 0);
        assertTrue(sameTsB.compareTo(sameTsA) > 0);

        assertTrue(oldest.compareTo(newest) > 0);
    }

    /**
     * Verifies equality and hash code semantics based solely on {@code returnId}.
     *
     * Preconditions:
     *  - Two entries share the same returnId but differ in other attributes.
     *
     * Expected:
     *  - Entries with the same returnId are equal and share the same hash code;
     *  - Different IDs result in inequality.
     *
     * @see ReturnEntry#equals(Object)
     * @see ReturnEntry#hashCode()
     */
    @Test
    void equalsAndHashCode_basedOnReturnId_only() {
        ReturnEntry a1 = e("RET-42", "SKU-A", 3, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);
        ReturnEntry a2 = e("RET-42", "DIFF", 99, ReturnReason.EXPIRED, "2025-10-27T11:00:00", "2026-01-01");
        ReturnEntry b = e("RET-43", "SKU-A", 3, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        assertNotEquals(a1, b);
        assertNotEquals(a1.hashCode(), b.hashCode());

        assertEquals(a1, a1);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "not-entry");
    }

    /**
     * Verifies that {@link ReturnEntry#toString()} includes all field names and values.
     *
     * Scenario:
     *  - Construct an entry with known parameters.
     *
     * Expected:
     *  - Returned string contains all main attributes in readable format.
     *
     * @see ReturnEntry#toString()
     */
    @Test
    void toString_includesAllFields() {
        ReturnEntry e = e("RET-9", "SKU9", 7, ReturnReason.CYCLE_COUNT, "2025-10-27T10:00:00", "2025-12-31");
        String s = e.toString();
        assertTrue(s.contains("returnId='RET-9'"));
        assertTrue(s.contains("sku='SKU9'"));
        assertTrue(s.contains("quantity=7"));
        assertTrue(s.contains("reason=CYCLE_COUNT"));
        assertTrue(s.contains("timestamp=2025-10-27T10:00"));
        assertTrue(s.contains("expiryDate='2025-12-31'"));
    }
}
