package domain.railway;

/**
 * Representa o tipo de via de um segmento
 */
public abstract class TrackType {
    protected int segmentId;

    public TrackType(int segmentId) {
        this.segmentId = segmentId;
    }

    public int getSegmentId() { return segmentId; }

    public abstract boolean isSingleTrack();
    public abstract boolean canCrossTrains();
    public abstract String getDescription();
}

