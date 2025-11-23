package domain.scheduling;

import domain.railway.Endpoint;
import domain.railway.LineSegment;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Test suite for {@link SegmentPassage}.
 * <p>
 * Each test method targets exactly one public member of {@code SegmentPassage}
 * and documents the scenario, preconditions and expected outcome.
 */
public class SegmentPassageTest {

    private LineSegment testSegment;
    private Endpoint departureEndpoint;
    private Endpoint arrivalEndpoint;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    @Before
    public void setUp() {
        testSegment = new LineSegment(1, true, 160, 20, 50000, 10);
        departureEndpoint = new Endpoint(1, "Lisbon", 100);
        arrivalEndpoint = new Endpoint(2, "Porto", 200);
        departureTime = LocalDateTime.of(2025, 1, 15, 10, 0);
        arrivalTime = LocalDateTime.of(2025, 1, 15, 10, 30);
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see SegmentPassage#SegmentPassage(LineSegment, Endpoint, Endpoint, LocalDateTime, LocalDateTime, int, boolean)
     */
    @Test
    public void constructor_initializesAllFields() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, true
        );

        assertEquals(testSegment, passage.getSegment());
        assertEquals(departureEndpoint, passage.getDepartureEndpoint());
        assertEquals(arrivalEndpoint, passage.getArrivalEndpoint());
        assertEquals(departureTime, passage.getDepartureTime());
        assertEquals(arrivalTime, passage.getArrivalTime());
        assertEquals(140, passage.getSpeedUsed());
        assertTrue(passage.requiresStop());
    }

    /**
     * Tests the getter for line segment.
     *
     * Scenario:
     *  - Create a passage and retrieve its segment.
     *
     * Expected:
     *  - returns the correct segment.
     *
     * @see SegmentPassage#getSegment()
     */
    @Test
    public void getSegment_returnsCorrectSegment() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(testSegment, passage.getSegment());
    }

    /**
     * Tests the getter for departure endpoint.
     *
     * Scenario:
     *  - Create a passage and retrieve its departure endpoint.
     *
     * Expected:
     *  - returns the correct endpoint.
     *
     * @see SegmentPassage#getDepartureEndpoint()
     */
    @Test
    public void getDepartureEndpoint_returnsCorrectEndpoint() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(departureEndpoint, passage.getDepartureEndpoint());
    }

    /**
     * Tests the getter for arrival endpoint.
     *
     * Scenario:
     *  - Create a passage and retrieve its arrival endpoint.
     *
     * Expected:
     *  - returns the correct endpoint.
     *
     * @see SegmentPassage#getArrivalEndpoint()
     */
    @Test
    public void getArrivalEndpoint_returnsCorrectEndpoint() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(arrivalEndpoint, passage.getArrivalEndpoint());
    }

    /**
     * Tests the getter for departure time.
     *
     * Scenario:
     *  - Create a passage and retrieve its departure time.
     *
     * Expected:
     *  - returns the correct time.
     *
     * @see SegmentPassage#getDepartureTime()
     */
    @Test
    public void getDepartureTime_returnsCorrectTime() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(departureTime, passage.getDepartureTime());
    }

    /**
     * Tests the getter for arrival time.
     *
     * Scenario:
     *  - Create a passage and retrieve its arrival time.
     *
     * Expected:
     *  - returns the correct time.
     *
     * @see SegmentPassage#getArrivalTime()
     */
    @Test
    public void getArrivalTime_returnsCorrectTime() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(arrivalTime, passage.getArrivalTime());
    }

    /**
     * Tests the getter for speed used.
     *
     * Scenario:
     *  - Create a passage with speed 120 km/h and retrieve it.
     *
     * Expected:
     *  - returns 120.
     *
     * @see SegmentPassage#getSpeedUsed()
     */
    @Test
    public void getSpeedUsed_returnsCorrectSpeed() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 120, false
        );

        assertEquals(120, passage.getSpeedUsed());
    }

    /**
     * Tests requiresStop when stop is required.
     *
     * Scenario:
     *  - Create a passage with requiresStop=true.
     *
     * Expected:
     *  - returns true.
     *
     * @see SegmentPassage#requiresStop()
     */
    @Test
    public void requiresStop_stopRequired_returnsTrue() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, true
        );

        assertTrue(passage.requiresStop());
    }

    /**
     * Tests requiresStop when stop is not required.
     *
     * Scenario:
     *  - Create a passage with requiresStop=false.
     *
     * Expected:
     *  - returns false.
     *
     * @see SegmentPassage#requiresStop()
     */
    @Test
    public void requiresStop_stopNotRequired_returnsFalse() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertFalse(passage.requiresStop());
    }

    /**
     * Tests the setter for departure time.
     *
     * Scenario:
     *  - Create a passage and update its departure time.
     *
     * Expected:
     *  - the new time is persisted.
     *
     * @see SegmentPassage#setDepartureTime(LocalDateTime)
     */
    @Test
    public void setDepartureTime_updatesTime() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        LocalDateTime newTime = LocalDateTime.of(2025, 1, 15, 10, 15);
        passage.setDepartureTime(newTime);

        assertEquals(newTime, passage.getDepartureTime());
    }

    /**
     * Tests the setter for arrival time.
     *
     * Scenario:
     *  - Create a passage and update its arrival time.
     *
     * Expected:
     *  - the new time is persisted.
     *
     * @see SegmentPassage#setArrivalTime(LocalDateTime)
     */
    @Test
    public void setArrivalTime_updatesTime() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        LocalDateTime newTime = LocalDateTime.of(2025, 1, 15, 10, 45);
        passage.setArrivalTime(newTime);

        assertEquals(newTime, passage.getArrivalTime());
    }

    /**
     * Tests applyDelay method.
     *
     * Scenario:
     *  - Create a passage and apply a 15-minute delay.
     *
     * Expected:
     *  - both departure and arrival times are increased by 15 minutes.
     *
     * @see SegmentPassage#applyDelay(long)
     */
    @Test
    public void applyDelay_updatesTimesCorrectly() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        passage.applyDelay(15);

        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 15), passage.getDepartureTime());
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 45), passage.getArrivalTime());
    }

    /**
     * Tests applyDelay with multiple delays.
     *
     * Scenario:
     *  - Apply two consecutive delays of 10 and 5 minutes.
     *
     * Expected:
     *  - times are cumulative (total 15 minutes delay).
     *
     * @see SegmentPassage#applyDelay(long)
     */
    @Test
    public void applyDelay_multipleTimes_cumulative() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        passage.applyDelay(10);
        passage.applyDelay(5);

        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 15), passage.getDepartureTime());
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 45), passage.getArrivalTime());
    }

    /**
     * Tests getTravelTimeMinutes calculation.
     *
     * Scenario:
     *  - Create a passage with 30 minutes travel time.
     *
     * Expected:
     *  - returns 30.
     *
     * @see SegmentPassage#getTravelTimeMinutes()
     */
    @Test
    public void getTravelTimeMinutes_calculatesCorrectly() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        assertEquals(30, passage.getTravelTimeMinutes());
    }

    /**
     * Tests getTravelTimeMinutes after applying delay.
     *
     * Scenario:
     *  - Create a passage with 30 minutes travel, then apply a delay.
     *
     * Expected:
     *  - travel time remains the same (30 minutes) since both times move equally.
     *
     * @see SegmentPassage#getTravelTimeMinutes()
     */
    @Test
    public void getTravelTimeMinutes_afterDelay_remainsSame() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        passage.applyDelay(15);

        assertEquals(30, passage.getTravelTimeMinutes());
    }

    /**
     * Tests toString method with stop required.
     *
     * Scenario:
     *  - Create a passage with stop and verify toString format.
     *
     * Expected:
     *  - format includes endpoints, times, speed, and "[STOP]".
     *
     * @see SegmentPassage#toString()
     */
    @Test
    public void toString_withStop_formatsCorrectly() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, true
        );

        String result = passage.toString();

        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("10:00"));
        assertTrue(result.contains("10:30"));
        assertTrue(result.contains("140 km/h"));
        assertTrue(result.contains("[STOP]"));
    }

    /**
     * Tests toString method without stop.
     *
     * Scenario:
     *  - Create a passage without stop and verify toString format.
     *
     * Expected:
     *  - format includes endpoints, times, speed, but no "[STOP]".
     *
     * @see SegmentPassage#toString()
     */
    @Test
    public void toString_withoutStop_formatsCorrectly() {
        SegmentPassage passage = new SegmentPassage(
                testSegment, departureEndpoint, arrivalEndpoint,
                departureTime, arrivalTime, 140, false
        );

        String result = passage.toString();

        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("10:00"));
        assertTrue(result.contains("10:30"));
        assertTrue(result.contains("140 km/h"));
        assertFalse(result.contains("[STOP]"));
    }
}