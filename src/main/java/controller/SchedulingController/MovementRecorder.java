package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;
import repository.MovementRepository;
import repository.LocationRepository;
import repository.RailwayRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Records train schedules into the database.
 * Part of USLP10 - automatically registers movements and updates locations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Convert TrainSchedule to database records (Train_Movement, Stop, Passage)</li>
 *   <li>Update locomotive/wagon locations when train departs</li>
 *   <li>Update locations when train arrives at destination</li>
 * </ul>
 *
 *
 */
public class MovementRecorder {

    private final MovementRepository movementRepo;
    private final LocationRepository locationRepo;
    private final RailwayRepository railwayRepo;

    public MovementRecorder() {
        this.railwayRepo = new RailwayRepository();
        this.movementRepo = new MovementRepository();
        this.locationRepo = new LocationRepository(railwayRepo);
    }

    /**
     * Records a complete train schedule into the database.
     * NOW INCLUDES: continuous tracking of rolling stock locations.
     *
     * @param schedule The train schedule to record
     * @throws SQLException if database operation fails
     */
    public void recordSchedule(TrainSchedule schedule) throws SQLException {
        Train train = schedule.getTrain();
        int trainId = train.getId();

        // Step 1: Clean previous movements (for rescheduling)
        movementRepo.deleteMovementsByTrain(trainId);

        // Step 2: Record all train movements (stops and passages)
        movementRepo.recordScheduleMovements(schedule);

        // Step 3: Update locomotive locations to "In Transit" at departure
        updateLocomotiveLocationsAtDeparture(train, schedule);

        // Step 4: Update wagon locations to "In Transit" at freight origin
        updateWagonLocationsAtDeparture(train, schedule);

        // Step 5: NEW - Record continuous location updates during journey
        recordRollingStockMovements(schedule);

        System.out.println("✅ Schedule recorded for Train " + trainId);
        System.out.println("   • Movements recorded: " + schedule.getPassages().size());
        System.out.println("   • Locomotives tracked: " + train.getLocomotives().size());
        System.out.println("   • Wagons tracked: " + countWagons(train));
        System.out.println("   • Final status: All rolling stock updated to FREE at destination");
    }

    /**
     * Helper method: Updates locomotives at departure (renamed for clarity)
     */
    private void updateLocomotiveLocationsAtDeparture(Train train, TrainSchedule schedule)
            throws SQLException {
        LocalDateTime departureTime = schedule.getScheduledDeparture();
        Endpoint startPoint = train.getStartEndpoint();

        for (Locomotive loco : train.getLocomotives()) {
            locationRepo.updateLocomotiveLocation(
                    loco.getId(),
                    startPoint.getId(),
                    LocomotiveLocation.Status.T, // In Transit
                    departureTime
            );
        }
    }

    /**
     * Helper method: Updates wagons at departure (renamed for clarity)
     */
    private void updateWagonLocationsAtDeparture(Train train, TrainSchedule schedule)
            throws SQLException {
        LocalDateTime departureTime = schedule.getScheduledDeparture();

        for (Freight freight : train.getFreights()) {
            Endpoint freightOrigin = freight.getOrigin();

            for (Wagon wagon : freight.getWagons()) {
                locationRepo.updateWagonLocation(
                        wagon.getId(),
                        freightOrigin.getId(),
                        WagonLocation.Status.T, // In Transit
                        departureTime
                );
            }
        }
    }

    /**
     * Updates all locomotive locations for a scheduled train.
     * Sets status to "In Transit" with current endpoint = train start point.
     */
    private void updateLocomotiveLocations(Train train, TrainSchedule schedule) throws SQLException {
        LocalDateTime departureTime = schedule.getScheduledDeparture();
        Endpoint startPoint = train.getStartEndpoint();

        for (Locomotive loco : train.getLocomotives()) {
            locationRepo.updateLocomotiveLocation(
                    loco.getId(),
                    startPoint.getId(),
                    LocomotiveLocation.Status.T, // In Transit
                    departureTime
            );
        }
    }

    /**
     * Updates all wagon locations for a scheduled train.
     * Sets status to "In Transit" with current endpoint = freight origin.
     */
    private void updateWagonLocations(Train train, TrainSchedule schedule) throws SQLException {
        LocalDateTime departureTime = schedule.getScheduledDeparture();

        for (Freight freight : train.getFreights()) {
            Endpoint freightOrigin = freight.getOrigin();

            for (Wagon wagon : freight.getWagons()) {
                locationRepo.updateWagonLocation(
                        wagon.getId(),
                        freightOrigin.getId(),
                        WagonLocation.Status.T, // In Transit
                        departureTime
                );
            }
        }
    }

    /**
     * Marks a train as completed - updates all rolling stock to destination.
     * Called when train physically arrives at final destination.
     *
     * @param trainId The train ID
     * @param arrivalTime Actual arrival time
     * @throws SQLException if update fails
     */
    public void markTrainCompleted(int trainId, LocalDateTime arrivalTime) throws SQLException {
        Train train = railwayRepo.getTrainById(trainId);
        if (train == null) {
            throw new IllegalArgumentException("Train " + trainId + " not found");
        }

        Endpoint destination = train.getEndEndpoint();

        // Update locomotives to FREE at destination
        for (Locomotive loco : train.getLocomotives()) {
            locationRepo.updateLocomotiveLocation(
                    loco.getId(),
                    destination.getId(),
                    LocomotiveLocation.Status.F, // Free
                    arrivalTime
            );
        }

        // Update wagons: each to its freight destination, status FREE
        for (Freight freight : train.getFreights()) {
            for (Wagon wagon : freight.getWagons()) {
                locationRepo.updateWagonLocation(
                        wagon.getId(),
                        freight.getDestination().getId(),
                        WagonLocation.Status.F, // Free
                        arrivalTime
                );
            }
        }

        System.out.println("✅ Train " + trainId + " marked as completed");
    }

