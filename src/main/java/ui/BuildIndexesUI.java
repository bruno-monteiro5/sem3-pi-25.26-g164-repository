package ui;

import controller.BuildIndexesController;
import controller.StationCsvLoader;
import domain.Option;
import domain.Station;
import domain.Utils;
import repository.StationRepository;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class BuildIndexesUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN  = COLOR_ON ? "\u001B[36m" : "";
    private static final String SEP   = "─";

    private String DEFAULT_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "train_stations_europe.csv";

    private StationRepository stationRepository = new StationRepository();
    private BuildIndexesController controller   = new BuildIndexesController(stationRepository);
    private boolean indexesBuilt = false;

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                        STATION INDEXES (USEI06)");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[6];

        options[0] = new Option("Build indexes (load CSV)", this::buildIndexesAction);
        options[1] = new Option("Latitude window query", this::latitudeWindowAction);
        options[2] = new Option("Longitude window query", this::longitudeWindowAction);
        options[3] = new Option("Time zone group query", this::tzGroupAction);
        options[4] = new Option("TZ group + country query", this::tzGroupCountryAction);
        options[5] = new Option("Go Back", new InfraestructureIndexMenu());

        Utils.listAndSelect(options);
    }

    private void buildIndexesAction() {

        System.out.print("CSV path: ");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().trim();
        String csvPath = input.isEmpty() ? DEFAULT_CSV_PATH : input;

        File file = new File(csvPath);
        if (!file.isFile()) {
            System.out.println("CSV file not found: " + csvPath);
            menu();
            return;
        }

        System.out.println("\nLoading CSV and building indexes...");

        StationCsvLoader loader = new StationCsvLoader();
        controller.buildFromCsvPath(loader, csvPath);

        System.out.println("CSV loaded. Indexes successfully built.");
        System.out.println("(AVL indexes for latitude and longitude; BST index for time-zone groups)");

        indexesBuilt = true;
        menu();
    }

    private void latitudeWindowAction() {
        if (!requireBuilt()) return;

        double minLat = askDouble("Min latitude", 37.0);
        double maxLat = askDouble("Max latitude", 39.0);

        List<Station> list = controller.queryByLatitude(minLat, maxLat);

        System.out.printf("%nLatitude window [%.4f, %.4f] → %d results%n",
                minLat, maxLat, list.size());

        printStations(list);
        menu();
    }

    private void longitudeWindowAction() {
        if (!requireBuilt()) return;

        double minLon = askDouble("Min longitude", -10.0);
        double maxLon = askDouble("Max longitude", -8.0);

        List<Station> list = controller.queryByLongitude(minLon, maxLon);

        System.out.printf("%nLongitude window [%.4f, %.4f] → %d results%n",
                minLon, maxLon, list.size());

        printStations(list);
        menu();
    }

    private void tzGroupAction() {
        if (!requireBuilt()) return;

        String group = askLine("Time zone group", "CET");

        List<Station> list = controller.queryByTimeZoneGroup(group);

        System.out.printf("%nGroup '%s' → %d results%n",
                group, list.size());

        printStations(list);
        menu();
    }

    private void tzGroupCountryAction() {
        if (!requireBuilt()) return;

        String group   = askLine("Time zone group", "CET");
        String country = askLine("Country", "ES");

        List<Station> list = controller.queryByTimeZoneAndCountry(group, country);

        System.out.printf("%nGroup '%s' + country '%s' → %d results%n",
                group, country, list.size());

        printStations(list);
        menu();
    }

    private boolean requireBuilt() {
        if (indexesBuilt)
            return true;

        System.out.println("Indexes not built yet. Use option 1 first.");
        menu();
        return false;
    }

    private void printStations(List<Station> stationList) {
        for (Station st : stationList) {
            System.out.printf("%-42s %-6s [%.5f, %.5f]  %s%n",
                    st.getName(),
                    st.getCountry(),
                    st.getLocation().getLatitude(),
                    st.getLocation().getLongitude(),
                    st.getTimeZoneGroup());
        }
        System.out.println("Total: " + stationList.size());
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

    private String askLine(String label, String defaultValue) {
        System.out.print(label + ": ");
        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private double askDouble(String label, double defaultValue) {
        System.out.print(label + ": ");
        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine().trim();
        if (text.isEmpty())
            return defaultValue;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid number. Using default value.");
            return defaultValue;
        }
    }
}
