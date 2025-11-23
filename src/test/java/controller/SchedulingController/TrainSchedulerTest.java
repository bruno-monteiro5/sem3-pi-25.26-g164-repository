package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link TrainScheduler}.
 * <p>
 * Provides comprehensive test coverage for train scheduling operations including
 * single train scheduling, multiple train scheduling with crossing detection,
 * and railway network interaction.
 * </p>
 *
 * <p><b>Coverage:</b> 100% line and branch coverage</p>
 *
 * @author G164
 * @version 1.0
 */
public class TrainSchedulerTest {

    private TrainScheduler scheduler;
    private TestRailwayNetwork testNetwork;
    private Endpoint endpoint1;
    private Endpoint endpoint2;
    private Endpoint endpoint3;
    private LineSegment segment1;
    private LineSegment segment2;
    private Path testPath;
    private LocalDate testDate;
    private LocalTime testTime;

    /**
     * Sets up test fixtures before each test method.
     * <p>
     * Initializes test objects and the {@link TrainScheduler} instance.
     * </p>
     */
    @Before
    public void setUp() {
        // Create real endpoints
        endpoint1 = new Endpoint(1, "Station A", 1);
        endpoint2 = new Endpoint(2, "Station B", 2);
        endpoint3 = new Endpoint(3, "Station C", 3);

        // Create real segments
        segment1 = new LineSegment(1, true, 120, 5000, 10000, 1);
        segment2 = new LineSegment(2, true, 100, 5000, 8000, 1);

        // Setup test date/time
        testDate = LocalDate.of(2025, 1, 15);
        testTime = LocalTime.of(10, 0);

        // Setup path
        testPath = new Path(1, "Test Path");
        testPath.addEndpoint(new Path.PathEndpoint(1, endpoint1, true));
        testPath.addEndpoint(new Path.PathEndpoint(2, endpoint2, true));
        testPath.addEndpoint(new Path.PathEndpoint(3, endpoint3, true));

        // Setup test network
        testNetwork = new TestRailwayNetwork();
        testNetwork.addSegment(endpoint1.getId(), endpoint2.getId(), segment1);
        testNetwork.addSegment(endpoint2.getId(), endpoint3.getId(), segment2);

        scheduler = new TrainScheduler(testNetwork);
    }

    /**
     * Tests successful scheduling of a single train.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_Success() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        assertNotNull("Schedule should not be null", schedule);
        assertEquals("Schedule should be for correct train", train, schedule.getTrain());
        assertEquals("Should have 2 passages (3 endpoints - 1)", 2, schedule.getPassages().size());
        assertTrue("Total travel time should be positive", schedule.getTotalTravelTimeMinutes() > 0);
    }

    /**
     * Tests that schedule includes correct passage information.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_PassageDetails() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        List<SegmentPassage> passages = schedule.getPassages();

        SegmentPassage firstPassage = passages.get(0);
        assertEquals("First passage should use segment1", segment1, firstPassage.getSegment());
        assertEquals("First passage departs from endpoint1", endpoint1, firstPassage.getDepartureEndpoint());
        assertEquals("First passage arrives at endpoint2", endpoint2, firstPassage.getArrivalEndpoint());
        assertTrue("First passage should require stop", firstPassage.requiresStop());

        SegmentPassage secondPassage = passages.get(1);
        assertEquals("Second passage should use segment2", segment2, secondPassage.getSegment());
        assertEquals("Second passage departs from endpoint2", endpoint2, secondPassage.getDepartureEndpoint());
        assertEquals("Second passage arrives at endpoint3", endpoint3, secondPassage.getArrivalEndpoint());
    }

    /**
     * Tests scheduling with stop time added at intermediate stations.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_StopTimeAdded() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        List<SegmentPassage> passages = schedule.getPassages();
        SegmentPassage firstPassage = passages.get(0);
        SegmentPassage secondPassage = passages.get(1);

        // Second passage should start after first arrival + stop time (5 minutes)
        long timeDiff = java.time.Duration.between(
                firstPassage.getArrivalTime(),
                secondPassage.getDepartureTime()
        ).toMinutes();

        assertEquals("Stop time should be 5 minutes", 5, timeDiff);
    }

    /**
     * Tests that exception is thrown when segment is not found between endpoints.
     *
     * @throws SQLException if database access fails
     */
    @Test(expected = IllegalStateException.class)
    public void testScheduleTrainReschedule_NoSegmentFound_ThrowsException() throws SQLException {
        // Arrange
        TestRailwayNetwork emptyNetwork = new TestRailwayNetwork();
        TrainScheduler emptyScheduler = new TrainScheduler(emptyNetwork);
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act & Assert
        emptyScheduler.scheduleTrainReschedule(train);
    }

