// src/test/java/domain/ReturnEntryTest.java
package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReturnEntryTest {

    @Test
    void testValidEntryAndExpiryNormalization() {
        LocalDateTime ts = LocalDateTime.now();
        ReturnEntry e = new ReturnEntry("ID1", "SKU", 10, ReturnReason.EXPIRED, ts, "");
        assertEquals("ID1", e.getReturnId());
        assertEquals("SKU", e.getSku());
        assertEquals(10, e.getQuantity());
        assertEquals(ReturnReason.EXPIRED, e.getReason());
        assertEquals(ts, e.getTimestamp());
        // Blank expiry normalized to null
        assertNull(e.getExpiryDate());
    }

    @Test
    void testInvalidArguments() {
        LocalDateTime ts = LocalDateTime.now();
        // null or blank returnId
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry(null, "SKU", 1, ReturnReason.EXPIRED, ts, null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry(" ", "SKU", 1, ReturnReason.EXPIRED, ts, null)
        );
        // null or blank sku
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("ID", "", 1, ReturnReason.EXPIRED, ts, null)
        );
        // non-positive quantity
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("ID", "SKU", 0, ReturnReason.EXPIRED, ts, null)
        );
        // null reason
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("ID", "SKU", 1, null, ts, null)
        );
        // null timestamp
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnEntry("ID", "SKU", 1, ReturnReason.EXPIRED, null, null)
        );
    }

    @Test
    void testCompareToOrdering() {
        LocalDateTime t1 = LocalDateTime.parse("2025-01-01T10:00:00");
        LocalDateTime t2 = LocalDateTime.parse("2025-01-02T10:00:00");
        ReturnEntry e1 = new ReturnEntry("A", "SKU", 1, ReturnReason.DAMAGED, t1, null);
        ReturnEntry e2 = new ReturnEntry("B", "SKU", 1, ReturnReason.DAMAGED, t2, null);
        // e2 has newer time, so e2 should come before e1 (i.e., e2.compareTo(e1) < 0)
        assertTrue(e2.compareTo(e1) < 0);
        // Same timestamp, compare by returnId lexicographically
        ReturnEntry e3 = new ReturnEntry("A01", "SKU", 1, ReturnReason.DAMAGED, t1, null);
        ReturnEntry e4 = new ReturnEntry("B02", "SKU", 1, ReturnReason.DAMAGED, t1, null);
        assertTrue(e3.compareTo(e4) < 0); // "A01" < "B02"
        assertTrue(e4.compareTo(e3) > 0);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime ts = LocalDateTime.now();
        ReturnEntry e1 = new ReturnEntry("ID1", "SKU", 5, ReturnReason.CYCLE_COUNT, ts, null);
        ReturnEntry e2 = new ReturnEntry("ID1", "SKU", 5, ReturnReason.CYCLE_COUNT, ts, null);
        // Same ID => equal (per equals implementation)
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        // Different ID => not equal
        ReturnEntry e3 = new ReturnEntry("ID2", "SKU", 5, ReturnReason.CYCLE_COUNT, ts, null);
        assertNotEquals(e1, e3);
    }

    @Test
    void testToStringContainsKeyFields() {
        LocalDateTime ts = LocalDateTime.parse("2025-10-26T15:00:00");
        ReturnEntry e = new ReturnEntry(
                "RET001", "SKU123", 5, ReturnReason.DAMAGED, ts, "2026-01-01");

        String str = e.toString();
        assertNotNull(str);

        // Campos essenciais
        assertTrue(str.contains("RET001"), "toString should contain returnId");
        assertTrue(str.contains("SKU123"), "toString should contain sku");
        assertTrue(str.contains("5"), "toString should contain quantity");
        assertTrue(str.contains(ReturnReason.DAMAGED.name()), "toString should contain reason enum name");

        // Timestamp: valida só o prefixo até aos minutos para não falhar por segundos omitidos
        String tsPrefix = ts.toString().substring(0, 16); // "2025-10-26T15:00"
        assertTrue(str.contains(tsPrefix), "toString should contain timestamp prefix: " + tsPrefix);

        // Expiry se não for null deve aparecer algures
        assertTrue(str.contains("2026-01-01"), "toString should contain expiry date when present");
    }


}
