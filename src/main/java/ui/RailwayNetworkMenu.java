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

        Option[] options = new Option[8];

        options[0] = new Option("Load Railway Network from CSV", new LoadNetworkUI());

        options[1] = new Option("List Stations", requireNetwork(new ListStationsUI()));
        options[2] = new Option("Directed Line Upgrade Plan (USEI11)", requireNetwork(new UpgradeOrderUI()));
        options[3] = new Option("Minimal Backbone Network (USEI12)", requireNetwork(new MinimalBackboneUI()));
        options[4] = new Option("Rail Hub Centrality Analysis (USEI13)", requireNetwork(new HubCentralityUI()));
        options[5] = new Option("Maximum Throughput Calculator (USEI14)", requireNetwork(new MaxThroughputUI()));
        options[6] = new Option("Risk-Aware Shortest Paths (USEI15)", requireNetwork(new RiskAwarePathUI()));

        options[7] = new Option(RED + "Back to Main Menu" + RESET, new MainMenu());

        Utils.listAndSelect(options);
    }

    /**
     * Wraps an UI option, blocking access if the railway network is not loaded.
     */
    private Runnable requireNetwork(Runnable targetUI) {
        return () -> {
            S3_RailwayRepository repo = S3_RailwayRepository.getInstance();
            if (!repo.isDataLoaded()) {
                System.out.println(YELLOW + "⚠ No network data loaded." + RESET);
                System.out.println("   Please run option " + CYAN + "1) Load Railway Network from CSV" + RESET + " to load the network first.");
                UIHelper.pause();
                new RailwayNetworkMenu().run();
                return;
            }
            targetUI.run();
        };
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