package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.Schedule;
import domain.scheduling.SegmentPassage;
import domain.scheduling.TrainSchedule;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for TrainScheduler (JUnit4).
 */
public class TrainSchedulerTest {

    private RailwayNetwork network;
    private TrainScheduler scheduler;

    private Endpoint endpointA;
    private Endpoint endpointB;
    private Endpoint endpointC;

    private LineSegment segmentAB;
    private LineSegment segmentBC;

    private Path pathABC;
    private Train train1;
    private Train train2;

    @Before
    public void setUp() {
        endpointA = new Endpoint(1, "Station A", 1);
        endpointB = new Endpoint(2, "Station B", 2);
        endpointC = new Endpoint(3, "Station C", 3);

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        endpoints.add(endpointA);
        endpoints.add(endpointB);
        endpoints.add(endpointC);

        Line lineAB = new Line(
                1,
                "Line AB",
                "Infraestruturas",
                endpointA.getId(),
                endpointB.getId(),
                1668,
                endpointA.getName(),
                endpointB.getName()
        );

        Line lineBC = new Line(
                2,
                "Line BC",
                "Infraestruturas",
                endpointB.getId(),
                endpointC.getId(),
                1668,
                endpointB.getName(),
                endpointC.getName()
        );

        List<Line> lines = new ArrayList<Line>();
        lines.add(lineAB);
        lines.add(lineBC);

        segmentAB = new LineSegment(1, true, 160, 20, 50000, lineAB.getId());
        segmentBC = new LineSegment(2, true, 160, 20, 50000, lineBC.getId());

        List<LineSegment> segments = new ArrayList<LineSegment>();
        segments.add(segmentAB);
        segments.add(segmentBC);

        List<Path> paths = new ArrayList<Path>();
        List<Train> trains = new ArrayList<Train>();

        network = buildNetworkForTests(endpoints, lines, segments, paths, trains);

        scheduler = new TrainScheduler(network);

        pathABC = new Path(1, "Path ABC");

        Path.PathEndpoint peA = new Path.PathEndpoint(1, endpointA, true);   // must stop (start)
        Path.PathEndpoint peB = new Path.PathEndpoint(2, endpointB, true);   // must stop at B
        Path.PathEndpoint peC = new Path.PathEndpoint(3, endpointC, false);  // no stop at C

        pathABC.addEndpoint(peA);
        pathABC.addEndpoint(peB);
        pathABC.addEndpoint(peC);

        LocalDate today = LocalDate.now();

        train1 = new Train(
                1,
                "VAT001",
                LocalTime.of(10, 0),
                today,
                pathABC,
                endpointA,
                endpointC
        );

        train2 = new Train(
                2,
                "VAT002",
                LocalTime.of(11, 0),
                today,
                pathABC,
                endpointA,
                endpointC
        );

        Locomotive loco1 = new Locomotive(
                1, "Loco1", "Maker", "Model",
                3000, 120, 140, "DIESEL",
                100, 1668
        );
        train1.addLocomotive(loco1);

        Locomotive loco2 = new Locomotive(
                2, "Loco2", "Maker", "Model",
                3000, 120, 140, "DIESEL",
                100, 1668
        );
        train2.addLocomotive(loco2);
    }

