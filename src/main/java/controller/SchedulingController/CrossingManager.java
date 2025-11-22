package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages train crossing operations on single track segments.
 *
 * <p><b>NEW in v2.0:</b> Now applies delays to train schedules when crossings occur.
 *
 * <p>Rules:
 * <ul>
 *   <li>Crossings only occur on single tracks</li>
 *   <li>Can occur at stations or sidings</li>
 *   <li>The train that arrives first waits for the opposite direction train</li>
 *   <li><b>Delays are propagated to all subsequent passages</b></li>
 * </ul>
 *
 * @author G164
 * @version 2.0
 */
public class CrossingManager {

    /** Safety margin added after a train passes (in minutes) */
    private static final int SAFETY_MARGIN_MINUTES = 2;

    /**
     * Detects and resolves crossings between a new train and already scheduled trains.
     *
     * <p><b>IMPORTANT:</b> This method now modifies the schedules by applying delays
     * when crossings are detected.
     *
     * @param newSchedule The schedule of the new train (will be modified if delays occur)
     * @param existingSchedules Schedules already processed
     * @param scheduler Main scheduler (to access TrackTypes)
     * @return List of crossing operations that were applied
     * @throws SQLException if database access fails
     */
    public List<CrossingOperation> detectAndResolveCrossings(
            TrainSchedule newSchedule,
            List<TrainSchedule> existingSchedules,
            TrainScheduler scheduler) throws SQLException {

        List<CrossingOperation> crossings = new ArrayList<>();

        // Analyze each passage of the new train
        for (int i = 0; i < newSchedule.getPassages().size(); i++) {
            SegmentPassage newPassage = newSchedule.getPassages().get(i);

            // Check if it's single track
            TrackType trackType = scheduler.getTrackType(newPassage.getSegment());

            if (trackType != null && trackType.isSingleTrack()) {
                // Look for conflicts with existing trains
                for (int j = 0; j < existingSchedules.size(); j++) {
                    TrainSchedule existingSchedule = existingSchedules.get(j);
                    CrossingOperation crossing = detectCrossingOnSegment(
                            newSchedule,
                            newPassage,
                            existingSchedule,
                            (SingleTrack) trackType
                    );

                    if (crossing != null) {
                        crossings.add(crossing);

                        // 🔧 NEW: Apply the delay immediately
                        applyDelayToSchedule(newSchedule, crossing);
                    }
                }
            }
        }

        return crossings;
    }

    /**
     * Detects if two trains cross on a specific segment.
     *
     * @param newSchedule Schedule of the new train
     * @param newPassage Passage of the new train
     * @param existingSchedule Schedule of the existing train
     * @param singleTrack Single track configuration
     * @return CrossingOperation if crossing detected, null otherwise
     */
    private CrossingOperation detectCrossingOnSegment(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SingleTrack singleTrack) {

        // Find if existing train also passes through this segment
        SegmentPassage existingPassage = findPassageOnSegment(
                existingSchedule,
                newPassage.getSegment()
        );

        if (existingPassage == null) {
            return null; // No crossing - trains don't use the same segment
        }

        // Check if trains travel in opposite directions
        boolean oppositeDirections = areOppositeDirections(newPassage, existingPassage);

        if (!oppositeDirections) {
            return null; // Same direction - no crossing
        }

        // Check temporal overlap
        boolean temporalOverlap = checkTemporalOverlap(
                newPassage.getDepartureTime(),
                newPassage.getArrivalTime(),
                existingPassage.getDepartureTime(),
                existingPassage.getArrivalTime()
        );

        if (!temporalOverlap) {
            return null; // No overlap - trains pass at different times
        }

        // CONFLICT! Resolve crossing
        return resolveCrossing(
                newSchedule,
                newPassage,
                existingSchedule,
                existingPassage,
                singleTrack
        );
    }

