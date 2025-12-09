package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized controller for loading railway network data from CSV files.
 * Updates the singleton repository that all Sprint 3 controllers share.
 */
public class BuildRailwayNetworkController {

    private final S3_RailwayRepository repo;

    public BuildRailwayNetworkController() {
        this.repo = S3_RailwayRepository.getInstance();
    }

    /**
     * Loads stations and lines from CSV files into the central repository.
     *
     * @param stationsPath path to stations CSV
     * @param linesPath path to lines CSV
     * @throws IOException if file reading fails
     */
    public void buildFromCsv(String stationsPath, String linesPath) throws IOException {
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        List<Station> stations = stationLoader.load(stationsPath);

        Map<Integer, Station> stationsById = new HashMap<>();
        for (Station s : stations) {
            stationsById.put(s.getId(), s);
        }

        LineCsvLoader lineLoader = new LineCsvLoader();
        List<Line> lines = lineLoader.load(linesPath, stationsById);

        repo.loadStations(stations);
        repo.loadLines(lines);

        System.out.printf("✓ Network loaded successfully: %d stations, %d lines%n",
                stations.size(), lines.size());
    }

    /**
     * Returns true if the repository has loaded network data.
     */
    public boolean isNetworkLoaded() {
        return repo.isDataLoaded();
    }

    /**
     * Returns the number of loaded stations.
     */
    public int getStationCount() {
        return repo.getStationCount();
    }

    /**
     * Returns the number of loaded lines.
     */
    public int getLineCount() {
        return repo.getLineCount();
    }

    /**
     * Clears all loaded data from the repository.
     */
    public void clearNetwork() {
        repo.clear();
        System.out.println("Repository cleared.");
    }
}