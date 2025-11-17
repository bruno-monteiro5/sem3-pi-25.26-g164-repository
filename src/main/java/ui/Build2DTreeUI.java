package ui;

import controller.Build2DTreeController;
import controller.BuildIndexesController;
import controller.StationCsvLoader;
import domain.KDTree2D;
import domain.Option;
import domain.StationIndexes;
import domain.Utils;
import repository.StationRepository;

import java.io.File;
import java.util.Scanner;

public class Build2DTreeUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN  = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String SEP   = "─";

    private String DEFAULT_CSV_PATH = "warehouseMngt/train_station_dataset/train_stations_europe.csv";

    private StationRepository stationRepository = new StationRepository();
    private BuildIndexesController indexController = new BuildIndexesController(stationRepository);
    private Build2DTreeController treeController = new Build2DTreeController(stationRepository);

    private boolean indexesBuilt = false;
    private boolean treeBuilt = false;

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                  BUILD 2D-TREE (USEI07)");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[4];

        options[0] = new Option("Build AVL Indexes (USEI06 - prerequisite)", this::buildIndexesAction);
        options[1] = new Option("Build 2D-Tree from AVL Indexes", this::build2DTreeAction);
        options[2] = new Option("Show 2D-Tree Statistics", this::showStatsAction);
        options[3] = new Option("Go Back", new InfraestructureIndexMenu());

        Utils.listAndSelect(options);
    }

    private void buildIndexesAction() {
        System.out.print("CSV path [" + DEFAULT_CSV_PATH + "]: ");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().trim();
        String csvPath = input.isEmpty() ? DEFAULT_CSV_PATH : input;

        File file = new File(csvPath);
        if (!file.isFile()) {
            System.out.println("CSV file not found: " + csvPath);
            menu();
            return;
        }

        System.out.println("\nLoading CSV and building AVL indexes...");

        StationCsvLoader loader = new StationCsvLoader();
        indexController.buildFromCsvPath(loader, csvPath);

        indexesBuilt = true;
        System.out.println(GREEN + "AVL indexes built successfully." + RESET);

        pause();
        menu();
    }

    private void build2DTreeAction() {
        if (stationRepository.getStationIndexes() != null) {
            indexesBuilt = true;
        }

        if (!indexesBuilt) {
            System.out.println("Please build AVL indexes first (option 1).");
            pause();
            menu();
            return;
        }

        System.out.println("\nBuilding balanced 2D-Tree...");

        // Note: You need to get StationIndexes from the repository
        // This requires adding a method to StationRepository
        StationIndexes indexes = stationRepository.getStationIndexes();
        treeController.build2DTree(indexes);

        treeBuilt = true;
        System.out.println(GREEN + "2D-Tree built successfully." + RESET);

        pause();
        menu();
    }

    private void showStatsAction() {
        if (!treeBuilt) {
            System.out.println("Please build 2D-Tree first (option 2).");
            pause();
            menu();
            return;
        }

        System.out.println();
        System.out.println(CYAN + line(72));
        System.out.println("                    2D-TREE STATISTICS");
        System.out.println(line(72) + RESET);
        System.out.println();

        System.out.println(treeController.getStatistics());

        pause();
        menu();
    }

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}