package controller.SchedulingController;

import domain.railway.LineSegment;
import domain.railway.Locomotive;
import domain.railway.Train;

import java.util.List;

/**
 * Calcula a velocidade efetiva de um comboio considerando:
 * - Limites de velocidade da via
 * - Potência das locomotivas
 * - Peso total do comboio
 * - Tipo de tração (elétrica vs diesel)
 */
public class SpeedCalculator {

    // Constantes baseadas em física ferroviária simplificada
    private static final double RESISTANCE_COEFFICIENT = 0.003; // 3 N/ton
    private static final double EFFICIENCY_ELECTRIC = 0.85;     // 85% eficiência elétrica
    private static final double EFFICIENCY_DIESEL = 0.75;       // 75% eficiência diesel
    private static final int MINIMUM_SPEED_KMH = 20;            // Velocidade mínima operacional

    /**
     * Calcula a velocidade efetiva do comboio num segmento
     *
     * @param train Comboio
     * @param segment Segmento da linha
     * @return Velocidade em km/h
     */
    public int calculateSpeed(Train train, LineSegment segment) {
        // 1. Limite da via
        int trackSpeedLimit = segment.getSpeedLimit();

        // 2. Limite das locomotivas (velocidade operacional)
        int locomotiveSpeedLimit = calculateLocomotiveSpeedLimit(train.getLocomotives());

        // 3. Velocidade máxima permitida (menor entre via e locomotiva)
        int maxAllowedSpeed = Math.min(trackSpeedLimit, locomotiveSpeedLimit);

        // 4. Verifica se há tração elétrica mas via não é eletrificada
        if (train.hasElectricLocomotive() && !segment.isElectrified()) {
            // Apenas locomotivas diesel podem operar
            return calculateDieselOnlySpeed(train, segment, maxAllowedSpeed);
        }

        // 5. Calcula velocidade baseada na potência disponível vs peso
        int calculatedSpeed = calculateSpeedFromPowerToWeight(
                train.getTotalPower(),
                train.getTotalWeight(),
                train.isFullyElectric(),
                maxAllowedSpeed
        );

        return Math.max(calculatedSpeed, MINIMUM_SPEED_KMH);
    }

    /**
     * Calcula o limite de velocidade das locomotivas (usa a operacional mais baixa)
     */
    private int calculateLocomotiveSpeedLimit(List<Locomotive> locomotives) {
        return locomotives.stream()
                .mapToInt(Locomotive::getOpSpeed)
                .min()
                .orElse(80); // default conservador
    }

    /**
     * Caso especial: via não eletrificada com comboio híbrido
     * Apenas locomotivas diesel contribuem
     */
    private int calculateDieselOnlySpeed(Train train, LineSegment segment, int maxSpeed) {
        int dieselPower = train.getLocomotives().stream()
                .filter(l -> !l.isElectric())
                .mapToInt(Locomotive::getPower)
                .sum();

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
     * Cálculo físico simplificado da velocidade baseado em potência/peso
     *
     * Fórmula simplificada:
     * v = sqrt((P * η) / (m * R))
     *
     * Onde:
     * - P = potência total (kW)
     * - η = eficiência (0.75-0.85)
     * - m = massa total (toneladas)
     * - R = coeficiente de resistência
     */
    private int calculateSpeedFromPowerToWeight(int powerKw,
                                                int weightTons,
                                                boolean isFullyElectric,
                                                int maxSpeed) {
        double efficiency = isFullyElectric ? EFFICIENCY_ELECTRIC : EFFICIENCY_DIESEL;

        // Potência útil em kW
        double usefulPower = powerKw * efficiency;

        // Relação potência/peso (kW por tonelada)
        double powerPerTon = usefulPower / weightTons;

        // Cálculo simplificado da velocidade (baseado em modelos ferroviários)
        // Fórmula empírica: v ≈ k * sqrt(P/m)
        double theoreticalSpeed = 25 * Math.sqrt(powerPerTon);

        // Aplica o limite máximo
        int finalSpeed = (int) Math.min(theoreticalSpeed, maxSpeed);

        return Math.max(finalSpeed, MINIMUM_SPEED_KMH);
    }

    /**
     * Calcula o tempo de viagem num segmento (em minutos)
     */
    public double calculateTravelTime(Train train, LineSegment segment) {
        int speed = calculateSpeed(train, segment);
        double lengthKm = segment.getLength() / 1000.0;
        return (lengthKm / speed) * 60; // converte para minutos
    }

    /**
     * Verifica se o comboio pode atravessar o segmento
     */
    public boolean canTraverse(Train train, LineSegment segment) {
        // Verifica peso máximo da via
        int trainWeightPerMeter = train.getTotalWeight() * 1000 / getTotalTrainLength(train);

        if (trainWeightPerMeter > segment.getMaxWeight()) {
            return false;
        }

        // Verifica se tem tração adequada
        if (train.hasElectricLocomotive() && !train.getLocomotives().stream()
                .anyMatch(l -> !l.isElectric())) {
            // Apenas locomotivas elétricas, via deve ser eletrificada
            return segment.isElectrified();
        }

        return true;
    }

    /**
     * Estima o comprimento total do comboio (locomotivas + vagões)
     * Aproximação: 20m por locomotiva, 15m por vagão
     */
    private int getTotalTrainLength(Train train) {
        int locoLength = train.getLocomotives().size() * 20;
        int wagonCount = train.getFreights().stream()
                .mapToInt(f -> f.getWagons().size())
                .sum();
        int wagonLength = wagonCount * 15;
        return locoLength + wagonLength;
    }
}