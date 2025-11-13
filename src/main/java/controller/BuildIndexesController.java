package controller;

import domain.Station;
import repository.StationRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Controller that orchestrates building the AVL indexes from a CSV loader and
 * exposes query methods required by the use case.
 */
public class BuildIndexesController {

    private StationRepository stationRepository;

    /**
     * Creates a controller with the given repository dependency.
     *
     * @param stationRepository repository that stores and queries stations
     */
    public BuildIndexesController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Builds all indexes from a CSV file path using the provided loader.
     * <p>
     * This method calls {@code stationCsvLoader.load(csvPath)} to obtain a
     * {@code List<Station>} and then delegates indexing to the repository.
     * A simple timing summary is printed to stdout.
     *
     * @param stationCsvLoader loader capable of reading the CSV file
     * @param csvPath          filesystem path to the CSV file
     */
    public void buildFromCsvPath(StationCsvLoader stationCsvLoader, String csvPath) {
        long startNanos = System.nanoTime();

        List<Station> loadedStations;
        try {
            loadedStations = stationCsvLoader.load(csvPath);
        } catch (IOException ioException) {
            System.out.printf("Error reading CSV '%s': %s%n", csvPath, ioException.getMessage());
            loadedStations = Collections.emptyList();
        }

        stationRepository.loadAll(loadedStations);

        long endNanos = System.nanoTime();
        double elapsedSeconds = (endNanos - startNanos) / 1e9;

        System.out.printf(
                "Index build finished. Loaded=%d, Rejected=%d, Time=%.3f s%n",
                stationRepository.getTotalLoadedCount(),
                stationRepository.getTotalRejectedCount(),
                elapsedSeconds
        );
    }




    public List<Station> queryByLatitude(double minLatitude, double maxLatitude) {
        return stationRepository.findByLatitudeWindow(minLatitude, maxLatitude);
    }

    public List<Station> queryByLongitude(double minLongitude, double maxLongitude) {
        return stationRepository.findByLongitudeWindow(minLongitude, maxLongitude);
    }

    public List<Station> queryByTimeZoneGroup(String timeZoneGroup) {
        return stationRepository.findByTimeZoneGroup(timeZoneGroup);
    }

    public List<Station> queryByTimeZoneAndCountry(String timeZoneGroup, String country) {
        return stationRepository.findByTimeZoneAndCountry(timeZoneGroup, country);
    }
}
