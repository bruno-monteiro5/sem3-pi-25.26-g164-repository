package domain.scheduling;

import domain.railway.Endpoint;
import domain.railway.LineSegment;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Test suite for {@link CrossingOperation}.
 * <p>
 * Each test method targets exactly one public member of {@code CrossingOperation}
 * and documents the scenario, preconditions and expected outcome.
 */
public class CrossingOperationTest {

    private LineSegment testSegment;
    private Endpoint location;
    private LocalDateTime waitStartTime;
    private LocalDateTime waitEndTime;

    @Before
    public void setUp() {
        testSegment = new LineSegment(1, true, 160, 20, 50000, 10);
        location = new Endpoint(1, "Coimbra-B", 100);
        waitStartTime = LocalDateTime.of(2025, 1, 15, 10, 0);
        waitEndTime = LocalDateTime.of(2025, 1, 15, 10, 15);
    }

    /**
     * Verifies that the constructor initializes all fields correctly for station crossing.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields;
     *  - crossing type is AT_STATION.
     *
     * Expected:
     *  - all getters return the supplied values;
     *  - wait duration is calculated correctly (15 minutes).
     *
     * @see CrossingOperation#CrossingOperation(int, int, LineSegment, Endpoint, CrossingOperation.CrossingType, LocalDateTime, LocalDateTime)
     */
    @Test
    public void constructor_atStation_initializesCorrectly() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(1, crossing.getWaitingTrainId());
        assertEquals(2, crossing.getPassingTrainId());
        assertEquals(testSegment, crossing.getSegment());
        assertEquals(location, crossing.getLocation());
        assertEquals(CrossingOperation.CrossingType.AT_STATION, crossing.getType());
        assertEquals(waitStartTime, crossing.getWaitStartTime());
        assertEquals(waitEndTime, crossing.getWaitEndTime());
        assertEquals(15, crossing.getWaitDurationMinutes());
    }

    /**
     * Verifies that the constructor initializes all fields correctly for siding crossing.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields;
     *  - crossing type is AT_SIDING.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see CrossingOperation#CrossingOperation(int, int, LineSegment, Endpoint, CrossingOperation.CrossingType, LocalDateTime, LocalDateTime)
     */
    @Test
    public void constructor_atSiding_initializesCorrectly() {
        CrossingOperation crossing = new CrossingOperation(
                3, 4, testSegment, location,
                CrossingOperation.CrossingType.AT_SIDING,
                waitStartTime, waitEndTime
        );

        assertEquals(3, crossing.getWaitingTrainId());
        assertEquals(4, crossing.getPassingTrainId());
        assertEquals(CrossingOperation.CrossingType.AT_SIDING, crossing.getType());
    }

    /**
     * Tests the getter for waiting train id.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve the waiting train id.
     *
     * Expected:
     *  - returns the correct id.
     *
     * @see CrossingOperation#getWaitingTrainId()
     */
    @Test
    public void getWaitingTrainId_returnsCorrectId() {
        CrossingOperation crossing = new CrossingOperation(
                10, 20, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(10, crossing.getWaitingTrainId());
    }

    /**
     * Tests the getter for passing train id.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve the passing train id.
     *
     * Expected:
     *  - returns the correct id.
     *
     * @see CrossingOperation#getPassingTrainId()
     */
    @Test
    public void getPassingTrainId_returnsCorrectId() {
        CrossingOperation crossing = new CrossingOperation(
                10, 20, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(20, crossing.getPassingTrainId());
    }

    /**
     * Tests the getter for segment.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve its segment.
     *
     * Expected:
     *  - returns the correct segment.
     *
     * @see CrossingOperation#getSegment()
     */
    @Test
    public void getSegment_returnsCorrectSegment() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(testSegment, crossing.getSegment());
    }

    /**
     * Tests the getter for location endpoint.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve its location.
     *
     * Expected:
     *  - returns the correct endpoint.
     *
     * @see CrossingOperation#getLocation()
     */
    @Test
    public void getLocation_returnsCorrectEndpoint() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(location, crossing.getLocation());
    }

    /**
     * Tests the getter for crossing type (AT_STATION).
     *
     * Scenario:
     *  - Create a station crossing and retrieve its type.
     *
     * Expected:
     *  - returns AT_STATION.
     *
     * @see CrossingOperation#getType()
     */
    @Test
    public void getType_atStation_returnsCorrectType() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(CrossingOperation.CrossingType.AT_STATION, crossing.getType());
    }

    /**
     * Tests the getter for crossing type (AT_SIDING).
     *
     * Scenario:
     *  - Create a siding crossing and retrieve its type.
     *
     * Expected:
     *  - returns AT_SIDING.
     *
     * @see CrossingOperation#getType()
     */
    @Test
    public void getType_atSiding_returnsCorrectType() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_SIDING,
                waitStartTime, waitEndTime
        );

        assertEquals(CrossingOperation.CrossingType.AT_SIDING, crossing.getType());
    }

    /**
     * Tests the getter for wait start time.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve the wait start time.
     *
     * Expected:
     *  - returns the correct time.
     *
     * @see CrossingOperation#getWaitStartTime()
     */
    @Test
    public void getWaitStartTime_returnsCorrectTime() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(waitStartTime, crossing.getWaitStartTime());
    }

    /**
     * Tests the getter for wait end time.
     *
     * Scenario:
     *  - Create a crossing operation and retrieve the wait end time.
     *
     * Expected:
     *  - returns the correct time.
     *
     * @see CrossingOperation#getWaitEndTime()
     */
    @Test
    public void getWaitEndTime_returnsCorrectTime() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(waitEndTime, crossing.getWaitEndTime());
    }

    /**
     * Tests wait duration calculation with 15 minutes.
     *
     * Scenario:
     *  - Create a crossing with 15-minute wait period.
     *
     * Expected:
     *  - returns 15.
     *
     * @see CrossingOperation#getWaitDurationMinutes()
     */
    @Test
    public void getWaitDurationMinutes_fifteenMinutes_calculatesCorrectly() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        assertEquals(15, crossing.getWaitDurationMinutes());
    }

    /**
     * Tests wait duration calculation with 30 minutes.
     *
     * Scenario:
     *  - Create a crossing with 30-minute wait period.
     *
     * Expected:
     *  - returns 30.
     *
     * @see CrossingOperation#getWaitDurationMinutes()
     */
    @Test
    public void getWaitDurationMinutes_thirtyMinutes_calculatesCorrectly() {
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 10, 30);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, endTime
        );

        assertEquals(30, crossing.getWaitDurationMinutes());
    }

    /**
     * Tests wait duration calculation with 1 hour.
     *
     * Scenario:
     *  - Create a crossing with 60-minute wait period.
     *
     * Expected:
     *  - returns 60.
     *
     * @see CrossingOperation#getWaitDurationMinutes()
     */
    @Test
    public void getWaitDurationMinutes_oneHour_calculatesCorrectly() {
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 11, 0);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, endTime
        );

        assertEquals(60, crossing.getWaitDurationMinutes());
    }

    /**
     * Tests toString method for station crossing.
     *
     * Scenario:
     *  - Create a station crossing and verify toString format.
     *
     * Expected:
     *  - format includes train ids, location name, "Station", wait duration, and times.
     *
     * @see CrossingOperation#toString()
     */
    @Test
    public void toString_atStation_formatsCorrectly() {
        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, waitEndTime
        );

        String result = crossing.toString();

        assertTrue(result.contains("Train 1"));
        assertTrue(result.contains("Train 2"));
        assertTrue(result.contains("Coimbra-B"));
        assertTrue(result.contains("Station"));
        assertTrue(result.contains("15 min"));
        assertTrue(result.contains("10:00"));
        assertTrue(result.contains("10:15"));
    }

    /**
     * Tests toString method for siding crossing.
     *
     * Scenario:
     *  - Create a siding crossing and verify toString format.
     *
     * Expected:
     *  - format includes train ids, location name, and "Siding".
     *
     * @see CrossingOperation#toString()
     */
    @Test
    public void toString_atSiding_formatsCorrectly() {
        CrossingOperation crossing = new CrossingOperation(
                3, 4, testSegment, location,
                CrossingOperation.CrossingType.AT_SIDING,
                waitStartTime, waitEndTime
        );

        String result = crossing.toString();

        assertTrue(result.contains("Train 3"));
        assertTrue(result.contains("Train 4"));
        assertTrue(result.contains("Coimbra-B"));
        assertTrue(result.contains("Siding"));
        assertTrue(result.contains("15 min"));
    }

    /**
     * Tests toString with longer wait duration.
     *
     * Scenario:
     *  - Create a crossing with 45-minute wait period.
     *
     * Expected:
     *  - toString includes "45 min".
     *
     * @see CrossingOperation#toString()
     */
    @Test
    public void toString_longerWait_includesCorrectDuration() {
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 15, 10, 45);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, location,
                CrossingOperation.CrossingType.AT_STATION,
                waitStartTime, endTime
        );

        String result = crossing.toString();

        assertTrue(result.contains("45 min"));
    }
}