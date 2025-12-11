package repository;

import domain.Line;
import domain.Station;
import domain.graph.map.MapGraph;

import java.util.*;

/**
 * Singleton repository for Railway Network data (Sprint 3).
 * Maintains a single source of truth for stations and lines.
 */
public class S3_RailwayRepository {

    private static S3_RailwayRepository instance = null;

    private final Map<Integer, Station> stationsById = new HashMap<>();
    private final List<Line> lines = new ArrayList<>();
    private boolean dataLoaded = false;

    private MapGraph<Station, Double> graph;

    private S3_RailwayRepository() {}

    public static S3_RailwayRepository getInstance() {
        if (instance == null) {
            instance = new S3_RailwayRepository();
        }
        return instance;
    }

    /**
     * Loads stations into the repository.
     * Marks data as loaded.
     */
    public void loadStations(List<Station> stations) {
        stationsById.clear();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }
        checkAndUpdateLoadedStatus();
    }

    /**
     * Loads lines into the repository.
     * Marks data as loaded.
     */
    public void loadLines(List<Line> lineList) {
        lines.clear();
        lines.addAll(lineList);
        checkAndUpdateLoadedStatus();
    }

    /**
     * Checks if both stations and lines are loaded.
     */
    private void checkAndUpdateLoadedStatus() {
        dataLoaded = !stationsById.isEmpty() && !lines.isEmpty();
    }

    /**
     * Returns true if the repository has loaded data.
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Returns the number of stations currently loaded.
     */
    public int getStationCount() {
        return stationsById.size();
    }

    /**
     * Returns the number of lines currently loaded.
     */
    public int getLineCount() {
        return lines.size();
    }

    /**
     * Returns an unmodifiable map of stations by ID.
     */
    public Map<Integer, Station> getStationsById() {
        return Collections.unmodifiableMap(stationsById);
    }

    /**
     * Returns an unmodifiable list of lines.
     */
    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Finds a station by its ID.
     * @param id station ID
     * @return the Station object, or null if not found
     */
    public Station getStationById(String id) {
        for(Map.Entry<Integer, Station> entry : stationsById.entrySet()) {
            if(id.equals(entry.getValue().getName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Sets the current graph
     * @param graph to set in the instance
     */
    public void setGraph(MapGraph<Station, Double> graph) {
        this.graph = graph;
    }

    /**
     * Returns the current instance's graph
     * @return graph
     */

    public MapGraph<Station, Double> getGraph() {
        return graph;
    }


    /**
     * Clears all loaded data.
     * Useful for testing or resetting the application.
     */
    public void clear() {
        stationsById.clear();
        lines.clear();
        dataLoaded = false;
    }
}