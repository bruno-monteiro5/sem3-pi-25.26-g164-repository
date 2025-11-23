package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link WagonModel}.
 * <p>
 * Each test method targets exactly one public member of {@code WagonModel}
 * and documents the scenario, preconditions and expected outcome.
 */
public class WagonModelTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see WagonModel#WagonModel(int, String, String, int, int, int, int, double, int, double, double, String, int)
     */
    @Test
    public void constructor_initializesAllFields() {
        WagonModel model = new WagonModel(1, "FreightWagon-X", "MakerCo", 4,
                15, 3, 4, 25.0, 100, 60.0, 100.0,
                "FREIGHT", 1668);

        assertEquals(1, model.getId());
        assertEquals("FreightWagon-X", model.getModel());
        assertEquals("MakerCo", model.getMaker());
        assertEquals(4, model.getNumberBogies());
        assertEquals(15, model.getLength());
        assertEquals(3, model.getWidth());
        assertEquals(4, model.getHeight());
        assertEquals(25.0, model.getWeight(), 0.001);
        assertEquals(100, model.getMaxSpeed());
        assertEquals(60.0, model.getPayload(), 0.001);
        assertEquals(100.0, model.getVolume(), 0.001);
        assertEquals("FREIGHT", model.getType());
        assertEquals(1668, model.getGauge());
    }

    /**
     * Tests the getter for model id.
     *
     * Scenario:
     *  - Create a wagon model with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see WagonModel#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        WagonModel model = new WagonModel(99, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(99, model.getId());
    }

    /**
     * Tests the getter for model name.
     *
     * Scenario:
     *  - Create a wagon model with a known name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see WagonModel#getModel()
     */
    @Test
    public void getModel_returnsCorrectName() {
        WagonModel model = new WagonModel(1, "TankCarrier-Pro", "Maker", 4, 15, 3, 4,
                30.0, 100, 50.0, 80.0, "TANK", 1668);
        assertEquals("TankCarrier-Pro", model.getModel());
    }

    /**
     * Tests the getter for maker.
     *
     * Scenario:
     *  - Create a wagon model with a known maker and read it back.
     *
     * Expected:
     *  - returned maker matches the constructor value.
     *
     * @see WagonModel#getMaker()
     */
    @Test
    public void getMaker_returnsCorrectMaker() {
        WagonModel model = new WagonModel(1, "Model", "IndustrialMakers Ltd", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals("IndustrialMakers Ltd", model.getMaker());
    }

    /**
     * Tests the getter for number of bogies.
     *
     * Scenario:
     *  - Create a wagon model with 2 bogies and read it back.
     *
     * Expected:
     *  - returns 2.
     *
     * @see WagonModel#getNumberBogies()
     */
    @Test
    public void getNumberBogies_returnsCorrectNumber() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 2, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(2, model.getNumberBogies());
    }

    /**
     * Tests the getter for length in meters.
     *
     * Scenario:
     *  - Create a wagon model with length 20m and read it back.
     *
     * Expected:
     *  - returns 20.
     *
     * @see WagonModel#getLength()
     */
    @Test
    public void getLength_returnsCorrectLength() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 20, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(20, model.getLength());
    }

    /**
     * Tests the getter for width in meters.
     *
     * Scenario:
     *  - Create a wagon model with width 3.5m and read it back.
     *
     * Expected:
     *  - returns 3 (stored as int).
     *
     * @see WagonModel#getWidth()
     */
    @Test
    public void getWidth_returnsCorrectWidth() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(3, model.getWidth());
    }

    /**
     * Tests the getter for height in meters.
     *
     * Scenario:
     *  - Create a wagon model with height 4m and read it back.
     *
     * Expected:
     *  - returns 4.
     *
     * @see WagonModel#getHeight()
     */
    @Test
    public void getHeight_returnsCorrectHeight() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 5,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(5, model.getHeight());
    }

    /**
     * Tests the getter for weight (tare) in tonnes.
     *
     * Scenario:
     *  - Create a wagon model with weight 28.5 tonnes and read it back.
     *
     * Expected:
     *  - returns 28.5.
     *
     * @see WagonModel#getWeight()
     */
    @Test
    public void getWeight_returnsCorrectWeight() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                28.5, 100, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(28.5, model.getWeight(), 0.001);
    }

    /**
     * Tests the getter for maximum speed in km/h.
     *
     * Scenario:
     *  - Create a wagon model with max speed 120 km/h and read it back.
     *
     * Expected:
     *  - returns 120.
     *
     * @see WagonModel#getMaxSpeed()
     */
    @Test
    public void getMaxSpeed_returnsCorrectSpeed() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 120, 60.0, 100.0, "FREIGHT", 1668);
        assertEquals(120, model.getMaxSpeed());
    }

    /**
     * Tests the getter for payload capacity in tonnes.
     *
     * Scenario:
     *  - Create a wagon model with payload 65.5 tonnes and read it back.
     *
     * Expected:
     *  - returns 65.5.
     *
     * @see WagonModel#getPayload()
     */
    @Test
    public void getPayload_returnsCorrectPayload() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 65.5, 100.0, "FREIGHT", 1668);
        assertEquals(65.5, model.getPayload(), 0.001);
    }

    /**
     * Tests the getter for volume capacity in cubic meters.
     *
     * Scenario:
     *  - Create a wagon model with volume 120.5 m³ and read it back.
     *
     * Expected:
     *  - returns 120.5.
     *
     * @see WagonModel#getVolume()
     */
    @Test
    public void getVolume_returnsCorrectVolume() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 120.5, "FREIGHT", 1668);
        assertEquals(120.5, model.getVolume(), 0.001);
    }

    /**
     * Tests the getter for wagon type.
     *
     * Scenario:
     *  - Create a wagon model with type "TANK" and read it back.
     *
     * Expected:
     *  - returns "TANK".
     *
     * @see WagonModel#getType()
     */
    @Test
    public void getType_returnsCorrectType() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                30.0, 100, 50.0, 80.0, "TANK", 1668);
        assertEquals("TANK", model.getType());
    }

    /**
     * Tests the getter for gauge in mm.
     *
     * Scenario:
     *  - Create a wagon model with gauge 1435mm and read it back.
     *
     * Expected:
     *  - returns 1435.
     *
     * @see WagonModel#getGauge()
     */
    @Test
    public void getGauge_returnsCorrectGauge() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1435);
        assertEquals(1435, model.getGauge());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a wagon model and verify toString format.
     *
     * Expected:
     *  - format includes model, maker, and type.
     *
     * @see WagonModel#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        WagonModel model = new WagonModel(1, "FreightMax-2000", "RailCo", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        String result = model.toString();

        assertEquals("FreightMax-2000 (RailCo) - FREIGHT", result);
    }

    /**
     * Tests toString with tank wagon type.
     *
     * Scenario:
     *  - Create a tank wagon model and verify toString format.
     *
     * Expected:
     *  - format includes "TANK" type.
     *
     * @see WagonModel#toString()
     */
    @Test
    public void toString_tankWagon_formatsCorrectly() {
        WagonModel model = new WagonModel(2, "TankPro-500", "TankMakers", 4, 15, 3, 4,
                30.0, 100, 50.0, 80.0, "TANK", 1668);
        String result = model.toString();

        assertEquals("TankPro-500 (TankMakers) - TANK", result);
    }

    /**
     * Tests wagon model with international gauge.
     *
     * Scenario:
     *  - Create a wagon model with gauge 1435mm (international standard).
     *
     * Expected:
     *  - gauge is correctly stored and retrieved.
     *
     * @see WagonModel#getGauge()
     */
    @Test
    public void getGauge_internationalGauge() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1435);
        assertEquals(1435, model.getGauge());
    }

    /**
     * Tests wagon model with narrow gauge.
     *
     * Scenario:
     *  - Create a wagon model with gauge 1000mm (narrow gauge).
     *
     * Expected:
     *  - gauge is correctly stored and retrieved.
     *
     * @see WagonModel#getGauge()
     */
    @Test
    public void getGauge_narrowGauge() {
        WagonModel model = new WagonModel(1, "Model", "Maker", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1000);
        assertEquals(1000, model.getGauge());
    }
}