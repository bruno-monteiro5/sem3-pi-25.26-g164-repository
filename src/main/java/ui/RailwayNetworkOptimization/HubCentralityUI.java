package ui.RailwayNetworkOptimization;

import controller.HubCentralityController;
import domain.HubScore;
import domain.Option;
import domain.Utils;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * User interface for USEI13 - Rail Hub Centrality Analysis.
 */
public class HubCentralityUI implements Runnable {

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

    private HubCentralityController controller = new HubCentralityController();

    @Override
    public void run() {
        menu();
    }

    /**
     * Displays the main menu for hub centrality analysis.
     */
    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("           RAIL HUB CENTRALITY ANALYSIS (USEI13)");
        System.out.println(line(72) + RESET);

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

        Option[] options = new Option[3];

        options[0] = new Option("Load Railway Network (CSV)", this::loadNetworkAction);
        options[1] = new Option("Calculate Hub Centrality", this::calculateCentralityAction);
        options[2] = new Option("Go Back", new RailwayNetworkMenu());

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
            controller.buildGraphFromCsv(stationsCsv, linesCsv);

            System.out.println(GREEN + "✓ Network loaded successfully" + RESET);
            System.out.printf("  • %d stations\n", controller.getStationCount());
            System.out.printf("  • %d connections\n", controller.getLineCount());

        } catch (IOException e) {
            System.out.println(RED + "Error loading network: " + e.getMessage() + RESET);
        }

        pause();
        menu();
    }

    /**
     * Action to calculate and display hub centrality metrics.
     */
    private void calculateCentralityAction() {
        if (!controller.isGraphBuilt()) {
            System.out.println(RED + "Please load the railway network first (option 1)." + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\n" + CYAN + "⏳ Calculating hub centrality metrics..." + RESET);

        try {
            List<HubScore> results = controller.calculateHubCentrality();
            displayResults(results);

        } catch (Exception e) {
            System.out.println(RED + "Error calculating centrality: " + e.getMessage() + RESET);
            e.printStackTrace();
        }

        pause();
        menu();
    }

    /**
     * Displays the calculated hub centrality results.
     *
     * @param results list of HubScore objects sorted by hubScore
     */
    private void displayResults(List<HubScore> results) {
        clear();
        System.out.println(CYAN + line(120));
        System.out.println("                                 HUB CENTRALITY ANALYSIS RESULTS");
        System.out.println(line(120) + RESET);

        System.out.printf("Total stations: %d\n\n", results.size());

        System.out.printf("%-4s %-35s %-8s %-10s %-12s %-12s %-12s %-10s\n",
                "Rank", "Station Name", "ID", "Degree", "Strength", "Betweenness", "Harmonic", "HubScore");
        System.out.println(line(120));

        int rank = 1;
        for (HubScore hs : results) {
            System.out.printf("%-4d %-35s %-8d %-10d %-12.2f %-12.6f %-12.6f %-10.6f\n",
                    rank++,
                    truncate(hs.getStation().getName(), 35),
                    hs.getStation().getId(),
                    hs.getDegree(),
                    hs.getStrength(),
                    hs.getBetweenness(),
                    hs.getHarmonicCloseness(),
                    hs.getHubScore()
            );
        }

        System.out.println(line(120));
        System.out.println("\nFormula: HubScore = 0.35 × Betweenness + 0.35 × Harmonic Closeness + 0.30 × Strength(norm)");
        System.out.println("All metrics normalized to [0,1] per connected component.");
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