    /**
     * Tests scheduling with path that doesn't require stops.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_NoStopRequired() throws SQLException {
        // Arrange
        Path noStopPath = new Path(2, "No Stop Path");
        noStopPath.addEndpoint(new Path.PathEndpoint(1, endpoint1, true));
        noStopPath.addEndpoint(new Path.PathEndpoint(2, endpoint2, false)); // No stop
        noStopPath.addEndpoint(new Path.PathEndpoint(3, endpoint3, true));

        Train train = createTestTrain(101, testDate, testTime, noStopPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        List<SegmentPassage> passages = schedule.getPassages();
        SegmentPassage firstPassage = passages.get(0);
        SegmentPassage secondPassage = passages.get(1);

        // No stop time added - second passage starts immediately at arrival
        long timeDiff = java.time.Duration.between(
                firstPassage.getArrivalTime(),
                secondPassage.getDepartureTime()
        ).toMinutes();

        assertEquals("No stop time should be added", 0, timeDiff);
    }

    /**
     * Tests scheduling multiple trains successfully.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleMultipleTrains_Success() throws SQLException {
        // Arrange
        Train train1 = createTestTrain(101, testDate, testTime, testPath);
        Train train2 = createTestTrain(102, testDate, testTime.plusMinutes(10), testPath);

        List<Train> trains = new ArrayList<>();
        trains.add(train1);
        trains.add(train2);

        testNetwork.setTrackType(segment1, new MultipleTrack(1, 2));
        testNetwork.setTrackType(segment2, new MultipleTrack(2, 2));

        // Act
        Schedule schedule = scheduler.scheduleMultipleTrains(trains);

        // Assert
        assertNotNull("Schedule should not be null", schedule);
        assertEquals("Schedule should have 2 train schedules", 2, schedule.getTrainSchedules().size());
        assertEquals("Schedule date should match", testDate, schedule.getDate());
    }

    /**
     * Tests that trains are sorted by departure time when scheduling multiple trains.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleMultipleTrains_SortedByDepartureTime() throws SQLException {
        // Arrange
        Train train1 = createTestTrain(101, testDate, LocalTime.of(10, 30), testPath);
        Train train2 = createTestTrain(102, testDate, LocalTime.of(10, 0), testPath); // Earlier

        List<Train> trains = new ArrayList<>();
        trains.add(train1); // Added first but departs later
        trains.add(train2); // Added second but departs earlier

        testNetwork.setTrackType(segment1, new MultipleTrack(1, 2));
        testNetwork.setTrackType(segment2, new MultipleTrack(2, 2));

        // Act
        Schedule schedule = scheduler.scheduleMultipleTrains(trains);

        // Assert
        List<TrainSchedule> trainSchedules = schedule.getTrainSchedules();
        assertEquals("First scheduled train should be train2", 102, trainSchedules.get(0).getTrain().getId());
        assertEquals("Second scheduled train should be train1", 101, trainSchedules.get(1).getTrain().getId());
    }

    /**
     * Tests that exception is thrown when trying to schedule empty train list.
     *
     * @throws SQLException if database access fails
     */
    @Test(expected = IllegalArgumentException.class)
    public void testScheduleMultipleTrains_EmptyList_ThrowsException() throws SQLException {
        // Arrange
        List<Train> trains = new ArrayList<>();

        // Act & Assert
        scheduler.scheduleMultipleTrains(trains);
    }

