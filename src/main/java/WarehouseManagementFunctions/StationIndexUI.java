package WarehouseManagementFunctions;

import domain.Station;
import controller.StationCsvLoader;

import java.io.IOException;
import java.util.List;


public class StationIndexUI implements Runnable {

    private static final String CSV_PATH = "warehouseMngt/train_station_dataset/train_stations_europe.csv";

    @Override
    public void run() {
        System.out.println("\n=== Time-Zone Index and Windowed Queries (USEI06) ===");
        System.out.println("CSV lecture\n");

        try {
            StationCsvLoader loader = new StationCsvLoader();
            System.out.println("Loading the CSV: " + CSV_PATH);
            List<Station> stations = loader.load(CSV_PATH);

            System.out.printf("CSV read with sucess. The total of stations loaded: %d\n", stations.size());

        } catch (IOException e) {
            System.err.println("Error reading the CSV: " + e.getMessage());
        }
    }
}
