package ui.InfrastructureIndex;

import controller.Build2DTreeController;
import controller.BuildIndexesController;
import controller.StationCsvLoader;
import domain.Option;
import domain.Utils;
import ui.InfraestructureIndexMenu;
import ui.UIHelper;

import java.io.File;
import java.util.Scanner;

public class Build2DTreeUI implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();
    private static final String SEP    = "─";

    private String DEFAULT_CSV_PATH =
            "warehouseMngt" + File.separator +
                    "train_station_dataset" + File.separator +
                    "train_stations_europe.csv";

    private BuildIndexesController indexController = new BuildIndexesController();
    private Build2DTreeController treeController   = new Build2DTreeController();

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

        Option[] options = new Option[4];

        options[0] = new Option("Load CSV & Build AVL Indexes (if needed)", this::buildIndexesAction);
        options[1] = new Option("Build 2D-Tree from AVL Indexes", this::build2DTreeAction);
        options[2] = new Option("Show 2D-Tree Statistics", this::showStatsAction);
        options[3] = new Option("Go Back", new InfraestructureIndexMenu());

        Utils.listAndSelect(options);
    }

    private void buildIndexesAction() {
        if (indexController.isLoaded()) {
            System.out.println(YELLOW + "AVL indexes are already loaded." + RESET);
            System.out.print(CYAN + "➤ " + RESET + "Do you want to reload? (y/n): ");
            Scanner sc = new Scanner(System.in);
            String answer = sc.nextLine().trim().toLowerCase();
            if (!answer.equals("y")) {
                menu();
                return;
            }
        }

        System.out.print(CYAN + "➤ " + RESET + "CSV path [" + DEFAULT_CSV_PATH + "]: ");
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
