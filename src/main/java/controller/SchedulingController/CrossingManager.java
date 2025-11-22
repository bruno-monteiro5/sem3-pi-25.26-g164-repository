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
 * <p>Rules:
 * <ul>
 *   <li>Crossings only occur on single tracks</li>
 *   <li>Can occur at stations or sidings</li>
 *   <li>The train that arrives first waits for the opposite direction train</li>
 * </ul>
 *
 * @author G164
 * @version 1.0
 */
public class CrossingManager {

    /**
     * Detects and resolves crossings between a new train and already scheduled trains.
     *
     * @param newSchedule The schedule of the new train
     * @param existingSchedules Schedules already processed
     * @param scheduler Main scheduler (to access TrackTypes)
     * @return List of necessary crossing operations
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

        // Margem de segurança de 1 minuto
        LocalDateTime end1Adjusted = end1.plusMinutes(1);
        LocalDateTime end2Adjusted = end2.plusMinutes(1);

        return start1.isBefore(end2Adjusted) && start2.isBefore(end1Adjusted);
    }

    /**
     * Resolves a crossing by determining which train waits and where.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Prefer stations over sidings</li>
     *   <li>The train that arrives later waits</li>
     *   <li>If there's a siding, can wait there</li>
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
            crossingLocation = waitingPassage.getDepartureEndpoint(); // approximation
        } else {
            // Force wait at previous station
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getDepartureEndpoint();
        }

        // Calculate wait time
        LocalDateTime waitStart = waitingPassage.getDepartureTime();
        LocalDateTime waitEnd = passingPassage.getArrivalTime().plusMinutes(2); // safety margin

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