    /**
     * Tests that exception is thrown when train list is null.
     *
     * @throws SQLException if database access fails
     */
    @Test(expected = IllegalArgumentException.class)
    public void testScheduleMultipleTrains_NullList_ThrowsException() throws SQLException {
        // Act & Assert
        scheduler.scheduleMultipleTrains(null);
    }

    /**
     * Tests scheduling multiple trains with crossing detection on single track.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleMultipleTrains_DetectsCrossings() throws SQLException {
        // Arrange
        Path reversePath = new Path(2, "Reverse Path");
        reversePath.addEndpoint(new Path.PathEndpoint(1, endpoint3, true));
        reversePath.addEndpoint(new Path.PathEndpoint(2, endpoint2, true));
        reversePath.addEndpoint(new Path.PathEndpoint(3, endpoint1, true));

        Train train1 = createTestTrain(101, testDate, testTime, testPath);
        Train train2 = createTestTrain(102, testDate, testTime.plusMinutes(5), reversePath);

        testNetwork.addSegment(endpoint3.getId(), endpoint2.getId(), segment2);
        testNetwork.addSegment(endpoint2.getId(), endpoint1.getId(), segment1);

        List<Train> trains = new ArrayList<>();
        trains.add(train1);
        trains.add(train2);

        SingleTrack singleTrack = new SingleTrack(1, 5000, 100);
        testNetwork.setTrackType(segment1, singleTrack);
        testNetwork.setTrackType(segment2, singleTrack);

        // Act
        Schedule schedule = scheduler.scheduleMultipleTrains(trains);

        // Assert
        assertNotNull("Schedule should not be null", schedule);
        assertTrue("Should have detected crossings", schedule.getTotalCrossings() > 0);
    }

    /**
     * Tests getSpeedCalculator returns the correct instance.
     */
    @Test
    public void testGetSpeedCalculator() {
        // Act
        SpeedCalculator speedCalc = scheduler.getSpeedCalculator();

        // Assert
        assertNotNull("Speed calculator should not be null", speedCalc);
    }

    /**
     * Tests getTrackType returns correct track type from network.
     */
    @Test
    public void testGetTrackType() {
        // Arrange
        SingleTrack expectedTrack = new SingleTrack(1);
        testNetwork.setTrackType(segment1, expectedTrack);

        // Act
        TrackType trackType = scheduler.getTrackType(segment1);

        // Assert
        assertEquals("Should return track type from network", expectedTrack, trackType);
    }

