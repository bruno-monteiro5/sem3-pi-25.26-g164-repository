package domain.railway;

/**
 * Multiple Tracks
 */
public class MultipleTrack extends TrackType {
    private int numberTracks;

    public MultipleTrack(int segmentId, int numberTracks) {
        super(segmentId);
        this.numberTracks = numberTracks;
    }

    @Override
    public boolean isSingleTrack() { return false; }

    @Override
    public boolean canCrossTrains() {
        return true; // multiple tracks always allow crossing
    }

    public int getNumberTracks() { return numberTracks; }

    @Override
    public String getDescription() {
        return String.format("Multiple Tracks (%d tracks)", numberTracks);
    }
}