package ui;

import controller.Build2DTreeController;
import controller.BuildIndexesController;
import controller.StationCsvLoader;
import domain.Option;
import domain.Utils;

import java.io.File;
import java.util.Scanner;

public class Build2DTreeUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN  = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED   = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP   = "─";

    private String DEFAULT_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "train_stations_europe.csv";

    private BuildIndexesController indexController = new BuildIndexesController();
    private Build2DTreeController treeController = new Build2DTreeController();

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                  BUILD 2D-TREE (USEI07)");
        System.out.println(line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        if (indexController.isLoaded()) {
            System.out.println(GREEN + "  ✓ AVL Indexes loaded" + RESET);
        } else {
            System.out.println(YELLOW + "  ⚠ AVL Indexes not loaded (run USEI06 first)" + RESET);
        }

        if (treeController.isBuilt()) {
            System.out.println(GREEN + "  ✓ 2D-Tree built" + RESET);
        } else {
            System.out.println("  ○ 2D-Tree not built yet");
        }
        System.out.println();

        Option[] options = new Option[5];

        options[0] = new Option("Load CSV & Build AVL Indexes (if needed)", this::buildIndexesAction);
        options[1] = new Option("Build 2D-Tree from AVL Indexes", this::build2DTreeAction);
        options[2] = new Option("Show 2D-Tree Statistics", this::showStatsAction);
        options[3] = new Option("Geographic search (USEI06)", this::geographicSearchAction);
        options[4] = new Option("Go Back", new InfraestructureIndexMenu());

        Utils.listAndSelect(options);
    }

    private void buildIndexesAction() {
        if (indexController.isLoaded()) {
            System.out.println(YELLOW + "AVL indexes are already loaded." + RESET);
            System.out.print("Do you want to reload? (y/n): ");
            Scanner sc = new Scanner(System.in);
            String answer = sc.nextLine().trim().toLowerCase();
            if (!answer.equals("y")) {
                menu();
                return;
            }
        }

        System.out.print("CSV path [" + DEFAULT_CSV_PATH + "]: ");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().trim();
        String csvPath = input.isEmpty() ? DEFAULT_CSV_PATH : input;

        File file = new File(csvPath);
        if (!file.isFile()) {
            System.out.println(RED + "CSV file not found: " + csvPath + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\nLoading CSV and building AVL indexes...");

        StationCsvLoader loader = new StationCsvLoader();
        indexController.buildFromCsvPath(loader, csvPath);

        System.out.println(GREEN + "AVL indexes built successfully." + RESET);

        pause();
        menu();
    }

    private void build2DTreeAction() {
        if (!indexController.isLoaded()) {
            System.out.println(RED + "Please build AVL indexes first (option 1 or run USEI06)." + RESET);
            pause();
            menu();
            return;
        }

        System.out.println("\nBuilding balanced 2D-Tree...");

        try {
            treeController.build2DTree();
            System.out.println(GREEN + "2D-Tree built successfully." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error building 2D-Tree: " + e.getMessage() + RESET);
        }

        pause();
        menu();
    }

    public void geographicSearchAction() {
        System.out.println("Please select a minimum latitude");
        Scanner sc = new Scanner(System.in);
        double minLat = sc.nextDouble();
        while(minLat<-90 || minLat>90) {
            System.out.println("Invalid latitude. Please try again.");
            minLat = sc.nextDouble();
        }
        System.out.println("Please select a maximum latitude");
        double maxLat = sc.nextDouble();
        while(maxLat<-90 || maxLat>90 && maxLat<minLat) {
            System.out.println("Invalid latitude. Please try again");
            maxLat = sc.nextDouble();
        }
        System.out.println("Please select a minimum longitude");
        double minLon = sc.nextDouble();
        while(minLon<-180 || minLon>180) {
            System.out.println("Invalid longitude. Please try again");
            minLon = sc.nextDouble();
        }
        System.out.println("Please select a maximum longitude");
        double maxLon = sc.nextDouble();
        while(maxLon<-180 || maxLon>180 && maxLon<minLon) {
            System.out.println("Invalid longitude. Please try again");
            maxLon = sc.nextDouble();
        }
        System.out.println("Do you wish to add additional filters? (y/n)");
        String awnser = sc.nextLine().trim().toLowerCase();
        while(!awnser.equals("y") && !awnser.equals("n")) {
            System.out.println("Invalid input. Please enter 'y' or 'n'");
            awnser = sc.nextLine().trim().toLowerCase();
        }
        if(awnser.equals("y")) {
            System.out.println("Should the station be in a city?(y/n/a)");
            String city = sc.nextLine().trim().toLowerCase();
            int isCity = -1;
            while(!city.equals("y") && !city.equals("n") && !city.equals("a")) {
                System.out.println("Invalid input. Please enter 'y' or 'n'");
                city = sc.nextLine().trim().toLowerCase();
            }
            if(city.equals("y")){
                isCity = 0;
            }else if(city.equals("n")){
                isCity = 1;
            }else{
                isCity = 2;
            }
            System.out.println("Should it be a main station?(y/n/a)");
            city = sc.nextLine().trim().toLowerCase();
            int isMain = -1;
            while (!city.equals("y") && !city.equals("n") && !city.equals("a") ) {
                System.out.println("Invalid input. Please enter 'y' or 'n' or 'a'");
                city = sc.nextLine().trim().toLowerCase();
            }
            if(city.equals("y")){
                isMain = 0;
            }else if(city.equals("n")){
                isMain = 1;
            }else {
                isMain = 2;
            }
            System.out.println("Which country should it be in?(PT/ES/all)");
            String country = sc.nextLine().toLowerCase();
            while(!country.equals("pt") && !country.equals("es") && !country.equals("all")) {
                System.out.println("Invalid input. Please enter 'PT', 'ES' or 'all'");
                country = sc.nextLine().toLowerCase();
            }
            System.out.println();treeController.searchInArea(minLat, maxLat, minLon, maxLon, isCity, isMain, country);


        }else {
            System.out.println(treeController.searchInArea(minLat, maxLat, minLon, maxLon));
        }
    }

    private void showStatsAction() {
        if (!treeController.isBuilt()) {
            System.out.println(RED + "Please build 2D-Tree first (option 2)." + RESET);
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