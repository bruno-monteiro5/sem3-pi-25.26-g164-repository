package ui.RailwayNetworkOptimization;


import controller.UpgradeOrderController;
import domain.Option;
import domain.Station;
import domain.Utils;
import domain.UpgradeOrderResult;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * UI para USEI11 - Directed Line Upgrade Plan
 */
public class UpgradeOrderUI implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();
    private static final String SEP    = "─";

    private String DEFAULT_STATIONS_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "stations.csv";

    private String DEFAULT_LINES_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "lines.csv";

    private UpgradeOrderController controller = new UpgradeOrderController();

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("           DIRECTED LINE UPGRADE PLAN (USEI11)");
        System.out.println(line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        if (controller.isGraphBuilt()) {
            System.out.println(GREEN + "  ✓ Railway network graph loaded" + RESET);
            System.out.printf("    • %d stations\n", controller.getStationCount());
            System.out.printf("    • %d directed connections\n", controller.getLineCount());
        } else {
            System.out.println(YELLOW + "  ⚠ Graph not loaded yet" + RESET);
        }
        System.out.println();

        Option[] options = new Option[3];

        options[0] = new Option("Load Railway Network (CSV)", this::loadNetworkAction);
        options[1] = new Option("Calculate Upgrade Order", this::calculateOrderAction);
        options[2] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
    }

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
            pause();
            menu();
            return;
        }

        if (!linesFile.isFile()) {
            System.out.println(RED + "Lines CSV file not found: " + linesCsv + RESET);
            pause();
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
            System.out.printf("  • %d directed connections\n", controller.getLineCount());

        } catch (IOException e) {
            System.out.println(RED + "Error loading network: " + e.getMessage() + RESET);
        }

        pause();
        menu();
    }

    private void calculateOrderAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first (option 1)." + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\n" + CYAN + "⏳ Calculating upgrade order..." + RESET);

        long start = System.currentTimeMillis();
        UpgradeOrderResult result = controller.calculateUpgradeOrder();
        long duration = System.currentTimeMillis() - start;

        displayResult(result, duration);

        pause();
        menu();
    }

    private void displayResult(UpgradeOrderResult result, long durationMs) {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                UPGRADE ORDER ANALYSIS RESULT");
        System.out.println(line(72) + RESET);
        System.out.println();

        if (result.hasCycle()) {
            displayCycleResult(result);
        } else {
            displayValidOrderResult(result);
        }

    }

    private void displayValidOrderResult(UpgradeOrderResult result) {
        LinkedList<Station> order = result.getUpgradeOrder();

        System.out.println(GREEN + "✓ Valid upgrade order found (no cycles detected)" + RESET);
        System.out.println();
        System.out.println("Stations should be upgraded in the following order:");
        System.out.println(line(72));

        int position = 1;
        for (Station s : order) {
            System.out.printf("%3d. %-40s [ID: %d]\n",
                    position++,
                    truncate(s.getName(), 40),
                    s.getId());
        }

        System.out.println(line(72));
        System.out.printf("Total stations: %d\n", order.size());
    }

    private void displayCycleResult(UpgradeOrderResult result) {
        List<List<Station>> cycles = result.getCycles();

        System.out.println(RED + "❌ Cycles detected - topological ordering NOT possible" + RESET);
        System.out.println();
        System.out.printf("Found %d cycle(s) in the network:\n", cycles.size());
        System.out.println(line(72));

        int cycleNum = 1;
        for (List<Station> cycle : cycles) {
            System.out.println(YELLOW + "Cycle #" + cycleNum++ + RESET);
            System.out.print("  ");

            for (int i = 0; i < cycle.size(); i++) {
                Station s = cycle.get(i);
                System.out.print(s.getName());

                if (i < cycle.size() - 1) {
                    System.out.print(" → ");
                }
            }
            System.out.println();

            System.out.print("  Stations involved: ");
            for (int i = 0; i < cycle.size() - 1; i++) { // -1 para não repetir o último
                if (i > 0) System.out.print(", ");
                System.out.print(cycle.get(i).getName());
            }
            System.out.println("\n");
        }

        System.out.println(line(72));
        System.out.println(RED + "⚠ Recommendation: Review track dependencies to eliminate cycles" + RESET);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    private void pause() {
        UIHelper.pause();
    }

    private void clear() {
        UIHelper.clear();
    }

    private String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}