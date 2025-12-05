package ui.RailwayNetworkOptimization;

import controller.MinimalBackboneController;
import domain.MinimalBackboneResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MinimalBackboneUI implements Runnable {

    private final MinimalBackboneController controller = new MinimalBackboneController();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        System.out.println("=== USEI12 - Minimal Backbone Network ===");

        try {
            String stations = "warehouseMngt/train_station_dataset/stations.csv";
            String lines = "warehouseMngt/train_station_dataset/lines.csv";

            System.out.println("Building graph...");
            controller.buildGraphFromCsv(stations, lines);

            System.out.println("Computing minimal backbone...");
            MinimalBackboneResult result = controller.calculateMinimalBackbone();

            showResults(result);
            askExport(result);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private void showResults(MinimalBackboneResult r) {
        System.out.println("\n--- Backbone Summary ---");
        System.out.printf("Total distance: %.2f km%n", r.getTotalDistance());
        System.out.printf("Stations: %d%n", r.getStationCount());
        System.out.printf("Connections: %d%n", r.getConnectionCount());

        System.out.println("\n--- Connections ---");
        for (Edge<Station, Double> e : r.getBackboneGraph().edges()) {
            System.out.printf(
                    "%s -- %s : %.2f km%n",
                    e.getVOrig().getName(),
                    e.getVDest().getName(),
                    e.getWeight()
            );
        }
    }

    private void askExport(MinimalBackboneResult result) {
        System.out.println("\nExport DOT file? (y/n)");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;

        System.out.println("Filename (default: backbone.dot):");
        String f = scanner.nextLine().trim();
        if (f.isEmpty()) f = "backbone.dot";

        try {
            result.exportToDot(f);
            System.out.println("DOT exported to: " + f);
            System.out.println("Generate SVG with:");
            System.out.println("   neato -Tsvg " + f + " -o backbone.svg");
        } catch (IOException e) {
            System.err.println("Failed to save DOT: " + e.getMessage());
        }
    }
}