    /**
     * Builds a RailwayNetwork instance for tests using reflection,
     * bypassing the database-based factory method.
     */
    private RailwayNetwork buildNetworkForTests(
            List<Endpoint> endpoints,
            List<Line> lines,
            List<LineSegment> segments,
            List<Path> paths,
            List<Train> trains
    ) {
        Map<Integer, TrackType> trackTypeBySegment = new HashMap<Integer, TrackType>();
        for (int i = 0; i < segments.size(); i++) {
            LineSegment seg = segments.get(i);
            trackTypeBySegment.put(seg.getId(), new SingleTrack(seg.getId()));
        }

        try {
            java.lang.reflect.Constructor<RailwayNetwork> ctor =
                    RailwayNetwork.class.getDeclaredConstructor(
                            List.class, List.class, List.class, List.class, List.class, Map.class
                    );
            ctor.setAccessible(true);
            return ctor.newInstance(
                    endpoints, lines, segments, paths, trains, trackTypeBySegment
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RailwayNetwork for tests", e);
        }
    }

    /**
     * Tests basic schedule generation for a single train along a multi-segment path.
     */
    @Test
    public void testScheduleTrainReschedule_basic() throws SQLException {
        TrainSchedule schedule = scheduler.scheduleTrainReschedule(train1);

        assertNotNull("Schedule should not be null", schedule);
        assertEquals("Train should match", train1, schedule.getTrain());

        // Path A -> B -> C => 2 segments
        List<SegmentPassage> passages = schedule.getPassages();
        assertEquals("There should be two segment passages", 2, passages.size());

        SegmentPassage first = passages.get(0);
        SegmentPassage second = passages.get(1);

        // Check endpoints sequence
        assertEquals(endpointA, first.getDepartureEndpoint());
        assertEquals(endpointB, first.getArrivalEndpoint());
        assertEquals(endpointB, second.getDepartureEndpoint());
        assertEquals(endpointC, second.getArrivalEndpoint());

        // Check chronological order
        assertTrue(first.getDepartureTime().isBefore(first.getArrivalTime()));
        assertTrue(first.getArrivalTime().isBefore(second.getDepartureTime()));
        assertTrue(second.getDepartureTime().isBefore(second.getArrivalTime()));
    }

    /**
     * Tests that scheduleTrainReschedule throws when there is no segment between endpoints.
     */
    @Test(expected = IllegalStateException.class)
    public void testScheduleTrainReschedule_noSegmentThrows() throws SQLException {
        // Path with endpoints that are not connected in the network
        Endpoint endpointX = new Endpoint(10, "Isolated X", 10);
        Endpoint endpointY = new Endpoint(11, "Isolated Y", 11);

        Path brokenPath = new Path(99, "Broken Path");
        brokenPath.addEndpoint(new Path.PathEndpoint(1, endpointX, true));
        brokenPath.addEndpoint(new Path.PathEndpoint(2, endpointY, false));

        Train badTrain = new Train(
                99,
                "VAT999",
                LocalTime.of(12, 0),
                LocalDate.now(),
                brokenPath,
                endpointX,
                endpointY
        );

        // Add locomotive so SpeedCalculator has some data
        Locomotive loco = new Locomotive(
                99, "BadLoco", "Maker", "Model",
                3000, 120, 140, "DIESEL",
                100, 1668
        );
        badTrain.addLocomotive(loco);

        // This should throw an IllegalStateException because no segment connects X and Y
        scheduler.scheduleTrainReschedule(badTrain);
    }

    /**
     * Tests scheduleMultipleTrains with two trains on the same day.
     */
    @Test
    public void testScheduleMultipleTrains() throws SQLException {
        List<Train> trains = new ArrayList<Train>();
        trains.add(train2); // later departure
        trains.add(train1); // earlier departure

        Schedule schedule = scheduler.scheduleMultipleTrains(trains);

        assertNotNull("Schedule should not be null", schedule);

        // If your Schedule class exposes the list of train schedules, you can assert ordering:
        List<TrainSchedule> trainSchedules = schedule.getTrainSchedules();
        assertEquals(2, trainSchedules.size());
        assertEquals(train1, trainSchedules.get(0).getTrain());
        assertEquals(train2, trainSchedules.get(1).getTrain());
    }

    /**
     * Tests that scheduleMultipleTrains with an empty list throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testScheduleMultipleTrains_emptyListThrows() throws SQLException {
        List<Train> empty = new ArrayList<Train>();
        scheduler.scheduleMultipleTrains(empty);
    }

    /**
     * Tests that getTrackType delegates to the underlying RailwayNetwork.
     */
    @Test
    public void testGetTrackType() {
        TrackType typeAB = scheduler.getTrackType(segmentAB);

        assertNotNull("Track type should not be null", typeAB);
        assertTrue("Track type should be SingleTrack", typeAB instanceof SingleTrack);

        SingleTrack single = (SingleTrack) typeAB;
        assertEquals(segmentAB.getId(), single.getSegmentId());
    }

    /**
     * Tests that getSpeedCalculator returns a non-null instance.
     */
    @Test
    public void testGetSpeedCalculator() {
        SpeedCalculator calculator = scheduler.getSpeedCalculator();
        assertNotNull("SpeedCalculator should not be null", calculator);
    }
}
