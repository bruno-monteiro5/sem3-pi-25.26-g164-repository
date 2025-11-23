package domain.scheduling;

import domain.railway.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link Schedule}.
 * <p>
 * Each test method targets exactly one public member of {@code Schedule}
 * and documents the scenario, preconditions and expected outcome.
 */
public class ScheduleTest {

    private LocalDate testDate;
    private Train train1;
    private Train train2;
    private TrainSchedule schedule1;
    private TrainSchedule schedule2;

    @Before
    public void setUp() {
        testDate = LocalDate.of(2025, 1, 15);

        Endpoint endpoint1 = new Endpoint(1, "Lisbon", 100);
        Endpoint endpoint2 = new Endpoint(2, "Porto", 200);
        Path path = new Path(1, "North Line");

        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(11, 0);

        train1 = new Train(1, "VAT123", time1, testDate, path, endpoint1, endpoint2);
        train2 = new Train(2, "VAT456", time2, testDate, path, endpoint1, endpoint2);

        schedule1 = new TrainSchedule(train1, LocalDateTime.of(testDate, time1));
        schedule2 = new TrainSchedule(train2, LocalDateTime.of(testDate, time2));
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid date supplied.
     *
     * Expected:
     *  - {@link Schedule#getDate()} returns the supplied date;
     *  - {@link Schedule#getTrainSchedules()} returns an empty list.
     *
     * @see Schedule#Schedule(LocalDate)
     */
    @Test
    public void constructor_initializesAllFields() {
        Schedule schedule = new Schedule(testDate);

        assertEquals(testDate, schedule.getDate());
        assertTrue(schedule.getTrainSchedules().isEmpty());
    }

    /**
     * Tests the getter for date.
     *
     * Scenario:
     *  - Create a schedule with a known date and read it back.
     *
     * Expected:
     *  - returned date matches the constructor value.
     *
     * @see Schedule#getDate()
     */
    @Test
    public void getDate_returnsCorrectDate() {
        LocalDate specificDate = LocalDate.of(2025, 12, 25);
        Schedule schedule = new Schedule(specificDate);

        assertEquals(specificDate, schedule.getDate());
    }

    /**
     * Tests getTrainSchedules returns initially empty list.
     *
     * Scenario:
     *  - Create a new schedule without adding train schedules.
     *
     * Expected:
     *  - returns an empty list.
     *
     * @see Schedule#getTrainSchedules()
     */
    @Test
    public void getTrainSchedules_initiallyEmpty() {
        Schedule schedule = new Schedule(testDate);
        assertTrue(schedule.getTrainSchedules().isEmpty());
    }

    /**
     * Tests adding a single train schedule.
     *
     * Scenario:
     *  - Create a schedule and add one train schedule.
     *
     * Expected:
     *  - train schedules list contains the added schedule.
     *
     * @see Schedule#addTrainSchedule(TrainSchedule)
     */
    @Test
    public void addTrainSchedule_singleSchedule_addsToList() {
        Schedule schedule = new Schedule(testDate);

        schedule.addTrainSchedule(schedule1);

        assertEquals(1, schedule.getTrainSchedules().size());
        assertTrue(schedule.getTrainSchedules().contains(schedule1));
    }

    /**
     * Tests adding multiple train schedules.
     *
     * Scenario:
     *  - Create a schedule and add two train schedules.
     *
     * Expected:
     *  - train schedules list contains both schedules in order.
     *
     * @see Schedule#addTrainSchedule(TrainSchedule)
     */
    @Test
    public void addTrainSchedule_multipleSchedules_addsInOrder() {
        Schedule schedule = new Schedule(testDate);

        schedule.addTrainSchedule(schedule1);
        schedule.addTrainSchedule(schedule2);

        assertEquals(2, schedule.getTrainSchedules().size());
        assertEquals(schedule1, schedule.getTrainSchedules().get(0));
        assertEquals(schedule2, schedule.getTrainSchedules().get(1));
    }

    /**
     * Tests getSchedulesByDeparture returns schedules sorted by departure time.
     *
     * Scenario:
     *  - Add schedules in reverse chronological order.
     *
     * Expected:
     *  - returns schedules sorted by departure time (earliest first).
     *
     * @see Schedule#getSchedulesByDeparture()
     */
    @Test
    public void getSchedulesByDeparture_returnsSortedByTime() {
        Schedule schedule = new Schedule(testDate);

        // Add in reverse order (later departure first)
        schedule.addTrainSchedule(schedule2);  // 11:00
        schedule.addTrainSchedule(schedule1);  // 10:00

        List<TrainSchedule> sorted = schedule.getSchedulesByDeparture();

        assertEquals(2, sorted.size());
        assertEquals(schedule1, sorted.get(0));  // 10:00 should be first
        assertEquals(schedule2, sorted.get(1));  // 11:00 should be second
    }

    /**
     * Tests getSchedulesByDeparture with single schedule.
     *
     * Scenario:
     *  - Add one train schedule and get sorted list.
     *
     * Expected:
     *  - returns list with single schedule.
     *
     * @see Schedule#getSchedulesByDeparture()
     */
    @Test
    public void getSchedulesByDeparture_singleSchedule_returnsSame() {
        Schedule schedule = new Schedule(testDate);
        schedule.addTrainSchedule(schedule1);

        List<TrainSchedule> sorted = schedule.getSchedulesByDeparture();

        assertEquals(1, sorted.size());
        assertEquals(schedule1, sorted.get(0));
    }

    /**
     * Tests getTotalCrossings with no crossings.
     *
     * Scenario:
     *  - Create schedule with train schedules but no crossing operations.
     *
     * Expected:
     *  - returns 0.
     *
     * @see Schedule#getTotalCrossings()
     */
    @Test
    public void getTotalCrossings_noCrossings_returnsZero() {
        Schedule schedule = new Schedule(testDate);
        schedule.addTrainSchedule(schedule1);
        schedule.addTrainSchedule(schedule2);

        assertEquals(0, schedule.getTotalCrossings());
    }

    /**
     * Tests getTotalCrossings with crossing operations.
     *
     * Scenario:
     *  - Add train schedules with crossing operations.
     *
     * Expected:
     *  - returns the sum of all crossings across all train schedules.
     *
     * @see Schedule#getTotalCrossings()
     */
    @Test
    public void getTotalCrossings_withCrossings_countsTotal() {
        Schedule schedule = new Schedule(testDate);

        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 10);
        Endpoint location = new Endpoint(3, "Coimbra", 300);

        CrossingOperation crossing1 = new CrossingOperation(
                1, 2, segment, location,
                CrossingOperation.CrossingType.AT_STATION,
                LocalDateTime.of(testDate, LocalTime.of(10, 30)),
                LocalDateTime.of(testDate, LocalTime.of(10, 45))
        );

        CrossingOperation crossing2 = new CrossingOperation(
                2, 1, segment, location,
                CrossingOperation.CrossingType.AT_SIDING,
                LocalDateTime.of(testDate, LocalTime.of(11, 15)),
                LocalDateTime.of(testDate, LocalTime.of(11, 25))
        );

        schedule1.addCrossingOperation(crossing1);
        schedule2.addCrossingOperation(crossing2);

        schedule.addTrainSchedule(schedule1);
        schedule.addTrainSchedule(schedule2);

        assertEquals(2, schedule.getTotalCrossings());
    }

    /**
     * Tests getTotalCrossings with multiple crossings in one schedule.
     *
     * Scenario:
     *  - One train schedule has 2 crossings, another has 1.
     *
     * Expected:
     *  - returns 3.
     *
     * @see Schedule#getTotalCrossings()
     */
    @Test
    public void getTotalCrossings_multipleCrossingsPerSchedule_countsAll() {
        Schedule schedule = new Schedule(testDate);

        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 10);
        Endpoint location = new Endpoint(3, "Coimbra", 300);

        CrossingOperation crossing1 = new CrossingOperation(
                1, 2, segment, location,
                CrossingOperation.CrossingType.AT_STATION,
                LocalDateTime.of(testDate, LocalTime.of(10, 30)),
                LocalDateTime.of(testDate, LocalTime.of(10, 45))
        );

        CrossingOperation crossing2 = new CrossingOperation(
                1, 3, segment, location,
                CrossingOperation.CrossingType.AT_SIDING,
                LocalDateTime.of(testDate, LocalTime.of(11, 0)),
                LocalDateTime.of(testDate, LocalTime.of(11, 10))
        );

        CrossingOperation crossing3 = new CrossingOperation(
                2, 1, segment, location,
                CrossingOperation.CrossingType.AT_STATION,
                LocalDateTime.of(testDate, LocalTime.of(11, 30)),
                LocalDateTime.of(testDate, LocalTime.of(11, 40))
        );

        schedule1.addCrossingOperation(crossing1);
        schedule1.addCrossingOperation(crossing2);
        schedule2.addCrossingOperation(crossing3);

        schedule.addTrainSchedule(schedule1);
        schedule.addTrainSchedule(schedule2);

        assertEquals(3, schedule.getTotalCrossings());
    }

    /**
     * Tests toString method with no train schedules.
     *
     * Scenario:
     *  - Create an empty schedule and verify toString format.
     *
     * Expected:
     *  - format includes date, 0 trains, and 0 crossings.
     *
     * @see Schedule#toString()
     */
    @Test
    public void toString_emptySchedule_formatsCorrectly() {
        Schedule schedule = new Schedule(testDate);

        String result = schedule.toString();

        assertTrue(result.contains("2025-01-15"));
        assertTrue(result.contains("Total Trains: 0"));
        assertTrue(result.contains("Total Crossings: 0"));
    }

    /**
     * Tests toString method with train schedules.
     *
     * Scenario:
     *  - Create a schedule with two trains and verify toString format.
     *
     * Expected:
     *  - format includes date, train count, and train details.
     *
     * @see Schedule#toString()
     */
    @Test
    public void toString_withSchedules_formatsCorrectly() {
        Schedule schedule = new Schedule(testDate);
        schedule.addTrainSchedule(schedule1);
        schedule.addTrainSchedule(schedule2);

        String result = schedule.toString();

        assertTrue(result.contains("2025-01-15"));
        assertTrue(result.contains("Total Trains: 2"));
        assertTrue(result.contains("Train 1"));
        assertTrue(result.contains("Train 2"));
        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
    }

    /**
     * Tests toString includes crossing count.
     *
     * Scenario:
     *  - Create schedule with crossings and verify toString shows count.
     *
     * Expected:
     *  - format includes correct crossing count.
     *
     * @see Schedule#toString()
     */
    @Test
    public void toString_withCrossings_includesCrossingCount() {
        Schedule schedule = new Schedule(testDate);

        LineSegment segment = new LineSegment(1, true, 160, 20, 50000, 10);
        Endpoint location = new Endpoint(3, "Coimbra", 300);

        CrossingOperation crossing = new CrossingOperation(
                1, 2, segment, location,
                CrossingOperation.CrossingType.AT_STATION,
                LocalDateTime.of(testDate, LocalTime.of(10, 30)),
                LocalDateTime.of(testDate, LocalTime.of(10, 45))
        );

        schedule1.addCrossingOperation(crossing);
        schedule.addTrainSchedule(schedule1);

        String result = schedule.toString();

        assertTrue(result.contains("Total Crossings: 1"));
    }
}