    /**
     * Gets all recorded movements for a train.
     * Useful for displaying the schedule from database.
     *
     * @param trainId Train ID
     * @return List of movement records
     * @throws SQLException if query fails
     */
    public List<TrainMovementRecord> getTrainMovements(int trainId) throws SQLException {
        return movementRepo.getMovementsByTrain(trainId);
    }

    /**
     * Checks if a train has recorded movements.
     *
     * @param trainId Train ID
     * @return true if movements exist, false otherwise
     * @throws SQLException if query fails
     */
    public boolean hasRecordedMovements(int trainId) throws SQLException {
        List<TrainMovementRecord> movements = movementRepo.getMovementsByTrain(trainId);
        return !movements.isEmpty();
    }

    /**
     * Helper: counts total wagons in a train.
     */
    private int countWagons(Train train) {
        int count = 0;
        for (Freight freight : train.getFreights()) {
            count += freight.getWagons().size();
        }
        return count;
    }

    /**
     * Displays recorded movements for a train in a formatted way.
     *
     * @param trainId Train ID
     * @throws SQLException if query fails
     */
    public void displayRecordedMovements(int trainId) throws SQLException {
        List<TrainMovementRecord> movements = getTrainMovements(trainId);

        if (movements.isEmpty()) {
            System.out.println("⚠️  No recorded movements for Train " + trainId);
            return;
        }

        System.out.println("\n📍 RECORDED MOVEMENTS FOR TRAIN " + trainId);
        System.out.println("─".repeat(70));

        for (int i = 0; i < movements.size(); i++) {
            TrainMovementRecord record = movements.get(i);
            System.out.printf("%2d. %s\n", i + 1, record);
        }

        System.out.println("─".repeat(70));
    }

    /**
     * Records intermediate movements of locomotives and wagons as train progresses.
     * Updates location when train passes through or stops at each endpoint.
     *
     * @param schedule The train schedule with all passages
     * @throws SQLException if database operation fails
     */
    public void recordRollingStockMovements(TrainSchedule schedule) throws SQLException {
        Train train = schedule.getTrain();
        List<SegmentPassage> passages = schedule.getPassages();

        // For each passage, update locomotive locations
        for (SegmentPassage passage : passages) {
            LocalDateTime arrivalTime = passage.getArrivalTime();
            Endpoint arrivalEndpoint = passage.getArrivalEndpoint();

            // Update all locomotives to new location (still in transit)
            for (Locomotive loco : train.getLocomotives()) {
                locationRepo.updateLocomotiveLocation(
                        loco.getId(),
                        arrivalEndpoint.getId(),
                        LocomotiveLocation.Status.T,
                        arrivalTime
                );
            }

            // Update wagons that have reached their freight destination
            updateWagonsAtDestination(train, arrivalEndpoint, arrivalTime);
        }

        // Mark as completed when reaching final destination
        SegmentPassage lastPassage = passages.get(passages.size() - 1);
        markTrainCompletedAutomatic(train.getId(), lastPassage.getArrivalTime());
    }

    /**
     * Checks if any wagon's freight destination is this endpoint.
     * If yes, marks wagon as FREE at this location.
     */
    private void updateWagonsAtDestination(Train train, Endpoint currentEndpoint,
                                           LocalDateTime time) throws SQLException {
        for (Freight freight : train.getFreights()) {
            // Check if this freight's destination is the current endpoint
            if (freight.getDestination().getId() == currentEndpoint.getId()) {
                // Unload all wagons of this freight
                for (Wagon wagon : freight.getWagons()) {
                    locationRepo.updateWagonLocation(
                            wagon.getId(),
                            currentEndpoint.getId(),
                            WagonLocation.Status.F,  // FREE - unloaded
                            time
                    );
                }
            }
        }
    }

    /**
     * Automatically marks train as completed when it reaches final destination.
     * Updates locomotives to FREE at final endpoint.
     * Remaining wagons (not yet unloaded) also become FREE.
     */
    private void markTrainCompletedAutomatic(int trainId, LocalDateTime arrivalTime)
            throws SQLException {
        Train train = railwayRepo.getTrainById(trainId);
        if (train == null) return;

        Endpoint finalDestination = train.getEndEndpoint();

        // Update locomotives to FREE at final destination
        for (Locomotive loco : train.getLocomotives()) {
            locationRepo.updateLocomotiveLocation(
                    loco.getId(),
                    finalDestination.getId(),
                    LocomotiveLocation.Status.F,
                    arrivalTime
            );
        }

        // Update any remaining wagons (those whose freight destination = final destination)
        for (Freight freight : train.getFreights()) {
            if (freight.getDestination().getId() == finalDestination.getId()) {
                for (Wagon wagon : freight.getWagons()) {
                    locationRepo.updateWagonLocation(
                            wagon.getId(),
                            finalDestination.getId(),
                            WagonLocation.Status.F,
                            arrivalTime
                    );
                }
            }
        }

        System.out.println("✅ Train " + trainId + " completed journey at " +
                finalDestination.getName());
    }
}