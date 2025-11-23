package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Line}.
 * <p>
 * Each test method targets exactly one public member of {@code Line}
 * and documents the scenario, preconditions and expected outcome.
 */
public class LineTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see Line#Line(int, String, String, int, int, int, String, String)
     */
    @Test
    public void constructor_initializesAllFields() {
        Line line = new Line(1, "North Line", "IP", 10, 20, 1668,
                "Lisbon", "Porto");

        assertEquals(1, line.getId());
        assertEquals("North Line", line.getName());
        assertEquals(1668, line.getGauge());
        assertEquals("Lisbon", line.getEndpointAName());
        assertEquals("Porto", line.getEndpointBName());
        assertEquals(10, line.getEndpointA());
        assertEquals(20, line.getEndpointB());
    }

    /**
     * Tests the getter for line id.
     *
     * Scenario:
     *  - Create a line with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Line#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Line line = new Line(42, "Test Line", "Owner", 1, 2, 1668,
                "A", "B");
        assertEquals(42, line.getId());
    }

    /**
     * Tests the getter for line name.
     *
     * Scenario:
     *  - Create a line with a known name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Line#getName()
     */
    @Test
    public void getName_returnsCorrectName() {
        Line line = new Line(1, "South Line", "Owner", 1, 2, 1668,
                "A", "B");
        assertEquals("South Line", line.getName());
    }

    /**
     * Tests the getter for gauge.
     *
     * Scenario:
     *  - Create a line with a specific gauge and read it back.
     *
     * Expected:
     *  - returned gauge matches the constructor value.
     *
     * @see Line#getGauge()
     */
    @Test
    public void getGauge_returnsCorrectGauge() {
        Line line = new Line(1, "Line", "Owner", 1, 2, 1435,
                "A", "B");
        assertEquals(1435, line.getGauge());
    }

    /**
     * Tests the getter for endpoint A name.
     *
     * Scenario:
     *  - Create a line with a known endpoint A name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Line#getEndpointAName()
     */
    @Test
    public void getEndpointAName_returnsCorrectName() {
        Line line = new Line(1, "Line", "Owner", 1, 2, 1668,
                "Lisbon Santa Apolonia", "Porto Campanha");
        assertEquals("Lisbon Santa Apolonia", line.getEndpointAName());
    }

    /**
     * Tests the getter for endpoint B name.
     *
     * Scenario:
     *  - Create a line with a known endpoint B name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Line#getEndpointBName()
     */
    @Test
    public void getEndpointBName_returnsCorrectName() {
        Line line = new Line(1, "Line", "Owner", 1, 2, 1668,
                "Lisbon Santa Apolonia", "Porto Campanha");
        assertEquals("Porto Campanha", line.getEndpointBName());
    }

    /**
     * Tests the getter for endpoint A id.
     *
     * Scenario:
     *  - Create a line with a known endpoint A id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Line#getEndpointA()
     */
    @Test
    public void getEndpointA_returnsCorrectId() {
        Line line = new Line(1, "Line", "Owner", 100, 200, 1668,
                "A", "B");
        assertEquals(100, line.getEndpointA());
    }

    /**
     * Tests the getter for endpoint B id.
     *
     * Scenario:
     *  - Create a line with a known endpoint B id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Line#getEndpointB()
     */
    @Test
    public void getEndpointB_returnsCorrectId() {
        Line line = new Line(1, "Line", "Owner", 100, 200, 1668,
                "A", "B");
        assertEquals(200, line.getEndpointB());
    }

    /**
     * Tests toString method with standard gauge (1668mm).
     *
     * Scenario:
     *  - Create a line and verify toString format.
     *
     * Expected:
     *  - format includes name, endpoint names, and gauge in mm.
     *
     * @see Line#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Line line = new Line(1, "North Line", "IP", 10, 20, 1668,
                "Lisbon", "Porto");

        String result = line.toString();

        assertEquals("North Line: Lisbon → Porto (Gauge: 1668mm)", result);
    }

    /**
     * Tests toString method with narrow gauge (1000mm).
     *
     * Scenario:
     *  - Create a line with narrow gauge and verify toString format.
     *
     * Expected:
     *  - format correctly displays the narrow gauge.
     *
     * @see Line#toString()
     */
    @Test
    public void toString_narrowGauge() {
        Line line = new Line(2, "Narrow Line", "IP", 5, 6, 1000,
                "Aveiro", "Espinho");

        String result = line.toString();

        assertEquals("Narrow Line: Aveiro → Espinho (Gauge: 1000mm)", result);
    }

    /**
     * Tests toString method with international gauge (1435mm).
     *
     * Scenario:
     *  - Create a line with international gauge and verify toString format.
     *
     * Expected:
     *  - format correctly displays the international gauge.
     *
     * @see Line#toString()
     */
    @Test
    public void toString_internationalGauge() {
        Line line = new Line(3, "International Line", "IP", 7, 8, 1435,
                "Vilar Formoso", "Badajoz");

        String result = line.toString();

        assertEquals("International Line: Vilar Formoso → Badajoz (Gauge: 1435mm)", result);
    }
}