package domain.railway;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Train}.
 * <p>
 * Each test method targets exactly one public member of {@code Train}
 * and documents the scenario, preconditions and expected outcome.
 */
public class TrainTest {

    private Endpoint startEndpoint;
    private Endpoint endEndpoint;
    private Path testPath;
    private LocalTime departureTime;
    private LocalDate date;

    @Before
    public void setUp() {
        startEndpoint = new Endpoint(1, "Lisbon", 100);
        endEndpoint = new Endpoint(2, "Porto", 200);
        testPath = new Path(1, "North Line");
        departureTime = LocalTime.of(10, 30);
        date = LocalDate.of(2025, 1, 15);
    }

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid parameters supplied for all fields.
     *
     * Expected:
     *  - all getters return the supplied values;
     *  - locomotives and freights lists are empty.
     *
     * @see Train#Train(int, String, LocalTime, LocalDate, Path, Endpoint, Endpoint)
     */
    @Test
    public void constructor_initializesAllFields() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);

        assertEquals(1, train.getId());
        assertEquals("VAT123", train.getOperatorVat());
        assertEquals(departureTime, train.getDepartureTime());
        assertEquals(date, train.getDate());
        assertEquals(testPath, train.getPath());
        assertEquals(startEndpoint, train.getStartEndpoint());
        assertEquals(endEndpoint, train.getEndEndpoint());
        assertTrue(train.getLocomotives().isEmpty());
        assertTrue(train.getFreights().isEmpty());
    }

    /**
     * Tests the getter for train id.
     *
     * Scenario:
     *  - Create a train with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Train#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Train train = new Train(42, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertEquals(42, train.getId());
    }

    /**
     * Tests the getter for operator VAT.
     *
     * Scenario:
     *  - Create a train with a known VAT and read it back.
     *
     * Expected:
     *  - returned VAT matches the constructor value.
     *
     * @see Train#getOperatorVat()
     */
    @Test
    public void getOperatorVat_returnsCorrectVat() {
        Train train = new Train(1, "PT987654321", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertEquals("PT987654321", train.getOperatorVat());
    }

    /**
     * Tests the getter for departure time.
     *
     * Scenario:
     *  - Create a train with a specific departure time and read it back.
     *
     * Expected:
     *  - returned time matches the constructor value.
     *
     * @see Train#getDepartureTime()
     */
    @Test
    public void getDepartureTime_returnsCorrectTime() {
        LocalTime specificTime = LocalTime.of(14, 45);
        Train train = new Train(1, "VAT123", specificTime, date,
                testPath, startEndpoint, endEndpoint);
        assertEquals(specificTime, train.getDepartureTime());
    }

    /**
     * Tests the getter for date.
     *
     * Scenario:
     *  - Create a train with a specific date and read it back.
     *
     * Expected:
     *  - returned date matches the constructor value.
     *
     * @see Train#getDate()
     */
    @Test
    public void getDate_returnsCorrectDate() {
        LocalDate specificDate = LocalDate.of(2025, 12, 25);
        Train train = new Train(1, "VAT123", departureTime, specificDate,
                testPath, startEndpoint, endEndpoint);
        assertEquals(specificDate, train.getDate());
    }

    /**
     * Tests the setter for path.
     *
     * Scenario:
     *  - Create a train and change its path.
     *
     * Expected:
     *  - the new path is persisted.
     *
     * @see Train#setPath(Path)
     * @see Train#getPath()
     */
    @Test
    public void setPath_updatesPath() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Path newPath = new Path(2, "Alternative Route");

        train.setPath(newPath);

        assertEquals(newPath, train.getPath());
    }

    /**
     * Tests the setter for start endpoint.
     *
     * Scenario:
     *  - Create a train and change its start endpoint.
     *
     * Expected:
     *  - the new start endpoint is persisted.
     *
     * @see Train#setStartEndpoint(Endpoint)
     * @see Train#getStartEndpoint()
     */
    @Test
    public void setStartEndpoint_updatesEndpoint() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Endpoint newStart = new Endpoint(3, "Coimbra", 300);

        train.setStartEndpoint(newStart);

        assertEquals(newStart, train.getStartEndpoint());
    }

    /**
     * Tests the setter for end endpoint.
     *
     * Scenario:
     *  - Create a train and change its end endpoint.
     *
     * Expected:
     *  - the new end endpoint is persisted.
     *
     * @see Train#setEndEndpoint(Endpoint)
     * @see Train#getEndEndpoint()
     */
    @Test
    public void setEndEndpoint_updatesEndpoint() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Endpoint newEnd = new Endpoint(4, "Braga", 400);

        train.setEndEndpoint(newEnd);

        assertEquals(newEnd, train.getEndEndpoint());
    }

    /**
     * Tests the setter for date.
     *
     * Scenario:
     *  - Create a train and change its date.
     *
     * Expected:
     *  - the new date is persisted.
     *
     * @see Train#setDate(LocalDate)
     * @see Train#getDate()
     */
    @Test
    public void setDate_updatesDate() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        LocalDate newDate = LocalDate.of(2026, 6, 15);

        train.setDate(newDate);

        assertEquals(newDate, train.getDate());
    }

    /**
     * Tests the setter for departure time.
     *
     * Scenario:
     *  - Create a train and change its departure time.
     *
     * Expected:
     *  - the new time is persisted.
     *
     * @see Train#setDepartureTime(LocalTime)
     * @see Train#getDepartureTime()
     */
    @Test
    public void setDepartureTime_updatesTime() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        LocalTime newTime = LocalTime.of(18, 20);

        train.setDepartureTime(newTime);

        assertEquals(newTime, train.getDepartureTime());
    }

    /**
     * Tests adding a locomotive to the train.
     *
     * Scenario:
     *  - Create a train and add one locomotive.
     *
     * Expected:
     *  - locomotives list contains the added locomotive.
     *
     * @see Train#addLocomotive(Locomotive)
     */
    @Test
    public void addLocomotive_addsLocomotiveToList() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);

        train.addLocomotive(loco);

        assertEquals(1, train.getLocomotives().size());
        assertTrue(train.getLocomotives().contains(loco));
    }

    /**
     * Tests adding a freight to the train.
     *
     * Scenario:
     *  - Create a train and add one freight.
     *
     * Expected:
     *  - freights list contains the added freight.
     *
     * @see Train#addFreight(Freight)
     */
    @Test
    public void addFreight_addsFreightToList() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Freight freight = new Freight(1, date, startEndpoint, endEndpoint);

        train.addFreight(freight);

        assertEquals(1, train.getFreights().size());
        assertTrue(train.getFreights().contains(freight));
    }

    /**
     * Tests getTotalPower with no locomotives.
     *
     * Scenario:
     *  - Create a train without locomotives.
     *
     * Expected:
     *  - returns 0.
     *
     * @see Train#getTotalPower()
     */
    @Test
    public void getTotalPower_noLocomotives_returnsZero() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertEquals(0, train.getTotalPower());
    }

    /**
     * Tests getTotalPower with one locomotive.
     *
     * Scenario:
     *  - Create a train with one locomotive of 6000 kW.
     *
     * Expected:
     *  - returns 6000.
     *
     * @see Train#getTotalPower()
     */
    @Test
    public void getTotalPower_oneLocomotive_returnsCorrectPower() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        train.addLocomotive(loco);

        assertEquals(6000, train.getTotalPower());
    }

    /**
     * Tests getTotalPower with multiple locomotives.
     *
     * Scenario:
     *  - Create a train with two locomotives of 6000 kW and 3000 kW.
     *
     * Expected:
     *  - returns 9000.
     *
     * @see Train#getTotalPower()
     */
    @Test
    public void getTotalPower_multipleLocomotives_returnsSumOfPower() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco1 = new Locomotive(1, "Loco1", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        Locomotive loco2 = new Locomotive(2, "Loco2", "GE", "PowerHaul",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        train.addLocomotive(loco1);
        train.addLocomotive(loco2);

        assertEquals(9000, train.getTotalPower());
    }

    /**
     * Tests getTotalWeight with no locomotives or freights.
     *
     * Scenario:
     *  - Create an empty train.
     *
     * Expected:
     *  - returns 0.
     *
     * @see Train#getTotalWeight()
     */
    @Test
    public void getTotalWeight_emptyTrain_returnsZero() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertEquals(0, train.getTotalWeight());
    }

    /**
     * Tests getTotalWeight with locomotives only.
     *
     * Scenario:
     *  - Create a train with locomotives but no freights.
     *
     * Expected:
     *  - returns sum of locomotive weights.
     *
     * @see Train#getTotalWeight()
     */
    @Test
    public void getTotalWeight_locomotivesOnly_returnsLocomotiveWeight() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco1 = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        Locomotive loco2 = new Locomotive(2, "Loco2", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        train.addLocomotive(loco1);
        train.addLocomotive(loco2);

        assertEquals(180, train.getTotalWeight()); // 80 + 100
    }

    /**
     * Tests getTotalWeight with locomotives and freight.
     *
     * Scenario:
     *  - Create a train with locomotives and wagons.
     *
     * Expected:
     *  - returns sum of all weights.
     *
     * @see Train#getTotalWeight()
     */
    @Test
    public void getTotalWeight_withFreight_returnsTotalWeight() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        train.addLocomotive(loco);

        Freight freight = new Freight(1, date, startEndpoint, endEndpoint);
        WagonModel model = new WagonModel(1, "Model1", "Maker1", 4, 15, 3, 4,
                25.0, 100, 60.0, 100.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, model, "VAT123");
        freight.addWagon(wagon);
        train.addFreight(freight);

        // 80 (loco) + 85 (wagon: 25 + 60)
        assertEquals(165, train.getTotalWeight());
    }

    /**
     * Tests hasElectricLocomotive with no locomotives.
     *
     * Scenario:
     *  - Create a train without locomotives.
     *
     * Expected:
     *  - returns false.
     *
     * @see Train#hasElectricLocomotive()
     */
    @Test
    public void hasElectricLocomotive_noLocomotives_returnsFalse() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertFalse(train.hasElectricLocomotive());
    }

    /**
     * Tests hasElectricLocomotive with only diesel locomotives.
     *
     * Scenario:
     *  - Create a train with only diesel locomotives.
     *
     * Expected:
     *  - returns false.
     *
     * @see Train#hasElectricLocomotive()
     */
    @Test
    public void hasElectricLocomotive_dieselOnly_returnsFalse() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        train.addLocomotive(loco);

        assertFalse(train.hasElectricLocomotive());
    }

    /**
     * Tests hasElectricLocomotive with at least one electric locomotive.
     *
     * Scenario:
     *  - Create a train with one electric locomotive.
     *
     * Expected:
     *  - returns true.
     *
     * @see Train#hasElectricLocomotive()
     */
    @Test
    public void hasElectricLocomotive_hasElectric_returnsTrue() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        train.addLocomotive(loco);

        assertTrue(train.hasElectricLocomotive());
    }

    /**
     * Tests hasElectricLocomotive with mixed locomotives.
     *
     * Scenario:
     *  - Create a train with both electric and diesel locomotives.
     *
     * Expected:
     *  - returns true.
     *
     * @see Train#hasElectricLocomotive()
     */
    @Test
    public void hasElectricLocomotive_mixed_returnsTrue() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive electric = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        Locomotive diesel = new Locomotive(2, "Loco2", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        train.addLocomotive(electric);
        train.addLocomotive(diesel);

        assertTrue(train.hasElectricLocomotive());
    }

    /**
     * Tests isFullyElectric with no locomotives.
     *
     * Scenario:
     *  - Create a train without locomotives.
     *
     * Expected:
     *  - returns false.
     *
     * @see Train#isFullyElectric()
     */
    @Test
    public void isFullyElectric_noLocomotives_returnsFalse() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        assertFalse(train.isFullyElectric());
    }

    /**
     * Tests isFullyElectric with all electric locomotives.
     *
     * Scenario:
     *  - Create a train with only electric locomotives.
     *
     * Expected:
     *  - returns true.
     *
     * @see Train#isFullyElectric()
     */
    @Test
    public void isFullyElectric_allElectric_returnsTrue() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco1 = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        Locomotive loco2 = new Locomotive(2, "Loco2", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        train.addLocomotive(loco1);
        train.addLocomotive(loco2);

        assertTrue(train.isFullyElectric());
    }

    /**
     * Tests isFullyElectric with mixed locomotives.
     *
     * Scenario:
     *  - Create a train with both electric and diesel locomotives.
     *
     * Expected:
     *  - returns false.
     *
     * @see Train#isFullyElectric()
     */
    @Test
    public void isFullyElectric_mixed_returnsFalse() {
        Train train = new Train(1, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive electric = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        Locomotive diesel = new Locomotive(2, "Loco2", "Maker", "Model",
                3000, 120, 100, "DIESEL", 100, 1668, 18000);
        train.addLocomotive(electric);
        train.addLocomotive(diesel);

        assertFalse(train.isFullyElectric());
    }

    /**
     * Tests toString method.
     *
     * Scenario:
     *  - Create a train with locomotives and freights.
     *
     * Expected:
     *  - format includes id, endpoints, time, date, and counts.
     *
     * @see Train#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Train train = new Train(5, "VAT123", departureTime, date,
                testPath, startEndpoint, endEndpoint);
        Locomotive loco = new Locomotive(1, "Loco1", "Maker", "Model",
                6000, 200, 160, "ELECTRIC", 80, 1668, 20000);
        train.addLocomotive(loco);

        Freight freight = new Freight(1, date, startEndpoint, endEndpoint);
        train.addFreight(freight);

        String result = train.toString();

        assertTrue(result.contains("Train 5"));
        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("10:30"));
        assertTrue(result.contains("2025-01-15"));
        assertTrue(result.contains("Locos: 1"));
        assertTrue(result.contains("Freights: 1"));
    }
}