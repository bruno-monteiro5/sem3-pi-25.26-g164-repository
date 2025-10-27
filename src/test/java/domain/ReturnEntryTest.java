package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnEntry}.
 * <p>
 * Covers constructor validation and normalization, getters, natural ordering
 * ({@link ReturnEntry#compareTo(ReturnEntry)}), {@link #toString()},
 * and equality/hash code semantics.
 */
class ReturnEntryTest {

    private static LocalDateTime t(String iso) {
        return LocalDateTime.parse(iso);
    }

    private static ReturnEntry e(String id, String sku, int qty, ReturnReason reason, String ts, String exp) {
        return new ReturnEntry(id, sku, qty, reason, t(ts), exp);
    }

    /** Verifies that a valid construction sets all fields and normalizes blank expiry to null. */
    @Test
    void constructor_valid_setsFields_andNormalizesBlankExpiry() {
        ReturnEntry e = new ReturnEntry(
                "RET-001",
                "SKU1",
                5,
                ReturnReason.CUSTOMER_REMORSE,
                t("2025-10-27T10:00:00"),
                "   " // blank -> must become null
        );

        assertEquals("RET-001", e.getReturnId());
        assertEquals("SKU1", e.getSku());
        assertEquals(5, e.getQuantity());
        assertEquals(ReturnReason.CUSTOMER_REMORSE, e.getReason());
        assertEquals(t("2025-10-27T10:00:00"), e.getTimestamp());
        assertNull(e.getExpiryDate());
    }

    /** Verifies constructor validations for required fields. */
    @Test
    void constructor_invalidArguments_throwIAE() {
        // returnId null/blank
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry(null, "SKU", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("  ", "SKU", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        // sku null/blank
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", null, 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "  ", 1, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        // quantity <= 0
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 0, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", -5, ReturnReason.DAMAGED, t("2025-10-27T10:00:00"), null));

        // reason null
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 1, null, t("2025-10-27T10:00:00"), null));

        // timestamp null
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("RET-1", "SKU", 1, ReturnReason.DAMAGED, null, null));
    }

    /** Verifies natural ordering: newer timestamps first, then returnId ascending for ties. */
    @Test
    void compareTo_ordersByTimestampDesc_thenIdAsc() {
        ReturnEntry newest = e("RET-100", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T13:00:00", null);
        ReturnEntry sameTsA = e("RET-001", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T12:00:00", null);
        ReturnEntry sameTsB = e("RET-002", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T12:00:00", null);
        ReturnEntry oldest = e("RET-000", "SKU", 1, ReturnReason.DAMAGED, "2025-10-27T09:00:00", null);

        // Newest first
        assertTrue(newest.compareTo(sameTsA) < 0);
        assertTrue(sameTsA.compareTo(newest) > 0);

        // Tie-breaker by id ascending
        assertTrue(sameTsA.compareTo(sameTsB) < 0);
        assertTrue(sameTsB.compareTo(sameTsA) > 0);

        // Oldest last
        assertTrue(oldest.compareTo(newest) > 0);
    }

    /** Verifies equals/hashCode are based solely on returnId. */
    @Test
    void equalsAndHashCode_basedOnReturnId_only() {
        ReturnEntry a1 = e("RET-42", "SKU-A", 3, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);
        // Same id, different other fields -> equals must be true
        ReturnEntry a2 = e("RET-42", "DIFF", 99, ReturnReason.EXPIRED, "2025-10-27T11:00:00", "2026-01-01");

        ReturnEntry b = e("RET-43", "SKU-A", 3, ReturnReason.CUSTOMER_REMORSE, "2025-10-27T10:00:00", null);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        assertNotEquals(a1, b);
        assertNotEquals(a1.hashCode(), b.hashCode());

        assertEquals(a1, a1);              // reflexive
        assertNotEquals(a1, null);         // null
        assertNotEquals(a1, "not-entry");  // other type
    }

    /** Verifies that toString includes all fields in a readable format. */
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
