package ui.RailwayNetworkOptimization;

import domain.Option;
import domain.Station;
import domain.Utils;
import repository.S3_RailwayRepository;
import ui.RailwayNetworkMenu;
import ui.UIHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import controller.MaxThroughputController;

public class MaxThroughputUI implements Runnable {

    //Creation of required class variables
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
    private S3_RailwayRepository repo;

    /**
     * Main runnable method
     */

    @Override
    public void run() {
        repo = S3_RailwayRepository.getInstance();
        menu();
    }

    /**
     * Main menu method
     */

    public void menu() {

        //Cleaning any existant instances and creation of a new one
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("           DIRECTED LINE UPGRADE PLAN (USEI11)");
        System.out.println(line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        //Printing of any existant graph and its data
        if (controller.isGraphBuilt()) {
            System.out.println(GREEN + "  ✓ Railway network graph loaded" + RESET);
            System.out.printf("    • %d stations\n", controller.getStationCount());
            System.out.printf("    • %d directed connections\n", controller.getLineCount());
        } else {
            System.out.println(YELLOW + "  ⚠ Graph not loaded yet" + RESET);
        }
        System.out.println();

        //Creation and display of user choices

        Option[] options = new Option[3];

        options[0] = new Option("Print station list", this::stationListPrinting);
        options[1] = new Option("Generate Maximum Throughput data", this::genMaxThrough);
        options[2] = new Option("Go Back", new RailwayNetworkMenu());

        Utils.listAndSelect(options);
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

        //Input of data
        Scanner sc = new Scanner(System.in);
        try {
            System.out.print(CYAN + "Enter SOURCE station ID: " + RESET);
            String name = sc.nextLine().trim();
            Station stationSrc = controller.getStationById(name);
            while(stationSrc == null) {
                System.out.println("Invalid station ID. Please enter a valid station ID.");
                System.out.print(CYAN + "Enter SOURCE station ID: " + RESET);
                name = sc.nextLine().trim();
                stationSrc = controller.getStationById(name);
            }

            System.out.print(CYAN + "Enter TARGET station ID: " + RESET);
            name = sc.nextLine().trim();
            Station stationDst = controller.getStationById(name);
            while(stationDst == null) {
                System.out.println("Invalid station ID. Please enter a valid station ID.");
                System.out.print(CYAN + "Enter TARGET station ID: " + RESET);
                name = sc.nextLine().trim();
                stationDst = controller.getStationById(name);
            }

            //Sending of data to the controller for computation

            long start = System.currentTimeMillis();
            int maxFlow = controller.computeMaxFlow(stationSrc, stationDst);
            long duration = System.currentTimeMillis() - start;

            System.out.println("Maximum Throughput Computed");
            System.out.printf("Source ID: %s\n", stationSrc);
            System.out.printf("Target ID: %s\n", stationDst);
            System.out.printf("Max Flow Value: %d trains/day\n", maxFlow);
        } catch (NumberFormatException ex) {
            System.out.println(RED + " Invalid input. Please enter numeric station IDs." + RESET);
        }

        pause();
        menu();
    }

    /**
     * Method to print all the existant station's at the user's request
     */

    public void stationListPrinting() {
        System.out.println(controller.getStations());
        menu();
    }

    private void pause() { UIHelper.pause(); }
    private void clear() { UIHelper.clear(); }
    private String line(int n) { StringBuilder sb = new StringBuilder(); for (int i=0;i<n;i++) sb.append(SEP); return sb.toString(); }
}