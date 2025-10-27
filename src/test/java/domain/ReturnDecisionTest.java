package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnDecision}.
 * <p>
 * Verifies that all fields are copied correctly from the source {@link ReturnEntry},
 * that the timestamp is generated, and that {@link #toString()} includes every element.
 */
class ReturnDecisionTest {

    /**
     * Helper to create a sample ReturnEntry for tests.
     */
    private ReturnEntry sampleEntry() {
        return new ReturnEntry(
                "RET-01",
                "SKU01",
                5,
                ReturnReason.CUSTOMER_REMORSE,
                LocalDateTime.parse("2025-10-27T10:00:00"),
                "2025-11-14"
        );
    }

    /**
     * Verifies that constructor copies all values and sets a non-null timestamp.
     */
    @Test
    void constructor_copiesValues_andSetsTimestamp() {
        ReturnEntry entry = sampleEntry();
        ReturnDecision d = new ReturnDecision(entry, ReturnAction.RESTOCK_ALL, 3, 2);

        assertEquals("RET-01", d.getReturnId());
        assertEquals("SKU01", d.getSku());
        assertEquals(ReturnAction.RESTOCK_ALL, d.getAction());
        assertEquals(3, d.getQtyRestocked());
        assertEquals(2, d.getQtyDiscarded());

        LocalDateTime ts = d.getTimestamp();
        assertNotNull(ts);
        assertTrue(ts.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    /**
     * Verifies that toString includes all field values and is formatted as expected.
     */
    @Test
    void toString_includesAllFields() {
        ReturnEntry entry = sampleEntry();
        ReturnDecision d = new ReturnDecision(entry, ReturnAction.PARTIAL, 1, 4);

        String s = d.toString();
        assertTrue(s.contains("returnId=RET-01"));
        assertTrue(s.contains("sku=SKU01"));
        assertTrue(s.contains("action=PARTIAL"));
        assertTrue(s.contains("qtyRestocked=1"));
        assertTrue(s.contains("qtyDiscarded=4"));
        assertTrue(s.matches(".*\\d{4}-\\d{2}-\\d{2}.*")); // timestamp pattern
    }
}
