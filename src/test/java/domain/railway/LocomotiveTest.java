package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Locomotive}.
 * <p>
 * Each test method targets exactly one public member of {@code Locomotive}
 * and documents the scenario, preconditions and expected outcome.
 */
public class LocomotiveTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see Locomotive#Locomotive(int, String, String, String, int, int, int, String, int, int, int)
     */
    @Test
    public void constructor_initializesAllFields() {
        Locomotive loco = new Locomotive(1, "Loco1", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);

        assertEquals(1, loco.getId());
        assertEquals("Loco1", loco.getName());
        assertEquals("Siemens", loco.getMake());
        assertEquals("Vectron", loco.getModel());
        assertEquals(6000, loco.getPower());
        assertEquals(200, loco.getMaxSpeed());
        assertEquals(160, loco.getOpSpeed());
        assertEquals("ELECTRIC", loco.getTractionType());
        assertEquals(80, loco.getTotalWeight());
        assertEquals(1668, loco.getGauge());
        assertEquals(20000, loco.getLength());
    }

    /**
     * Tests the getter for locomotive id.
     *
     * Scenario:
     *  - Create a locomotive with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Locomotive#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Locomotive loco = new Locomotive(42, "Test", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertEquals(42, loco.getId());
    }

    /**
     * Tests the getter for locomotive name.
     *
     * Scenario:
     *  - Create a locomotive with a known name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Locomotive#getName()
     */
    @Test
    public void getName_returnsCorrectName() {
        Locomotive loco = new Locomotive(1, "PowerLoco", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertEquals("PowerLoco", loco.getName());
    }

    /**
     * Tests the getter for locomotive make (manufacturer).
     *
     * Scenario:
     *  - Create a locomotive with a known make and read it back.
     *
     * Expected:
     *  - returned make matches the constructor value.
     *
     * @see Locomotive#getMake()
     */
    @Test
    public void getMake_returnsCorrectMake() {
        Locomotive loco = new Locomotive(1, "Loco", "General Electric", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertEquals("General Electric", loco.getMake());
    }

    /**
     * Tests the getter for locomotive model.
     *
     * Scenario:
     *  - Create a locomotive with a known model and read it back.
     *
     * Expected:
     *  - returned model matches the constructor value.
     *
     * @see Locomotive#getModel()
     */
    @Test
    public void getModel_returnsCorrectModel() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Vectron MS",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertEquals("Vectron MS", loco.getModel());
    }

    /**
     * Tests the getter for locomotive power in kW.
     *
     * Scenario:
     *  - Create a locomotive with a specific power and read it back.
     *
     * Expected:
     *  - returned power matches the constructor value.
     *
     * @see Locomotive#getPower()
     */
    @Test
    public void getPower_returnsCorrectPower() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                8000, 120, 100, "ELECTRIC", 100, 1668, 20000);
        assertEquals(8000, loco.getPower());
    }

    /**
     * Tests the getter for maximum speed.
     *
     * Scenario:
     *  - Create a locomotive with a specific max speed and read it back.
     *
     * Expected:
     *  - returned max speed matches the constructor value.
     *
     * @see Locomotive#getMaxSpeed()
     */
    @Test
    public void getMaxSpeed_returnsCorrectSpeed() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 220, 180, "ELECTRIC", 80, 1668, 20000);
        assertEquals(220, loco.getMaxSpeed());
    }

    /**
     * Tests the getter for operational speed.
     *
     * Scenario:
     *  - Create a locomotive with a specific operational speed and read it back.
     *
     * Expected:
     *  - returned operational speed matches the constructor value.
     *
     * @see Locomotive#getOpSpeed()
     */
    @Test
    public void getOpSpeed_returnsCorrectSpeed() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 175, "ELECTRIC", 80, 1668, 20000);
        assertEquals(175, loco.getOpSpeed());
    }

    /**
     * Tests the getter for traction type.
     *
     * Scenario:
     *  - Create a locomotive with a specific traction type and read it back.
     *
     * Expected:
     *  - returned traction type matches the constructor value.
     *
     * @see Locomotive#getTractionType()
     */
    @Test
    public void getTractionType_returnsCorrectType() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertEquals("DIESEL", loco.getTractionType());
    }

    /**
     * Tests the getter for total weight in tonnes.
     *
     * Scenario:
     *  - Create a locomotive with a specific weight and read it back.
     *
     * Expected:
     *  - returned weight matches the constructor value.
     *
     * @see Locomotive#getTotalWeight()
     */
    @Test
    public void getTotalWeight_returnsCorrectWeight() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 90, 1668, 20000);
        assertEquals(90, loco.getTotalWeight());
    }

    /**
     * Tests the getter for gauge in mm.
     *
     * Scenario:
     *  - Create a locomotive with a specific gauge and read it back.
     *
     * Expected:
     *  - returned gauge matches the constructor value.
     *
     * @see Locomotive#getGauge()
     */
    @Test
    public void getGauge_returnsCorrectGauge() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1435, 18000);
        assertEquals(1435, loco.getGauge());
    }

    /**
     * Tests the getter for locomotive length in mm.
     *
     * Scenario:
     *  - Create a locomotive with a specific length and read it back.
     *
     * Expected:
     *  - returned length matches the constructor value.
     *
     * @see Locomotive#getLength()
     */
    @Test
    public void getLength_returnsCorrectLength() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 22000);
        assertEquals(22000, loco.getLength());
    }

    /**
     * Tests isElectric method for an electric locomotive.
     *
     * Scenario:
     *  - Create an electric locomotive and check if it's electric.
     *
     * Expected:
     *  - returns true.
     *
     * @see Locomotive#isElectric()
     */
    @Test
    public void isElectric_electricLocomotive_returnsTrue() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        assertTrue(loco.isElectric());
    }

    /**
     * Tests isElectric method for a diesel locomotive.
     *
     * Scenario:
     *  - Create a diesel locomotive and check if it's electric.
     *
     * Expected:
     *  - returns false.
     *
     * @see Locomotive#isElectric()
     */
    @Test
    public void isElectric_dieselLocomotive_returnsFalse() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        assertFalse(loco.isElectric());
    }

    /**
     * Tests isElectric method with mixed case traction type.
     *
     * Scenario:
     *  - Create a locomotive with "Electric" (mixed case) traction type.
     *
     * Expected:
     *  - returns true (case-insensitive comparison).
     *
     * @see Locomotive#isElectric()
     */
    @Test
    public void isElectric_mixedCaseElectric_returnsTrue() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 160, "Electric", 80, 1668, 20000);
        assertTrue(loco.isElectric());
    }

    /**
     * Tests isElectric method with lowercase traction type.
     *
     * Scenario:
     *  - Create a locomotive with "electric" (lowercase) traction type.
     *
     * Expected:
     *  - returns true (case-insensitive comparison).
     *
     * @see Locomotive#isElectric()
     */
    @Test
    public void isElectric_lowercaseElectric_returnsTrue() {
        Locomotive loco = new Locomotive(1, "Loco", "Maker", "Model",
                6000, 200, 160, "electric", 80, 1668, 20000);
        assertTrue(loco.isElectric());
    }

    /**
     * Tests toString method for electric locomotive.
     *
     * Scenario:
     *  - Create an electric locomotive and verify toString format.
     *
     * Expected:
     *  - format includes name, make, model, traction type, power, speeds, and length.
     *
     * @see Locomotive#toString()
     */
    @Test
    public void toString_electricLocomotive_formatsCorrectly() {
        Locomotive loco = new Locomotive(1, "Vectron-1", "Siemens", "Vectron MS",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);

        String result = loco.toString();

        assertTrue(result.contains("Vectron-1"));
        assertTrue(result.contains("Siemens"));
        assertTrue(result.contains("Vectron MS"));
        assertTrue(result.contains("ELECTRIC"));
        assertTrue(result.contains("6000kW"));
        assertTrue(result.contains("Max: 200km/h"));
        assertTrue(result.contains("Op: 160km/h"));
        assertTrue(result.contains("Length: 20000mm"));
    }

    /**
     * Tests toString method for diesel locomotive.
     *
     * Scenario:
     *  - Create a diesel locomotive and verify toString format.
     *
     * Expected:
     *  - format includes all relevant information including DIESEL type.
     *
     * @see Locomotive#toString()
     */
    @Test
    public void toString_dieselLocomotive_formatsCorrectly() {
        Locomotive loco = new Locomotive(2, "PowerHaul", "GE", "PH37ACai",
                2400, 120, 100, "DIESEL", 120, 1668, 18000);

        String result = loco.toString();

        assertTrue(result.contains("PowerHaul"));
        assertTrue(result.contains("GE"));
        assertTrue(result.contains("PH37ACai"));
        assertTrue(result.contains("DIESEL"));
        assertTrue(result.contains("2400kW"));
        assertTrue(result.contains("Max: 120km/h"));
        assertTrue(result.contains("Op: 100km/h"));
        assertTrue(result.contains("Length: 18000mm"));
    }
}