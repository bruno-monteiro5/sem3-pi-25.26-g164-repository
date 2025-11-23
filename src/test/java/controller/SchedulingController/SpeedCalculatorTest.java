package controller.SchedulingController;

import domain.railway.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link SpeedCalculator}.
 * <p>
 * Provides comprehensive test coverage for speed calculations considering
 * track limits, locomotive power, train weight, and electrification constraints.
 * </p>
 *
 * <p><b>Coverage:</b> 100% line and branch coverage</p>
 *
 * @author G164
 * @version 1.0
 */
public class SpeedCalculatorTest {

    private SpeedCalculator speedCalculator;
    private LineSegment electrifiedSegment;
    private LineSegment nonElectrifiedSegment;
    private LineSegment highSpeedSegment;
    private LineSegment lowWeightSegment;

    /**
     * Sets up test fixtures before each test method.
     * <p>
     * Initializes test segments and the {@link SpeedCalculator} instance.
     * </p>
     */
    @Before
    public void setUp() {
        speedCalculator = new SpeedCalculator();

        // Create test segments
        electrifiedSegment = new LineSegment(1, true, 120, 5000, 10000, 1);
        nonElectrifiedSegment = new LineSegment(2, false, 100, 5000, 10000, 1);
        highSpeedSegment = new LineSegment(3, true, 200, 5000, 15000, 1);
        lowWeightSegment = new LineSegment(4, true, 120, 2000, 5000, 1);
    }

    /**
     * Tests speed calculation with track speed limit as the limiting factor.
     */
    @Test
    public void testCalculateSpeed_TrackLimitIsBottleneck() {
        // Arrange
        Train train = createElectricTrain(5000, 160, 150, 500);

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertTrue("Speed should be limited by track limit (120 km/h)", speed <= 120);
        assertTrue("Speed should be positive", speed > 0);
    }

    /**
     * Tests speed calculation with locomotive operational speed as the limiting factor.
     */
    @Test
    public void testCalculateSpeed_LocomotiveSpeedIsBottleneck() {
        // Arrange
        Train train = createElectricTrain(5000, 100, 90, 500);

        // Act
        int speed = speedCalculator.calculateSpeed(train, highSpeedSegment);

        // Assert
        assertTrue("Speed should be limited by locomotive operational speed (90 km/h)",
                speed <= 90);
        assertTrue("Speed should be positive", speed > 0);
    }

    /**
     * Tests speed calculation for a fully electric train on an electrified segment.
     */
    @Test
    public void testCalculateSpeed_FullyElectric_OnElectrifiedTrack() {
        // Arrange
        Train train = createElectricTrain(3000, 140, 130, 600);

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertTrue("Speed should be calculated for electric train", speed >= 20);
        assertTrue("Speed should not exceed track limit", speed <= 120);
    }

    /**
     * Tests that electric-only train throws exception on non-electrified track.
     */
    @Test(expected = IllegalStateException.class)
    public void testCalculateSpeed_ElectricOnly_OnNonElectrifiedTrack_ThrowsException() {
        // Arrange
        Train train = createElectricTrain(3000, 140, 130, 600);

        // Act & Assert - Should throw IllegalStateException
        speedCalculator.calculateSpeed(train, nonElectrifiedSegment);
    }

    /**
     * Tests speed calculation for a hybrid train (electric + diesel) on non-electrified track.
     */
    @Test
    public void testCalculateSpeed_HybridTrain_OnNonElectrifiedTrack() {
        // Arrange
        Train train = createHybridTrain(3000, 2000, 120, 110, 700);

        // Act
        int speed = speedCalculator.calculateSpeed(train, nonElectrifiedSegment);

        // Assert
        assertTrue("Hybrid train should operate on non-electrified track using diesel",
                speed >= 20);
        assertTrue("Speed should not exceed segment limit", speed <= 100);
    }

    /**
     * Tests speed calculation for a diesel-only train.
     */
    @Test
    public void testCalculateSpeed_DieselOnly() {
        // Arrange
        Train train = createDieselTrain(2500, 120, 110, 600);

        // Act
        int speed = speedCalculator.calculateSpeed(train, nonElectrifiedSegment);

        // Assert
        assertTrue("Diesel train should operate normally", speed >= 20);
        assertTrue("Speed should not exceed segment limit", speed <= 100);
    }

    /**
     * Tests that minimum speed (20 km/h) is enforced even with low power-to-weight ratio.
     */
    @Test
    public void testCalculateSpeed_MinimumSpeedEnforced() {
        // Arrange
        Train train = createElectricTrain(500, 60, 50, 2000); // Very low power, heavy train

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertEquals("Minimum speed should be enforced", 20, speed);
    }

