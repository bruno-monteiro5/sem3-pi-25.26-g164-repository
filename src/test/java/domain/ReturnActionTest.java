package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnAction}.
 * <p>
 * Ensures all enum constants are defined and accessible via {@link Enum#valueOf(Class, String)}.
 */
class ReturnActionTest {

    /**
     * Verifies that all expected enum constants exist and have the correct names.
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
     * Verifies that valueOf returns the correct enum constant for each name.
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertSame(ReturnAction.RESTOCK_ALL, ReturnAction.valueOf("RESTOCK_ALL"));
        assertSame(ReturnAction.DISCARD_ALL, ReturnAction.valueOf("DISCARD_ALL"));
        assertSame(ReturnAction.PARTIAL, ReturnAction.valueOf("PARTIAL"));
    }

    /**
     * Verifies that an invalid name triggers IllegalArgumentException.
     */
    @Test
    void valueOf_invalidName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ReturnAction.valueOf("INVALID"));
    }
}
