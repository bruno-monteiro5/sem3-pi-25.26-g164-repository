package domain.railway;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Path} and {@link Path.PathEndpoint}.
 * <p>
 * Each test method targets exactly one public member of {@code Path}
 * and documents the scenario, preconditions and expected outcome.
 */
public class PathTest {

    private Endpoint endpoint1;
    private Endpoint endpoint2;
    private Endpoint endpoint3;

    @Before
    public void setUp() {
        endpoint1 = new Endpoint(1, "Lisbon", 100);
        endpoint2 = new Endpoint(2, "Coimbra", 200);
        endpoint3 = new Endpoint(3, "Porto", 300);
    }

    /**
     * Verifies that the Path constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid id and name supplied.
     *
     * Expected:
     *  - {@link Path#getId()} returns the supplied id;
     *  - {@link Path#getName()} returns the supplied name;
     *  - {@link Path#getEndpoints()} returns an empty list.
     *
     * @see Path#Path(int, String)
     */
    @Test
    public void constructor_initializesAllFields() {
        Path path = new Path(1, "North Line Path");

        assertEquals(1, path.getId());
        assertEquals("North Line Path", path.getName());
        assertTrue(path.getEndpoints().isEmpty());
    }

    /**
     * Tests the getter for path id.
     *
     * Scenario:
     *  - Create a path with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Path#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Path path = new Path(42, "Test Path");
        assertEquals(42, path.getId());
    }

    /**
     * Tests the getter for path name.
     *
     * Scenario:
     *  - Create a path with a known name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Path#getName()
     */
    @Test
    public void getName_returnsCorrectName() {
        Path path = new Path(1, "Scenic Route");
        assertEquals("Scenic Route", path.getName());
    }

    /**
     * Tests getEndpoints returns initially empty list.
     *
     * Scenario:
     *  - Create a new path without adding endpoints.
     *
     * Expected:
     *  - returns an empty list.
     *
     * @see Path#getEndpoints()
     */
    @Test
    public void getEndpoints_initiallyEmpty() {
        Path path = new Path(1, "Test Path");
        assertTrue(path.getEndpoints().isEmpty());
    }

    /**
     * Tests adding a PathEndpoint to the path.
     *
     * Scenario:
     *  - Create a path and add one endpoint.
     *
     * Expected:
     *  - endpoints list contains the added endpoint.
     *
     * @see Path#addEndpoint(Path.PathEndpoint)
     */
    @Test
    public void addEndpoint_addsEndpointToList() {
        Path path = new Path(1, "Test Path");
        Path.PathEndpoint pe = new Path.PathEndpoint(1, endpoint1, true);

        path.addEndpoint(pe);

        assertEquals(1, path.getEndpoints().size());
        assertTrue(path.getEndpoints().contains(pe));
    }

    /**
     * Tests adding multiple endpoints to the path.
     *
     * Scenario:
     *  - Create a path and add three endpoints.
     *
     * Expected:
     *  - endpoints list contains all three endpoints in order.
     *
     * @see Path#addEndpoint(Path.PathEndpoint)
     */
    @Test
    public void addEndpoint_multipleEndpoints() {
        Path path = new Path(1, "Test Path");
        Path.PathEndpoint pe1 = new Path.PathEndpoint(1, endpoint1, true);
        Path.PathEndpoint pe2 = new Path.PathEndpoint(2, endpoint2, false);
        Path.PathEndpoint pe3 = new Path.PathEndpoint(3, endpoint3, true);

        path.addEndpoint(pe1);
        path.addEndpoint(pe2);
        path.addEndpoint(pe3);

        assertEquals(3, path.getEndpoints().size());
        assertEquals(pe1, path.getEndpoints().get(0));
        assertEquals(pe2, path.getEndpoints().get(1));
        assertEquals(pe3, path.getEndpoints().get(2));
    }

    /**
     * Tests getOrderedEndpoints returns endpoints sorted by sequence.
     *
     * Scenario:
     *  - Add endpoints in non-sequential order.
     *
     * Expected:
     *  - returns endpoints sorted by sequence number.
     *
     * @see Path#getOrderedEndpoints()
     */
    @Test
    public void getOrderedEndpoints_returnsInSequenceOrder() {
        Path path = new Path(1, "Test Path");
        Path.PathEndpoint pe3 = new Path.PathEndpoint(3, endpoint3, true);
        Path.PathEndpoint pe1 = new Path.PathEndpoint(1, endpoint1, true);
        Path.PathEndpoint pe2 = new Path.PathEndpoint(2, endpoint2, false);

        path.addEndpoint(pe3);
        path.addEndpoint(pe1);
        path.addEndpoint(pe2);

        List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();

        assertEquals(3, ordered.size());
        assertEquals(1, ordered.get(0).getSequence());
        assertEquals(2, ordered.get(1).getSequence());
        assertEquals(3, ordered.get(2).getSequence());
    }

    /**
     * Tests getStopEndpoints returns only endpoints where train must stop.
     *
     * Scenario:
     *  - Add three endpoints: two with mustStop=true, one with mustStop=false.
     *
     * Expected:
     *  - returns only the two stop endpoints, in sequence order.
     *
     * @see Path#getStopEndpoints()
     */
    @Test
    public void getStopEndpoints_returnsOnlyStopEndpoints() {
        Path path = new Path(1, "Test Path");
        Path.PathEndpoint pe1 = new Path.PathEndpoint(1, endpoint1, true);
        Path.PathEndpoint pe2 = new Path.PathEndpoint(2, endpoint2, false);
        Path.PathEndpoint pe3 = new Path.PathEndpoint(3, endpoint3, true);

        path.addEndpoint(pe1);
        path.addEndpoint(pe2);
        path.addEndpoint(pe3);

        List<Path.PathEndpoint> stops = path.getStopEndpoints();

        assertEquals(2, stops.size());
        assertEquals(endpoint1, stops.get(0).getEndpoint());
        assertEquals(endpoint3, stops.get(1).getEndpoint());
    }

