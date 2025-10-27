package domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnDecision}.
 * <p>
 * Each test method documents the scenario, preconditions and expected outcome,
 * verifying correct field copying, timestamp generation, and string formatting.
 */
class ReturnDecisionTest {

    /**
     * Helper method to create a sample {@link ReturnEntry} used across tests.
     *
     * @return a valid {@link ReturnEntry} with predefined attributes
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
     * Verifies that the constructor copies all values from the provided {@link ReturnEntry}
     * and assigns a non-null timestamp.
     *
     * Preconditions:
     *  - Valid {@link ReturnEntry} and {@link ReturnAction} provided;
     *  - Positive restocked and discarded quantities.
     *
     * Expected:
     *  - All fields in {@link ReturnDecision} match their corresponding values from the entry;
     *  - Timestamp is automatically generated and is recent.
     *
     * @see ReturnDecision#ReturnDecision(ReturnEntry, ReturnAction, int, int)
     * @see ReturnDecision#getTimestamp()
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
     * Verifies that {@link ReturnDecision#toString()} includes all field names and values
     * and follows the documented output format.
     *
     * Scenario:
     *  - Create a decision with known parameters.
     *
     * Expected:
     *  - String contains identifiers, action type, quantities, and a timestamp
     *    in ISO-8601 date-time format.
     *
     * @see ReturnDecision#toString()
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
