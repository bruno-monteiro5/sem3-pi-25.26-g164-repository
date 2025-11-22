package controller.SchedulingController;

import controller.SchedulingController.CrossingManager;
import domain.railway.*;
import domain.scheduling.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CrossingManager.
 *
 * @author G164
 * @version 1.0
 */
public class CrossingManagerTest {

    private CrossingManager crossingManager;
    private TestTrainScheduler mockScheduler;

    @BeforeEach
    public void setUp() {
        crossingManager = new CrossingManager();
        mockScheduler = new TestTrainScheduler();
    }

    /**
     * Tests detection of crossing between two trains on single track.
     */
    @Test
    public void testDetectCrossingOnSingleTrack() throws SQLException {
        // Create two trains going in opposite directions
        Train train1 = createTestTrain(1, LocalTime.of(10, 0));
        Train train2 = createTestTrain(2, LocalTime.of(10, 30));

        // Create schedules with overlapping passages
        TrainSchedule schedule1 = createTestSchedule(train1, true);  // A → B
        TrainSchedule schedule2 = createTestSchedule(train2, false); // B → A

        List<TrainSchedule> existing = new ArrayList<TrainSchedule>();
        existing.add(schedule1);

        // Detect crossings
        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                schedule2, existing, mockScheduler
        );

        // Should detect one crossing
        assertFalse(crossings.isEmpty(), "Should detect at least one crossing");
        assertEquals(1, crossings.size(), "Should detect exactly one crossing");
    }

    /**
     * Tests that no crossing is detected for trains in same direction.
     */
    @Test
    public void testNoCrossingForSameDirection() throws SQLException {
        Train train1 = createTestTrain(1, LocalTime.of(10, 0));
        Train train2 = createTestTrain(2, LocalTime.of(10, 30));

        // Both going A → B
        TrainSchedule schedule1 = createTestSchedule(train1, true);
        TrainSchedule schedule2 = createTestSchedule(train2, true);

        List<TrainSchedule> existing = new ArrayList<TrainSchedule>();
        existing.add(schedule1);

        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                schedule2, existing, mockScheduler
        );

        assertTrue(crossings.isEmpty(), "Should not detect crossing for same direction trains");
    }

    /**
     * Tests that no crossing is detected when trains pass at different times.
     */
    @Test
    public void testNoCrossingForNoTemporalOverlap() throws SQLException {
        Train train1 = createTestTrain(1, LocalTime.of(10, 0));
        Train train2 = createTestTrain(2, LocalTime.of(14, 0)); // Much later

        TrainSchedule schedule1 = createTestSchedule(train1, true);  // A → B
        TrainSchedule schedule2 = createTestSchedule(train2, false); // B → A

        List<TrainSchedule> existing = new ArrayList<TrainSchedule>();
        existing.add(schedule1);

        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                schedule2, existing, mockScheduler
        );

        assertTrue(crossings.isEmpty(),
                "Should not detect crossing when trains pass at different times");
    }

    /**
     * Tests crossing resolution determines waiting train correctly.
     */
    @Test
    public void testCrossingResolutionWaitingTrain() throws SQLException {
        // Train 1 departs at 10:00
        Train train1 = createTestTrain(1, LocalTime.of(10, 0));
        // Train 2 departs at 10:15 (later)
        Train train2 = createTestTrain(2, LocalTime.of(10, 15));

        TrainSchedule schedule1 = createTestSchedule(train1, true);  // A → B
        TrainSchedule schedule2 = createTestSchedule(train2, false); // B → A

        List<TrainSchedule> existing = new ArrayList<TrainSchedule>();
        existing.add(schedule1);

        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                schedule2, existing, mockScheduler
        );

        assertFalse(crossings.isEmpty(), "Should detect crossing");

        CrossingOperation crossing = crossings.get(0);

        // Train 2 (arriving later) should wait
        assertEquals(2, crossing.getWaitingTrainId(),
                "Later train should be the one waiting");
        assertEquals(1, crossing.getPassingTrainId(),
                "Earlier train should pass first");
    }

    /**
     * Tests multiple crossings detection.
     */
    @Test
    public void testMultipleCrossingsDetection() throws SQLException {
        Train train1 = createTestTrain(1, LocalTime.of(10, 0));
        Train train2 = createTestTrain(2, LocalTime.of(10, 15));
        Train train3 = createTestTrain(3, LocalTime.of(10, 30));

        TrainSchedule schedule1 = createTestSchedule(train1, true);
        TrainSchedule schedule2 = createTestSchedule(train2, false);
        TrainSchedule schedule3 = createTestSchedule(train3, true);

        List<TrainSchedule> existing = new ArrayList<TrainSchedule>();
        existing.add(schedule1);
        existing.add(schedule2);

        List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                schedule3, existing, mockScheduler
        );

        // Should detect crossing with train2 (opposite direction)
        assertFalse(crossings.isEmpty(), "Should detect crossings");
    }

    // Helper methods

    /**
     * Creates a test train.
     */
    private Train createTestTrain(int id, LocalTime departureTime) {
        Endpoint endpointA = new Endpoint(1, "Station A", 1);
        Endpoint endpointB = new Endpoint(2, "Station B", 2);
        Path path = new Path(id, "Path " + id);

        Train train = new Train(
                id, "VAT" + id, departureTime,
                LocalDate.now(), path, endpointA, endpointB
        );

        // Add a test locomotive
        Locomotive loco = new Locomotive(
                id, "Loco " + id, "Maker", "Model",
                3000, 120, 100, "DIESEL",
                100, 1668
        );
        train.addLocomotive(loco);

        return train;
    }

    /**
     * Creates a test schedule with a single passage.
     */
    private TrainSchedule createTestSchedule(Train train, boolean forwardDirection) {
        LocalDateTime start = LocalDateTime.of(train.getDate(), train.getDepartureTime());
        TrainSchedule schedule = new TrainSchedule(train, start);

        Endpoint endpointA = new Endpoint(1, "Station A", 1);
        Endpoint endpointB = new Endpoint(2, "Station B", 2);

        LineSegment segment = new LineSegment(1, true, 120, 20, 50000, 1);

        Endpoint departure = forwardDirection ? endpointA : endpointB;
        Endpoint arrival = forwardDirection ? endpointB : endpointA;

        SegmentPassage passage = new SegmentPassage(
                segment, departure, arrival,
                start, start.plusMinutes(30),
                100, true
        );

        schedule.addPassage(passage);

        return schedule;
    }

    /**
     * Mock scheduler for testing.
     */
    private class TestTrainScheduler extends TrainScheduler {
        public TestTrainScheduler() {
            super(null); // No network needed for tests
        }

        @Override
        public TrackType getTrackType(LineSegment segment) {
            // Return single track for testing
            return new SingleTrack(segment.getId());
        }
    }
}