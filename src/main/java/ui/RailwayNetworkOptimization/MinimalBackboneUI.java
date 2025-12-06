package ui.RailwayNetworkOptimization;

import controller.MinimalBackboneController;
import domain.MinimalBackboneResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * UI for USEI12 - Minimal Backbone Network.
 * <p>
 * Handles user interaction for calculating and visualizing the minimal backbone
 * of the railway network.
 */
public class MinimalBackboneUI implements Runnable {

    private final MinimalBackboneController controller;
    private final Scanner scanner;

    /**
     * Constructs the UI with a new controller.
     */
    public MinimalBackboneUI() {
        this.controller = new MinimalBackboneController();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Runs the UI workflow.
     */
    @Override
    public void run() {
        System.out.println("\n=== US12 - Minimal Backbone Network ===");

        try {
            // Default paths based on project structure
            String defaultStations = "warehouseMngt/train_station_dataset/stations.csv";
            String defaultLines = "warehouseMngt/train_station_dataset/lines.csv";

            System.out.println("Enter path for Stations CSV (default: " + defaultStations + "):");
            String stationsPath = scanner.nextLine().trim();
            if (stationsPath.isEmpty()) {
                stationsPath = defaultStations;
            }

            System.out.println("Enter path for Lines CSV (default: " + defaultLines + "):");
            String linesPath = scanner.nextLine().trim();
            if (linesPath.isEmpty()) {
                linesPath = defaultLines;
            }

            System.out.println("Building graph...");
            controller.buildGraphFromCsv(stationsPath, linesPath);

            System.out.println("Calculating minimal backbone (Minimum Spanning Tree)...");
            MinimalBackboneResult result = controller.calculateMinimalBackbone();

            displayResults(result);
            handleExport(result);

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Logic error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the summary of the backbone network.
     *
     * @param result the calculation result
     */
    private void displayResults(MinimalBackboneResult result) {
        System.out.println("\n--- Results ---");
        System.out.printf("Total Backbone Distance: %.3f km%n", result.getTotalDistance());
        System.out.printf("Total Stations: %d%n", result.getStationCount());
        System.out.printf("Total Connections: %d%n", result.getConnectionCount());

        System.out.println("\nDetailed Connections:");
        MapGraph<Station, Double> graph = result.getBackboneGraph();
        List<Edge<Station, Double>> edges = new java.util.ArrayList<>();

        for (Edge<Station, Double> e : graph.edges()) {
            edges.add(e);
        }

        // Simple manual sort for display consistency if needed, or just print
        for (Edge<Station, Double> edge : edges) {
            System.out.printf("  %s -- %s : %.3f km%n",
                    edge.getVOrig().getName(),
                    edge.getVDest().getName(),
                    edge.getWeight());
        }
    }

    /**
     * Asks the user if they want to export the result to a DOT file.
     *
     * @param result the calculation result
     */
    private void handleExport(MinimalBackboneResult result) {
        System.out.println("\nDo you want to export the graph to a .dot file? (y/n)");
        String choice = scanner.nextLine().trim();

        if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
            System.out.println("Enter output filename (default: backbone.dot):");
            String filename = scanner.nextLine().trim();
            if (filename.isEmpty()) {
                filename = "backbone.dot";
            }

            try {
                result.exportToDot(filename);
                System.out.println("Successfully exported to " + filename);
                System.out.println("You can generate an image using Graphviz: neato -Tpng " + filename + " -o backbone.png");
            } catch (IOException e) {
                System.err.println("Failed to write file: " + e.getMessage());
            }
        }
    }
}