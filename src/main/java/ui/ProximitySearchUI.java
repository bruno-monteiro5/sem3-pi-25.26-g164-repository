package ui;

import controller.Build2DTreeController;
import controller.BuildIndexesController;
import controller.ProximitySearchController;
import controller.StationCsvLoader;
import domain.Option;
import domain.ProximitySearchResult;
import domain.TimeZoneFilter;
import domain.Utils;

import java.io.File;
import java.util.Scanner;

/**
 * User interface for proximity search queries (USEI09).
 */
public class ProximitySearchUI implements Runnable {

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
    private ProximitySearchController searchController = new ProximitySearchController(treeController);

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("           PROXIMITY SEARCH - NEAREST-N (USEI09)");
        System.out.println(line(72) + RESET);

        System.out.println();
        System.out.println("Status:");
        if (indexController.isLoaded()) {
            System.out.println(GREEN + "  ✓ AVL Indexes loaded" + RESET);
        } else {
            System.out.println(YELLOW + "  ⚠ AVL Indexes not loaded" + RESET);
        }

        if (treeController.isBuilt()) {
            System.out.println(GREEN + "  ✓ 2D-Tree built" + RESET);
        } else {
            System.out.println(YELLOW + "  ⚠ 2D-Tree not built" + RESET);
        }
        System.out.println();

        Option[] options = new Option[5];

        options[0] = new Option("Load CSV & Build Indexes (if needed)", this::buildIndexesAction);
        options[1] = new Option("Build 2D-Tree (if needed)", this::build2DTreeAction);
        options[2] = new Option("Find N Nearest Stations (no filter)", this::findNearestAction);
        options[3] = new Option("Find N Nearest Stations (with time zone filter)", this::findNearestWithFilterAction);
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
            System.out.println(RED + "Please build AVL indexes first (option 1)." + RESET);
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

    private void findNearestAction() {
        if (!treeController.isBuilt()) {
            System.out.println(RED + "Please build 2D-Tree first (option 2)." + RESET);
            pause();
            menu();
            return;
        }

        Scanner sc = new Scanner(System.in);

        double lat = askLatitude(sc);
        double lon = askLongitude(sc);
        int n = askInt(sc, "Number of nearest neighbors (N)");

        System.out.println("\nSearching...");

        try {
            ProximitySearchResult result = searchController.findNearestN(lat, lon, n);
            displayResults(result);
        } catch (Exception e) {
            System.out.println(RED + "Error: " + e.getMessage() + RESET);
        }

        pause();
        menu();
    }

    private void findNearestWithFilterAction() {
        if (!treeController.isBuilt()) {
            System.out.println(RED + "Please build 2D-Tree first (option 2)." + RESET);
            pause();
            menu();
            return;
        }

        Scanner sc = new Scanner(System.in);

        double lat = askLatitude(sc);
        double lon = askLongitude(sc);
        int n = askInt(sc, "Number of nearest neighbors (N)");

        System.out.print("Time zone group filter (e.g., 'Europe/Lisbon', or leave blank for no filter): ");
        String tzInput = sc.nextLine().trim();

        TimeZoneFilter filter;
        if (tzInput.isEmpty()) {
            filter = new TimeZoneFilter(); // No filter
            System.out.println(YELLOW + "No time zone filter applied." + RESET);
        } else {
            filter = new TimeZoneFilter(tzInput);
            System.out.println(GREEN + "Filter: " + filter + RESET);
        }

        System.out.println("\nSearching...");

        try {
            ProximitySearchResult result = searchController.findNearestN(lat, lon, n, filter);
            displayResults(result);
        } catch (Exception e) {
            System.out.println(RED + "Error: " + e.getMessage() + RESET);
        }

        pause();
        menu();
    }

