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
 * <p><b>FIXED VERSION:</b> Corrected crossing logic and safety margin application
 *
 * @author G164
 * @version 2.1
 */
public class CrossingManager {

    /** Safety margin added after a train passes (in minutes) */
    private static final int SAFETY_MARGIN_MINUTES = 2;

    public List<CrossingOperation> detectAndResolveCrossings(
            TrainSchedule newSchedule,
            List<TrainSchedule> existingSchedules,
            TrainScheduler scheduler) throws SQLException {

        List<CrossingOperation> crossings = new ArrayList<>();

        for (int i = 0; i < newSchedule.getPassages().size(); i++) {
            SegmentPassage newPassage = newSchedule.getPassages().get(i);

            TrackType trackType = scheduler.getTrackType(newPassage.getSegment());

            if (trackType != null && trackType.isSingleTrack()) {
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
                        applyDelayToSchedule(newSchedule, crossing);
                    }
                }
            }
        }

        return crossings;
    }

    private CrossingOperation detectCrossingOnSegment(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SingleTrack singleTrack) {

        SegmentPassage existingPassage = findPassageOnSegment(
                existingSchedule,
                newPassage.getSegment()
        );

        if (existingPassage == null) {
            return null;
        }

        boolean oppositeDirections = areOppositeDirections(newPassage, existingPassage);

        if (!oppositeDirections) {
            return null;
        }

        boolean temporalOverlap = checkTemporalOverlapWithSafety(
                newPassage.getDepartureTime(),
                newPassage.getArrivalTime(),
                existingPassage.getDepartureTime(),
                existingPassage.getArrivalTime()
        );

        if (!temporalOverlap) {
            return null;
        }

        return resolveCrossing(
                newSchedule,
                newPassage,
                existingSchedule,
                existingPassage,
                singleTrack
        );
    }

    private void applyDelayToSchedule(TrainSchedule schedule, CrossingOperation crossing) {
        if (schedule.getTrain().getId() != crossing.getWaitingTrainId()) {
            return;
        }

        long delayMinutes = java.time.Duration.between(
                crossing.getWaitStartTime(),
                crossing.getWaitEndTime()
        ).toMinutes();

        List<SegmentPassage> passages = schedule.getPassages();
        boolean foundCrossing = false;

        for (int i = 0; i < passages.size(); i++) {
            SegmentPassage passage = passages.get(i);

            if (passage.getSegment().getId() == crossing.getSegment().getId()) {
                foundCrossing = true;
            }

            if (foundCrossing) {
                passage.applyDelay(delayMinutes);
            }
        }

        schedule.updateTotalTravelTime();
    }

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

    private boolean areOppositeDirections(SegmentPassage p1, SegmentPassage p2) {
        return !p1.getDepartureEndpoint().equals(p2.getDepartureEndpoint());
    }

    /**
     * 🔧 FIX: Verifies temporal overlap with safety margin
     */
    private boolean checkTemporalOverlapWithSafety(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2) {

        // Adition of security margin to departure times
        LocalDateTime end1Adjusted = end1.plusMinutes(SAFETY_MARGIN_MINUTES);
        LocalDateTime end2Adjusted = end2.plusMinutes(SAFETY_MARGIN_MINUTES);

        // There is overlap if: one's start is before the other's end (with margin)
        return start1.isBefore(end2Adjusted) && start2.isBefore(end1Adjusted);
    }

    /**
     * 🔧 FIX: Crossing logic corrected for tests
     */
    private CrossingOperation resolveCrossing(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SegmentPassage existingPassage,
            SingleTrack singleTrack) {

        // Determine which train arrives first at the BEGINNING of the segment
        boolean newTrainFirst = newPassage.getDepartureTime()
                .isBefore(existingPassage.getDepartureTime());

        TrainSchedule waitingSchedule;
        TrainSchedule passingSchedule;
        SegmentPassage waitingPassage;
        SegmentPassage passingPassage;

        if (newTrainFirst) {
            // New train arrives first → existant train waits
            waitingSchedule = existingSchedule;
            passingSchedule = newSchedule;
            waitingPassage = existingPassage;
            passingPassage = newPassage;
        } else {
            // Existant train arrives first → new train WAITS
            waitingSchedule = newSchedule;
            passingSchedule = existingSchedule;
            waitingPassage = newPassage;
            passingPassage = existingPassage;
        }


        CrossingOperation.CrossingType crossingType;
        Endpoint crossingLocation;

        if (waitingPassage.requiresStop() && isStation(waitingPassage.getArrivalEndpoint())) {
            // Priority 1: arrival
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getArrivalEndpoint();

        } else if (singleTrack.hasSiding()) {
            // Priority 2: Siding
            crossingType = CrossingOperation.CrossingType.AT_SIDING;
            crossingLocation = waitingPassage.getDepartureEndpoint();

        } else if (isStation(waitingPassage.getDepartureEndpoint())) {
            // Priority 3: departure
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getDepartureEndpoint();

        } else {
            throw new IllegalStateException(
                    String.format(
                            "Cannot resolve crossing between trains %d and %d on segment %d: " +
                                    "no valid crossing location (station or siding) available!",
                            waitingSchedule.getTrain().getId(),
                            passingSchedule.getTrain().getId(),
                            waitingPassage.getSegment().getId()
                    )
            );
        }

        // Calculation of waiting time
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

    private boolean isStation(Endpoint endpoint) {
        return endpoint.getMetafacilityId() > 0;
    }
}