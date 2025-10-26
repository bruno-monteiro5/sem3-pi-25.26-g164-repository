// src/test/java/domain/ReturnDecisionTest.java
package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReturnDecisionTest {

    @Test
    void testValidDecisions() {
        LocalDateTime ts = LocalDateTime.now();
        ReturnEntry entry = new ReturnEntry("RID", "SKU", 10, ReturnReason.DAMAGED, ts, null);
        // RESTOCK_ALL: restock all 10, discard 0
        ReturnDecision d1 = new ReturnDecision(entry, ReturnAction.RESTOCK_ALL, 10, 0);
        assertEquals(entry, d1.getEntry());
        assertEquals(ReturnAction.RESTOCK_ALL, d1.getAction());
        assertEquals(10, d1.getQtyRestocked());
        assertEquals(0, d1.getQtyDiscarded());
        assertNotNull(d1.getDecidedAt());

        // DISCARD_ALL: restock 0, discard 10
        ReturnDecision d2 = new ReturnDecision(entry, ReturnAction.DISCARD_ALL, 0, 10);
        assertEquals(ReturnAction.DISCARD_ALL, d2.getAction());
        assertEquals(0, d2.getQtyRestocked());
        assertEquals(10, d2.getQtyDiscarded());

        // PARTIAL: restock 6, discard 4
        ReturnDecision d3 = new ReturnDecision(entry, ReturnAction.PARTIAL, 6, 4);
        assertEquals(ReturnAction.PARTIAL, d3.getAction());
        assertEquals(6, d3.getQtyRestocked());
        assertEquals(4, d3.getQtyDiscarded());
    }

    @Test
    void testInvalidArguments() {
        LocalDateTime ts = LocalDateTime.now();
        ReturnEntry entry = new ReturnEntry("ID", "SKU", 5, ReturnReason.CUSTOMER_REMORSE, ts, null);
        // Null entry or action
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnDecision(null, ReturnAction.PARTIAL, 3, 2)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnDecision(entry, null, 3, 2)
        );
        // Negative quantities
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnDecision(entry, ReturnAction.PARTIAL, -1, 6)
        );
        // Sum mismatch
        assertThrows(IllegalArgumentException.class, () ->
                new ReturnDecision(entry, ReturnAction.PARTIAL, 3, 3)  // should sum to 5
        );
    }

    @Test
    void testEqualsAndToString() {
        LocalDateTime ts = LocalDateTime.now();
        ReturnEntry e1 = new ReturnEntry("X", "SKU", 5, ReturnReason.DAMAGED, ts, null);
        ReturnEntry e2 = new ReturnEntry("X", "SKU", 5, ReturnReason.EXPIRED, ts, null);
        ReturnDecision d1 = new ReturnDecision(e1, ReturnAction.RESTOCK_ALL, 5, 0);
        ReturnDecision d2 = new ReturnDecision(e2, ReturnAction.RESTOCK_ALL, 5, 0);
        // equals is based only on entry ID
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        // toString includes action and quantities
        assertTrue(d1.toString().contains("action=RESTOCK_ALL"));
        assertTrue(d1.toString().contains("qtyRestocked=5"));
    }
}
