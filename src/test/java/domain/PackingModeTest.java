package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PackingMode}.
 * <p>
 * Each test method targets one public behavior of the enumeration.
 */
class PackingModeTest {

    /**
     * Verifies that all expected enumeration constants are defined.
     *
     * Expected:
     *  - {@link PackingMode#FF}, {@link PackingMode#FFD}, and {@link PackingMode#BFD}.
     *
     * @see PackingMode#values()
     */
    @Test
    void values_containsAllConstants() {
        PackingMode[] values = PackingMode.values();

        assertEquals(3, values.length);
        assertArrayEquals(
                new PackingMode[]{PackingMode.FF, PackingMode.FFD, PackingMode.BFD},
                values
        );
    }

    /**
     * Verifies that {@link PackingMode#valueOf(String)} resolves each name correctly.
     *
     * Expected:
     *  - Each enum name returns its matching constant.
     *
     * @see PackingMode#valueOf(String)
     */
    @Test
    void valueOf_returnsCorrectConstant() {
        assertEquals(PackingMode.FF, PackingMode.valueOf("FF"));
        assertEquals(PackingMode.FFD, PackingMode.valueOf("FFD"));
        assertEquals(PackingMode.BFD, PackingMode.valueOf("BFD"));
    }
}
