package repository;

import domain.Line;
import domain.Station;

import java.util.*;

public class S3_RailwayRepository {

    private static S3_RailwayRepository instance = null;

    private final Map<Integer, Station> stationsById = new HashMap<>();
    private final List<Line> lines = new ArrayList<>();

    private S3_RailwayRepository() {}

    public static S3_RailwayRepository getInstance() {
        if (instance == null) {
            instance = new S3_RailwayRepository();
        }
        return instance;
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
