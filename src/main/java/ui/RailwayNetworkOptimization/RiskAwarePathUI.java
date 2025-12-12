package ui.RailwayNetworkOptimization;

import controller.RiskAwarePathController;
import domain.Option;
import domain.RiskAwarePathResult;
import domain.Station;
import domain.Utils;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * User interface for USEI15 - Risk-Aware Shortest Paths.
 */
public class RiskAwarePathUI implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();

    private String DEFAULT_STATIONS_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "stations.csv";

    private String DEFAULT_LINES_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "lines.csv";

    private RiskAwarePathController controller = new RiskAwarePathController();

    @Override
    public void run() {
        menu();
    }

    /**
     * Displays the main menu for risk-aware shortest path analysis.
     */
    public void menu() {
        UIHelper.clear();
        System.out.println(CYAN + UIHelper.line(72));
        System.out.println("           RISK-AWARE SHORTEST PATHS (USEI15)");
        System.out.println(UIHelper.line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        if (controller.isGraphBuilt()) {
            System.out.println(GREEN + "  ✓ Railway network loaded" + RESET);
            System.out.printf("    • %d stations\n", controller.getStationCount());
            System.out.printf("    • %d connections\n", controller.getLineCount());
        } else {
            System.out.println(YELLOW + "  ⚠ Network not loaded yet" + RESET);
        }
        System.out.println();

        Option[] options = new Option[4];

        options[0] = new Option("Load Railway Network (CSV)", this::loadNetworkAction);
        options[1] = new Option("List Available Stations", this::listStationsAction);
        options[2] = new Option("Calculate Shortest Path with Costs", this::calculatePathAction);
        options[3] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
    }

    /**
     * Action to load railway network from CSV files.
     */
    private void loadNetworkAction() {
        Scanner sc = new Scanner(System.in);

        System.out.print(CYAN + "➤ " + RESET + "Stations CSV [" + DEFAULT_STATIONS_CSV + "]: ");
        String stationInput = sc.nextLine().trim();
        String stationsCsv = stationInput.isEmpty() ? DEFAULT_STATIONS_CSV : stationInput;

        System.out.print(CYAN + "➤ " + RESET + "Lines CSV [" + DEFAULT_LINES_CSV + "]: ");
        String lineInput = sc.nextLine().trim();
        String linesCsv = lineInput.isEmpty() ? DEFAULT_LINES_CSV : lineInput;

        File stationsFile = new File(stationsCsv);
        File linesFile = new File(linesCsv);

        if (!stationsFile.isFile()) {
            System.out.println(RED + "Stations CSV file not found: " + stationsCsv + RESET);
            UIHelper.pause();
            menu();
            return;
        }

        if (!linesFile.isFile()) {
            System.out.println(RED + "Lines CSV file not found: " + linesCsv + RESET);
            UIHelper.pause();
            menu();
            return;
        }

        System.out.println("\n" + CYAN + "⏳ Loading railway network..." + RESET);

        try {
            long start = System.currentTimeMillis();
            controller.buildGraphFromCsv(stationsCsv, linesCsv);
            long duration = System.currentTimeMillis() - start;

            System.out.printf(GREEN + "✓ Network loaded successfully in %.2f seconds\n" + RESET,
                    duration / 1000.0);
            System.out.printf("  • %d stations\n", controller.getStationCount());
            System.out.printf("  • %d connections\n", controller.getLineCount());

        } catch (IOException e) {
            System.out.println(RED + "Error loading network: " + e.getMessage() + RESET);
        }

        UIHelper.pause();
        menu();
    }

    /**
     * Action to list all available stations.
     */
    private void listStationsAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first (option 1)." + RESET);
            UIHelper.pause();
            menu();
            return;
        }

        List<Station> stations = controller.getAllStations();

        if (stations.isEmpty()) {
            System.out.println(YELLOW + "No stations found." + RESET);
            UIHelper.pause();
            menu();
            return;
        }

        // Ordenar por ID para facilitar a leitura
        stations.sort(Comparator.comparingInt(Station::getId));

        UIHelper.clear();
        System.out.println(CYAN + UIHelper.line(60));
        System.out.println("                   AVAILABLE STATIONS");
        System.out.println(UIHelper.line(60) + RESET);

        System.out.printf("%-10s %-40s\n", "ID", "Station Name");
        System.out.println(UIHelper.line(60));

        for (Station s : stations) {
            System.out.printf("%-10d %-40s\n",
                    s.getId(),
                    UIHelper.truncate(s.getName(), 40));
        }

        System.out.println(UIHelper.line(60));
        System.out.printf("Total: %d stations\n", stations.size());

        UIHelper.pause();
        menu();
    }

    /**
     * Action to calculate shortest path between two stations.
     */
    private void calculatePathAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first (option 1)." + RESET);
            UIHelper.pause();
            menu();
            return;
        }

        Scanner sc = new Scanner(System.in);

        try {
            System.out.println(YELLOW + "\nUse 'List Available Stations' to find IDs if needed." + RESET);

            System.out.print(CYAN + "➤ " + RESET + "Enter SOURCE station ID: ");
            int sourceId = Integer.parseInt(sc.nextLine().trim());

            System.out.print(CYAN + "➤ " + RESET + "Enter TARGET station ID: ");
            int targetId = Integer.parseInt(sc.nextLine().trim());

            Station source = controller.getStationById(sourceId);
            Station target = controller.getStationById(targetId);

            if (source == null) {
                System.out.println(RED + "❌ Source station ID " + sourceId + " not found." + RESET);
                UIHelper.pause();
                menu();
                return;
            }

            if (target == null) {
                System.out.println(RED + "❌ Target station ID " + targetId + " not found." + RESET);
                UIHelper.pause();
                menu();
                return;
            }

            System.out.println("\n" + CYAN + "⏳ Calculating shortest path with costs..." + RESET);

            RiskAwarePathResult result = controller.calculateShortestPath(source, target);

            displayResult(result);

        } catch (NumberFormatException e) {
            System.out.println(RED + "❌ Invalid input. Please enter numeric station IDs." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error calculating path: " + e.getMessage() + RESET);
            e.printStackTrace();
        }

        UIHelper.pause();
        menu();
    }

    /**
     * Displays the calculated shortest path result.
     *
     * @param result the calculation result
     */
    private void displayResult(RiskAwarePathResult result) {
        UIHelper.clear();
        System.out.println(CYAN + UIHelper.line(120));
        System.out.println("                                 RISK-AWARE SHORTEST PATH RESULTS");
        System.out.println(UIHelper.line(120) + RESET);
        System.out.println();

        if (result.hasNegativeCycle()) {
            displayNegativeCycleResult(result);
        } else if (!result.isReachable()) {
            displayNoPathResult(result);
        } else {
            displayValidPathResult(result);
        }
    }

    /**
     * Displays result when a valid path is found.
     */
    private void displayValidPathResult(RiskAwarePathResult result) {
        System.out.println(GREEN + "✓ Shortest path found" + RESET);
        System.out.println();

        System.out.println("Path Summary:");
        System.out.printf("  • Origin        : %s (ID: %d)\n",
                result.getSource().getName(), result.getSource().getId());
        System.out.printf("  • Destination   : %s (ID: %d)\n",
                result.getTarget().getName(), result.getTarget().getId());
        System.out.printf("  • Total Cost    : %.3f\n", result.getTotalCost());
        System.out.printf("  • Path Length   : %d stations\n", result.getPath().size());
        System.out.println();

        System.out.println("Detailed Path:");
        System.out.println(UIHelper.line(120));

        List<Station> path = result.getPath();

        System.out.printf("%-4s %-40s %-10s\n", "Step", "Station Name", "ID");
        System.out.println(UIHelper.line(120));

        for (int i = 0; i < path.size(); i++) {
            Station current = path.get(i);

            System.out.printf("%-4d %-40s %-10d",
                    i + 1,
                    UIHelper.truncate(current.getName(), 40),
                    current.getId());

            if (i < path.size() - 1) {
                System.out.print("  →");
            }

            System.out.println();
        }

        System.out.println(UIHelper.line(120));
        System.out.printf("\nTotal Path Cost: %.3f\n", result.getTotalCost());

        if (result.getTotalCost() < 0) {
            System.out.println(GREEN + "ℹ Negative cost indicates this route uses highly preferred links." + RESET);
        }
    }

    /**
     * Displays result when a negative cycle is detected.
     */
    private void displayNegativeCycleResult(RiskAwarePathResult result) {
        System.out.println(RED + "❌ NEGATIVE CYCLE DETECTED" + RESET);
        System.out.println();

        List<Station> cycle = result.getNegativeCycle();

        System.out.println("A negative cost cycle was detected in the network.");
        System.out.println("This indicates a configuration error where following a loop");
        System.out.println("continuously decreases the total cost, which is inconsistent.");
        System.out.println();

        System.out.println(YELLOW + "Stations and edges involved in the cycle:" + RESET);
        System.out.println(UIHelper.line(120));

        if (cycle != null && !cycle.isEmpty()) {
            System.out.printf("%-40s %-10s\n", "Station Name", "ID");
            System.out.println(UIHelper.line(120));

            for (int i = 0; i < cycle.size(); i++) {
                Station s = cycle.get(i);

                System.out.printf("%-40s %-10d",
                        UIHelper.truncate(s.getName(), 40),
                        s.getId());

                if (i < cycle.size() - 1) {
                    System.out.print("  →");
                } else if (i == cycle.size() - 1 && cycle.size() > 1) {
                    System.out.print("  ↻ (cycle)");
                }

                System.out.println();
            }
        }

        System.out.println(UIHelper.line(120));
        System.out.println();
        System.out.println(RED + "⚠ Recommendation: Review edge costs for these connections" + RESET);
        System.out.println("  to eliminate the negative cycle before routing trains.");
    }

    /**
     * Displays result when no path exists between source and target.
     */
    private void displayNoPathResult(RiskAwarePathResult result) {
        System.out.println(YELLOW + "⚠ No path found" + RESET);
        System.out.println();

        System.out.printf("No route exists from %s (ID: %d) to %s (ID: %d)\n",
                result.getSource().getName(),
                result.getSource().getId(),
                result.getTarget().getName(),
                result.getTarget().getId());

        System.out.println();
        System.out.println("The stations may be in different disconnected components");
        System.out.println("of the railway network.");
    }
}