    private void displayResults(ProximitySearchResult result) {
        System.out.println();
        System.out.println(CYAN + line(72));
        System.out.println("                     PROXIMITY SEARCH RESULTS");
        System.out.println(line(72) + RESET);
        System.out.println();

        System.out.printf("Target: (%.4f, %.4f)%n", result.getTargetLatitude(), result.getTargetLongitude());
        System.out.printf("Requested: %d nearest neighbors%n", result.getRequestedCount());

        if (result.getFilter().isEnabled()) {
            System.out.printf("Filter: %s%n", result.getFilter());
        } else {
            System.out.println("Filter: None");
        }

        System.out.printf("Found: %d stations%n", result.getResultCount());
        System.out.println();

        if (result.getResultCount() == 0) {
            System.out.println(YELLOW + "No stations found matching the criteria." + RESET);
            return;
        }

        System.out.println(line(72));
        System.out.printf("%-6s | %-10s | %-35s | %-8s | %-15s%n",
                "Rank", "Distance", "Station", "Country", "Time Zone");
        System.out.println(line(72));

        int rank = 1;
        for (ProximitySearchResult.StationDistance sd : result.getResults()) {
            System.out.printf("%-6d | %7.3f km | %-35s | %-8s | %-15s%n",
                    rank++,
                    sd.getDistanceKm(),
                    truncate(sd.getStation().getName(), 35),
                    sd.getStation().getCountry(),
                    truncate(sd.getStation().getTimeZoneGroup(), 15));
        }

        System.out.println(line(72));
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    private double askDouble(Scanner sc, String label) {
        boolean valid = false;
        double value = 0;

        do {
            System.out.print(label + ": ");
            String text = sc.nextLine().trim();

            if (!text.isEmpty()) {
                try {
                    value = Double.parseDouble(text);
                    valid = true;
                } catch (NumberFormatException ex) {
                    System.out.println(YELLOW + "Invalid number. Please enter a valid decimal value." + RESET);
                }
            } else {
                System.out.println(YELLOW + "Value is required. Please enter a number." + RESET);
            }

        } while (!valid);

        return value;
    }

    private int askInt(Scanner sc, String label) {
        boolean valid = false;
        int value = 0;

        do {
            System.out.print(label + ": ");
            String text = sc.nextLine().trim();

            if (!text.isEmpty()) {
                try {
                    int parsed = Integer.parseInt(text);
                    if (parsed > 0) {
                        value = parsed;
                        valid = true;
                    } else {
                        System.out.println(YELLOW + "Value must be > 0. Please try again." + RESET);
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(YELLOW + "Invalid integer. Please enter a whole number." + RESET);
                }
            } else {
                System.out.println(YELLOW + "Value is required. Please enter an integer." + RESET);
            }

        } while (!valid);

        return value;
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

    // ---- Métodos específicos para latitude/longitude com intervalo ----

    private double askLatitude(Scanner sc) {
        boolean valid = false;
        double value = 0.0;

        do {
            System.out.print("Target latitude (-90 to 90): ");
            String text = sc.nextLine().trim();

            if (!text.isEmpty()) {
                try {
                    value = Double.parseDouble(text);
                    if (value < -90.0 || value > 90.0) {
                        System.out.println(YELLOW + "Latitude must be between -90 and 90 degrees." + RESET);
                    } else {
                        valid = true;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(YELLOW + "Invalid number. Please enter a valid decimal value." + RESET);
                }
            } else {
                System.out.println(YELLOW + "Value is required. Please enter a number." + RESET);
            }

        } while (!valid);

        return value;
    }

    private double askLongitude(Scanner sc) {
        boolean valid = false;
        double value = 0.0;

        do {
            System.out.print("Target longitude (-180 to 180): ");
            String text = sc.nextLine().trim();

            if (!text.isEmpty()) {
                try {
                    value = Double.parseDouble(text);
                    if (value < -180.0 || value > 180.0) {
                        System.out.println(YELLOW + "Longitude must be between -180 and 180 degrees." + RESET);
                    } else {
                        valid = true;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println(YELLOW + "Invalid number. Please enter a valid decimal value." + RESET);
                }
            } else {
                System.out.println(YELLOW + "Value is required. Please enter a number." + RESET);
            }

        } while (!valid);

        return value;
    }
}
