package controller;

import domain.railway.*;
import repository.RailwayRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates travel-time estimations over the railway network.
 * <p>
 * Delegates data access to {@link RailwayRepository} and computes the
 * estimated travel time for a given {@link Line} with a specific {@link Locomotive},
 * validating gauge compatibility and electrification constraints along the way.
 */
public class TravelTimeController {
    private final RailwayRepository repository;

    /**
     * Creates a controller backed by a new {@link RailwayRepository} instance.
     */
    public TravelTimeController() {
        this.repository = new RailwayRepository();
    }

    /**
     * Retrieves all available railway lines.
     *
     * @return list of {@link Line} entities
     * @throws SQLException if the repository query fails
     */
    public List<Line> getAvailableLines() throws SQLException {
        return repository.getAllLines();
    }

    /**
     * Retrieves all available locomotives.
     *
     * @return list of {@link Locomotive} entities
     * @throws SQLException if the repository query fails
     */
    public List<Locomotive> getAvailableLocomotives() throws SQLException {
        return repository.getAllLocomotives();
    }

    /**
     * Computes the estimated travel time for the given line and locomotive.
     * <p>
     * The algorithm validates:
     * <ul>
     *   <li><b>Gauge compatibility</b>: {@link Line#getGauge()} must match {@link Locomotive#getGauge()}.</li>
     *   <li><b>Electrification</b>: if {@link Locomotive#isElectric()} is true, all {@link LineSegment}s must be electrified.</li>
     * </ul>
     * Then, for each segment, the effective speed is the minimum between the segment speed limit
     * and the locomotive operational speed. Segment time is computed as:
     * {@code timeMinutes = (segment.length(km) / effectiveSpeed(km/h)) * 60}.
     *
     * @param line        the target railway line
     * @param locomotive  the locomotive to operate on the line
     * @return a {@link TravelTimeResult} detailing per-segment and total time
     * @throws SQLException              if segment retrieval fails
     * @throws IllegalArgumentException  if gauges are incompatible, the line has no segments,
     *                                   or an electric locomotive is assigned to non-electrified segments
     */
    public TravelTimeResult calculateTravelTime(Line line, Locomotive locomotive)
            throws SQLException, IllegalArgumentException {

        if (line.getGauge() != locomotive.getGauge()) {
            throw new IllegalArgumentException(
                    String.format("Gauge incompatível! Linha: %dmm, Locomotiva: %dmm",
                            line.getGauge(), locomotive.getGauge())
            );
        }

        List<LineSegment> segments = repository.getLineSegments(line.getId());

        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Linha não possui segmentos!");
        }

        if (locomotive.isElectric()) {
            boolean allElectrified = segments.stream()
                    .allMatch(LineSegment::isElectrified);

            if (!allElectrified) {
                throw new IllegalArgumentException(
                        "Locomotiva elétrica não pode circular em segmentos não eletrificados!"
                );
            }
        }

        List<TravelTimeResult.SegmentTime> segmentTimes = new ArrayList<>();

        for (LineSegment segment : segments) {
            int effectiveSpeed = Math.min(segment.getSpeedLimit(), locomotive.getOpSpeed());
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
