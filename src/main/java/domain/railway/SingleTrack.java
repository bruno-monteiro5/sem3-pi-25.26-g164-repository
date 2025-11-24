package domain.railway;

import domain.railway.TrackType;

/**
 * Single Track
 */
public class SingleTrack extends TrackType {
    private boolean hasSiding;
    private Integer sidingPosition;  // siding position (meters since the beginning of the segment)
    private Integer sidingLength;    // siding length

    public SingleTrack(int segmentId) {
        super(segmentId);
        this.hasSiding = false;
    }

    public SingleTrack(int segmentId, int sidingPosition, int sidingLength) {
        super(segmentId);
        this.hasSiding = true;
        this.sidingPosition = sidingPosition;
        this.sidingLength = sidingLength;
    }

    @Override
    public boolean isSingleTrack() { return true; }

    @Override
    public boolean canCrossTrains() {
        return hasSiding; // can only cross if it has siding
    }

    public boolean hasSiding() { return hasSiding; }
    public Integer getSidingPosition() { return sidingPosition; }
    public Integer getSidingLength() { return sidingLength; }

    @Override
    public String getDescription() {
        if (hasSiding) {
            return String.format("Single Track with Siding (pos: %dm, len: %dm)",
                    sidingPosition, sidingLength);
        }
        return "Single Track (no siding)";
    }
}