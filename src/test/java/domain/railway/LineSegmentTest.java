package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link LineSegment}.
 * <p>
 * Each test method targets exactly one public member of {@code LineSegment}
 * and documents the scenario, preconditions and expected outcome.
 */
public class LineSegmentTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see LineSegment#LineSegment(int, boolean, int, int, int, int)
     */
    @Test
    public void constructor_initializesAllFields() {
        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 10);

        assertEquals(1, segment.getId());
        assertTrue(segment.isElectrified());
        assertEquals(160, segment.getSpeedLimit());
        assertEquals(20, segment.getMaxWeight());
        assertEquals(50000, segment.getLength());
        assertEquals(10, segment.getLineId());
    }

    /**
     * Tests the getter for segment id.
     *
     * Scenario:
     *  - Create a segment with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see LineSegment#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        LineSegment segment = new LineSegment(99, false, 100, 15, 30000, 5);
        assertEquals(99, segment.getId());
    }

    /**
     * Tests the getter for electrification status (electrified segment).
     *
     * Scenario:
     *  - Create an electrified segment and read its status.
     *
     * Expected:
     *  - returns true.
     *
     * @see LineSegment#isElectrified()
     */
    @Test
    public void isElectrified_electrifiedSegment_returnsTrue() {
        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 10);
        assertTrue(segment.isElectrified());
    }

    /**
     * Tests the getter for electrification status (non-electrified segment).
     *
     * Scenario:
     *  - Create a non-electrified segment and read its status.
     *
     * Expected:
     *  - returns false.
     *
     * @see LineSegment#isElectrified()
     */
    @Test
    public void isElectrified_nonElectrifiedSegment_returnsFalse() {
        LineSegment segment = new LineSegment(2, false, 120, 18, 40000, 10);
        assertFalse(segment.isElectrified());
    }

    /**
     * Tests the getter for speed limit.
     *
     * Scenario:
     *  - Create a segment with a specific speed limit and read it back.
     *
     * Expected:
     *  - returned speed limit matches the constructor value.
     *
     * @see LineSegment#getSpeedLimit()
     */
    @Test
    public void getSpeedLimit_returnsCorrectLimit() {
        LineSegment segment = new LineSegment(1, true, 220, 20, 50000, 10);
        assertEquals(220, segment.getSpeedLimit());
    }

    /**
     * Tests the getter for maximum weight per meter.
     *
     * Scenario:
     *  - Create a segment with a specific max weight and read it back.
     *
     * Expected:
     *  - returned max weight matches the constructor value.
     *
     * @see LineSegment#getMaxWeight()
     */
    @Test
    public void getMaxWeight_returnsCorrectWeight() {
        LineSegment segment = new LineSegment(1, true, 160, 25, 50000, 10);
        assertEquals(25, segment.getMaxWeight());
    }

    /**
     * Tests the getter for segment length in meters.
     *
     * Scenario:
     *  - Create a segment with a specific length and read it back.
     *
     * Expected:
     *  - returned length matches the constructor value.
     *
     * @see LineSegment#getLength()
     */
    @Test
    public void getLength_returnsCorrectLength() {
        LineSegment segment = new LineSegment(1, true, 160, 20, 75000, 10);
        assertEquals(75000, segment.getLength());
    }

    /**
     * Tests the getter for line id.
     *
     * Scenario:
     *  - Create a segment associated with a specific line and read its line id.
     *
     * Expected:
     *  - returned line id matches the constructor value.
     *
     * @see LineSegment#getLineId()
     */
    @Test
    public void getLineId_returnsCorrectLineId() {
        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 42);
        assertEquals(42, segment.getLineId());
    }

    /**
     * Tests toString method for electrified segment.
     *
     * Scenario:
     *  - Create an electrified segment and verify toString format.
     *
     * Expected:
     *  - format includes segment id, length, speed, and "Electrified: Yes".
     *
     * @see LineSegment#toString()
     */
    @Test
    public void toString_electrifiedSegment_formatsCorrectly() {
        LineSegment segment = new LineSegment(5, true, 160, 20, 50000, 10);

        String result = segment.toString();

        assertEquals("Segment 5: 50000m, 160km/h, Electrified: Yes", result);
    }

    /**
     * Tests toString method for non-electrified segment.
     *
     * Scenario:
     *  - Create a non-electrified segment and verify toString format.
     *
     * Expected:
     *  - format includes segment id, length, speed, and "Electrified: No".
     *
     * @see LineSegment#toString()
     */
    @Test
    public void toString_nonElectrifiedSegment_formatsCorrectly() {
        LineSegment segment = new LineSegment(7, false, 120, 18, 40000, 10);

        String result = segment.toString();

        assertEquals("Segment 7: 40000m, 120km/h, Electrified: No", result);
    }

    /**
     * Tests toString method with high speed segment (300 km/h).
     *
     * Scenario:
     *  - Create a high-speed segment and verify toString format.
     *
     * Expected:
     *  - format correctly displays the high speed limit.
     *
     * @see LineSegment#toString()
     */
    @Test
    public void toString_highSpeedSegment() {
        LineSegment segment = new LineSegment(10, true, 300, 22, 100000, 15);

        String result = segment.toString();

        assertTrue(result.contains("300km/h"));
    }
}