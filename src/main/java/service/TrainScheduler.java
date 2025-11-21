package service;

import domain.railway.*;
import domain.scheduling.*;
import repository.RailwayRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviço principal de agendamento de comboios
 *
 * Responsabilidades:
 * 1. Calcular tempos de passagem por cada segmento
 * 2. Identificar e resolver cruzamentos em vias simples
 * 3. Gerar agendamentos completos para múltiplos comboios
 */
public class TrainScheduler {

    private final RailwayNetwork network;
    private final SpeedCalculator speedCalculator;
    private final CrossingManager crossingManager;


    // Tempo de paragem numa estação (minutos)
    private static final int STOP_TIME_MINUTES = 5;

    public TrainScheduler(RailwayNetwork network) {
        this.network = network;
        this.speedCalculator = new SpeedCalculator();
        this.crossingManager = new CrossingManager();
    }

    /**
     * Gera um agendamento completo para um único comboio
     */
    public TrainSchedule scheduleTrainReschedule(Train train) throws SQLException {
        LocalDateTime currentTime = LocalDateTime.of(
                train.getDate(),
                train.getDepartureTime()
        );

        TrainSchedule schedule = new TrainSchedule(train, currentTime);

        // Obter a rota ordenada do comboio
        List<Path.PathEndpoint> route = train.getPath().getOrderedEndpoints();

        // Processar cada segmento da rota
        for (int i = 0; i < route.size() - 1; i++) {
            Path.PathEndpoint current = route.get(i);
            Path.PathEndpoint next = route.get(i + 1);

            // Encontrar o segmento entre os dois endpoints
            LineSegment segment = findSegmentBetween(
                    current.getEndpoint().getId(),
                    next.getEndpoint().getId()
            );

            if (segment == null) {
                throw new IllegalStateException(
                        "No segment found between " + current.getEndpoint().getName()
                                + " and " + next.getEndpoint().getName()
                );
            }

            // Calcular velocidade e tempo de viagem
            int speed = speedCalculator.calculateSpeed(train, segment);
            double travelTimeMinutes = speedCalculator.calculateTravelTime(train, segment);

            // Calcular tempo de chegada
            LocalDateTime arrivalTime = currentTime.plusMinutes((long) travelTimeMinutes);

            // Criar passagem
            SegmentPassage passage = new SegmentPassage(
                    segment,
                    current.getEndpoint(),
                    next.getEndpoint(),
                    currentTime,
                    arrivalTime,
                    speed,
                    next.isMustStop()
            );

            schedule.addPassage(passage);

            // Se deve parar, adiciona tempo de paragem
            if (next.isMustStop()) {
                currentTime = arrivalTime.plusMinutes(STOP_TIME_MINUTES);
            } else {
                currentTime = arrivalTime;
            }
        }

        return schedule;
    }

    /**
     * Agenda múltiplos comboios e resolve conflitos de cruzamento
     */
    public Schedule scheduleMultipleTrains(List<Train> trains) throws SQLException {
        if (trains.isEmpty()) {
            throw new IllegalArgumentException("Train list cannot be empty");
        }

        // Todos os comboios devem ser da mesma data
        LocalDateTime date = LocalDateTime.of(trains.get(0).getDate(), trains.get(0).getDepartureTime());
        Schedule schedule = new Schedule(date.toLocalDate());

        // Ordenar comboios por hora de partida
        List<Train> sortedTrains = trains.stream()
                .sorted(Comparator.comparing(Train::getDepartureTime))
                .toList();

        // Lista para armazenar agendamentos já processados (para detetar cruzamentos)
        List<TrainSchedule> processedSchedules = new ArrayList<>();

        for (Train train : sortedTrains) {
            TrainSchedule trainSchedule = scheduleTrainReschedule(train);

            // Verificar cruzamentos com comboios já agendados
            List<CrossingOperation> crossings = crossingManager.detectAndResolveCrossings(
                    trainSchedule,
                    processedSchedules,
                    this
            );

            // Adicionar operações de cruzamento ao agendamento
            for (CrossingOperation crossing : crossings) {
                trainSchedule.addCrossingOperation(crossing);
            }

            processedSchedules.add(trainSchedule);
            schedule.addTrainSchedule(trainSchedule);
        }

        return schedule;
    }

    /**
     * Encontra o segmento de linha entre dois endpoints
     */
    private LineSegment findSegmentBetween(int endpointAId, int endpointBId) {
        LineSegment segment = network.findSegmentBetween(endpointAId, endpointBId);
        if (segment == null) {
            throw new IllegalStateException("No segment found between endpoints "
                    + endpointAId + " and " + endpointBId);
        }
        return segment;
    }


    /**
     * Obtém o tipo de via de um segmento
     */
    public TrackType getTrackType(LineSegment segment) {
        return network.getTrackType(segment);
    }


    /**
     * Getter para o SpeedCalculator (usado pelo CrossingManager)
     */
    public SpeedCalculator getSpeedCalculator() {
        return speedCalculator;
    }
}