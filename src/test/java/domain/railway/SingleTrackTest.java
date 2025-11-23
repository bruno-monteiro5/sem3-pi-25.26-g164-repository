package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link SingleTrack}.
 * <p>
 * Each test method targets exactly one public member of {@code SingleTrack}
 * and documents the scenario, preconditions and expected outcome.
 */
public class SingleTrackTest {

    /**
     * Verifies that the simple constructor initializes without siding.
     *
     * Preconditions:
     *  - valid segmentId supplied.
     *
     * Expected:
     *  - {@link SingleTrack#getSegmentId()} returns the supplied segmentId;
     *  - {@link SingleTrack#hasSiding()} returns false;
     *  - {@link SingleTrack#getSidingPosition()} returns null;
     *  - {@link SingleTrack#getSidingLength()} returns null.
     *
     * @see SingleTrack#SingleTrack(int)
     * @see SingleTrack#hasSiding()
     * @see SingleTrack#getSidingPosition()
     * @see SingleTrack#getSidingLength()
     */
    @Test
    public void constructor_withoutSiding_initializesCorrectly() {
        SingleTrack track = new SingleTrack(1);

        assertEquals(1, track.getSegmentId());
        assertFalse(track.hasSiding());
        assertNull(track.getSidingPosition());
        assertNull(track.getSidingLength());
    }

    /**
     * Verifies that the constructor with siding initializes all fields correctly.
     *
     * Preconditions:
     *  - valid segmentId, sidingPosition, and sidingLength supplied.
     *
     * Expected:
     *  - {@link SingleTrack#getSegmentId()} returns the supplied segmentId;
     *  - {@link SingleTrack#hasSiding()} returns true;
     *  - {@link SingleTrack#getSidingPosition()} returns the supplied position;
     *  - {@link SingleTrack#getSidingLength()} returns the supplied length.
     *
     * @see SingleTrack#SingleTrack(int, int, int)
     * @see SingleTrack#hasSiding()
     * @see SingleTrack#getSidingPosition()
     * @see SingleTrack#getSidingLength()
     */
    @Test
    public void constructor_withSiding_initializesCorrectly() {
        SingleTrack track = new SingleTrack(1, 5000, 300);

        assertEquals(1, track.getSegmentId());
        assertTrue(track.hasSiding());
        assertEquals(Integer.valueOf(5000), track.getSidingPosition());
        assertEquals(Integer.valueOf(300), track.getSidingLength());
    }

    /**
     * Tests isSingleTrack method.
     *
     * Scenario:
     *  - Create a single track (without siding).
     *
     * Expected:
     *  - returns true.
     *
     * @see SingleTrack#isSingleTrack()
     */
    @Test
    public void isSingleTrack_returnsTrue() {
        SingleTrack track = new SingleTrack(1);
        assertTrue(track.isSingleTrack());
    }

    /**
     * Tests isSingleTrack method with siding.
     *
     * Scenario:
     *  - Create a single track with siding.
     *
     * Expected:
     *  - returns true (still a single track, just with a siding).
     *
     * @see SingleTrack#isSingleTrack()
     */
    @Test
    public void isSingleTrack_withSiding_returnsTrue() {
        SingleTrack track = new SingleTrack(1, 5000, 300);
        assertTrue(track.isSingleTrack());
    }

    /**
     * Tests canCrossTrains without siding.
     *
     * Scenario:
     *  - Create a single track without siding.
     *
     * Expected:
     *  - returns false (cannot cross without siding).
     *
     * @see SingleTrack#canCrossTrains()
     */
    @Test
    public void canCrossTrains_withoutSiding_returnsFalse() {
        SingleTrack track = new SingleTrack(1);
        assertFalse(track.canCrossTrains());
    }

    /**
     * Tests canCrossTrains with siding.
     *
     * Scenario:
     *  - Create a single track with siding.
     *
     * Expected:
     *  - returns true (can cross at siding).
     *
     * @see SingleTrack#canCrossTrains()
     */
    @Test
    public void canCrossTrains_withSiding_returnsTrue() {
        SingleTrack track = new SingleTrack(1, 5000, 300);
        assertTrue(track.canCrossTrains());
    }

    /**
     * Tests hasSiding method when siding is absent.
     *
     * Scenario:
     *  - Create a single track without siding.
     *
     * Expected:
     *  - returns false.
     *
     * @see SingleTrack#hasSiding()
     */
    @Test
    public void hasSiding_withoutSiding_returnsFalse() {
        SingleTrack track = new SingleTrack(2);
        assertFalse(track.hasSiding());
    }

