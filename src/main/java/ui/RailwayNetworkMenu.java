package ui;

import domain.Option;
import domain.Utils;
import ui.RailwayNetworkOptimization.*;

public class RailwayNetworkMenu implements Runnable {

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String RED    = UIHelper.getRed();
    private static final String SEP    = "─";

    @Override
    public void run() {
        menu();
    }

    public void menu() {
        clear();
        System.out.println(CYAN + line(72));
        System.out.println("                 RAILWAY NETWORK OPTIMIZATION");
        System.out.println(line(72) + RESET);

        Option[] options = new Option[3];

        options[0] = new Option("Directed Line Upgrade Plan (USEI11)", new UpgradeOrderUI());
        options[1] = new Option("Directed Line Upgrade Plan (USEI11)", new MinimalBackboneUI());
        options[2] = new Option("Rail Hub Centrality Analysis (USEI13)", new HubCentralityUI());
        options[3] = new Option(RED + "Back to Main Menu" + RESET, new MainMenu());

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
