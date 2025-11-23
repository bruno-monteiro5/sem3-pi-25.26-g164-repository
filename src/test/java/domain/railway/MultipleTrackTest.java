package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link MultipleTrack}.
 * <p>
 * Each test method targets exactly one public member of {@code MultipleTrack}
 * and documents the scenario, preconditions and expected outcome.
 */
public class MultipleTrackTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid segmentId and numberTracks supplied.
     *
     * Expected:
     *  - {@link MultipleTrack#getSegmentId()} returns the supplied segmentId;
     *  - {@link MultipleTrack#getNumberTracks()} returns the supplied numberTracks.
     *
     * @see MultipleTrack#MultipleTrack(int, int)
     * @see MultipleTrack#getSegmentId()
     * @see MultipleTrack#getNumberTracks()
     */
    @Test
    public void constructor_initializesAllFields() {
        MultipleTrack track = new MultipleTrack(1, 2);

        assertEquals(1, track.getSegmentId());
        assertEquals(2, track.getNumberTracks());
    }

    /**
     * Tests the getter for number of tracks.
     *
     * Scenario:
     *  - Create a multiple track with 2 tracks and read it back.
     *
     * Expected:
     *  - returned number matches the constructor value.
     *
     * @see MultipleTrack#getNumberTracks()
     */
    @Test
    public void getNumberTracks_returnsCorrectNumber() {
        MultipleTrack track = new MultipleTrack(5, 3);
        assertEquals(3, track.getNumberTracks());
    }

    /**
     * Tests isSingleTrack method.
     *
     * Scenario:
     *  - Create a multiple track configuration.
     *
     * Expected:
     *  - returns false (it's not a single track).
     *
     * @see MultipleTrack#isSingleTrack()
     */
    @Test
    public void isSingleTrack_returnsFalse() {
        MultipleTrack track = new MultipleTrack(1, 2);
        assertFalse(track.isSingleTrack());
    }

    /**
     * Tests canCrossTrains method.
     *
     * Scenario:
     *  - Create a multiple track configuration.
     *
     * Expected:
     *  - returns true (trains can always cross on multiple tracks).
     *
     * @see MultipleTrack#canCrossTrains()
     */
    @Test
    public void canCrossTrains_alwaysReturnsTrue() {
        MultipleTrack track = new MultipleTrack(1, 2);
        assertTrue(track.canCrossTrains());
    }

    /**
     * Tests canCrossTrains with more tracks.
     *
     * Scenario:
     *  - Create a multiple track configuration with 4 tracks.
     *
     * Expected:
     *  - returns true.
     *
     * @see MultipleTrack#canCrossTrains()
     */
    @Test
    public void canCrossTrains_multipleTracksReturnsTrue() {
        MultipleTrack track = new MultipleTrack(2, 4);
        assertTrue(track.canCrossTrains());
    }

    /**
     * Tests getDescription method with 2 tracks.
     *
     * Scenario:
     *  - Create a multiple track with 2 tracks and get its description.
     *
     * Expected:
     *  - description includes "Multiple Tracks" and the number of tracks.
     *
     * @see MultipleTrack#getDescription()
     */
    @Test
    public void getDescription_twoTracks_formatsCorrectly() {
        MultipleTrack track = new MultipleTrack(1, 2);
        String description = track.getDescription();

        assertEquals("Multiple Tracks (2 tracks)", description);
    }

    /**
     * Tests getDescription method with 3 tracks.
     *
     * Scenario:
     *  - Create a multiple track with 3 tracks and get its description.
     *
     * Expected:
     *  - description includes "Multiple Tracks" and the number of tracks.
     *
     * @see MultipleTrack#getDescription()
     */
    @Test
    public void getDescription_threeTracks_formatsCorrectly() {
        MultipleTrack track = new MultipleTrack(5, 3);
        String description = track.getDescription();

        assertEquals("Multiple Tracks (3 tracks)", description);
    }

    /**
     * Tests getDescription method with 4 tracks.
     *
     * Scenario:
     *  - Create a multiple track with 4 tracks and get its description.
     *
     * Expected:
     *  - description includes "Multiple Tracks (4 tracks)".
     *
     * @see MultipleTrack#getDescription()
     */
    @Test
    public void getDescription_fourTracks_formatsCorrectly() {
        MultipleTrack track = new MultipleTrack(10, 4);
        String description = track.getDescription();

        assertEquals("Multiple Tracks (4 tracks)", description);
    }

    /**
     * Tests inheritance from TrackType.
     *
     * Scenario:
     *  - Verify that MultipleTrack is an instance of TrackType.
     *
     * Expected:
     *  - instanceof check returns true.
     *
     * @see TrackType
     */
    @Test
    public void inheritance_isTrackType() {
        MultipleTrack track = new MultipleTrack(1, 2);
        assertTrue(track instanceof TrackType);
    }

    /**
     * Tests getSegmentId inherited from TrackType.
     *
     * Scenario:
     *  - Create a multiple track and verify the segment id.
     *
     * Expected:
     *  - returns the correct segment id.
     *
     * @see TrackType#getSegmentId()
     */
    @Test
    public void getSegmentId_returnsCorrectId() {
        MultipleTrack track = new MultipleTrack(42, 2);
        assertEquals(42, track.getSegmentId());
    }
}