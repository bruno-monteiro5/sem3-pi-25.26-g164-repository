package domain.scheduling;

import domain.railway.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Test suite for {@link TrainSchedule}.
 * <p>
 * Each test method targets exactly one public member of {@code TrainSchedule}
 * and documents the scenario, preconditions and expected outcome.
 */
public class TrainScheduleTest {

    private Train testTrain;
    private LocalDateTime scheduledDeparture;
    private LineSegment testSegment;
    private Endpoint endpoint1;
    private Endpoint endpoint2;

    @Before
    public void setUp() {
        endpoint1 = new Endpoint(1, "Lisbon", 100);
        endpoint2 = new Endpoint(2, "Porto", 200);
        Path path = new Path(1, "North Line");
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);

        testTrain = new Train(1, "VAT123", time, date, path, endpoint1, endpoint2);
        scheduledDeparture = LocalDateTime.of(date, time);
        testSegment = new LineSegment(1, true, 160, 20, 50000, 10);
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid train and scheduledDeparture supplied.
     *
     * Expected:
     *  - getters return correct values;
     *  - passages and crossings lists are empty;
     *  - travel and wait times are zero.
     *
     * @see TrainSchedule#TrainSchedule(Train, LocalDateTime)
     */
    @Test
    public void constructor_initializesAllFields() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        assertEquals(testTrain, schedule.getTrain());
        assertEquals(scheduledDeparture, schedule.getScheduledDeparture());
        assertEquals(scheduledDeparture, schedule.getActualDeparture());
        assertTrue(schedule.getPassages().isEmpty());
        assertTrue(schedule.getCrossingOperations().isEmpty());
        assertEquals(0, schedule.getTotalTravelTimeMinutes());
        assertEquals(0, schedule.getTotalWaitTimeMinutes());
    }

    /**
     * Tests adding a single passage to the schedule.
     *
     * Scenario:
     *  - Create a schedule and add one passage.
     *
     * Expected:
     *  - passages list contains the passage;
     *  - estimated arrival is updated.
     *
     * @see TrainSchedule#addPassage(SegmentPassage)
     */
    @Test
    public void addPassage_singlePassage_updatesSchedule() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrivalTime = scheduledDeparture.plusMinutes(30);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrivalTime, 140, false
        );

        schedule.addPassage(passage);

        assertEquals(1, schedule.getPassages().size());
        assertEquals(arrivalTime, schedule.getEstimatedArrival());
        assertEquals(30, schedule.getTotalTravelTimeMinutes());
    }

    /**
     * Tests adding multiple passages.
     *
     * Scenario:
     *  - Add two consecutive passages.
     *
     * Expected:
     *  - passages list contains both passages;
     *  - estimated arrival is the arrival of the last passage.
     *
     * @see TrainSchedule#addPassage(SegmentPassage)
     */
    @Test
    public void addPassage_multiplePassages_updatesCorrectly() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival1 = scheduledDeparture.plusMinutes(30);
        SegmentPassage passage1 = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival1, 140, false
        );

        LocalDateTime departure2 = arrival1.plusMinutes(5);
        LocalDateTime arrival2 = departure2.plusMinutes(25);
        Endpoint endpoint3 = new Endpoint(3, "Coimbra", 300);
        SegmentPassage passage2 = new SegmentPassage(
                testSegment, endpoint2, endpoint3,
                departure2, arrival2, 140, false
        );

        schedule.addPassage(passage1);
        schedule.addPassage(passage2);

        assertEquals(2, schedule.getPassages().size());
        assertEquals(arrival2, schedule.getEstimatedArrival());
        assertEquals(60, schedule.getTotalTravelTimeMinutes());
    }

    /**
     * Tests adding a crossing operation.
     *
     * Scenario:
     *  - Add a crossing with 15 minutes wait.
     *
     * Expected:
     *  - crossing is added to list;
     *  - total wait time is updated.
     *
     * @see TrainSchedule#addCrossingOperation(CrossingOperation)
     */
    @Test
    public void addCrossingOperation_updatesTotalWaitTime() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, endpoint1,
                CrossingOperation.CrossingType.AT_STATION,
                scheduledDeparture, scheduledDeparture.plusMinutes(15)
        );

        schedule.addCrossingOperation(crossing);

        assertEquals(1, schedule.getCrossingOperations().size());
        assertEquals(15, schedule.getTotalWaitTimeMinutes());
    }

    /**
     * Tests adding multiple crossing operations.
     *
     * Scenario:
     *  - Add two crossings with 15 and 10 minutes wait.
     *
     * Expected:
     *  - total wait time is the sum (25 minutes).
     *
     * @see TrainSchedule#addCrossingOperation(CrossingOperation)
     */
    @Test
    public void addCrossingOperation_multipleCrossings_cumulativeWait() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        CrossingOperation crossing1 = new CrossingOperation(
                1, 2, testSegment, endpoint1,
                CrossingOperation.CrossingType.AT_STATION,
                scheduledDeparture, scheduledDeparture.plusMinutes(15)
        );

        CrossingOperation crossing2 = new CrossingOperation(
                1, 3, testSegment, endpoint2,
                CrossingOperation.CrossingType.AT_SIDING,
                scheduledDeparture.plusMinutes(30), scheduledDeparture.plusMinutes(40)
        );

        schedule.addCrossingOperation(crossing1);
        schedule.addCrossingOperation(crossing2);

        assertEquals(2, schedule.getCrossingOperations().size());
        assertEquals(25, schedule.getTotalWaitTimeMinutes());
    }

    /**
     * Tests updateTotalTravelTime with no passages.
     *
     * Scenario:
     *  - Create empty schedule and call updateTotalTravelTime.
     *
     * Expected:
     *  - travel time remains zero;
     *  - estimated arrival equals scheduled departure.
     *
     * @see TrainSchedule#updateTotalTravelTime()
     */
    @Test
    public void updateTotalTravelTime_noPassages_resetsToZero() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        schedule.updateTotalTravelTime();

        assertEquals(0, schedule.getTotalTravelTimeMinutes());
        assertEquals(scheduledDeparture, schedule.getEstimatedArrival());
        assertEquals(scheduledDeparture, schedule.getActualDeparture());
    }

    /**
     * Tests updateTotalTravelTime after passages are added.
     *
     * Scenario:
     *  - Add passage, then manually modify times, then call update.
     *
     * Expected:
     *  - total travel time is recalculated correctly.
     *
     * @see TrainSchedule#updateTotalTravelTime()
     */
    @Test
    public void updateTotalTravelTime_withPassages_recalculates() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival = scheduledDeparture.plusMinutes(30);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);

        // Manually apply delay
        passage.applyDelay(15);

        // Recalculate
        schedule.updateTotalTravelTime();

        assertEquals(45, schedule.getTotalTravelTimeMinutes());
    }

    /**
     * Tests getEffectiveTravelTimeMinutes without wait time.
     *
     * Scenario:
     *  - Schedule with 60 minutes travel, no crossings.
     *
     * Expected:
     *  - effective time equals total time (60 minutes).
     *
     * @see TrainSchedule#getEffectiveTravelTimeMinutes()
     */
    @Test
    public void getEffectiveTravelTimeMinutes_noWait_equalsTotal() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival = scheduledDeparture.plusMinutes(60);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);

        assertEquals(60, schedule.getEffectiveTravelTimeMinutes());
    }

    /**
     * Tests getEffectiveTravelTimeMinutes with wait time.
     *
     * Scenario:
     *  - Schedule with 60 minutes total travel, 15 minutes wait.
     *
     * Expected:
     *  - effective time is 45 minutes (60 - 15).
     *
     * @see TrainSchedule#getEffectiveTravelTimeMinutes()
     */
    @Test
    public void getEffectiveTravelTimeMinutes_withWait_subtractsWait() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival = scheduledDeparture.plusMinutes(60);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, endpoint1,
                CrossingOperation.CrossingType.AT_STATION,
                scheduledDeparture, scheduledDeparture.plusMinutes(15)
        );

        schedule.addCrossingOperation(crossing);

        assertEquals(45, schedule.getEffectiveTravelTimeMinutes());
    }

    /**
     * Tests getActualDeparture when passages modify departure time.
     *
     * Scenario:
     *  - Add passage with delayed departure.
     *
     * Expected:
     *  - actual departure reflects the first passage's departure time.
     *
     * @see TrainSchedule#getActualDeparture()
     */
    @Test
    public void getActualDeparture_withDelayedPassage_returnsDelayedTime() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime delayedDeparture = scheduledDeparture.plusMinutes(10);
        LocalDateTime arrival = delayedDeparture.plusMinutes(30);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                delayedDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);
        schedule.updateTotalTravelTime();

        assertEquals(delayedDeparture, schedule.getActualDeparture());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a schedule with passages and verify toString format.
     *
     * Expected:
     *  - format includes train id, route, times, and crossing count.
     *
     * @see TrainSchedule#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival = scheduledDeparture.plusMinutes(60);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);

        String result = schedule.toString();

        assertTrue(result.contains("TRAIN SCHEDULE: 1"));
        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("60 min"));
    }

    /**
     * Tests getDetailedReport includes passages.
     *
     * Scenario:
     *  - Add passages and verify detailed report includes them.
     *
     * Expected:
     *  - report contains "SEGMENT PASSAGES" section.
     *
     * @see TrainSchedule#getDetailedReport()
     */
    @Test
    public void getDetailedReport_includesPassages() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        LocalDateTime arrival = scheduledDeparture.plusMinutes(30);
        SegmentPassage passage = new SegmentPassage(
                testSegment, endpoint1, endpoint2,
                scheduledDeparture, arrival, 140, false
        );

        schedule.addPassage(passage);

        String report = schedule.getDetailedReport();

        assertTrue(report.contains("SEGMENT PASSAGES"));
        assertTrue(report.contains("Lisbon"));
        assertTrue(report.contains("Porto"));
    }

    /**
     * Tests getDetailedReport includes crossing operations.
     *
     * Scenario:
     *  - Add crossings and verify detailed report includes them.
     *
     * Expected:
     *  - report contains "CROSSING OPERATIONS" section.
     *
     * @see TrainSchedule#getDetailedReport()
     */
    @Test
    public void getDetailedReport_includesCrossings() {
        TrainSchedule schedule = new TrainSchedule(testTrain, scheduledDeparture);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, testSegment, endpoint1,
                CrossingOperation.CrossingType.AT_STATION,
                scheduledDeparture, scheduledDeparture.plusMinutes(15)
        );

        schedule.addCrossingOperation(crossing);

        String report = schedule.getDetailedReport();

        assertTrue(report.contains("CROSSING OPERATIONS"));
    }
}