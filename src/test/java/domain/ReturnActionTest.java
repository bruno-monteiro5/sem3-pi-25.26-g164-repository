package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnAction}.
 * <p>
 * Each test method documents its scenario, preconditions and expected outcome,
 * verifying the correct behaviour of the {@link Enum} methods defined by {@link ReturnAction}.
 */
class ReturnActionTest {

    /**
     * Verifies that all expected enum constants are defined and appear in the correct order.
     *
     * Preconditions:
     *  - {@link ReturnAction} defines exactly three constants: RESTOCK_ALL, DISCARD_ALL and PARTIAL.
     *
     * Expected:
     *  - {@link ReturnAction#values()} returns an array of length three containing those constants in declaration order.
     *
     * @see ReturnAction#values()
     */
    @Test
    void values_containsAllExpectedConstants() {
        ReturnAction[] values = ReturnAction.values();
        assertEquals(3, values.length);
        assertArrayEquals(
                new ReturnAction[]{ReturnAction.RESTOCK_ALL, ReturnAction.DISCARD_ALL, ReturnAction.PARTIAL},
                values
        );
    }

    /**
     * Verifies that {@link Enum#valueOf(Class, String)} correctly resolves
     * each constant name defined in {@link ReturnAction}.
     *
     * Scenario:
     *  - Call {@code valueOf()} with valid constant names.
     *
     * Expected:
     *  - Each name returns its matching constant.
     *
     * @see ReturnAction#valueOf(String)
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertSame(ReturnAction.RESTOCK_ALL, ReturnAction.valueOf("RESTOCK_ALL"));
        assertSame(ReturnAction.DISCARD_ALL, ReturnAction.valueOf("DISCARD_ALL"));
        assertSame(ReturnAction.PARTIAL, ReturnAction.valueOf("PARTIAL"));
    }

    /**
     * Verifies that {@link Enum#valueOf(Class, String)} throws an {@link IllegalArgumentException}
     * when provided with a name that does not match any existing constant.
     *
     * Preconditions:
     *  - Input string is not one of the defined constant names.
     *
     * Expected:
     *  - {@link IllegalArgumentException} is thrown.
     *
     * @see ReturnAction#valueOf(String)
     */
    @Test
    void valueOf_invalidName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ReturnAction.valueOf("INVALID"));
    }
}
