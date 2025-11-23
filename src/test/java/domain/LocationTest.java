package domain;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for {@link Location}.
 * Provides 100% coverage, testing:
 * <ul>
 *   <li>Valid construction</li>
 *   <li>Getter methods</li>
 *   <li>Latitude boundary validation</li>
 *   <li>Longitude boundary validation</li>
 * </ul>
 */
public class LocationTest {

    /**
     * Tests that a valid Location is created and getters return correct values.
     */
    @Test
    public void testValidConstructorAndGetters() {
        Location loc = new Location(40.0, -8.0);

        assertEquals(40.0, loc.getLatitude(), 0.0001);
        assertEquals(-8.0, loc.getLongitude(), 0.0001);
    }

    /**
     * Tests that latitude below -90 triggers an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLatitudeTooLow() {
        new Location(-91.0, 0.0);
    }

    /**
     * Tests that latitude above 90 triggers an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLatitudeTooHigh() {
        new Location(91.0, 0.0);
    }

    /**
     * Tests that longitude below -180 triggers an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLongitudeTooLow() {
        new Location(0.0, -181.0);
    }

    /**
     * Tests that longitude above 180 triggers an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLongitudeTooHigh() {
        new Location(0.0, 181.0);
    }

    /**
     * Tests that boundary values are accepted (valid range inclusive).
     */
    @Test
    public void testBoundaryValues() {
        Location loc1 = new Location(-90, -180);
        Location loc2 = new Location(90, 180);

        assertEquals(-90, loc1.getLatitude(), 0.0001);
        assertEquals(-180, loc1.getLongitude(), 0.0001);

        assertEquals(90, loc2.getLatitude(), 0.0001);
        assertEquals(180, loc2.getLongitude(), 0.0001);
    }
}