    /**
     * Applies a delay to all passages in a schedule starting from a specific crossing point.
     *
     * <p>This method:
     * <ol>
     *   <li>Finds the passage where the crossing occurs</li>
     *   <li>Calculates the required delay</li>
     *   <li>Applies the delay to that passage and all subsequent passages</li>
     *   <li>Updates the schedule's estimated arrival time</li>
     * </ol>
     *
     * @param schedule The train schedule to modify
     * @param crossing The crossing operation containing delay information
     */
    private void applyDelayToSchedule(TrainSchedule schedule, CrossingOperation crossing) {
        // Only apply delay if this schedule is the one waiting
        if (schedule.getTrain().getId() != crossing.getWaitingTrainId()) {
            return;
        }

        // Calculate delay duration in minutes
        long delayMinutes = java.time.Duration.between(
                crossing.getWaitStartTime(),
                crossing.getWaitEndTime()
        ).toMinutes();

        // Find the passage on the crossing segment
        List<SegmentPassage> passages = schedule.getPassages();
        boolean foundCrossing = false;

        for (int i = 0; i < passages.size(); i++) {
            SegmentPassage passage = passages.get(i);

            // Check if this is the crossing segment
            if (passage.getSegment().getId() == crossing.getSegment().getId()) {
                foundCrossing = true;
            }

            // Apply delay to this passage and all subsequent ones
            if (foundCrossing) {
                passage.applyDelay(delayMinutes);
            }
        }

        // Update total travel time
        schedule.updateTotalTravelTime();
    }

    /**
     * Finds the passage of a train on a specific segment.
     *
     * @param schedule Train schedule
     * @param segment Line segment
     * @return SegmentPassage if found, null otherwise
     */
    private SegmentPassage findPassageOnSegment(TrainSchedule schedule, LineSegment segment) {
        List<SegmentPassage> passages = schedule.getPassages();
        for (int i = 0; i < passages.size(); i++) {
            SegmentPassage passage = passages.get(i);
            if (passage.getSegment().getId() == segment.getId()) {
                return passage;
            }
        }
        return null;
    }

    /**
     * Checks if two passages are in opposite directions.
     *
     * @param p1 First passage
     * @param p2 Second passage
     * @return true if opposite directions, false otherwise
     */
    private boolean areOppositeDirections(SegmentPassage p1, SegmentPassage p2) {
        return !p1.getDepartureEndpoint().equals(p2.getDepartureEndpoint());
    }

    /**
     * Checks if there is temporal overlap between two intervals.
     *
     * @param start1 Start of first interval
     * @param end1 End of first interval
     * @param start2 Start of second interval
     * @param end2 End of second interval
     * @return true if overlap exists, false otherwise
     */
    private boolean checkTemporalOverlap(LocalDateTime start1, LocalDateTime end1,
                                         LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Resolves a crossing by determining which train waits and where.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Prefer stations over sidings</li>
     *   <li>The train that arrives later waits</li>
     *   <li>If there's a siding, can wait there</li>
     *   <li>Calculate exact wait time including safety margin</li>
     * </ol>
     *
     * @param newSchedule Schedule of the new train
     * @param newPassage Passage of the new train
     * @param existingSchedule Schedule of the existing train
     * @param existingPassage Passage of the existing train
     * @param singleTrack Single track configuration
     * @return CrossingOperation describing the resolution
     */
    private CrossingOperation resolveCrossing(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SegmentPassage existingPassage,
            SingleTrack singleTrack) {

        // Determine which train arrives first at the segment
        boolean newTrainFirst = newPassage.getDepartureTime()
                .isBefore(existingPassage.getDepartureTime());

        TrainSchedule waitingSchedule;
        TrainSchedule passingSchedule;
        SegmentPassage waitingPassage;
        SegmentPassage passingPassage;

        if (newTrainFirst) {
            // New train arrives first - existing train waits
            waitingSchedule = existingSchedule;
            passingSchedule = newSchedule;
            waitingPassage = existingPassage;
            passingPassage = newPassage;
        } else {
            // Existing train arrives first - new train waits
            waitingSchedule = newSchedule;
            passingSchedule = existingSchedule;
            waitingPassage = newPassage;
            passingPassage = existingPassage;
        }

        // Determine where crossing occurs
        CrossingOperation.CrossingType crossingType;
        Endpoint crossingLocation;

        if (waitingPassage.requiresStop()) {
            // Prefer station (stop endpoint)
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getArrivalEndpoint();
        } else if (singleTrack.hasSiding()) {
            // Use siding
            crossingType = CrossingOperation.CrossingType.AT_SIDING;
            crossingLocation = waitingPassage.getDepartureEndpoint();
        } else {
            // Force wait at previous station
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getDepartureEndpoint();
        }

        // Calculate wait time
        LocalDateTime waitStart = waitingPassage.getDepartureTime();
        LocalDateTime waitEnd = passingPassage.getArrivalTime().plusMinutes(SAFETY_MARGIN_MINUTES);

        return new CrossingOperation(
                waitingSchedule.getTrain().getId(),
                passingSchedule.getTrain().getId(),
                waitingPassage.getSegment(),
                crossingLocation,
                crossingType,
                waitStart,
                waitEnd
        );
    }
}