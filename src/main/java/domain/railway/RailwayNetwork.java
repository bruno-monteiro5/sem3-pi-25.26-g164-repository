package domain.railway;

import repository.RailwayRepository;

import java.sql.SQLException;
import java.util.*;

/**
 * Represents the entire railway network loaded in memory.
 *
 * <p>Concept:
 * <ul>
 *   <li>Load all data once from the database at startup</li>
 *   <li>The scheduler and UI work only with these in-memory objects</li>
 *   <li>New paths or route changes are NOT persisted in the database</li>
 * </ul>
 *
 * <p><b>VERSION 2.1 - USLP07 Improvements:</b>
 * <ul>
 *   <li>Added route validation when assigning paths to trains</li>
 *   <li>Validates gauge compatibility</li>
 *   <li>Validates weight limits</li>
 *   <li>Validates electrification for electric locomotives</li>
 * </ul>
 */
public class RailwayNetwork {

    private final List<Endpoint> endpoints;
    private final List<Line> lines;
    private final List<LineSegment> segments;
    private final List<Path> paths;
    private final List<Train> trains;

    private final Map<Integer, Endpoint> endpointById;
    private final Map<Integer, Line> lineById;
    private final Map<Integer, List<LineSegment>> segmentsByLineId;
    private final Map<Integer, Path> pathById;
    private final Map<Integer, Train> trainById;
    private final Map<Integer, TrackType> trackTypeBySegmentId;

    private int nextPathId;

    /**
     * Private constructor: all data is injected already prepared.
     */
    private RailwayNetwork(List<Endpoint> endpoints,
                           List<Line> lines,
                           List<LineSegment> segments,
                           List<Path> paths,
                           List<Train> trains,
                           Map<Integer, TrackType> trackTypeBySegmentId) {

        this.endpoints = endpoints;
        this.lines = lines;
        this.segments = segments;
        this.paths = paths;
        this.trains = trains;


        this.endpointById = new HashMap<>();
        for (Endpoint e : endpoints) {
            endpointById.put(e.getId(), e);
        }

        this.lineById = new HashMap<>();
        for (Line l : lines) {
            lineById.put(l.getId(), l);
        }

        this.segmentsByLineId = new HashMap<>();
        for (LineSegment seg : segments) {
            int lineId = seg.getLineId();
            if (!segmentsByLineId.containsKey(lineId)) {
                segmentsByLineId.put(lineId, new ArrayList<>());
            }
            segmentsByLineId.get(lineId).add(seg);
        }

        this.pathById = new HashMap<>();
        for (Path p : paths) {
            pathById.put(p.getId(), p);
        }

        this.trainById = new HashMap<>();
        for (Train t : trains) {
            trainById.put(t.getId(), t);
        }

        this.trackTypeBySegmentId = trackTypeBySegmentId;

        int maxId = 0;
        for (Path p : paths) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        this.nextPathId = maxId + 1;
    }

    /**
     * Loads all required railway data from the database one time.
     */
    public static RailwayNetwork loadFromDatabase(RailwayRepository repository) throws SQLException {
        List<Endpoint> endpoints = repository.getAllEndpoints();
        List<Line> lines = repository.getAllLines();

        List<LineSegment> segments = new ArrayList<>();
        for (Line line : lines) {
            segments.addAll(repository.getLineSegments(line.getId()));
        }

        List<Path> paths = repository.getAllPaths();
        List<Train> trains = repository.getAllTrains();

        // Load track type for each segment
        Map<Integer, TrackType> trackTypeBySegmentId = new HashMap<>();
        for (LineSegment seg : segments) {
            TrackType type = repository.getTrackType(seg.getId());
            trackTypeBySegmentId.put(seg.getId(), type);
        }

        return new RailwayNetwork(
                endpoints,
                lines,
                segments,
                paths,
                trains,
                trackTypeBySegmentId
        );
    }


    public List<Train> getTrains() {
        return Collections.unmodifiableList(trains);
    }

    public Train getTrainById(int id) {
        return trainById.get(id);
    }

    public List<Path> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public Path getPathById(int id) {
        return pathById.get(id);
    }

    public List<Endpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    public Endpoint getEndpointById(int id) {
        return endpointById.get(id);
    }

    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public List<LineSegment> getSegmentsForLine(int lineId) {
        List<LineSegment> list = segmentsByLineId.get(lineId);
        return list != null ? list : List.of();
    }

    public TrackType getTrackType(LineSegment segment) {
        return trackTypeBySegmentId.get(segment.getId());
    }


    /**
     * Finds the segment connecting two endpoints, using in-memory data only.
     *
     * <p>Logic:
     * <ul>
     *   <li>Find a line whose endpoints match A and B (in any direction)</li>
     *   <li>Return the FIRST segment belonging to that line</li>
     * </ul>
     */
    public LineSegment findSegmentBetween(int endpointAId, int endpointBId) {
        for (Line line : lines) {
            boolean connects =
                    (line.getEndpointA() == endpointAId && line.getEndpointB() == endpointBId) ||
                            (line.getEndpointA() == endpointBId && line.getEndpointB() == endpointAId);

            if (connects) {
                List<LineSegment> segs = segmentsByLineId.get(line.getId());
                if (segs != null && !segs.isEmpty()) {
                    return segs.get(0);
                }
            }
        }
        return null;
    }


    /**
     * Creates a new Path only in memory (not persisted).
     */
    public Path createInMemoryPath(String name) {
        int id = nextPathId++;
        Path path = new Path(id, name);
        paths.add(path);
        pathById.put(id, path);
        return path;
    }

