package ui;

//import WarehouseManagementFunctions.StationIndexUI;

import domain.Option;
import domain.Utils;
import ui.InfrastructureIndex.*;
import ui.UIHelper;

public class InfraestructureIndexMenu implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String RED    = UIHelper.getRed();
    private static final String BLUE   = UIHelper.getBlue();
    private static final String BOLD   = UIHelper.getBold();
    private static final String SEP   = "─";

    @Override
    public void run() {
        clear();
        System.out.println(CYAN + line(72) + RESET);
        System.out.println("                   INFRASTRUCTURE INDEX MENU");
        System.out.println(CYAN + line(72) + RESET);

        Option[] options = new Option[6];

        options[0] = new Option("Build indexes (USEI06)", new BuildIndexesUI());
        options[1] = new Option("Build 2D-Tree (USEI07)", new Build2DTreeUI());
        options[2] = new Option("Search by geographical area (USEI08)", new Search2DTreeUI());
        options[3] = new Option("Proximity search (USEI09)", new ProximitySearchUI());
        options[4] = new Option("Radius search & density summary (USEI10)", new RadiusSearchUI());
        options[5] = new Option("Back to main menu", new MainMenu());

        System.out.println();
        Utils.listAndSelect(options);
    }

    private static void clear() {
        UIHelper.clear();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
