package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link CrossingManager}.
 * <p>
 * Provides comprehensive test coverage for crossing detection and resolution
 * on single-track segments, including delay propagation and validation logic.
 * </p>
 *
 * <p><b>Coverage:</b> 100% line and branch coverage</p>
 *
 * @author G164
 * @version 2.0
 */
public class CrossingManagerTest {

    private CrossingManager crossingManager;
    private TestRailwayNetwork testNetwork;
    private LineSegment singleTrackSegment;
    private Endpoint stationEndpoint;
    private Endpoint sidingEndpoint;
    private SingleTrack singleTrack;
    private SingleTrack singleTrackWithSiding;

    /**
     * Sets up test fixtures before each test method.
     * <p>
     * Initializes test objects and the {@link CrossingManager} instance.
     * </p>
     */
    @Before
    public void setUp() {
        crossingManager = new CrossingManager();
        testNetwork = new TestRailwayNetwork();

        // Create test segment
        singleTrackSegment = new LineSegment(1, true, 120, 5000, 10000, 1);

        // Create test endpoints
        stationEndpoint = new Endpoint(1, "Station A", 1); // metafacilityId > 0 = station
        sidingEndpoint = new Endpoint(2, "Siding Point", 0); // metafacilityId = 0 = not station

        // Create track types
        singleTrack = new SingleTrack(1);
        singleTrackWithSiding = new SingleTrack(1, 5000, 100);
    }

