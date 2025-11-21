package controller.SchedulingController;

import domain.railway.*;
import domain.scheduling.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gere operações de cruzamento entre comboios em vias simples
 *
 * Regras:
 * 1. Cruzamentos só ocorrem em vias simples (Single Track)
 * 2. Podem ocorrer em estações ou sidings
 * 3. O comboio que chega primeiro espera pelo que vem em sentido contrário
 */
public class CrossingManager {

    /**
     * Deteta e resolve cruzamentos entre um novo comboio e comboios já agendados
     *
     * @param newSchedule Agendamento do novo comboio
     * @param existingSchedules Agendamentos já processados
     * @param scheduler Scheduler principal (para aceder a TrackTypes)
     * @return Lista de operações de cruzamento necessárias
     */
    public List<CrossingOperation> detectAndResolveCrossings(
            TrainSchedule newSchedule,
            List<TrainSchedule> existingSchedules,
            TrainScheduler scheduler) throws SQLException {

        List<CrossingOperation> crossings = new ArrayList<>();

        // Analisar cada passagem do novo comboio
        for (SegmentPassage newPassage : newSchedule.getPassages()) {

            // Verificar se é via simples
            TrackType trackType = scheduler.getTrackType(newPassage.getSegment());

            if (trackType != null && trackType.isSingleTrack()) {
                // Procurar conflitos com comboios existentes
                for (TrainSchedule existingSchedule : existingSchedules) {
                    CrossingOperation crossing = detectCrossingOnSegment(
                            newSchedule,
                            newPassage,
                            existingSchedule,
                            (SingleTrack) trackType
                    );

                    if (crossing != null) {
                        crossings.add(crossing);
                    }
                }
            }
        }

        return crossings;
    }

    /**
     * Deteta se dois comboios cruzam num segmento específico
     */
    private CrossingOperation detectCrossingOnSegment(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SingleTrack singleTrack) {

        // Encontrar se o comboio existente também passa por este segmento
        SegmentPassage existingPassage = findPassageOnSegment(
                existingSchedule,
                newPassage.getSegment()
        );

        if (existingPassage == null) {
            return null; // Não há cruzamento - comboios não usam o mesmo segmento
        }

        // Verificar se os comboios viajam em sentidos opostos
        boolean oppositeDomainions = areOppositeDirections(newPassage, existingPassage);

        if (!oppositeDomainions) {
            return null; // Mesmo sentido - não há cruzamento
        }

        // Verificar sobreposição temporal
        boolean temporalOverlap = checkTemporalOverlap(
                newPassage.getDepartureTime(),
                newPassage.getArrivalTime(),
                existingPassage.getDepartureTime(),
                existingPassage.getArrivalTime()
        );

        if (!temporalOverlap) {
            return null; // Não há sobreposição - comboios passam em momentos diferentes
        }

        // HÁ CONFLITO! Resolver cruzamento
        return resolveCrossing(
                newSchedule,
                newPassage,
                existingSchedule,
                existingPassage,
                singleTrack
        );
    }

    /**
     * Encontra a passagem de um comboio num segmento específico
     */
    private SegmentPassage findPassageOnSegment(TrainSchedule schedule, LineSegment segment) {
        return schedule.getPassages().stream()
                .filter(p -> p.getSegment().getId() == segment.getId())
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica se duas passagens são em sentidos opostos
     */
    private boolean areOppositeDirections(SegmentPassage p1, SegmentPassage p2) {
        return !p1.getDepartureEndpoint().equals(p2.getDepartureEndpoint());
    }

    /**
     * Verifica se há sobreposição temporal entre dois intervalos
     */
    private boolean checkTemporalOverlap(LocalDateTime start1, LocalDateTime end1,
                                         LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Resolve um cruzamento determinando qual comboio espera e onde
     *
     * Estratégia:
     * 1. Preferir estações a sidings
     * 2. O comboio que chega depois espera
     * 3. Se há siding, pode esperar lá
     */
    private CrossingOperation resolveCrossing(
            TrainSchedule newSchedule,
            SegmentPassage newPassage,
            TrainSchedule existingSchedule,
            SegmentPassage existingPassage,
            SingleTrack singleTrack) {

        // Determinar qual comboio chega primeiro ao segmento
        boolean newTrainFirst = newPassage.getDepartureTime()
                .isBefore(existingPassage.getDepartureTime());

        TrainSchedule waitingSchedule;
        TrainSchedule passingSchedule;
        SegmentPassage waitingPassage;
        SegmentPassage passingPassage;

        if (newTrainFirst) {
            // Novo comboio chega primeiro - comboio existente espera
            waitingSchedule = existingSchedule;
            passingSchedule = newSchedule;
            waitingPassage = existingPassage;
            passingPassage = newPassage;
        } else {
            // Comboio existente chega primeiro - novo comboio espera
            waitingSchedule = newSchedule;
            passingSchedule = existingSchedule;
            waitingPassage = newPassage;
            passingPassage = existingPassage;
        }

        // Determinar onde ocorre o cruzamento
        CrossingOperation.CrossingType crossingType;
        Endpoint crossingLocation;

        if (waitingPassage.requiresStop()) {
            // Preferir estação (endpoint de paragem)
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getArrivalEndpoint();
        } else if (singleTrack.hasSiding()) {
            // Usar siding
            crossingType = CrossingOperation.CrossingType.AT_SIDING;
            crossingLocation = waitingPassage.getDepartureEndpoint(); // aproximação
        } else {
            // Forçar espera na estação anterior
            crossingType = CrossingOperation.CrossingType.AT_STATION;
            crossingLocation = waitingPassage.getDepartureEndpoint();
        }

        // Calcular tempo de espera
        LocalDateTime waitStart = waitingPassage.getDepartureTime();
        LocalDateTime waitEnd = passingPassage.getArrivalTime().plusMinutes(2); // margem de segurança

        return new CrossingOperation(
                waitingSchedule.getTrain().getId(),
                passingSchedule.getTrain().getId(),
                waitingPassage.getSegment(),
                crossingLocation,
                crossingType,
                waitStart,
                waitEnd
        );
    }
}