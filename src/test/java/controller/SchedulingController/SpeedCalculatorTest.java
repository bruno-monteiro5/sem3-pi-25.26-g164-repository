package controller.SchedulingController;

import domain.railway.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpeedCalculator.
 *
 * @author G164
 * @version 1.0
 */
public class SpeedCalculatorTest {

    private SpeedCalculator calculator;
    private Train testTrain;
    private LineSegment electrifiedSegment;
    private LineSegment nonElectrifiedSegment;

    @BeforeEach
    public void setUp() {
        calculator = new SpeedCalculator();

        // Create test locomotives
        Locomotive electricLoco = new Locomotive(
                1, "Electric-1", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC",
                80, 1668
        );

        Locomotive dieselLoco = new Locomotive(
                2, "Diesel-1", "General Electric", "PowerHaul",
                2400, 120, 100, "DIESEL",
                120, 1668
        );

        // Create test segments
        electrifiedSegment = new LineSegment(
                1, true, 160, 20, 50000, 1
        );

        nonElectrifiedSegment = new LineSegment(
                2, false, 120, 20, 50000, 1
        );

        // Create test path and endpoints
        Endpoint startEndpoint = new Endpoint(1, "Station A", 1);
        Endpoint endEndpoint = new Endpoint(2, "Station B", 2);
        Path testPath = new Path(1, "Test Path");

        // Create test train
        testTrain = new Train(
                1, "VAT123", LocalTime.of(10, 0),
                LocalDate.now(), testPath, startEndpoint, endEndpoint
        );

        testTrain.addLocomotive(electricLoco);
        testTrain.addLocomotive(dieselLoco);
    }

    /**
     * Tests speed calculation on electrified segment with mixed locomotives.
     */
    @Test
    public void testCalculateSpeedElectrifiedSegment() {
        int speed = calculator.calculateSpeed(testTrain, electrifiedSegment);

        // Speed should be positive and respect segment limit
        assertTrue(speed > 0, "Speed must be positive");
        assertTrue(speed <= electrifiedSegment.getSpeedLimit(),
                "Speed must not exceed segment limit");
        assertTrue(speed >= 20, "Speed must be at least minimum operational speed");
    }

    /**
     * Tests speed calculation on non-electrified segment.
     * Only diesel locomotives should contribute.
     */
    @Test
    public void testCalculateSpeedNonElectrifiedSegment() {
        int speed = calculator.calculateSpeed(testTrain, nonElectrifiedSegment);

        // Speed should be positive but potentially lower due to diesel-only power
        assertTrue(speed > 0, "Speed must be positive");
        assertTrue(speed <= nonElectrifiedSegment.getSpeedLimit(),
                "Speed must not exceed segment limit");
    }

    /**
     * Tests that electric-only train cannot operate on non-electrified segment.
     */
    @Test
    public void testElectricOnlyTrainOnNonElectrifiedSegment() {
        // Create electric-only train
        Locomotive electricLoco = new Locomotive(
                3, "Electric-2", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC",
                80, 1668
        );

        Endpoint startEndpoint = new Endpoint(1, "Station A", 1);
        Endpoint endEndpoint = new Endpoint(2, "Station B", 2);
        Path testPath = new Path(2, "Test Path 2");

        Train electricTrain = new Train(
                2, "VAT456", LocalTime.of(11, 0),
                LocalDate.now(), testPath, startEndpoint, endEndpoint
        );

        electricTrain.addLocomotive(electricLoco);

        // Should throw exception
        assertThrows(IllegalStateException.class, () -> {
            calculator.calculateSpeed(electricTrain, nonElectrifiedSegment);
        }, "Electric-only train on non-electrified segment should throw exception");
    }

    /**
     * Tests travel time calculation.
     */
    @Test
    public void testCalculateTravelTime() {
        double travelTime = calculator.calculateTravelTime(testTrain, electrifiedSegment);

        // Travel time should be positive
        assertTrue(travelTime > 0, "Travel time must be positive");

        // Travel time should be reasonable for 50km segment
        // At 160 km/h: 50km takes ~18.75 minutes
        // At 20 km/h: 50km takes 150 minutes
        assertTrue(travelTime >= 18 && travelTime <= 150,
                "Travel time should be between 18 and 150 minutes for 50km segment");
    }

    /**
     * Tests that train can traverse appropriate segments.
     */
    @Test
    public void testCanTraverse() {
        // Mixed train should be able to traverse both types
        assertTrue(calculator.canTraverse(testTrain, electrifiedSegment),
                "Mixed train should traverse electrified segment");
        assertTrue(calculator.canTraverse(testTrain, nonElectrifiedSegment),
                "Mixed train should traverse non-electrified segment");
    }

    /**
     * Tests that electric-only train cannot traverse non-electrified segment.
     */
    @Test
    public void testCannotTraverseNonElectrified() {
        Locomotive electricLoco = new Locomotive(
                4, "Electric-3", "Siemens", "Vectron",
                6000, 200, 160, "ELECTRIC",
                80, 1668
        );

        Endpoint startEndpoint = new Endpoint(1, "Station A", 1);
        Endpoint endEndpoint = new Endpoint(2, "Station B", 2);
        Path testPath = new Path(3, "Test Path 3");

        Train electricTrain = new Train(
                3, "VAT789", LocalTime.of(12, 0),
                LocalDate.now(), testPath, startEndpoint, endEndpoint
        );

        electricTrain.addLocomotive(electricLoco);

        assertFalse(calculator.canTraverse(electricTrain, nonElectrifiedSegment),
                "Electric-only train should not traverse non-electrified segment");
    }

    /**
     * Tests speed calculation with very heavy train.
     */
    @Test
    public void testCalculateSpeedHeavyTrain() {
        // Add heavy freight to make train slower
        Endpoint origin = new Endpoint(3, "Origin", 3);
        Endpoint destination = new Endpoint(4, "Destination", 4);

        Freight freight = new Freight(1, LocalDate.now(), origin, destination);

        // Add many heavy wagons
        WagonModel heavyModel = new WagonModel(
                1, "Heavy Wagon", "Maker", 4,
                15, 3, 4, 25.0, 100, 60.0, 100.0,
                "FREIGHT", 1668
        );

        for (int i = 0; i < 20; i++) {
            Wagon wagon = new Wagon(i, 2020, heavyModel, "VAT123");
            freight.addWagon(wagon);
        }

        testTrain.addFreight(freight);

        int speed = calculator.calculateSpeed(testTrain, electrifiedSegment);

        // Heavy train should be slower
        assertTrue(speed >= 20, "Heavy train should maintain minimum speed");
        assertTrue(speed < 160, "Heavy train should be slower than max segment speed");
    }

    /**
     * Tests that minimum speed is enforced.
     */
    @Test
    public void testMinimumSpeedEnforced() {
        int speed = calculator.calculateSpeed(testTrain, electrifiedSegment);

        assertTrue(speed >= 20, "Speed must never be below minimum operational speed (20 km/h)");
    }
}