package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main train scheduling service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Calculate passage times for each line segment</li>
 *   <li>Identify and resolve crossings on single-track segments</li>
 *   <li>Generate complete schedules for multiple trains</li>
 * </ul>
 */
public class TrainScheduler {

    private final RailwayNetwork network;
    private final SpeedCalculator speedCalculator;
    private final CrossingManager crossingManager;

    /** Default stop time at a station, in minutes. */
    private static final int STOP_TIME_MINUTES = 5;

    /**
     * Creates a new TrainScheduler for a given railway network.
     *
     * @param network In-memory railway network
     */
    public TrainScheduler(RailwayNetwork network) {
        this.network = network;
        this.speedCalculator = new SpeedCalculator();
        this.crossingManager = new CrossingManager();
    }

    /**
     * Generates a complete schedule for a single train.
     *
     * <p>For each pair of consecutive path endpoints:
     * <ul>
     *   <li>Finds the line segment between them</li>
     *   <li>Calculates speed and travel time</li>
     *   <li>Creates a {@link SegmentPassage} entry</li>
     *   <li>Adds station stop time when required</li>
     * </ul>
     *
     * @param train Train to schedule
     * @return Generated train schedule
     * @throws SQLException if any database-related operation fails (reserved for future extensions)
     * @throws IllegalStateException if no segment is found between two endpoints
     */
    public TrainSchedule scheduleTrainReschedule(Train train) throws SQLException {
        LocalDateTime currentTime = LocalDateTime.of(
                train.getDate(),
                train.getDepartureTime()
        );

        TrainSchedule schedule = new TrainSchedule(train, currentTime);

        List<Path.PathEndpoint> route = train.getPath().getOrderedEndpoints();

        // Process each segment in the route
        for (int i = 0; i < route.size() - 1; i++) {
            Path.PathEndpoint current = route.get(i);
            Path.PathEndpoint next = route.get(i + 1);

            // Find the segment between the two endpoints
            LineSegment segment = findSegmentBetween(
                    current.getEndpoint().getId(),
                    next.getEndpoint().getId()
            );

            if (segment == null) {
                throw new IllegalStateException(
                        "No segment found between " + current.getEndpoint().getName()
                                + " and " + next.getEndpoint().getName()
                );
            }

            // Calculate speed and travel time
            int speed = speedCalculator.calculateSpeed(train, segment);
            double travelTimeMinutes = speedCalculator.calculateTravelTime(train, segment);

            // Calculate arrival time
            LocalDateTime arrivalTime = currentTime.plusMinutes((long) travelTimeMinutes);

            // Create passage
            SegmentPassage passage = new SegmentPassage(
                    segment,
                    current.getEndpoint(),
                    next.getEndpoint(),
                    currentTime,
                    arrivalTime,
                    speed,
                    next.isMustStop()
            );

            schedule.addPassage(passage);

            // If the next endpoint requires a stop, add stop time
            if (next.isMustStop()) {
                currentTime = arrivalTime.plusMinutes(STOP_TIME_MINUTES);
            } else {
                currentTime = arrivalTime;
            }
        }

        return schedule;
    }

    /**
     * Schedules multiple trains and resolves crossing conflicts.
     *
     * <p>Process:
     * <ol>
     *   <li>Creates a {@link Schedule} for the (common) train date</li>
     *   <li>Sorts trains by departure time</li>
     *   <li>Schedules each train individually</li>
     *   <li>Detects and resolves crossings against already scheduled trains</li>
     *   <li>Adds crossing operations to each affected schedule</li>
     * </ol>
     *
     * @param trains List of trains to schedule (must not be empty)
     * @return Global schedule containing all train schedules
     * @throws SQLException if scheduling requires database access and that access fails
     * @throws IllegalArgumentException if the train list is empty
     */
    public Schedule scheduleMultipleTrains(List<Train> trains) throws SQLException {
        if (trains == null || trains.isEmpty()) {
            throw new IllegalArgumentException("Train list cannot be empty");
        }

        LocalDateTime firstDeparture = LocalDateTime.of(
                trains.get(0).getDate(),
                trains.get(0).getDepartureTime()
        );
        Schedule schedule = new Schedule(firstDeparture.toLocalDate());

        // Sort trains by departure time
        List<Train> sortedTrains = new ArrayList<Train>(trains);
        Collections.sort(sortedTrains, new Comparator<Train>() {
            @Override
            public int compare(Train t1, Train t2) {
                return t1.getDepartureTime().compareTo(t2.getDepartureTime());
            }
        });

        // Already processed schedules (used to detect crossings)
        List<TrainSchedule> processedSchedules = new ArrayList<TrainSchedule>();

        for (int i = 0; i < sortedTrains.size(); i++) {
            Train train = sortedTrains.get(i);

            TrainSchedule trainSchedule = scheduleTrainReschedule(train);

            // Detect crossings with previously scheduled trains
            List<CrossingOperation> crossings =
                    crossingManager.detectAndResolveCrossings(
                            trainSchedule,
                            processedSchedules,
                            this
                    );

            // Attach crossing operations to this train schedule
            for (int j = 0; j < crossings.size(); j++) {
                trainSchedule.addCrossingOperation(crossings.get(j));
            }

            processedSchedules.add(trainSchedule);
            schedule.addTrainSchedule(trainSchedule);
        }

        return schedule;
    }

    /**
     * Finds the line segment between two endpoints using the railway network.
     *
     * @param endpointAId ID of the first endpoint
     * @param endpointBId ID of the second endpoint
     * @return Matching line segment
     * @throws IllegalStateException if no segment exists between the two endpoints
     */
    private LineSegment findSegmentBetween(int endpointAId, int endpointBId) {
        LineSegment segment = network.findSegmentBetween(endpointAId, endpointBId);
        if (segment == null) {
            throw new IllegalStateException(
                    "No segment found between endpoints " + endpointAId + " and " + endpointBId
            );
        }
        return segment;
    }

    /**
     * Returns the track type for the given segment.
     *
     * @param segment Line segment
     * @return Track type for that segment (may be null if not configured)
     */
    public TrackType getTrackType(LineSegment segment) {
        return network.getTrackType(segment);
    }

    /**
     * Returns the {@link SpeedCalculator} used by this scheduler.
     *
     * @return SpeedCalculator instance
     */
    public SpeedCalculator getSpeedCalculator() {
        return speedCalculator;
    }
}