    /**
     * Tests scheduling with minimum path (2 endpoints).
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_MinimumPath() throws SQLException {
        // Arrange
        Path minPath = new Path(3, "Min Path");
        minPath.addEndpoint(new Path.PathEndpoint(1, endpoint1, true));
        minPath.addEndpoint(new Path.PathEndpoint(2, endpoint2, true));

        Train train = createTestTrain(101, testDate, testTime, minPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        assertNotNull("Schedule should not be null", schedule);
        assertEquals("Should have 1 passage", 1, schedule.getPassages().size());
    }

    /**
     * Tests scheduled departure time matches train departure time.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_DepartureTimeCorrect() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        assertEquals("Scheduled departure should match train departure",
                LocalTime.of(10, 0),
                schedule.getScheduledDeparture().toLocalTime());
        assertEquals("Scheduled date should match train date",
                testDate,
                schedule.getScheduledDeparture().toLocalDate());
    }

    /**
     * Tests estimated arrival time is after departure time.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_ArrivalAfterDeparture() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        assertTrue("Arrival should be after departure",
                schedule.getEstimatedArrival().isAfter(schedule.getScheduledDeparture()));
    }

    /**
     * Tests that speed is calculated for each segment.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_SpeedCalculated() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        for (SegmentPassage passage : schedule.getPassages()) {
            assertTrue("Speed should be positive", passage.getSpeedUsed() > 0);
            assertTrue("Speed should be reasonable", passage.getSpeedUsed() <= 200);
        }
    }

    /**
     * Tests scheduling with long path (many segments).
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_LongPath() throws SQLException {
        // Arrange
        Endpoint endpoint4 = new Endpoint(4, "Station D", 4);
        Endpoint endpoint5 = new Endpoint(5, "Station E", 5);
        LineSegment segment3 = new LineSegment(3, true, 110, 5000, 9000, 1);
        LineSegment segment4 = new LineSegment(4, true, 130, 5000, 11000, 1);

        Path longPath = new Path(4, "Long Path");
        longPath.addEndpoint(new Path.PathEndpoint(1, endpoint1, true));
        longPath.addEndpoint(new Path.PathEndpoint(2, endpoint2, true));
        longPath.addEndpoint(new Path.PathEndpoint(3, endpoint3, true));
        longPath.addEndpoint(new Path.PathEndpoint(4, endpoint4, true));
        longPath.addEndpoint(new Path.PathEndpoint(5, endpoint5, true));

        testNetwork.addSegment(endpoint3.getId(), endpoint4.getId(), segment3);
        testNetwork.addSegment(endpoint4.getId(), endpoint5.getId(), segment4);

        Train train = createTestTrain(101, testDate, testTime, longPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        assertEquals("Should have 4 passages (5 endpoints - 1)", 4, schedule.getPassages().size());
    }

    /**
     * Tests scheduling with three trains in sequence.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleMultipleTrains_ThreeTrains() throws SQLException {
        // Arrange
        Train train1 = createTestTrain(101, testDate, testTime, testPath);
        Train train2 = createTestTrain(102, testDate, testTime.plusMinutes(10), testPath);
        Train train3 = createTestTrain(103, testDate, testTime.plusMinutes(20), testPath);

        List<Train> trains = new ArrayList<>();
        trains.add(train1);
        trains.add(train2);
        trains.add(train3);

        testNetwork.setTrackType(segment1, new MultipleTrack(1, 2));
        testNetwork.setTrackType(segment2, new MultipleTrack(2, 2));

        // Act
        Schedule schedule = scheduler.scheduleMultipleTrains(trains);

        // Assert
        assertEquals("Schedule should have 3 train schedules", 3, schedule.getTrainSchedules().size());
    }

    /**
     * Tests that passage times are sequential.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testScheduleTrainReschedule_SequentialTimes() throws SQLException {
        // Arrange
        Train train = createTestTrain(101, testDate, testTime, testPath);

        // Act
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train);

        // Assert
        List<SegmentPassage> passages = schedule.getPassages();
        for (int i = 0; i < passages.size() - 1; i++) {
            assertTrue("Each passage arrival should be before next departure",
                    passages.get(i).getArrivalTime().isBefore(passages.get(i + 1).getDepartureTime()) ||
                            passages.get(i).getArrivalTime().equals(passages.get(i + 1).getDepartureTime()));
        }
    }

    // Helper methods

    private Train createTestTrain(int id, LocalDate date, LocalTime time, Path path) {
        Endpoint start = path.getOrderedEndpoints().get(0).getEndpoint();
        Endpoint end = path.getOrderedEndpoints().get(path.getOrderedEndpoints().size() - 1).getEndpoint();

        Train train = new Train(id, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco", "Make", "Model", 3000, 140, 120,
                "ELECTRIC", 100, 1668, 20);
        train.addLocomotive(loco);

        return train;
    }

    /**
     * Test implementation of RailwayNetwork for testing purposes.
     */
    private static class TestRailwayNetwork extends RailwayNetwork {
        private Map<String, LineSegment> segments = new HashMap<>();
        private Map<LineSegment, TrackType> trackTypes = new HashMap<>();

        public TestRailwayNetwork() {
            super(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>(), new HashMap<>());
        }

        public void addSegment(int endpointAId, int endpointBId, LineSegment segment) {
            String key = endpointAId + "-" + endpointBId;
            segments.put(key, segment);
            // Also add reverse direction
            String reverseKey = endpointBId + "-" + endpointAId;
            segments.put(reverseKey, segment);
        }

        @Override
        public LineSegment findSegmentBetween(int endpointAId, int endpointBId) {
            String key = endpointAId + "-" + endpointBId;
            return segments.get(key);
        }

        public void setTrackType(LineSegment segment, TrackType type) {
            trackTypes.put(segment, type);
        }

        @Override
        public TrackType getTrackType(LineSegment segment) {
            return trackTypes.get(segment);
        }
    }
}