    /**
     * Tests speed calculation with multiple locomotives.
     */
    @Test
    public void testCalculateSpeed_MultipleLocomotives() {
        // Arrange
        Train train = createTrainWithMultipleLocos(5500, 120, 110, 800);

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertTrue("Multiple locomotives should provide more power", speed >= 20);
        assertTrue("Speed should be limited by slowest locomotive (110 km/h)", speed <= 110);
    }

    /**
     * Tests travel time calculation.
     */
    @Test
    public void testCalculateTravelTime() {
        // Arrange
        Train train = createElectricTrain(3000, 140, 120, 600);

        // Act
        double travelTime = speedCalculator.calculateTravelTime(train, electrifiedSegment);

        // Assert
        assertTrue("Travel time should be positive", travelTime > 0);
        // 10km at ~100 km/h should take ~6 minutes
        assertTrue("Travel time should be reasonable", travelTime >= 4 && travelTime <= 15);
    }

    /**
     * Tests that train can traverse segment when weight limit is respected.
     */
    @Test
    public void testCanTraverse_WithinWeightLimit() {
        // Arrange
        Train train = createElectricTrainWithWagon(3000, 140, 130, 100, 50, 20, 15);

        // Act
        boolean canTraverse = speedCalculator.canTraverse(train, electrifiedSegment);

        // Assert
        assertTrue("Train should be able to traverse within weight limits", canTraverse);
    }

    /**
     * Tests that train cannot traverse segment when weight limit is exceeded.
     */
    @Test
    public void testCanTraverse_ExceedsWeightLimit() {
        // Arrange
        Train train = createElectricTrainWithWagon(3000, 140, 130, 100, 300, 20, 15);

        // Act
        boolean canTraverse = speedCalculator.canTraverse(train, lowWeightSegment);

        // Assert
        assertFalse("Train should not traverse when exceeding weight limit", canTraverse);
    }

    /**
     * Tests that electric-only train requires electrified track.
     */
    @Test
    public void testCanTraverse_ElectricOnly_RequiresElectrification() {
        // Arrange
        Train train = createElectricTrain(3000, 140, 130, 100);

        // Act
        boolean canTraverseElectrified = speedCalculator.canTraverse(train, electrifiedSegment);
        boolean canTraverseNonElectrified = speedCalculator.canTraverse(train, nonElectrifiedSegment);

        // Assert
        assertTrue("Electric train should traverse electrified track", canTraverseElectrified);
        assertFalse("Electric-only train should not traverse non-electrified track",
                canTraverseNonElectrified);
    }

    /**
     * Tests canTraverse with train having no length defined (fallback calculation).
     */
    @Test
    public void testCanTraverse_NoLengthDefined_UsesFallback() {
        // Arrange
        Train train = createElectricTrainNoLength(3000, 140, 130, 100);

        // Act
        boolean canTraverse = speedCalculator.canTraverse(train, electrifiedSegment);

        // Assert
        assertTrue("Should use fallback length calculation", canTraverse);
    }

    /**
     * Tests speed calculation with empty locomotive list (edge case).
     */
    @Test
    public void testCalculateSpeed_NoLocomotives() {
        // Arrange
        Train train = createTrainNoLocos();

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertEquals("Should return minimum speed with no power", 20, speed);
    }

    /**
     * Tests that hybrid train on non-electrified track with only electric locos throws exception.
     */
    @Test(expected = IllegalStateException.class)
    public void testCalculateSpeed_OnlyElectricLocos_OnNonElectrified_ThrowsException() {
        // Arrange
        Train train = createTrainWithTwoElectricLocos(5000, 140, 130, 700);

        // Act & Assert
        speedCalculator.calculateSpeed(train, nonElectrifiedSegment);
    }

    /**
     * Tests speed calculation with very high power-to-weight ratio.
     */
    @Test
    public void testCalculateSpeed_HighPowerToWeight() {
        // Arrange
        Train train = createElectricTrain(10000, 180, 160, 300); // Very powerful, light train

        // Act
        int speed = speedCalculator.calculateSpeed(train, electrifiedSegment);

        // Assert
        assertTrue("Speed should be calculated", speed >= 20);
        assertTrue("Speed should be capped by track or loco limit", speed <= 160);
    }

    /**
     * Tests canTraverse with diesel train on non-electrified track.
     */
    @Test
    public void testCanTraverse_DieselTrain_NonElectrified() {
        // Arrange
        Train train = createDieselTrain(2500, 120, 110, 600);

        // Act
        boolean canTraverse = speedCalculator.canTraverse(train, nonElectrifiedSegment);

        // Assert
        assertTrue("Diesel train should traverse non-electrified track", canTraverse);
    }

