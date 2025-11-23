package ui.InfrastructureIndex;

import controller.BuildIndexesController;
import controller.StationCsvLoader;
import domain.Option;
import domain.Station;
import domain.Utils;
import ui.InfraestructureIndexMenu;
import ui.UIHelper;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class BuildIndexesUI implements Runnable {

    private static final String RESET = UIHelper.getReset();
    private static final String CYAN  = UIHelper.getCyan();
    private static final String GREEN = UIHelper.getGreen();
    private static final String YL    = UIHelper.getYellow();
    private static final String SEP   = "─";

    private String DEFAULT_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "train_stations_europe.csv";

    private BuildIndexesController controller = new BuildIndexesController();

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                        STATION INDEXES (USEI06)");
        System.out.println(line(72) + RESET);

        if (controller.isLoaded()) {
            System.out.println(GREEN + "✓ Indexes are loaded and ready" + RESET);
        } else {
            System.out.println(YL + "⚠ Indexes not loaded yet" + RESET);
        }
        System.out.println();

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

        System.out.print(CYAN + "➤ " + RESET + "CSV path [" + DEFAULT_CSV_PATH + "]: ");
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

        System.out.println(GREEN + "CSV loaded. Indexes successfully built." + RESET);
        System.out.println("(AVL indexes for latitude, longitude, and time-zone groups)");

        pause();
        menu();
    }

    private void latitudeWindowAction() {
        if (!requireBuilt()) return;

        double minLat = askLatitude("Min latitude (-90 to 90)");
        double maxLat = askLatitude("Max latitude (-90 to 90)");

        while (minLat > maxLat) {
            System.out.println("Min latitude cannot be greater than max latitude.");
            minLat = askLatitude("Min latitude (-90 to 90)");
            maxLat = askLatitude("Max latitude (-90 to 90)");
        }

        List<Station> list = controller.queryByLatitude(minLat, maxLat);

        System.out.printf("%nLatitude window [%.4f, %.4f] → %d results%n",
                minLat, maxLat, list.size());

        printStations(list);
        pause();
        menu();
    }

    private void longitudeWindowAction() {
        if (!requireBuilt()) return;

        double minLon = askLongitude("Min longitude (-180 to 180)");
        double maxLon = askLongitude("Max longitude (-180 to 180)");

        while (minLon > maxLon) {
            System.out.println("Min longitude cannot be greater than max longitude.");
            minLon = askLongitude("Min longitude (-180 to 180)");
            maxLon = askLongitude("Max longitude (-180 to 180)");
        }

        List<Station> list = controller.queryByLongitude(minLon, maxLon);

        System.out.printf("%nLongitude window [%.4f, %.4f] → %d results%n",
                minLon, maxLon, list.size());

        printStations(list);
        pause();
        menu();
    }

    private void tzGroupAction() {
        if (!requireBuilt()) return;

        System.out.print(CYAN + "➤ " + RESET);
        String group = askLine("Time zone group", "CET");

        List<Station> list = controller.queryByTimeZoneGroup(group);

        System.out.printf("%nGroup '%s' → %d results%n", group, list.size());

        printStations(list);
        pause();
        menu();
    }

    private void tzGroupCountryAction() {
        if (!requireBuilt()) return;

        System.out.print(CYAN + "➤ " + RESET);
        String group = askLine("Time zone group", "CET");

        System.out.print(CYAN + "➤ " + RESET);
        String country = askLine("Country", "ES");

        List<Station> list = controller.queryByTimeZoneAndCountry(group, country);

        System.out.printf("%nGroup '%s' + country '%s' → %d results%n",
                group, country, list.size());

        printStations(list);
        pause();
        menu();
    }

    private boolean requireBuilt() {
        if (controller.isLoaded())
            return true;

        System.out.println("Indexes not built yet. Use option 1 first.");
        pause();
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

    private String askLine(String label) {
        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine().trim();

        while (text.isEmpty()) {
            System.out.println("Input cannot be empty.");
            System.out.print(label + ": ");
            text = scanner.nextLine().trim();
        }
        return text;
    }

    private String askLine(String label, String defaultValue) {
        System.out.print(label + " [" + defaultValue + "]: ");
        Scanner scanner = new Scanner(System.in);
        String text = scanner.nextLine().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private double askDouble(String label) {
        Scanner scanner = new Scanner(System.in);

        String text = null;
        boolean valid = false;
        double result = 0;

        while (!valid) {
            System.out.print(label + ": ");
            text = scanner.nextLine().trim();

            try {
                result = Double.parseDouble(text);
                valid = true;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid numeric value.");
            }
        }
        return result;
    }

    private double askLatitude(String label) {
        double value = askDouble(label);

        while (value < -90 || value > 90) {
            System.out.println("Latitude must be between -90 and 90 degrees.");
            value = askDouble(label);
        }
        return value;
    }

    private double askLongitude(String label) {
        double value = askDouble(label);

        while (value < -180 || value > 180) {
            System.out.println("Longitude must be between -180 and 180 degrees.");
            value = askDouble(label);
        }
        return value;
    }
}
