package ui.FreightUI;

import controller.FreightController.RoutePlannerController;
import controller.FreightController.RoutePlannerController.FreightChain;
import domain.railway.Freight;
import domain.railway.Path;
import domain.railway.RouteOperation;
import domain.railway.RoutePlan;
import domain.railway.Train;
import ui.MainMenu;
import ui.UIHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RoutePlannerUI implements Runnable {

    private final RoutePlannerController controller;
    private final Scanner scanner;

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String RED    = UIHelper.getRed();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();
    private static final String SEP = "─";

    public RoutePlannerUI() {
        this.controller = new RoutePlannerController();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        boolean exit = false;

        while (!exit) {
            printMainMenu();
            int option = readOption();

            switch (option) {
                case 1 -> planPathForPendingTrain();
                case 2 -> listPendingTrains();
                case 0 -> exit = true;
                default -> System.out.println(RED + "❌ Invalid option!" + RESET);
            }
        }

        System.out.println(GREEN + "\n✓ Returning to main menu..." + RESET);
        MainMenu.menu();
    }

    private void printMainMenu() {
        System.out.println("\n" + CYAN + line(72));
        System.out.println("                    ROUTE PLANNER - USLP08");
        System.out.println(line(72) + RESET);
        System.out.println("1. Plan/assign Path for a Train (UNASSIGNED)");
        System.out.println("2. View pending trains (UNASSIGNED)");
        System.out.println("0. " + RED + "Return to main menu" + RESET + " ");
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    private void listPendingTrains() {
        try {
            List<Train> trains = controller.getTrainsWithUnassignedPath();

            System.out.println("\n" + CYAN + "PENDING TRAINS (path = UNASSIGNED)" + RESET);
            System.out.println(line(72));

            if (trains.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No trains with UNASSIGNED path." + RESET);
            } else {
                for (Train t : trains) {
                    System.out.println("• Train " + t.getId() + " | date=" + t.getDate() + " | time=" + t.getDepartureTime());
                }
            }

            System.out.println(line(72));
        } catch (SQLException e) {
            System.err.println(RED + "❌ DB error: " + e.getMessage() + RESET);
        }
        pause();
    }

    private void planPathForPendingTrain() {
        try {
            List<Train> trains = controller.getTrainsWithUnassignedPath();
            if (trains.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No trains with UNASSIGNED path." + RESET);
                pause();
                return;
            }

            System.out.println("\n" + CYAN + "SELECT A TRAIN" + RESET);
            for (int i = 0; i < trains.size(); i++) {
                Train t = trains.get(i);
                System.out.printf("%d) Train %d | %s %s%n", i + 1, t.getId(), t.getDate(), t.getDepartureTime());
            }
            System.out.println("0) Cancel");
            System.out.print("➤ Choice: ");
            int idx = readOption() - 1;
            if (idx == -1) return;
            if (idx < 0 || idx >= trains.size()) {
                System.out.println(RED + "❌ Invalid train selection." + RESET);
                pause();
                return;
            }

            Train chosen = trains.get(idx);

            List<Freight> freights = controller.getFreightsByTrain(chosen.getId());
            if (freights.isEmpty()) {
                System.out.println(RED + "❌ Train has no freights associated." + RESET);
                pause();
                return;
            }

            System.out.println("\n" + CYAN + "TRAIN " + chosen.getId() + " FREIGHTS" + RESET);
            System.out.println(line(72));
            for (Freight f : freights) {
                System.out.println("• Freight " + f.getId() + " | " + f.getOrigin().getName() + " → " + f.getDestination().getName());
            }
            System.out.println(line(72));

            List<FreightChain> chains = controller.buildChains(freights);
            if (chains.isEmpty()) {
                System.out.println(RED + "❌ Could not build any route chain." + RESET);
                pause();
                return;
            }

            System.out.println("\n" + CYAN + "CANDIDATE ROUTES (CHAINS)" + RESET);
            for (int i = 0; i < chains.size(); i++) {
                FreightChain c = chains.get(i);
                System.out.printf("%d) %s%n", i + 1, c.toString());
            }
            System.out.println("0) Cancel");
            System.out.print("➤ Choose chain: ");
            int cidx = readOption() - 1;
            if (cidx == -1) return;
            if (cidx < 0 || cidx >= chains.size()) {
                System.out.println(RED + "❌ Invalid chain selection." + RESET);
                pause();
                return;
            }

            FreightChain selected = chains.get(cidx);
            RoutePlan plan = controller.planRouteForChain(selected);
            displayRoutePlan(plan);

            System.out.print("\n➤ Save this chain as Path and assign to Train " + chosen.getId() + "? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println(YELLOW + "⚠️  Not saved." + RESET);
                pause();
                return;
            }

            String pathName = askNonEmpty("➤ Path name (0 to cancel): ");
            if (pathName == null) {
                System.out.println(YELLOW + "⚠️  Cancelled." + RESET);
                pause();
                return;
            }

            Path path = controller.savePlanAsPathAndAssignToTrain(chosen.getId(), pathName, plan);

            System.out.println(GREEN + "\n✓ Path created and assigned!" + RESET);
            System.out.println("   • Train: " + chosen.getId());
            System.out.println("   • Path id: " + path.getId());
            System.out.println("   • Path name: " + path.getName());

        } catch (SQLException e) {
            System.err.println(RED + "❌ DB error: " + e.getMessage() + RESET);
        } catch (IllegalArgumentException e) {
            System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
        }
        pause();
    }

    private void displayRoutePlan(RoutePlan plan) {
        System.out.println("\n" + CYAN + "ROUTE PLAN (Stations + LOAD/UNLOAD)" + RESET);
        System.out.println(line(72));

        for (RouteOperation op : plan.getOperations()) {
            String t = (op.getType() == RouteOperation.Type.LOAD) ? "LOAD" : "UNLOAD";
            String arrow = (op.getType() == RouteOperation.Type.LOAD) ? "⬆" : "⬇";

            System.out.printf("%s %-6s | %-20s | Freight %-6d | %s → %s%n",
                    arrow,
                    t,
                    op.getStation().getName(),
                    op.getFreight().getId(),
                    op.getFreight().getOrigin().getName(),
                    op.getFreight().getDestination().getName()
            );
        }

        System.out.println(line(72));
    }

    private String askNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (s.equals("0")) return null;
            if (!s.isEmpty()) return s;
            System.out.println(RED + "❌ Value cannot be empty." + RESET);
        }
    }

    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
