package ui;

import domain.Option;
import domain.Utils;
import repository.S3_RailwayRepository;
import ui.RailwayNetworkOptimization.*;

public class RailwayNetworkMenu implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();
    private static final String SEP    = "─";

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                 RAILWAY NETWORK OPTIMIZATION");
        System.out.println(line(72) + RESET);

        displayGlobalStatus();

        Option[] options = new Option[6];

        options[0] = new Option("Load Railway Network from CSV", new LoadNetworkUI());
        options[1] = new Option("Directed Line Upgrade Plan (USEI11)", new UpgradeOrderUI());
        options[2] = new Option("Minimal Backbone Network (USEI12)", new MinimalBackboneUI());
        options[3] = new Option("Rail Hub Centrality Analysis (USEI13)", new HubCentralityUI());
        options[4] = new Option("Maximum Throughput Calculator (USEI14)", new MaxThroughputUI());
        options[5] = new Option(RED + "Back to Main Menu" + RESET, new MainMenu());

        Utils.listAndSelect(options);
    }

    /**
     * Displays the global network loading status from the repository.
     */
    private void displayGlobalStatus() {
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();

        System.out.println();
        System.out.println("Network Status:");

        if (repo.isDataLoaded()) {
            System.out.println(GREEN + "  ✓ Railway network loaded" + RESET);
            System.out.printf("    • %d stations\n", repo.getStationCount());
            System.out.printf("    • %d lines\n", repo.getLineCount());
        } else {
            System.out.println(YELLOW + "  ⚠ No network data loaded" + RESET);
            System.out.println("    Please load network data first (option 1)");
        }
        System.out.println();
    }

    private static void clear() {
        UIHelper.clear();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }

    /**
     * UI for loading the railway network (option 1).
     */
    private static class LoadNetworkUI implements Runnable {
        @Override
        public void run() {
            controller.BuildRailwayNetworkController ctrl =
                    new controller.BuildRailwayNetworkController();

            java.util.Scanner sc = new java.util.Scanner(System.in);

            String defaultStations = "warehouseMngt/train_station_dataset/stations.csv";
            String defaultLines = "warehouseMngt/train_station_dataset/lines.csv";

            System.out.print(CYAN + "➤ " + RESET + "Stations CSV [" + defaultStations + "]: ");
            String stationsInput = sc.nextLine().trim();
            String stationsCsv = stationsInput.isEmpty() ? defaultStations : stationsInput;

            System.out.print(CYAN + "➤ " + RESET + "Lines CSV [" + defaultLines + "]: ");
            String linesInput = sc.nextLine().trim();
            String linesCsv = linesInput.isEmpty() ? defaultLines : linesInput;

            java.io.File stationsFile = new java.io.File(stationsCsv);
            java.io.File linesFile = new java.io.File(linesCsv);

            if (!stationsFile.isFile()) {
                System.out.println(RED + "❌ Stations CSV not found: " + stationsCsv + RESET);
                UIHelper.pause();
                new RailwayNetworkMenu().run();
                return;
            }

            if (!linesFile.isFile()) {
                System.out.println(RED + "❌ Lines CSV not found: " + linesCsv + RESET);
                UIHelper.pause();
                new RailwayNetworkMenu().run();
                return;
            }

            System.out.println("\n" + CYAN + "⏳ Loading railway network..." + RESET);

            try {
                ctrl.buildFromCsv(stationsCsv, linesCsv);
                System.out.println(GREEN + "✓ Network loaded successfully" + RESET);
            } catch (java.io.IOException e) {
                System.out.println(RED + "❌ Error loading network: " + e.getMessage() + RESET);
            }

            UIHelper.pause();
            new RailwayNetworkMenu().run();
        }
    }
}