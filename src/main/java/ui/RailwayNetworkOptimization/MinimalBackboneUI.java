package ui.RailwayNetworkOptimization;

import controller.MinimalBackboneController;
import domain.MinimalBackboneResult;
import domain.Station;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import ui.UIHelper;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * UI for USEI12 - Minimal Backbone Network.
 * Handles user interaction for calculating and visualizing the minimal backbone
 * of the railway network.
 */
public class MinimalBackboneUI implements Runnable {

    private final MinimalBackboneController controller;
    private final Scanner scanner;

    public MinimalBackboneUI() {
        this.controller = new MinimalBackboneController();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {

        UIHelper.printHeader("USEI12 - MINIMAL BACKBONE NETWORK");

        try {
            String defaultStations = "warehouseMngt/train_station_dataset/stations.csv";
            String defaultLines = "warehouseMngt/train_station_dataset/lines.csv";

            System.out.println();
            System.out.print(UIHelper.getCyan() + "➤ " + UIHelper.getReset() +
                    "Enter path for Stations CSV [" + defaultStations + "]: ");
            String stationsPath = scanner.nextLine().trim();
            if (stationsPath.isEmpty()) stationsPath = defaultStations;

            System.out.print(UIHelper.getCyan() + "➤ " + UIHelper.getReset() +
                    "Enter path for Lines CSV [" + defaultLines + "]: ");
            String linesPath = scanner.nextLine().trim();
            if (linesPath.isEmpty()) linesPath = defaultLines;

            UIHelper.printInfo("Building graph...");
            controller.buildGraphFromCsv(stationsPath, linesPath);

            UIHelper.printInfo("Calculating minimal backbone (Minimum Spanning Tree)...");
            MinimalBackboneResult result = controller.calculateMinimalBackbone();

            UIHelper.printSuccess("Minimal backbone successfully calculated.");

            displayResults(result);
            handleExport(result);

        } catch (IOException e) {
            UIHelper.printError("Error reading files: " + e.getMessage());
        } catch (IllegalStateException e) {
            UIHelper.printError("Logic error: " + e.getMessage());
        } catch (Exception e) {
            UIHelper.printError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayResults(MinimalBackboneResult result) {

        UIHelper.printSubHeader("Backbone Summary");

        System.out.printf("  Total Backbone Distance : %.3f km%n", result.getTotalDistance());
        System.out.printf("  Total Stations          : %d%n", result.getStationCount());
        System.out.printf("  Total Connections       : %d%n", result.getConnectionCount());

        System.out.println();
        UIHelper.printSubHeader("Detailed Connections");

        MapGraph<Station, Double> graph = result.getBackboneGraph();
        List<Edge<Station, Double>> edges = new java.util.ArrayList<>();

        for (Edge<Station, Double> e : graph.edges()) edges.add(e);

        if (edges.isEmpty()) {
            UIHelper.printWarning("No connections found in the backbone.");
            return;
        }

        for (Edge<Station, Double> edge : edges) {
            System.out.printf("  %-25s -> %-25s : %7.3f km%n",
                    edge.getVOrig().getName(),
                    edge.getVDest().getName(),
                    edge.getWeight());
        }
    }

    private void handleExport(MinimalBackboneResult result) {

        UIHelper.printSubHeader("Export Graph (.dot)");

        System.out.print(UIHelper.getCyan() + "➤ " + UIHelper.getReset() +
                "Do you want to export the graph to a .dot file? (y/n): ");
        String choice = scanner.nextLine().trim();

        if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {

            System.out.print(UIHelper.getCyan() + "➤ " + UIHelper.getReset() +
                    "Output filename [backbone.dot]: ");
            String filename = scanner.nextLine().trim();
            if (filename.isEmpty()) filename = "backbone.dot";

            try {
                result.exportToDot(filename);
                UIHelper.printSuccess("Successfully exported to " + filename);
                System.out.println("  You can generate an image using Graphviz:");
                System.out.println("  neato -Tpng " + filename + " -o backbone.png");
            } catch (IOException e) {
                UIHelper.printError("Failed to write file: " + e.getMessage());
            }

        } else {
            UIHelper.printInfo("Export cancelled.");
        }
    }
}
