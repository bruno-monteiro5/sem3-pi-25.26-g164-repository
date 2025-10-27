package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnReason}.
 * <p>
 * Each test method documents its scenario, preconditions and expected outcome,
 * verifying correct parsing behavior of {@link ReturnReason#fromString(String)}
 * and ensuring all enum constants are defined and accessible.
 */
class ReturnReasonTest {

    /**
     * Verifies that all expected enum constants are defined and appear in the declared order.
     *
     * Preconditions:
     *  - {@link ReturnReason} defines exactly four constants.
     *
     * Expected:
     *  - {@link ReturnReason#values()} returns all constants in declaration order.
     *
     * @see ReturnReason#values()
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
     * Verifies that {@link ReturnReason#fromString(String)} correctly parses case-insensitive
     * and hyphenated text values into the corresponding enum constants.
     *
     * Scenario:
     *  - Input strings contain lower/upper case letters, optional hyphens or spaces.
     *
     * Expected:
     *  - Returned constant matches the logical reason, ignoring case and formatting.
     *
     * @see ReturnReason#fromString(String)
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
     * Verifies that {@link ReturnReason#fromString(String)} throws an exception
     * when the provided string does not match any valid constant.
     *
     * Preconditions:
     *  - Input text does not correspond to any known reason.
     *
     * Expected:
     *  - {@link IllegalArgumentException} is thrown.
     *
     * @see ReturnReason#fromString(String)
     */
    @Test
    void fromString_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ReturnReason.fromString("unknown-reason"));
    }

    /**
     * Verifies that {@link Enum#valueOf(Class, String)} returns the correct constant for valid names.
     *
     * Scenario:
     *  - Invoke {@code valueOf()} with known constant names.
     *
     * Expected:
     *  - Correct constant returned for each name.
     *
     * @see ReturnReason#valueOf(String)
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertSame(ReturnReason.EXPIRED, ReturnReason.valueOf("EXPIRED"));
        assertSame(ReturnReason.DAMAGED, ReturnReason.valueOf("DAMAGED"));
    }
}
