package controller.SchedulingController;

import domain.railway.*;

import java.util.List;

/**
 * Calculates the effective speed of a train considering:
 * <ul>
 *   <li>Track speed limits</li>
 *   <li>Locomotive power</li>
 *   <li>Total train weight</li>
 *   <li>Traction type (electric vs diesel)</li>
 * </ul>
 *
 * <p>Based on simplified railway physics principles.
 *
 * @author G164
 * @version 1.0
 */
public class SpeedCalculator {

    // Constants based on simplified railway physics
    private static final double RESISTANCE_COEFFICIENT = 0.003; // 3 N/ton
    private static final double EFFICIENCY_ELECTRIC = 0.85;     // 85% electric efficiency
    private static final double EFFICIENCY_DIESEL = 0.75;       // 75% diesel efficiency
    private static final int MINIMUM_SPEED_KMH = 20;            // Minimum operational speed

    /**
     * Calculates the effective speed of a train on a segment.
     *
     * <p>The calculation considers:
     * <ol>
     *   <li>Track speed limit</li>
     *   <li>Locomotive operational speed limit</li>
     *   <li>Power-to-weight ratio</li>
     *   <li>Electrification constraints</li>
     * </ol>
     *
     * @param train The train
     * @param segment The line segment
     * @return Speed in km/h
     * @throws IllegalStateException if train has only electric locomotives on non-electrified track
     */
    public int calculateSpeed(Train train, LineSegment segment) {
        // 1. Track limit
        int trackSpeedLimit = segment.getSpeedLimit();

        // 2. Locomotive limit (operational speed)
        int locomotiveSpeedLimit = calculateLocomotiveSpeedLimit(train.getLocomotives());

        // 3. Maximum allowed speed (minimum of track and locomotive)
        int maxAllowedSpeed = Math.min(trackSpeedLimit, locomotiveSpeedLimit);

        // 4. Check if there's electric traction but track is not electrified
        if (train.hasElectricLocomotive() && !segment.isElectrified()) {
            // Only diesel locomotives can operate
            return calculateDieselOnlySpeed(train, segment, maxAllowedSpeed);
        }

        // 5. Calculate speed based on available power vs weight
        int calculatedSpeed = calculateSpeedFromPowerToWeight(
                train.getTotalPower(),
                train.getTotalWeight(),
                train.isFullyElectric(),
                maxAllowedSpeed
        );

        return Math.max(calculatedSpeed, MINIMUM_SPEED_KMH);
    }

    /**
     * Calculates the speed limit of locomotives (uses the lowest operational speed).
     *
     * @param locomotives List of locomotives
     * @return Speed limit in km/h
     */
    private int calculateLocomotiveSpeedLimit(List<Locomotive> locomotives) {
        int minSpeed = Integer.MAX_VALUE;

        for (int i = 0; i < locomotives.size(); i++) {
            Locomotive loco = locomotives.get(i);
            int opSpeed = loco.getOpSpeed();
            if (opSpeed < minSpeed) {
                minSpeed = opSpeed;
            }
        }

        return minSpeed == Integer.MAX_VALUE ? 80 : minSpeed; // conservative default
    }

    /**
     * Special case: non-electrified track with hybrid train.
     * Only diesel locomotives contribute to power.
     *
     * @param train The train
     * @param segment The segment
     * @param maxSpeed Maximum allowed speed
     * @return Speed in km/h
     * @throws IllegalStateException if train has only electric locomotives
     */
    private int calculateDieselOnlySpeed(Train train, LineSegment segment, int maxSpeed) {
        int dieselPower = 0;
        List<Locomotive> locomotives = train.getLocomotives();

        for (int i = 0; i < locomotives.size(); i++) {
            Locomotive loco = locomotives.get(i);
            if (!loco.isElectric()) {
                dieselPower += loco.getPower();
            }
        }

        if (dieselPower == 0) {
            throw new IllegalStateException(
                    "Train " + train.getId() + " has only electric locomotives but segment "
                            + segment.getId() + " is not electrified!");
        }

        return calculateSpeedFromPowerToWeight(
                dieselPower,
                train.getTotalWeight(),
                false, // diesel only
                maxSpeed
        );
    }