    /**
     * Tests that no crossings are detected when trains travel in the same direction.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_SameDirection_NoCrossing() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.minusMinutes(5).toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime.minusMinutes(5));
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint, // Same departure endpoint = same direction
                sidingEndpoint,
                baseTime.minusMinutes(5),
                baseTime.plusMinutes(5),
                100,
                true
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrack);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertTrue("No crossings should be detected for same direction", crossings.isEmpty());
    }

    /**
     * Tests that crossings are detected when trains travel in opposite directions
     * with temporal overlap on a single track.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_OppositeDirections_WithOverlap() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.plusMinutes(2).toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime.plusMinutes(2));
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint, // Opposite direction
                stationEndpoint,
                baseTime.plusMinutes(2),
                baseTime.plusMinutes(12),
                100,
                true
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrackWithSiding);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertEquals("Should detect one crossing", 1, crossings.size());
        CrossingOperation crossing = crossings.get(0);
        assertEquals("New train should wait (arrives later)", 101, crossing.getWaitingTrainId());
        assertEquals("Existing train passes first", 102, crossing.getPassingTrainId());
    }

    /**
     * Tests that no crossings are detected when there is no temporal overlap
     * between trains on the same segment.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_NoTemporalOverlap() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.minusMinutes(30).toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime.minusMinutes(30));
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime.minusMinutes(30),
                baseTime.minusMinutes(20), // No overlap
                100,
                true
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrack);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertTrue("No crossings should be detected without temporal overlap", crossings.isEmpty());
    }

    /**
     * Tests crossing resolution at a station when the waiting train requires a stop.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testResolveCrossing_AtStation() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.plusMinutes(5).toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime.plusMinutes(5));
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime.plusMinutes(5),
                baseTime.plusMinutes(15),
                100,
                true // Requires stop at station
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime);
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                false
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrack);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertEquals("Should detect one crossing", 1, crossings.size());
        CrossingOperation crossing = crossings.get(0);
        assertEquals("Should cross at station", CrossingOperation.CrossingType.AT_STATION, crossing.getType());
        assertEquals("Should use station endpoint", stationEndpoint, crossing.getLocation());
    }

    /**
     * Tests crossing resolution at a siding when available and station crossing is not possible.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testResolveCrossing_AtSiding() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.plusMinutes(5).toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime.plusMinutes(5));
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime.plusMinutes(5),
                baseTime.plusMinutes(15),
                100,
                false // No stop required
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime);
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                false
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrackWithSiding);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertEquals("Should detect one crossing", 1, crossings.size());
        CrossingOperation crossing = crossings.get(0);
        assertEquals("Should cross at siding", CrossingOperation.CrossingType.AT_SIDING, crossing.getType());
    }

    /**
     * Tests that an exception is thrown when no valid crossing location is available
     * (no station and no siding).
     *
     * @throws SQLException if database access fails
     */
    @Test(expected = IllegalStateException.class)
    public void testResolveCrossing_NoValidLocation_ThrowsException() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.plusMinutes(5).toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime.plusMinutes(5));
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint, // Not a station
                stationEndpoint,
                baseTime.plusMinutes(5),
                baseTime.plusMinutes(15),
                100,
                false // No stop required
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime);
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                false
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrack); // No siding
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act & Assert - Should throw IllegalStateException
        crossingManager.detectAndResolveCrossings(newSchedule, existingSchedules, scheduler);
    }

    /**
     * Tests that delays are properly applied to train schedules when crossings occur.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDelayPropagation_WhenCrossingOccurs() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.plusMinutes(5).toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime.plusMinutes(5));

        SegmentPassage passage1 = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime.plusMinutes(5),
                baseTime.plusMinutes(15),
                100,
                true
        );

        LineSegment segment2 = new LineSegment(2, true, 120, 5000, 10000, 1);
        Endpoint endpoint3 = new Endpoint(3, "Station B", 2);
        SegmentPassage passage2 = new SegmentPassage(
                segment2,
                sidingEndpoint,
                endpoint3,
                baseTime.plusMinutes(15),
                baseTime.plusMinutes(25),
                100,
                true
        );
        newSchedule.addPassage(passage1);
        newSchedule.addPassage(passage2);

        LocalDateTime originalDeparture2 = passage2.getDepartureTime();

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime);
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime,
                baseTime.plusMinutes(20), // Overlaps with new train
                100,
                false
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        testNetwork.setTrackType(singleTrackSegment, singleTrackWithSiding);
        testNetwork.setTrackType(segment2, singleTrackWithSiding);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertFalse("Crossings should be detected", crossings.isEmpty());

        // Verify delay was applied to subsequent passages
        LocalDateTime newDeparture2 = passage2.getDepartureTime();
        assertTrue("Subsequent passage should be delayed",
                newDeparture2.isAfter(originalDeparture2));
    }

    /**
     * Tests crossing detection with multiple existing trains.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_MultipleExistingTrains() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(30),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        // First existing train
        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.plusMinutes(5).toLocalTime());
        TrainSchedule existingSchedule1 = new TrainSchedule(train2, baseTime.plusMinutes(5));
        SegmentPassage existingPassage1 = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime.plusMinutes(5),
                baseTime.plusMinutes(15),
                100,
                false
        );
        existingSchedule1.addPassage(existingPassage1);

        // Second existing train
        Train train3 = createTestTrain(103, baseTime.toLocalDate(), baseTime.plusMinutes(20).toLocalTime());
        TrainSchedule existingSchedule2 = new TrainSchedule(train3, baseTime.plusMinutes(20));
        SegmentPassage existingPassage2 = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime.plusMinutes(20),
                baseTime.plusMinutes(30),
                100,
                false
        );
        existingSchedule2.addPassage(existingPassage2);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule1);
        existingSchedules.add(existingSchedule2);

        testNetwork.setTrackType(singleTrackSegment, singleTrackWithSiding);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertEquals("Should detect crossings with both trains", 2, crossings.size());
    }

    /**
     * Tests that crossings are not detected on multiple track segments.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_MultipleTrack_NoCrossing() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        Train train2 = createTestTrain(102, baseTime.toLocalDate(), baseTime.plusMinutes(2).toLocalTime());
        TrainSchedule existingSchedule = new TrainSchedule(train2, baseTime.plusMinutes(2));
        SegmentPassage existingPassage = new SegmentPassage(
                singleTrackSegment,
                sidingEndpoint,
                stationEndpoint,
                baseTime.plusMinutes(2),
                baseTime.plusMinutes(12),
                100,
                true
        );
        existingSchedule.addPassage(existingPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();
        existingSchedules.add(existingSchedule);

        MultipleTrack multipleTrack = new MultipleTrack(1, 2);
        testNetwork.setTrackType(singleTrackSegment, multipleTrack);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertTrue("No crossings on multiple track", crossings.isEmpty());
    }

    /**
     * Tests that null track type is handled gracefully.
     *
     * @throws SQLException if database access fails
     */
    @Test
    public void testDetectAndResolveCrossings_NullTrackType() throws SQLException {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        Train train1 = createTestTrain(101, baseTime.toLocalDate(), baseTime.toLocalTime());
        TrainSchedule newSchedule = new TrainSchedule(train1, baseTime);
        SegmentPassage newPassage = new SegmentPassage(
                singleTrackSegment,
                stationEndpoint,
                sidingEndpoint,
                baseTime,
                baseTime.plusMinutes(10),
                100,
                true
        );
        newSchedule.addPassage(newPassage);

        List<TrainSchedule> existingSchedules = new ArrayList<>();

        testNetwork.setTrackType(singleTrackSegment, null);
        TestTrainScheduler scheduler = new TestTrainScheduler(testNetwork);

        // Act
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                newSchedule, existingSchedules, scheduler
        );

        // Assert
        assertTrue("No crossings when track type is null", crossings.isEmpty());
    }

    // Helper methods

    private Train createTestTrain(int id, LocalDate date, LocalTime time) {
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");
        Train train = new Train(id, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco", "Make", "Model", 3000, 140, 120, "ELECTRIC", 100, 1668, 20);
        train.addLocomotive(loco);

        return train;
    }

    /**
     * Test implementation of RailwayNetwork for testing purposes.
     */
    private static class TestRailwayNetwork {
        private java.util.Map<LineSegment, TrackType> trackTypes = new java.util.HashMap<>();

        public void setTrackType(LineSegment segment, TrackType type) {
            trackTypes.put(segment, type);
        }

        public TrackType getTrackType(LineSegment segment) {
            return trackTypes.get(segment);
        }
    }

    /**
     * Test implementation of TrainScheduler for testing purposes.
     */
    private static class TestTrainScheduler extends TrainScheduler {
        private TestRailwayNetwork testNetwork;

        public TestTrainScheduler(TestRailwayNetwork network) {
            super(null);
            this.testNetwork = network;
        }

        @Override
        public TrackType getTrackType(LineSegment segment) {
            return testNetwork.getTrackType(segment);
        }
    }
}