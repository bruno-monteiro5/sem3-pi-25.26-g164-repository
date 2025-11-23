package domain.railway;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Freight}.
 * <p>
 * Each test method targets exactly one public member of {@code Freight}
 * and documents the scenario, preconditions and expected outcome.
 */
public class FreightTest {

    private Endpoint originEndpoint;
    private Endpoint destinationEndpoint;
    private LocalDate testDate;

    @Before
    public void setUp() {
        originEndpoint = new Endpoint(1, "Lisbon", 100);
        destinationEndpoint = new Endpoint(2, "Porto", 200);
        testDate = LocalDate.of(2025, 1, 15);
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid id, date, origin, and destination supplied.
     *
     * Expected:
     *  - all getters return the supplied values;
     *  - wagons list is empty.
     *
     * @see Freight#Freight(int, LocalDate, Endpoint, Endpoint)
     */
    @Test
    public void constructor_initializesAllFields() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);

        assertEquals(1, freight.getId());
        assertEquals(testDate, freight.getDate());
        assertEquals(originEndpoint, freight.getOrigin());
        assertEquals(destinationEndpoint, freight.getDestination());
        assertTrue(freight.getWagons().isEmpty());
    }

    /**
     * Tests the getter for freight id.
     *
     * Scenario:
     *  - Create a freight with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Freight#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Freight freight = new Freight(42, testDate, originEndpoint, destinationEndpoint);
        assertEquals(42, freight.getId());
    }

    /**
     * Tests the getter for freight date.
     *
     * Scenario:
     *  - Create a freight with a known date and read it back.
     *
     * Expected:
     *  - returned date matches the constructor value.
     *
     * @see Freight#getDate()
     */
    @Test
    public void getDate_returnsCorrectDate() {
        LocalDate specificDate = LocalDate.of(2025, 12, 25);
        Freight freight = new Freight(1, specificDate, originEndpoint, destinationEndpoint);
        assertEquals(specificDate, freight.getDate());
    }

    /**
     * Tests the getter for origin endpoint.
     *
     * Scenario:
     *  - Create a freight with a known origin and read it back.
     *
     * Expected:
     *  - returned origin matches the constructor value.
     *
     * @see Freight#getOrigin()
     */
    @Test
    public void getOrigin_returnsCorrectEndpoint() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        assertEquals(originEndpoint, freight.getOrigin());
    }

    /**
     * Tests the getter for destination endpoint.
     *
     * Scenario:
     *  - Create a freight with a known destination and read it back.
     *
     * Expected:
     *  - returned destination matches the constructor value.
     *
     * @see Freight#getDestination()
     */
    @Test
    public void getDestination_returnsCorrectEndpoint() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        assertEquals(destinationEndpoint, freight.getDestination());
    }

    /**
     * Tests the getter for wagons list.
     *
     * Scenario:
     *  - Create a freight and verify the wagons list is initially empty.
     *
     * Expected:
     *  - returns an empty list.
     *
     * @see Freight#getWagons()
     */
    @Test
    public void getWagons_initiallyEmpty() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        assertTrue(freight.getWagons().isEmpty());
    }

    /**
     * Tests adding a wagon to the freight.
     *
     * Scenario:
     *  - Create a freight and add one wagon.
     *
     * Expected:
     *  - wagons list contains the added wagon.
     *
     * @see Freight#addWagon(Wagon)
     */
    @Test
    public void addWagon_addsWagonToList() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        WagonModel model = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, model, "VAT123");

        freight.addWagon(wagon);

        assertEquals(1, freight.getWagons().size());
        assertTrue(freight.getWagons().contains(wagon));
    }

    /**
     * Tests adding multiple wagons to the freight.
     *
     * Scenario:
     *  - Create a freight and add three wagons.
     *
     * Expected:
     *  - wagons list contains all three wagons in order.
     *
     * @see Freight#addWagon(Wagon)
     */
    @Test
    public void addWagon_multipleWagons() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        WagonModel model = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);

        Wagon wagon1 = new Wagon(1, 2020, model, "VAT123");
        Wagon wagon2 = new Wagon(2, 2021, model, "VAT456");
        Wagon wagon3 = new Wagon(3, 2022, model, "VAT789");

        freight.addWagon(wagon1);
        freight.addWagon(wagon2);
        freight.addWagon(wagon3);

        assertEquals(3, freight.getWagons().size());
        assertEquals(wagon1, freight.getWagons().get(0));
        assertEquals(wagon2, freight.getWagons().get(1));
        assertEquals(wagon3, freight.getWagons().get(2));
    }

    /**
     * Tests getTotalWeight with no wagons.
     *
     * Scenario:
     *  - Create a freight without wagons.
     *
     * Expected:
     *  - returns 0.
     *
     * @see Freight#getTotalWeight()
     */
    @Test
    public void getTotalWeight_noWagons_returnsZero() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        assertEquals(0, freight.getTotalWeight());
    }

    /**
     * Tests getTotalWeight with one wagon.
     *
     * Scenario:
     *  - Create a freight with one wagon having specific weight and payload.
     *
     * Expected:
     *  - returns the sum of weight and payload.
     *
     * @see Freight#getTotalWeight()
     */
    @Test
    public void getTotalWeight_oneWagon_returnsCorrectWeight() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);
        WagonModel model = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, model, "VAT123");

        freight.addWagon(wagon);

        // Weight = 25.0 + 60.0 = 85.0, cast to int = 85
        assertEquals(85, freight.getTotalWeight());
    }

    /**
     * Tests getTotalWeight with multiple wagons.
     *
     * Scenario:
     *  - Create a freight with three wagons.
     *
     * Expected:
     *  - returns the sum of all wagons' total weights.
     *
     * @see Freight#getTotalWeight()
     */
    @Test
    public void getTotalWeight_multipleWagons_returnsSumOfWeights() {
        Freight freight = new Freight(1, testDate, originEndpoint, destinationEndpoint);

        WagonModel model1 = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                20.0, 100, 50.0, 100.0, "FREIGHT", 1668);
        WagonModel model2 = new WagonModel(2, "Model2", "Maker2", 4, 15, 3, 4,
                30.0, 100, 70.0, 100.0, "FREIGHT", 1668);
        WagonModel model3 = new WagonModel(3, "Model3", "Maker3", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);

        freight.addWagon(new Wagon(1, 2020, model1, "VAT123"));
        freight.addWagon(new Wagon(2, 2021, model2, "VAT456"));
        freight.addWagon(new Wagon(3, 2022, model3, "VAT789"));

        // (20+50) + (30+70) + (25+60) = 70 + 100 + 85 = 255
        assertEquals(255, freight.getTotalWeight());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a freight with 2 wagons and verify toString format.
     *
     * Expected:
     *  - format includes freight id, origin, destination, and wagon count.
     *
     * @see Freight#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Freight freight = new Freight(5, testDate, originEndpoint, destinationEndpoint);
        WagonModel model = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);

        freight.addWagon(new Wagon(1, 2020, model, "VAT123"));
        freight.addWagon(new Wagon(2, 2021, model, "VAT456"));

        String result = freight.toString();

        assertTrue(result.contains("Freight 5"));
        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("2 wagons"));
    }
}