package ui.RailwayNetworkOptimization;

import controller.UpgradeOrderController;
import domain.Option;
import domain.Station;
import domain.Utils;
import domain.UpgradeOrderResult;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;

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

        Option[] options = new Option[2];

        options[0] = new Option("Calculate Upgrade Order", this::calculateOrderAction);
        options[1] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
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
        Set<Station> cycleStations = result.getCycleStations();

        System.out.println(RED + "❌ Cycles detected - topological ordering NOT possible" + RESET);
        System.out.println();
        System.out.println("Stations that are part of at least one cycle:");
        System.out.println(line(72));

        int i = 1;
        for (Station s : cycleStations) {
            System.out.printf("%3d. %-40s [ID: %d]\n",
                    i++,
                    truncate(s.getName(), 40),
                    s.getId());
        }

        System.out.println(line(72));
        System.out.printf("Total stations in cycle(s): %d\n", result.getStationCount());
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
