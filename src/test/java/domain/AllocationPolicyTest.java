package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link AllocationPolicy}.
 * <p>
 * Each test method targets one aspect of the enumeration behavior.
 */
class AllocationPolicyTest {

    /**
     * Verifies that all expected enumeration constants are defined.
     *
     * Expected:
     *  - {@link AllocationPolicy#SPLIT_LINE} and {@link AllocationPolicy#DEFER_LINE} exist.
     *
     * @see AllocationPolicy#values()
     */
    @Test
    void values_containsAllConstants() {
        AllocationPolicy[] values = AllocationPolicy.values();

        assertEquals(2, values.length);
        assertArrayEquals(
                new AllocationPolicy[]{AllocationPolicy.SPLIT_LINE, AllocationPolicy.DEFER_LINE},
                values
        );
    }

    /**
     * Verifies that {@link AllocationPolicy#valueOf(String)} correctly resolves names.
     *
     * Expected:
     *  - Each name maps to the corresponding enum constant.
     *
     * @see AllocationPolicy#valueOf(String)
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertEquals(AllocationPolicy.SPLIT_LINE, AllocationPolicy.valueOf("SPLIT_LINE"));
        assertEquals(AllocationPolicy.DEFER_LINE, AllocationPolicy.valueOf("DEFER_LINE"));
    }
}
