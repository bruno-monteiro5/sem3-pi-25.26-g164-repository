package repository;

import domain.Line;
import domain.Station;

import java.util.*;

public class S3_RailwayRepository {

    private static final S3_RailwayRepository INSTANCE = new S3_RailwayRepository();

    private final Map<Integer, Station> stationsById = new HashMap<>();
    private final List<Line> lines = new ArrayList<>();

    private S3_RailwayRepository() {}

    public static S3_RailwayRepository getInstance() {
        return INSTANCE;
    }

    public void loadStations(List<Station> stations) {
        stationsById.clear();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }
    }

    public void loadLines(List<Line> lineList) {
        lines.clear();
        lines.addAll(lineList);
    }

    public Map<Integer, Station> getStationsById() {
        return Collections.unmodifiableMap(stationsById);
    }

    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }

}