    /**
     * Tests hasSiding method when siding is present.
     *
     * Scenario:
     *  - Create a single track with siding.
     *
     * Expected:
     *  - returns true.
     *
     * @see SingleTrack#hasSiding()
     */
    @Test
    public void hasSiding_withSiding_returnsTrue() {
        SingleTrack track = new SingleTrack(2, 10000, 500);
        assertTrue(track.hasSiding());
    }

    /**
     * Tests getSidingPosition when siding is present.
     *
     * Scenario:
     *  - Create a single track with siding at position 5000m.
     *
     * Expected:
     *  - returns 5000.
     *
     * @see SingleTrack#getSidingPosition()
     */
    @Test
    public void getSidingPosition_withSiding_returnsCorrectPosition() {
        SingleTrack track = new SingleTrack(1, 5000, 300);
        assertEquals(Integer.valueOf(5000), track.getSidingPosition());
    }

    /**
     * Tests getSidingPosition when siding is absent.
     *
     * Scenario:
     *  - Create a single track without siding.
     *
     * Expected:
     *  - returns null.
     *
     * @see SingleTrack#getSidingPosition()
     */
    @Test
    public void getSidingPosition_withoutSiding_returnsNull() {
        SingleTrack track = new SingleTrack(1);
        assertNull(track.getSidingPosition());
    }

    /**
     * Tests getSidingLength when siding is present.
     *
     * Scenario:
     *  - Create a single track with siding of length 300m.
     *
     * Expected:
     *  - returns 300.
     *
     * @see SingleTrack#getSidingLength()
     */
    @Test
    public void getSidingLength_withSiding_returnsCorrectLength() {
        SingleTrack track = new SingleTrack(1, 5000, 300);
        assertEquals(Integer.valueOf(300), track.getSidingLength());
    }

    /**
     * Tests getSidingLength when siding is absent.
     *
     * Scenario:
     *  - Create a single track without siding.
     *
     * Expected:
     *  - returns null.
     *
     * @see SingleTrack#getSidingLength()
     */
    @Test
    public void getSidingLength_withoutSiding_returnsNull() {
        SingleTrack track = new SingleTrack(1);
        assertNull(track.getSidingLength());
    }

    /**
     * Tests getDescription without siding.
     *
     * Scenario:
     *  - Create a single track without siding and get its description.
     *
     * Expected:
     *  - description is "Single Track (no siding)".
     *
     * @see SingleTrack#getDescription()
     */
    @Test
    public void getDescription_withoutSiding_formatsCorrectly() {
        SingleTrack track = new SingleTrack(1);
        String description = track.getDescription();

        assertEquals("Single Track (no siding)", description);
    }

    /**
     * Tests getDescription with siding.
     *
     * Scenario:
     *  - Create a single track with siding and get its description.
     *
     * Expected:
     *  - description includes position and length information.
     *
     * @see SingleTrack#getDescription()
     */
    @Test
    public void getDescription_withSiding_formatsCorrectly() {
        SingleTrack track = new SingleTrack(1, 5000, 300);
        String description = track.getDescription();

        assertEquals("Single Track with Siding (pos: 5000m, len: 300m)", description);
    }

    /**
     * Tests getDescription with different siding parameters.
     *
     * Scenario:
     *  - Create a single track with siding at position 10000m, length 500m.
     *
     * Expected:
     *  - description correctly reflects these values.
     *
     * @see SingleTrack#getDescription()
     */
    @Test
    public void getDescription_withDifferentSiding_formatsCorrectly() {
        SingleTrack track = new SingleTrack(2, 10000, 500);
        String description = track.getDescription();

        assertEquals("Single Track with Siding (pos: 10000m, len: 500m)", description);
    }

    /**
     * Tests inheritance from TrackType.
     *
     * Scenario:
     *  - Verify that SingleTrack is an instance of TrackType.
     *
     * Expected:
     *  - instanceof check returns true.
     *
     * @see TrackType
     */
    @Test
    public void inheritance_isTrackType() {
        SingleTrack track = new SingleTrack(1);
        assertTrue(track instanceof TrackType);
    }

    /**
     * Tests getSegmentId inherited from TrackType.
     *
     * Scenario:
     *  - Create a single track and verify the segment id.
     *
     * Expected:
     *  - returns the correct segment id.
     *
     * @see TrackType#getSegmentId()
     */
    @Test
    public void getSegmentId_returnsCorrectId() {
        SingleTrack track = new SingleTrack(42, 5000, 300);
        assertEquals(42, track.getSegmentId());
    }
}