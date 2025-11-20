package ui.InfrastructureIndex;

import controller.Build2DTreeController;
import controller.BuildIndexesController;
import controller.RadiusSearchController;
import controller.StationCsvLoader;
import domain.DistanceKey;
import domain.Option;
import domain.RadiusSearchResult;
import domain.Station;
import domain.Utils;
import ui.InfraestructureIndexMenu;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

/**
 * User interface for radius search and density summary (USEI10).
 */
public class RadiusSearchUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    private String DEFAULT_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "train_stations_europe.csv";

    private BuildIndexesController indexController = new BuildIndexesController();
    private Build2DTreeController treeController   = new Build2DTreeController();
    private RadiusSearchController searchController = new RadiusSearchController(treeController);

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("             RADIUS SEARCH & DENSITY SUMMARY (USEI10)");
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

        Option[] options = new Option[4];

        options[0] = new Option("Load CSV & Build Indexes (if needed)", this::buildIndexesAction);
        options[1] = new Option("Build 2D-Tree (if needed)", this::build2DTreeAction);
        options[2] = new Option("Radius Search & Density (USEI10)", this::radiusSearchAction);
        options[3] = new Option("Go Back", new InfraestructureIndexMenu());

        Utils.listAndSelect(options);
    }

    /** Option 1 – Build indexes from CSV (if needed). */
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

    /** Option 2 – Build 2D-Tree (if needed). */
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

    /** Option 3 – Execute radius search and print density summary. */
    private void radiusSearchAction() {
        if (!treeController.isBuilt()) {
            System.out.println(RED + "Please build the 2D-Tree first (option 2)." + RESET);
            pause();
            menu();
            return;
        }

        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                    RADIUS SEARCH (USEI10)");
        System.out.println(line(72) + RESET);
        System.out.println();

        Scanner sc = new Scanner(System.in);

        double lat = askLatitude(sc);
        double lon = askLongitude(sc);

        double radiusKm;
        boolean validRadius = false;
        radiusKm = 0;
        do {
            radiusKm = askDouble(sc, "Radius (km, must be > 0)");
            if (radiusKm > 0) {
                validRadius = true;
            } else {
                System.out.println(YELLOW + "Radius must be > 0. Please try again." + RESET);
            }
        } while (!validRadius);


        RadiusSearchResult result;
        try {
            result = searchController.searchWithinRadius(lat, lon, radiusKm);
        } catch (Exception e) {
            System.out.println(RED + "Error executing radius search: " + e.getMessage() + RESET);
            pause();
            menu();
            return;
        }

        printResult(result);

        pause();
        menu();
    }

    /** Prints a formatted summary of the radius search result. */
    private void printResult(RadiusSearchResult result) {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                    RADIUS SEARCH RESULT");
        System.out.println(line(72) + RESET);
        System.out.println();

        System.out.printf("Center: (%.4f, %.4f)%n",
                result.getSearchCenter().getLatitude(),
                result.getSearchCenter().getLongitude());
        System.out.printf("Radius: %.2f km%n", result.getSearchRadius());
        System.out.printf("Total stations: %d%n", result.getTotalCount());
        System.out.printf("Cities: %d, Non-cities: %d%n",
                result.getCityCount(), result.getNonCityCount());
        System.out.println();

        System.out.println("Stations per country:");
        for (Map.Entry<String, Integer> entry : result.getCountByCountry().entrySet()) {
            System.out.printf("  %s: %d%n", entry.getKey(), entry.getValue());
        }
        System.out.println();

        if (result.getTotalCount() == 0) {
            System.out.println(YELLOW + "No stations found within the given radius." + RESET);
            return;
        }

        System.out.println(line(72));
        System.out.printf("%-6s | %-10s | %-35s | %-8s | %-5s%n",
                "Rank", "Distance", "Station", "Country", "City?");
        System.out.println(line(72));

        int rank = 1;
        for (DistanceKey dk : result.getSortedStations().inOrder()) {
            Station s = dk.getStation();
            System.out.printf("%-6d | %7.3f km | %-35s | %-8s | %-5s%n",
                    rank++,
                    dk.getDistance(),
                    truncate(s.getName(), 35),
                    s.getCountry(),
                    s.isCity() ? "Yes" : "No");
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

    private double askLatitude(Scanner sc) {
        boolean valid = false;
        double value = 0;

        do {
            System.out.print("Target latitude (decimal degrees): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println(YELLOW + "Latitude is required. Please enter a number." + RESET);
                continue;
            }

            try {
                value = Double.parseDouble(text);
                if (value < -90.0 || value > 90.0) {
                    System.out.println(YELLOW + "Latitude must be between -90 and 90 degrees. Please try again." + RESET);
                } else {
                    valid = true;
                }
            } catch (NumberFormatException ex) {
                System.out.println(YELLOW + "Invalid number. Please enter a valid decimal value." + RESET);
            }

        } while (!valid);

        return value;
    }

    private double askLongitude(Scanner sc) {
        boolean valid = false;
        double value = 0;

        do {
            System.out.print("Target longitude (decimal degrees): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println(YELLOW + "Longitude is required. Please enter a number." + RESET);
                continue;
            }

            try {
                value = Double.parseDouble(text);
                if (value < -180.0 || value > 180.0) {
                    System.out.println(YELLOW + "Longitude must be between -180 and 180 degrees. Please try again." + RESET);
                } else {
                    valid = true;
                }
            } catch (NumberFormatException ex) {
                System.out.println(YELLOW + "Invalid number. Please enter a valid decimal value." + RESET);
            }

        } while (!valid);

        return value;
    }

}
