package ui.RailwayNetworkOptimization;

import controller.MinimalBackboneController;
import domain.MinimalBackboneResult;
import domain.Option;
import domain.Station;
import domain.Utils;
import domain.graph.Edge;
import domain.graph.map.MapGraph;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * UI for USEI12 - Minimal Backbone Network.
 * Handles user interaction for calculating and visualizing the minimal backbone
 * of the railway network.
 */
public class MinimalBackboneUI implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();
    private static final String SEP    = "─";

    private final MinimalBackboneController controller;

    public MinimalBackboneUI() {
        this.controller = new MinimalBackboneController();
    }

    @Override
    public void run() {
        menu();
    }

    /**
     * Displays the main menu for minimal backbone analysis.
     */
    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("           MINIMAL BACKBONE NETWORK (USEI12)");
        System.out.println(line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        if (controller.isGraphBuilt()) {
            System.out.println(GREEN + "  ✓ Railway network loaded" + RESET);
            System.out.printf("    • %d stations\n", controller.getStationCount());
            System.out.printf("    • %d connections\n", controller.getLineCount());
        } else {
            System.out.println(YELLOW + "  ⚠ Network not loaded yet" + RESET);
            System.out.println("    Please load network data from the main menu (option 1)");
        }
        System.out.println();

        Option[] options = new Option[3];

        options[0] = new Option("Calculate Minimal Backbone (MST)", this::calculateBackboneAction);
        options[1] = new Option("Calculate and Export (DOT + SVG)", this::calculateAndExportAction);
        options[2] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
    }

    /**
     * Action to calculate and display minimal backbone.
     */
    private void calculateBackboneAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first from the main menu." + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\n" + CYAN + "⏳ Calculating minimal backbone network..." + RESET);

        try {
            long start = System.currentTimeMillis();
            MinimalBackboneResult result = controller.calculateMinimalBackbone();
            long duration = System.currentTimeMillis() - start;

            displayResults(result, duration);

        } catch (Exception e) {
            System.out.println(RED + "Error calculating backbone: " + e.getMessage() + RESET);
            e.printStackTrace();
        }

        pause();
        menu();
    }

    /**
     * Action to calculate backbone and export to DOT and SVG.
     */
    private void calculateAndExportAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first from the main menu." + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\n" + CYAN + "⏳ Calculating minimal backbone network..." + RESET);

        try {
            long start = System.currentTimeMillis();
            MinimalBackboneResult result = controller.calculateMinimalBackbone();
            long duration = System.currentTimeMillis() - start;

            displayResults(result, duration);

            // Export to DOT
            Scanner sc = new Scanner(System.in);
            System.out.print("\n" + CYAN + "➤ " + RESET + "Output filename for DOT file [backbone.dot]: ");
            String dotFilename = sc.nextLine().trim();
            if (dotFilename.isEmpty()) dotFilename = "backbone.dot";

            System.out.println(CYAN + "⏳ Exporting to DOT format..." + RESET);
            result.exportToDot(dotFilename);
            System.out.println(GREEN + "✓ Successfully exported to " + dotFilename + RESET);

            // Ask if user wants to generate SVG
            System.out.print(CYAN + "➤ " + RESET + "Generate SVG file using Graphviz neato? (y/n): ");
            String choice = sc.nextLine().trim();

            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
                System.out.print(CYAN + "➤ " + RESET + "Output filename for SVG [backbone.svg]: ");
                String svgFilename = sc.nextLine().trim();
                if (svgFilename.isEmpty()) svgFilename = "backbone.svg";

                System.out.println(CYAN + "⏳ Generating SVG with Graphviz neato..." + RESET);
                try {
                    result.exportToSvg(dotFilename, svgFilename);
                    System.out.println(GREEN + "✓ Successfully generated " + svgFilename + RESET);
                    System.out.println("  You can now open the SVG file in a web browser or image viewer.");
                } catch (IOException e) {
                    System.out.println(RED + "❌ SVG generation failed: " + e.getMessage() + RESET);
                    System.out.println(YELLOW + "  You can manually generate it using: neato -Tsvg " + dotFilename + " -o " + svgFilename + RESET);
                }
            } else {
                System.out.println(YELLOW + "ℹ You can manually generate SVG using: neato -Tsvg " + dotFilename + " -o backbone.svg" + RESET);
            }

        } catch (Exception e) {
            System.out.println(RED + "Error: " + e.getMessage() + RESET);
            e.printStackTrace();
        }

        pause();
        menu();
    }

    /**
     * Displays the calculated minimal backbone results.
     *
     * @param result the calculated minimal backbone result
     * @param durationMs calculation duration in milliseconds
     */
    private void displayResults(MinimalBackboneResult result, long durationMs) {
        clear();
        System.out.println(CYAN + line(120));
        System.out.println("                                 MINIMAL BACKBONE NETWORK RESULTS");
        System.out.println(line(120) + RESET);

        System.out.println();
        System.out.println(GREEN + "✓ Minimal backbone successfully calculated" + RESET);
        System.out.println();

        // Summary
        System.out.println("Summary:");
        System.out.printf("  • Total Stations        : %d\n", result.getStationCount());
        System.out.printf("  • Total Connections     : %d\n", result.getConnectionCount());
        System.out.printf("  • Total Distance        : %.3f km\n", result.getTotalDistance());
        System.out.printf("  • Calculation Time      : %d ms\n", durationMs);
        System.out.println();

        // Detailed connections
        System.out.println("Detailed Connections (sorted by distance):");
        System.out.println(line(120));
        System.out.printf("%-40s   %-40s   %10s\n", "Origin", "Destination", "Distance");
        System.out.println(line(120));

        MapGraph<Station, Double> graph = result.getBackboneGraph();
        List<EdgeInfo> edges = new ArrayList<>();

        // Collect all edges (avoiding duplicates in undirected graph)
        java.util.Set<String> processed = new java.util.HashSet<>();
        for (Edge<Station, Double> edge : graph.edges()) {
            Station u = edge.getVOrig();
            Station v = edge.getVDest();

            String key1 = u.getId() + "-" + v.getId();
            String key2 = v.getId() + "-" + u.getId();

            if (!processed.contains(key1) && !processed.contains(key2)) {
                edges.add(new EdgeInfo(u.getName(), v.getName(), edge.getWeight()));
                processed.add(key1);
                processed.add(key2);
            }
        }

        // Sort by distance
        edges.sort(Comparator.comparingDouble(e -> e.distance));

        // Display
        for (EdgeInfo edge : edges) {
            System.out.printf("%-40s → %-40s   %10.3f km\n",
                    truncate(edge.origin, 40),
                    truncate(edge.destination, 40),
                    edge.distance);
        }

        System.out.println(line(120));
        System.out.println();
    }

    /**
     * Helper class to store edge information.
     */
    private static class EdgeInfo {
        String origin;
        String destination;
        double distance;

        EdgeInfo(String origin, String destination, double distance) {
            this.origin = origin;
            this.destination = destination;
            this.distance = distance;
        }
    }

    /**
     * Truncates a string to maximum length.
     *
     * @param s the string to truncate
     * @param maxLen maximum length
     * @return truncated string with "..." if necessary
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    /**
     * Pauses execution until user presses Enter.
     */
    private void pause() {
        UIHelper.pause();
    }

    /**
     * Clears the console screen.
     */
    private void clear() {
        UIHelper.clear();
    }

    /**
     * Creates a line separator of specified length.
     *
     * @param n number of characters
     * @return line separator string
     */
    private String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}