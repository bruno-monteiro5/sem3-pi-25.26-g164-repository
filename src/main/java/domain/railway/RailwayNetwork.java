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

        // ========== Build Maps Manually (NO STREAMS) ==========

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

        // Compute next path ID without streams
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

    // ==================== BASIC GETTERS ====================

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

    // =================== NETWORK LOGIC ===================

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

    // =================== IN-MEMORY PATH OPERATIONS ===================

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
     */
    public void assignPathToTrain(Train train, Path path) {
        List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();

        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("Path has no endpoints");
        }

        Endpoint start = ordered.get(0).getEndpoint();
        Endpoint end = ordered.get(ordered.size() - 1).getEndpoint();

        train.setPath(path);
        train.setStartEndpoint(start);
        train.setEndEndpoint(end);
    }
}
