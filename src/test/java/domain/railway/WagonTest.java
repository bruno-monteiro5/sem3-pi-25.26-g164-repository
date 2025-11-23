package domain.railway;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Wagon}.
 * <p>
 * Each test method targets exactly one public member of {@code Wagon}
 * and documents the scenario, preconditions and expected outcome.
 */
public class WagonTest {

    private WagonModel testModel;

    @Before
    public void setUp() {
        testModel = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid id, serviceYear, model, and operatorVat supplied.
     *
     * Expected:
     *  - all getters return the supplied values.
     *
     * @see Wagon#Wagon(int, int, WagonModel, String)
     */
    @Test
    public void constructor_initializesAllFields() {
        Wagon wagon = new Wagon(1, 2020, testModel, "VAT123");

        assertEquals(1, wagon.getId());
        assertEquals(2020, wagon.getServiceYear());
        assertEquals(testModel, wagon.getModel());
        assertEquals("VAT123", wagon.getOperatorVat());
    }

    /**
     * Tests the getter for wagon id.
     *
     * Scenario:
     *  - Create a wagon with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Wagon#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Wagon wagon = new Wagon(42, 2021, testModel, "VAT456");
        assertEquals(42, wagon.getId());
    }

    /**
     * Tests the getter for service year.
     *
     * Scenario:
     *  - Create a wagon with a known service year and read it back.
     *
     * Expected:
     *  - returned service year matches the constructor value.
     *
     * @see Wagon#getServiceYear()
     */
    @Test
    public void getServiceYear_returnsCorrectYear() {
        Wagon wagon = new Wagon(1, 2018, testModel, "VAT123");
        assertEquals(2018, wagon.getServiceYear());
    }

    /**
     * Tests the getter for wagon model.
     *
     * Scenario:
     *  - Create a wagon with a specific model and read it back.
     *
     * Expected:
     *  - returned model matches the constructor value.
     *
     * @see Wagon#getModel()
     */
    @Test
    public void getModel_returnsCorrectModel() {
        WagonModel specificModel = new WagonModel(2, "Model2", "Maker2", 4, 15, 3, 4,
                30.0, 120, 70.0, 120.0, "TANK", 1668);
        Wagon wagon = new Wagon(1, 2020, specificModel, "VAT123");

        assertEquals(specificModel, wagon.getModel());
    }

    /**
     * Tests the getter for operator VAT.
     *
     * Scenario:
     *  - Create a wagon with a known operator VAT and read it back.
     *
     * Expected:
     *  - returned VAT matches the constructor value.
     *
     * @see Wagon#getOperatorVat()
     */
    @Test
    public void getOperatorVat_returnsCorrectVat() {
        Wagon wagon = new Wagon(1, 2020, testModel, "PT987654321");
        assertEquals("PT987654321", wagon.getOperatorVat());
    }

    /**
     * Tests getTotalWeight calculation.
     *
     * Scenario:
     *  - Create a wagon with model having weight=25.0 and payload=60.0.
     *
     * Expected:
     *  - returns 85 (25 + 60, cast to int).
     *
     * @see Wagon#getTotalWeight()
     */
    @Test
    public void getTotalWeight_calculatesCorrectly() {
        Wagon wagon = new Wagon(1, 2020, testModel, "VAT123");
        // testModel has weight=25.0, payload=60.0
        // Total = 85
        assertEquals(85, wagon.getTotalWeight());
    }

    /**
     * Tests getTotalWeight with different model.
     *
     * Scenario:
     *  - Create a wagon with heavier model (weight=30.0, payload=70.0).
     *
     * Expected:
     *  - returns 100.
     *
     * @see Wagon#getTotalWeight()
     */
    @Test
    public void getTotalWeight_differentModel_calculatesCorrectly() {
        WagonModel heavyModel = new WagonModel(2, "Heavy", "Maker", 4, 15, 3, 4,
                30.0, 120, 70.0, 120.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, heavyModel, "VAT123");

        assertEquals(100, wagon.getTotalWeight());
    }

    /**
     * Tests getTotalWeight with decimal values that round.
     *
     * Scenario:
     *  - Create a wagon with model having weight=25.5 and payload=60.3.
     *
     * Expected:
     *  - returns 85 (25.5 + 60.3 = 85.8, cast to int = 85).
     *
     * @see Wagon#getTotalWeight()
     */
    @Test
    public void getTotalWeight_withDecimals_truncates() {
        WagonModel model = new WagonModel(3, "Model", "Maker", 4, 15, 3, 4,
                25.5, 120, 60.3, 120.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, model, "VAT123");

        // 25.5 + 60.3 = 85.8 -> cast to int = 85
        assertEquals(85, wagon.getTotalWeight());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a wagon and verify toString format.
     *
     * Expected:
     *  - format includes wagon id and model type.
     *
     * @see Wagon#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Wagon wagon = new Wagon(5, 2020, testModel, "VAT123");
        String result = wagon.toString();

        assertEquals("Wagon 5 (FREIGHT)", result);
    }

    /**
     * Tests toString with different wagon type.
     *
     * Scenario:
     *  - Create a tank wagon and verify toString format.
     *
     * Expected:
     *  - format includes "TANK" type.
     *
     * @see Wagon#toString()
     */
    @Test
    public void toString_tankWagon_formatsCorrectly() {
        WagonModel tankModel = new WagonModel(2, "Tank", "Maker", 4, 15, 3, 4,
                30.0, 100, 50.0, 80.0, "TANK", 1668);
        Wagon wagon = new Wagon(10, 2019, tankModel, "VAT456");
        String result = wagon.toString();

        assertEquals("Wagon 10 (TANK)", result);
    }

    /**
     * Tests wagon with older service year.
     *
     * Scenario:
     *  - Create a wagon with service year 2005.
     *
     * Expected:
     *  - service year is correctly stored and retrieved.
     *
     * @see Wagon#getServiceYear()
     */
    @Test
    public void getServiceYear_olderWagon() {
        Wagon wagon = new Wagon(1, 2005, testModel, "VAT123");
        assertEquals(2005, wagon.getServiceYear());
    }

    /**
     * Tests wagon with recent service year.
     *
     * Scenario:
     *  - Create a wagon with service year 2024.
     *
     * Expected:
     *  - service year is correctly stored and retrieved.
     *
     * @see Wagon#getServiceYear()
     */
    @Test
    public void getServiceYear_recentWagon() {
        Wagon wagon = new Wagon(1, 2024, testModel, "VAT123");
        assertEquals(2024, wagon.getServiceYear());
    }
}