    /**
     * Tests getStopEndpoints when no endpoints require stop.
     *
     * Scenario:
     *  - Add endpoints where all have mustStop=false.
     *
     * Expected:
     *  - returns an empty list.
     *
     * @see Path#getStopEndpoints()
     */
    @Test
    public void getStopEndpoints_noStops_returnsEmpty() {
        Path path = new Path(1, "Test Path");
        Path.PathEndpoint pe1 = new Path.PathEndpoint(1, endpoint1, false);
        Path.PathEndpoint pe2 = new Path.PathEndpoint(2, endpoint2, false);

        path.addEndpoint(pe1);
        path.addEndpoint(pe2);

        List<Path.PathEndpoint> stops = path.getStopEndpoints();

        assertTrue(stops.isEmpty());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a path with 2 endpoints and verify toString format.
     *
     * Expected:
     *  - format includes path id, name, and endpoint count.
     *
     * @see Path#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Path path = new Path(5, "Test Route");
        Path.PathEndpoint pe1 = new Path.PathEndpoint(1, endpoint1, true);
        Path.PathEndpoint pe2 = new Path.PathEndpoint(2, endpoint2, false);

        path.addEndpoint(pe1);
        path.addEndpoint(pe2);

        String result = path.toString();

        assertEquals("Path[id=5, name=Test Route, endpoints=2]", result);
    }

    // PathEndpoint tests

    /**
     * Tests PathEndpoint constructor.
     *
     * Scenario:
     *  - Create a PathEndpoint with known values.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see Path.PathEndpoint#PathEndpoint(int, Endpoint, boolean)
     */
    @Test
    public void pathEndpoint_constructor_initializesCorrectly() {
        Path.PathEndpoint pe = new Path.PathEndpoint(1, endpoint1, true);

        assertEquals(1, pe.getSequence());
        assertEquals(endpoint1, pe.getEndpoint());
        assertTrue(pe.isMustStop());
    }

    /**
     * Tests PathEndpoint getSequence.
     *
     * Scenario:
     *  - Create a PathEndpoint with sequence 3.
     *
     * Expected:
     *  - returns 3.
     *
     * @see Path.PathEndpoint#getSequence()
     */
    @Test
    public void pathEndpoint_getSequence_returnsCorrectValue() {
        Path.PathEndpoint pe = new Path.PathEndpoint(3, endpoint2, false);
        assertEquals(3, pe.getSequence());
    }

    /**
     * Tests PathEndpoint getEndpoint.
     *
     * Scenario:
     *  - Create a PathEndpoint with a known endpoint.
     *
     * Expected:
     *  - returns the correct endpoint.
     *
     * @see Path.PathEndpoint#getEndpoint()
     */
    @Test
    public void pathEndpoint_getEndpoint_returnsCorrectEndpoint() {
        Path.PathEndpoint pe = new Path.PathEndpoint(1, endpoint2, true);
        assertEquals(endpoint2, pe.getEndpoint());
    }

    /**
     * Tests PathEndpoint isMustStop when true.
     *
     * Scenario:
     *  - Create a PathEndpoint with mustStop=true.
     *
     * Expected:
     *  - returns true.
     *
     * @see Path.PathEndpoint#isMustStop()
     */
    @Test
    public void pathEndpoint_isMustStop_trueCase() {
        Path.PathEndpoint pe = new Path.PathEndpoint(1, endpoint1, true);
        assertTrue(pe.isMustStop());
    }

    /**
     * Tests PathEndpoint isMustStop when false.
     *
     * Scenario:
     *  - Create a PathEndpoint with mustStop=false.
     *
     * Expected:
     *  - returns false.
     *
     * @see Path.PathEndpoint#isMustStop()
     */
    @Test
    public void pathEndpoint_isMustStop_falseCase() {
        Path.PathEndpoint pe = new Path.PathEndpoint(2, endpoint2, false);
        assertFalse(pe.isMustStop());
    }

    /**
     * Tests PathEndpoint toString with stop required.
     *
     * Scenario:
     *  - Create a PathEndpoint with mustStop=true.
     *
     * Expected:
     *  - format includes sequence, endpoint name, and "[STOP]".
     *
     * @see Path.PathEndpoint#toString()
     */
    @Test
    public void pathEndpoint_toString_withStop() {
        Path.PathEndpoint pe = new Path.PathEndpoint(1, endpoint1, true);
        String result = pe.toString();

        assertEquals("1. Lisbon [STOP]", result);
    }

    /**
     * Tests PathEndpoint toString without stop.
     *
     * Scenario:
     *  - Create a PathEndpoint with mustStop=false.
     *
     * Expected:
     *  - format includes sequence, endpoint name, and "[PASS]".
     *
     * @see Path.PathEndpoint#toString()
     */
    @Test
    public void pathEndpoint_toString_withoutStop() {
        Path.PathEndpoint pe = new Path.PathEndpoint(2, endpoint2, false);
        String result = pe.toString();

        assertEquals("2. Coimbra [PASS]", result);
    }
}