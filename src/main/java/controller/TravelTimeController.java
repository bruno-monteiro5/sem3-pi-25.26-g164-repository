package controller;

import domain.railway.*;
import repository.RailwayRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TravelTimeController {
    private final RailwayRepository repository;

    public TravelTimeController() {
        this.repository = new RailwayRepository();
    }

    /**
     * Obtém todas as linhas disponíveis
     */
    public List<Line> getAvailableLines() throws SQLException {
        return repository.getAllLines();
    }

    /**
     * Obtém todas as locomotivas disponíveis
     */
    public List<Locomotive> getAvailableLocomotives() throws SQLException {
        return repository.getAllLocomotives();
    }

    /**
     * Calcula o tempo de viagem estimado
     */
    public TravelTimeResult calculateTravelTime(Line line, Locomotive locomotive)
            throws SQLException, IllegalArgumentException {

        // 1. Validar compatibilidade de gauge
        if (line.getGauge() != locomotive.getGauge()) {
            throw new IllegalArgumentException(
                    String.format("Gauge incompatível! Linha: %dmm, Locomotiva: %dmm",
                            line.getGauge(), locomotive.getGauge())
            );
        }

        // 2. Obter segmentos da linha
        List<LineSegment> segments = repository.getLineSegments(line.getId());

        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Linha não possui segmentos!");
        }

        // 3. Validar eletrificação (se locomotiva elétrica)
        if (locomotive.isElectric()) {
            boolean allElectrified = segments.stream()
                    .allMatch(LineSegment::isElectrified);

            if (!allElectrified) {
                throw new IllegalArgumentException(
                        "Locomotiva elétrica não pode circular em segmentos não eletrificados!"
                );
            }
        }

        // 4. Calcular tempo para cada segmento
        List<TravelTimeResult.SegmentTime> segmentTimes = new ArrayList<>();

        for (LineSegment segment : segments) {
            // Velocidade efetiva = mínimo entre limite do segmento e velocidade operacional
            int effectiveSpeed = Math.min(segment.getSpeedLimit(), locomotive.getOpSpeed());

            // Tempo = distância / velocidade (em horas), depois converter para minutos
            double distanceKm = segment.getLength() / 1000.0;
            double timeHours = distanceKm / effectiveSpeed;
            double timeMinutes = timeHours * 60;

            segmentTimes.add(new TravelTimeResult.SegmentTime(
                    segment, effectiveSpeed, timeMinutes
            ));
        }

        return new TravelTimeResult(line, locomotive, segmentTimes);
    }
}