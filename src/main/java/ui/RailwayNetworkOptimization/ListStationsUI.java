package ui.RailwayNetworkOptimization;

import domain.Station;
import repository.S3_RailwayRepository;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * UI for listing all stations in the railway network.
 */
public class ListStationsUI implements Runnable {

    private static final String SEP = "─";

    @Override
    public void run() {
        S3_RailwayRepository repo = S3_RailwayRepository.getInstance();

        if (!repo.isDataLoaded()) {
            System.out.println("⚠ No network data loaded");
            System.out.println("  Please load network data first (option 1)");
            UIHelper.pause();
            new RailwayNetworkMenu().run();
            return;
        }

        List<Station> stations = new ArrayList<>(repo.getStationsById().values());
        stations.sort(Comparator.comparingInt(Station::getId));

        printHeader();
        for (Station s : stations) {
            System.out.printf("%-10d %s%n", s.getId(), s.getName());
        }
        printFooter(stations.size());

        UIHelper.pause();
        new RailwayNetworkMenu().run();
    }

    private void printHeader() {
        System.out.println(line(72));
        System.out.println("                   AVAILABLE STATIONS");
        System.out.println(line(72));
        System.out.printf("%-10s %s%n", "ID", "Station Name");
        System.out.println(line(72));
    }

    private void printFooter(int total) {
        System.out.println(line(72));
        System.out.println("Total: " + total + " stations");
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