    /**
     * Simplified physical calculation of speed based on power/weight ratio.
     *
     * <p>Simplified formula:
     * <pre>
     * v = sqrt((P * η) / (m * R))
     * </pre>
     *
     * Where:
     * <ul>
     *   <li>P = total power (kW)</li>
     *   <li>η = efficiency (0.75-0.85)</li>
     *   <li>m = total mass (tonnes)</li>
     *   <li>R = resistance coefficient</li>
     * </ul>
     *
     * @param powerKw Total power in kW
     * @param weightTons Total weight in tonnes
     * @param isFullyElectric Whether all locomotives are electric
     * @param maxSpeed Maximum allowed speed
     * @return Calculated speed in km/h
     */
    private int calculateSpeedFromPowerToWeight(int powerKw,
                                                int weightTons,
                                                boolean isFullyElectric,
                                                int maxSpeed) {
        double efficiency = isFullyElectric ? EFFICIENCY_ELECTRIC : EFFICIENCY_DIESEL;

        // Useful power in kW
        double usefulPower = powerKw * efficiency;

        // Power-to-weight ratio (kW per tonne)
        double powerPerTon = usefulPower / weightTons;

        // Simplified speed calculation (based on railway models)
        // Empirical formula: v ≈ k * sqrt(P/m)
        double theoreticalSpeed = 25 * Math.sqrt(powerPerTon);

        // Apply maximum limit
        int finalSpeed = (int) Math.min(theoreticalSpeed, maxSpeed);

        return Math.max(finalSpeed, MINIMUM_SPEED_KMH);
    }

    /**
     * Calculates travel time on a segment (in minutes).
     *
     * @param train The train
     * @param segment The segment
     * @return Travel time in minutes
     */
    public double calculateTravelTime(Train train, LineSegment segment) {
        int speed = calculateSpeed(train, segment);
        double lengthKm = segment.getLength() / 1000.0;
        return (lengthKm / speed) * 60; // convert to minutes
    }

    /**
     * Checks if the train can traverse the segment.
     *
     * <p>Verifies:
     * <ul>
     *   <li>Maximum track weight capacity</li>
     *   <li>Adequate traction (electric track requires electric or hybrid locomotives)</li>
     * </ul>
     *
     * @param train The train
     * @param segment The segment
     * @return true if train can traverse, false otherwise
     */
    public boolean canTraverse(Train train, LineSegment segment) {
        // Check maximum track weight (kg/m)
        int totalLengthMeters = getTotalTrainLength(train); // já em metros

        if (totalLengthMeters == 0) {
            // Comboio sem comprimento definido - usar fallback
            totalLengthMeters = train.getLocomotives().size() * 20 +
                    countWagons(train) * 15;
        }

        // Peso por metro linear
        double trainWeightPerMeter = (double) train.getTotalWeight() * 1000 / totalLengthMeters; // kg/m

        if (trainWeightPerMeter > segment.getMaxWeight()) {
            return false;
        }

        // Check if has adequate traction
        boolean hasOnlyElectric = true;
        List<Locomotive> locomotives = train.getLocomotives();

        for (int i = 0; i < locomotives.size(); i++) {
            if (!locomotives.get(i).isElectric()) {
                hasOnlyElectric = false;
                break;
            }
        }

        if (train.hasElectricLocomotive() && hasOnlyElectric) {
            // Only electric locomotives, track must be electrified
            return segment.isElectrified();
        }

        return true;
    }

    /**
     * Helper: conta o número total de vagões
     */
    private int countWagons(Train train) {
        int count = 0;
        for (int i = 0; i < train.getFreights().size(); i++) {
            count += train.getFreights().get(i).getWagons().size();
        }
        return count;
    }

    /**
     * Estimates the total train length (locomotives + wagons).
     *
     * <p>Approximation: 20m per locomotive, 15m per wagon
     *
     * @param train The train
     * @return Total length in metres
     */
    private int getTotalTrainLength(Train train) {
        int locoLength = 0;
        for (int i = 0; i < train.getLocomotives().size(); i++) {
            Locomotive loco = train.getLocomotives().get(i);
            locoLength += loco.getLength();
        }

        int wagonLength = 0;
        for (int i = 0; i < train.getFreights().size(); i++) {
            Freight freight = train.getFreights().get(i);
            for (int j = 0; j < freight.getWagons().size(); j++) {
                Wagon wagon = freight.getWagons().get(j);
                wagonLength += wagon.getModel().getLength();
            }
        }

        return locoLength + wagonLength;
    }
}