    /**
     * Adds an endpoint to an in-memory Path.
     */
    public void addEndpointToPath(Path path, Endpoint endpoint, boolean mustStop) {
        List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();

        int nextSeq;
        if (ordered.isEmpty()) {
            nextSeq = 1;
        } else {
            Path.PathEndpoint last = ordered.get(ordered.size() - 1);
            nextSeq = last.getSequence() + 1;
        }

        Path.PathEndpoint pe = new Path.PathEndpoint(nextSeq, endpoint, mustStop);
        path.addEndpoint(pe);
    }

    /**
     * Assigns an in-memory path to a train.
     * Updates its path, startEndpoint, and endEndpoint.
     *
     * <p><b>VERSION 2.1 - NEW VALIDATIONS:</b>
     * <ul>
     *   <li>Validates gauge compatibility between train and path</li>
     *   <li>Validates weight limits on all segments</li>
     *   <li>Validates electrification requirements for electric trains</li>
     * </ul>
     *
     * @param train The train to assign the path to
     * @param path The path to assign
     * @throws IllegalArgumentException if validation fails
     */
    public void assignPathToTrain(Train train, Path path) {
        List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();

        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("Path has no endpoints");
        }


        // Validate Gauge Compatibility
        if (!validateGaugeCompatibility(train, path)) {
            throw new IllegalArgumentException(
                    String.format(
                            "GAUGE INCOMPATIBILITY: Train %d locomotives do not support all gauges in path '%s'",
                            train.getId(), path.getName()
                    )
            );
        }

        // Validate Weight Limits
        String weightError = validateWeightLimits(train, path);
        if (weightError != null) {
            throw new IllegalArgumentException(weightError);
        }

        // Validate Electrification
        if (!validateElectrification(train, path)) {
            throw new IllegalArgumentException(
                    String.format(
                            "ELECTRIFICATION ERROR: Train %d has only electric locomotives but path '%s' contains non-electrified segments",
                            train.getId(), path.getName()
                    )
            );
        }


        Endpoint start = ordered.get(0).getEndpoint();
        Endpoint end = ordered.get(ordered.size() - 1).getEndpoint();

        train.setPath(path);
        train.setStartEndpoint(start);
        train.setEndEndpoint(end);
    }


    /**
     * Validates that the train's locomotives support all gauges in the path.
     *
     * @param train The train to validate
     * @param path The path to validate against
     * @return true if compatible, false otherwise
     */
    private boolean validateGaugeCompatibility(Train train, Path path) {
        List<Path.PathEndpoint> endpoints = path.getOrderedEndpoints();

        Set<Integer> requiredGauges = new HashSet<>();

        for (int i = 0; i < endpoints.size() - 1; i++) {
            Endpoint from = endpoints.get(i).getEndpoint();
            Endpoint to = endpoints.get(i + 1).getEndpoint();

            LineSegment segment = findSegmentBetween(from.getId(), to.getId());
            if (segment != null) {
                // Find the line that contains this segment
                for (Line line : lines) {
                    List<LineSegment> lineSegs = segmentsByLineId.get(line.getId());
                    if (lineSegs != null && lineSegs.contains(segment)) {
                        requiredGauges.add(line.getGauge());
                        break;
                    }
                }
            }
        }

        // Check if all locomotives support at least one of the required gauges
        for (Locomotive loco : train.getLocomotives()) {
            boolean supportsAnyGauge = false;
            for (Integer gauge : requiredGauges) {
                if (loco.getGauge() == gauge) {
                    supportsAnyGauge = true;
                    break;
                }
            }
            if (!supportsAnyGauge && !requiredGauges.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that the train's weight does not exceed limits on any segment.
     *
     * @param train The train to validate
     * @param path The path to validate against
     * @return null if valid, error message otherwise
     */
    private String validateWeightLimits(Train train, Path path) {
        double trainWeightKg = train.getTotalWeight() * 1000.0;

        for (int i = 0; i < path.getOrderedEndpoints().size() - 1; i++) {
            Endpoint from = path.getOrderedEndpoints().get(i).getEndpoint();
            Endpoint to = path.getOrderedEndpoints().get(i + 1).getEndpoint();

            LineSegment segment = findSegmentBetween(from.getId(), to.getId());
            if (segment == null) continue;

            double segmentCapacity = segment.getMaxWeight() * segment.getLength();

            if (trainWeightKg > segmentCapacity) {
                return String.format(
                        "Train %d (%.0f kg) exceeds segment '%s → %s' capacity (%.0f kg)",
                        train.getId(), trainWeightKg, from.getName(), to.getName(), segmentCapacity
                );
            }
        }
        return null;
    }

    /**
     * Validates that electric-only trains can operate on the entire path.
     *
     * @param train The train to validate
     * @param path The path to validate against
     * @return true if valid, false if electric train on non-electrified track
     */
    private boolean validateElectrification(Train train, Path path) {
        // Only validate if train has ONLY electric locomotives
        boolean hasOnlyElectric = true;
        for (Locomotive loco : train.getLocomotives()) {
            if (!loco.isElectric()) {
                hasOnlyElectric = false;
                break;
            }
        }

        if (!hasOnlyElectric) {
            return true;
        }

        // Check if all segments are electrified
        List<Path.PathEndpoint> endpoints = path.getOrderedEndpoints();

        for (int i = 0; i < endpoints.size() - 1; i++) {
            Endpoint from = endpoints.get(i).getEndpoint();
            Endpoint to = endpoints.get(i + 1).getEndpoint();

            LineSegment segment = findSegmentBetween(from.getId(), to.getId());
            if (segment != null && !segment.isElectrified()) {
                return false;
            }
        }

        return true;
    }
}