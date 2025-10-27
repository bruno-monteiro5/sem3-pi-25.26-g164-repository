package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnReason}.
 * <p>
 * Verifies correct parsing behavior of {@link ReturnReason#fromString(String)}
 * and ensures all enum constants are defined and accessible.
 */
class ReturnReasonTest {

    /**
     * Verifies that all expected enum constants exist and are in order.
     */
    @Test
    void values_containsAllExpectedConstants() {
        ReturnReason[] values = ReturnReason.values();
        assertArrayEquals(
                new ReturnReason[]{
                        ReturnReason.CUSTOMER_REMORSE,
                        ReturnReason.DAMAGED,
                        ReturnReason.EXPIRED,
                        ReturnReason.CYCLE_COUNT
                },
                values
        );
    }

    /**
     * Verifies that {@code fromString} parses hyphenated, lowercase, and trimmed inputs correctly.
     */
    @Test
    void fromString_parsesCaseInsensitiveAndHyphenatedValues() {
        assertEquals(ReturnReason.CUSTOMER_REMORSE, ReturnReason.fromString("customer-remorse"));
        assertEquals(ReturnReason.CUSTOMER_REMORSE, ReturnReason.fromString("Customer-Remorse"));
        assertEquals(ReturnReason.DAMAGED, ReturnReason.fromString("damaged"));
        assertEquals(ReturnReason.EXPIRED, ReturnReason.fromString(" expired  "));
        assertEquals(ReturnReason.CYCLE_COUNT, ReturnReason.fromString("cycle-count"));
    }

    /**
     * Verifies that {@code fromString} throws an exception for invalid inputs.
     */
    @Test
    void fromString_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ReturnReason.fromString("unknown-reason"));
    }

    /**
     * Verifies that valueOf returns the correct constant for each name.
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertSame(ReturnReason.EXPIRED, ReturnReason.valueOf("EXPIRED"));
        assertSame(ReturnReason.DAMAGED, ReturnReason.valueOf("DAMAGED"));
    }
}
