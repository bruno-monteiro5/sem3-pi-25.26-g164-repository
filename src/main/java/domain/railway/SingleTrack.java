package domain.railway;

import domain.railway.TrackType;

/**
 * Via Simples (Single Track)
 */
public class SingleTrack extends TrackType {
    private boolean hasSiding;
    private Integer sidingPosition;  // posição do siding (metros desde o início)
    private Integer sidingLength;    // comprimento do siding

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
        return hasSiding; // só pode cruzar se tiver siding
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