// src/test/java/domain/ReturnReasonTest.java
package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReturnReasonTest {

    @Test
    void testFromStringValid() {
        assertEquals(ReturnReason.CUSTOMER_REMORSE, ReturnReason.fromString("customer-remorse"));
        assertEquals(ReturnReason.CUSTOMER_REMORSE, ReturnReason.fromString("CUSTOMER_REMORSE"));
        assertEquals(ReturnReason.EXPIRED, ReturnReason.fromString("expired"));
        assertEquals(ReturnReason.CYCLE_COUNT, ReturnReason.fromString("CYCLE-COUNT"));
    }

    @Test
    void testFromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> ReturnReason.fromString("not_a_reason"));
    }
}