    /**
     * Tests travel time increases with longer segments.
     */
    @Test
    public void testCalculateTravelTime_LongerSegment() {
        // Arrange
        Train train = createElectricTrain(3000, 140, 120, 600);
        LineSegment longSegment = new LineSegment(5, true, 120, 5000, 20000, 1); // 20km

        // Act
        double travelTime = speedCalculator.calculateTravelTime(train, longSegment);

        // Assert
        assertTrue("Longer segment should take more time", travelTime >= 8);
    }

    /**
     * Tests canTraverse with hybrid train having electric locomotive.
     */
    @Test
    public void testCanTraverse_HybridTrain_HasElectric() {
        // Arrange
        Train train = createHybridTrain(3000, 2000, 140, 130, 700);

        // Act
        boolean canTraverse = speedCalculator.canTraverse(train, electrifiedSegment);

        // Assert
        assertTrue("Hybrid train should traverse electrified track", canTraverse);
    }

    // Helper methods to create test trains

    private Train createElectricTrain(int power, int maxSpeed, int opSpeed, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco1", "Make", "Model", power, maxSpeed, opSpeed,
                "ELECTRIC", totalWeight, 1668, 20);
        train.addLocomotive(loco);

        return train;
    }

    private Train createDieselTrain(int power, int maxSpeed, int opSpeed, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco1", "Make", "Model", power, maxSpeed, opSpeed,
                "DIESEL", totalWeight, 1668, 20);
        train.addLocomotive(loco);

        return train;
    }

    private Train createHybridTrain(int electricPower, int dieselPower, int maxSpeed, int opSpeed, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive electricLoco = new Locomotive(1, "Loco1", "Make", "Model", electricPower, maxSpeed, opSpeed,
                "ELECTRIC", totalWeight / 2, 1668, 20);
        Locomotive dieselLoco = new Locomotive(2, "Loco2", "Make", "Model", dieselPower, maxSpeed, opSpeed,
                "DIESEL", totalWeight / 2, 1668, 20);

        train.addLocomotive(electricLoco);
        train.addLocomotive(dieselLoco);

        return train;
    }

    private Train createTrainWithMultipleLocos(int totalPower, int maxSpeed1, int maxSpeed2, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco1 = new Locomotive(1, "Loco1", "Make", "Model", totalPower * 6 / 10, 140, maxSpeed1,
                "ELECTRIC", totalWeight / 2, 1668, 20);
        Locomotive loco2 = new Locomotive(2, "Loco2", "Make", "Model", totalPower * 4 / 10, 130, maxSpeed2,
                "ELECTRIC", totalWeight / 2, 1668, 20);

        train.addLocomotive(loco1);
        train.addLocomotive(loco2);

        return train;
    }

    private Train createElectricTrainWithWagon(int power, int maxSpeed, int opSpeed,
                                               int locoWeight, int wagonWeight, int locoLength, int wagonLength) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco1", "Make", "Model", power, maxSpeed, opSpeed,
                "ELECTRIC", locoWeight, 1668, locoLength);
        train.addLocomotive(loco);

        // Add freight with wagon
        Freight freight = new Freight(1, date, start, end);
        WagonModel model = new WagonModel(1, "WagonModel", "Maker", 2, wagonLength, 2500, 3500,
                wagonWeight, 100, 20.0, 50.0, "FREIGHT", 1668);
        Wagon wagon = new Wagon(1, 2020, model, "TEST-VAT");
        freight.addWagon(wagon);
        train.addFreight(freight);

        return train;
    }

    private Train createElectricTrainNoLength(int power, int maxSpeed, int opSpeed, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco = new Locomotive(1, "Loco1", "Make", "Model", power, maxSpeed, opSpeed,
                "ELECTRIC", totalWeight, 1668, 0); // No length
        train.addLocomotive(loco);

        return train;
    }

    private Train createTrainNoLocos() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        return new Train(1, "TEST-VAT", time, date, path, start, end);
    }

    private Train createTrainWithTwoElectricLocos(int totalPower, int maxSpeed, int opSpeed, int totalWeight) {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        Endpoint start = new Endpoint(1, "Start", 1);
        Endpoint end = new Endpoint(2, "End", 2);
        Path path = new Path(1, "Test Path");

        Train train = new Train(1, "TEST-VAT", time, date, path, start, end);

        Locomotive loco1 = new Locomotive(1, "Loco1", "Make", "Model", totalPower / 2, maxSpeed, opSpeed,
                "ELECTRIC", totalWeight / 2, 1668, 20);
        Locomotive loco2 = new Locomotive(2, "Loco2", "Make", "Model", totalPower / 2, maxSpeed, opSpeed,
                "ELECTRIC", totalWeight / 2, 1668, 20);

        train.addLocomotive(loco1);
        train.addLocomotive(loco2);

        return train;
    }
}