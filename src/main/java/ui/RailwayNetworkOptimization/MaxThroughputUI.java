package ui.RailwayNetworkOptimization;

import controller.UpgradeOrderController;
import domain.Option;
import domain.Station;
import domain.Utils;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import controller.MaxThroughputController;

public class MaxThroughputUI implements Runnable {
    private static final String RESET = UIHelper.getReset();
    private static final String CYAN = UIHelper.getCyan();
    private static final String GREEN = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED = UIHelper.getRed();
    private static final String SEP = "─";

    private String DEFAULT_STATIONS_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "stations.csv";

    private String DEFAULT_LINES_CSV =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "lines.csv";

    private MaxThroughputController controller = new MaxThroughputController();

    /**
     * Main runnable method
     */

    @Override
    public void run() {
        menu();
    }

    /**
     * Main menu method
     */

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
        options[1] = new Option("Generate Maximum Throughput data", this::genMaxThrough);
        options[2] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
    }

    /**
     * Method used to load railway network from CSV files.
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

    /**
     * Method used to generate maximum throughput between two stations using the controller methods.
     */

    public void genMaxThrough() {
        clear();
        System.out.println(CYAN + "=== MAXIMUM THROUGHPUT BETWEEN TWO STATIONS (USEI14) ===" + RESET);

        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "⚠ Railway network graph is not loaded yet!" + RESET);
            pause();
            menu();
            return;
        }

        Scanner sc = new Scanner(System.in);
        try {
            System.out.print(CYAN + "Enter SOURCE station ID: " + RESET);
            int srcId = Integer.parseInt(sc.nextLine().trim());

            System.out.print(CYAN + "Enter TARGET station ID: " + RESET);
            int dstId = Integer.parseInt(sc.nextLine().trim());

            Station src = controller.getStationById(srcId);
            Station dst = controller.getStationById(dstId);

            if (src == null || dst == null) {
                System.out.println(RED + "❌ Invalid station ID(s)." + RESET);
                pause();
                menu();
                return;
            }

            System.out.println("\n" + CYAN + "⏳ Computing Maximum Throughput..." + RESET);
            long start = System.currentTimeMillis();
            int maxFlow = controller.computeMaxFlow(src, dst);
            long duration = System.currentTimeMillis() - start;

            System.out.println(GREEN + "\n✓ Maximum Throughput Computed" + RESET);
            System.out.printf("  • Source ID: %d  Name: %s\n", srcId, src.getName());
            System.out.printf("  • Target ID: %d  Name: %s\n", dstId, dst.getName());
            System.out.printf("  • Max Flow Value: %d trains/day\n", maxFlow);

            // Per PDF: Edmonds-Karp complexity O(V * E^2)
            System.out.println(YELLOW + "\nTemporal Complexity (Edmonds–Karp / BFS variant): O(V · E^2)" + RESET);
            System.out.printf("Execution Time: %d ms\n", duration);

        } catch (NumberFormatException ex) {
            System.out.println(RED + "❌ Invalid input. Please enter numeric station IDs." + RESET);
        }

        pause();
        menu();
    }

    private void pause() { UIHelper.pause(); }
    private void clear() { UIHelper.clear(); }
    private String line(int n) { StringBuilder sb = new StringBuilder(); for (int i=0;i<n;i++) sb.append(SEP); return sb.toString(); }
}