package domain.railway;

import repository.RailwayRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa toda a rede ferroviária carregada em memória.
 *
 * A ideia é:
 *  - Carregar tudo uma vez da BD (no arranque)
 *  - A partir daí, o scheduler e o UI trabalham só com estes objetos
 *  - Criação de novos Paths / alteração de rotas NÃO é persistida na BD
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

    // Para gerar IDs de paths em memória (que não existem na BD)
    private int nextPathId;

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

        this.endpointById = endpoints.stream()
                .collect(Collectors.toMap(Endpoint::getId, e -> e));

        this.lineById = lines.stream()
                .collect(Collectors.toMap(Line::getId, l -> l));

        this.segmentsByLineId = segments.stream()
                .collect(Collectors.groupingBy(LineSegment::getLineId));

        this.pathById = paths.stream()
                .collect(Collectors.toMap(Path::getId, p -> p));

        this.trainById = trains.stream()
                .collect(Collectors.toMap(Train::getId, t -> t));

        this.trackTypeBySegmentId = trackTypeBySegmentId;

        this.nextPathId = paths.stream()
                .mapToInt(Path::getId)
                .max()
                .orElse(0) + 1;
    }

    /**
     * Carrega todos os dados necessários da BD uma vez.
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

        // Carregar o tipo de via de cada segmento só uma vez
        Map<Integer, TrackType> trackTypeBySegmentId = new HashMap<>();
        for (LineSegment segment : segments) {
            TrackType type = repository.getTrackType(segment.getId());
            trackTypeBySegmentId.put(segment.getId(), type);
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

    // ================== GETTERS BÁSICOS ==================

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
        return segmentsByLineId.getOrDefault(lineId, List.of());
    }

    public TrackType getTrackType(LineSegment segment) {
        return trackTypeBySegmentId.get(segment.getId());
    }

    // ================== LÓGICA DE REDE ==================

    /**
     * Encontrar o segmento entre dois endpoints, usando apenas dados em memória.
     * Mantém a mesma lógica simplificada do TrainScheduler atual:
     *  - procura a linha que liga os dois endpoints
     *  - devolve o primeiro segmento dessa linha
     */
    public LineSegment findSegmentBetween(int endpointAId, int endpointBId) {
        for (Line line : lines) {
            boolean connectsAB =
                    (line.getEndpointA() == endpointAId && line.getEndpointB() == endpointBId) ||
                            (line.getEndpointA() == endpointBId && line.getEndpointB() == endpointAId);

            if (connectsAB) {
                List<LineSegment> lineSegments = segmentsByLineId.getOrDefault(line.getId(), List.of());
                if (!lineSegments.isEmpty()) {
                    return lineSegments.get(0);
                }
            }
        }
        return null;
    }

    // ================== OPERAÇÕES EM MEMÓRIA (Paths / Rotas) ==================

    /**
     * Cria um Path NOVO só em memória (não é persistido na BD).
     */
    public Path createInMemoryPath(String name) {
        int id = nextPathId++;
        Path path = new Path(id, name);
        paths.add(path);
        pathById.put(id, path);
        return path;
    }

    /**
     * Adiciona um endpoint a um Path em memória.
     */
    public void addEndpointToPath(Path path, Endpoint endpoint, boolean mustStop) {
        List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();
        int nextSeq = ordered.isEmpty()
                ? 1
                : ordered.get(ordered.size() - 1).getSequence() + 1;

        Path.PathEndpoint pe = new Path.PathEndpoint(nextSeq, endpoint, mustStop);
        path.addEndpoint(pe);
    }

    /**
     * Atribui um Path (em memória) a um Train (também em memória).
     * Atualiza path, startEndpoint e endEndpoint no objeto Train.
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
