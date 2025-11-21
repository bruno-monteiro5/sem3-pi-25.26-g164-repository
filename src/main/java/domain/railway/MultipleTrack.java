package domain.railway;

/**
 * Via Múltipla (Multiple Tracks)
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
        return true; // vias múltiplas permitem sempre cruzamento
    }

    public int getNumberTracks() { return numberTracks; }

    @Override
    public String getDescription() {
        return String.format("Multiple Tracks (%d tracks)", numberTracks);
    }
}