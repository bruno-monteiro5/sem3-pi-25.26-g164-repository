package WarehouseManagementFunctions;

import domain.Station;
import domain.StationIndex;
import controller.StationCsvLoader;

import java.io.IOException;
import java.util.*;

public class StationIndexUI implements Runnable {

    private static final String CSV_PATH = "warehouseMngt/train_station_dataset/train_stations_europe.csv";

    @Override
    public void run() {
        boolean running = true;
        Scanner sc = new Scanner(System.in);

        while (running) {
            System.out.println("\n=== Time-Zone Index and Windowed Queries (USEI06) ===\n");

            try {
                StationCsvLoader loader = new StationCsvLoader();
                System.out.println("Loading CSV: " + CSV_PATH);
                List<Station> stations = loader.load(CSV_PATH);
                System.out.printf("CSV read successfully. Total stations loaded: %d\n\n", stations.size());

                StationIndex index = new StationIndex();
                index.addAll(stations);

                System.out.println("Indexes built successfully!");
                System.out.printf("→ %d distinct latitudes\n", index.sizeLatKeys());
                System.out.printf("→ %d distinct longitudes\n", index.sizeLonKeys());
                System.out.printf("→ %d distinct time-zone groups\n\n", index.sizeTzGroups());

                // 5 demonstration queries
                System.out.println("--- Query 1: Latitude [38.0, 42.0] ---");
                printSample(index.queryLatitudeWindow(38.0, 42.0));

                System.out.println("\n--- Query 2: Longitude [-9.5, -6.0] ---");
                printSample(index.queryLongitudeWindow(-9.5, -6.0));

                System.out.println("\n--- Query 3: Group 'CET' ---");
                printSample(index.queryByTzGroup("CET"));

                System.out.println("\n--- Query 4: Groups ['CET', 'WET'] ---");
                printSample(index.queryByTzWindow(Arrays.asList("CET", "WET")));

                System.out.println("\n--- Query 5: Group 'CET' and Country 'ES' ---");
                printSample(index.queryByTzAndCountry("CET", "ES"));

            } catch (IOException e) {
                System.err.println("Error reading CSV: " + e.getMessage());
            }

            System.out.println("\nPress Enter to return to the previous menu...");
            sc.nextLine();
            running = false;

        }
    }

    private void printSample(List<Station> list) {
        if (list.isEmpty()) {
            System.out.println("(no results)");
            return;
        }
        int count = 0;
        for (Station s : list) {
            System.out.printf("%s (%s)  lat=%.2f  lon=%.2f  tz=%s\n",
                    s.getName(), s.getCountry(),
                    s.getLocation().getLatitude(),
                    s.getLocation().getLongitude(),
                    s.getTimeZoneGroup());
            count++;
            if (count >= 10) {
                System.out.printf("... (%d more)\n", list.size() - count);
                break;
            }
        }
    }
}
