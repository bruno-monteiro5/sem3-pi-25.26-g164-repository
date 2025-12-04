package controller;

import controller.Sprint3Loaders.LineCsvLoader;
import controller.Sprint3Loaders.StationCsvLoaderS3;
import domain.Line;
import domain.Station;
import repository.S3_RailwayRepository;

import java.io.IOException;
import java.util.List;

public class BuildRailwayNetworkController {

    private final S3_RailwayRepository repo = S3_RailwayRepository.getInstance();

    public void buildFromCsv(String stationsPath, String linesPath) {
        StationCsvLoaderS3 stationLoader = new StationCsvLoaderS3();
        LineCsvLoader lineLoader = new LineCsvLoader();

        try {
            List<Station> stations = stationLoader.load(stationsPath);
            repo.loadStations(stations);

            List<Line> lines = lineLoader.load(linesPath, repo.getStationsById());
            repo.loadLines(lines);

            System.out.printf("Loaded %d stations and %d lines.%n",
                    stations.size(), lines.size());
        } catch (IOException e) {
            System.out.println("Error loading CSVs: " + e.getMessage());
        }
    }
}
