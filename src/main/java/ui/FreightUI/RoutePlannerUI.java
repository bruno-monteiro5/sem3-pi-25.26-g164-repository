package ui.FreightUI;

import controller.FreightController.RoutePlannerController;
import controller.FreightController.RoutePlannerController.FreightChain;
import domain.railway.Freight;
import domain.railway.Path;
import domain.railway.RouteOperation;
import domain.railway.RoutePlan;
import ui.MainMenu;
import ui.UIHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * USLP08 - Route Planner UI
 *
 * Allows Freight Managers to:
 * - View pending freights (no train assigned)
 * - Build freight chains
 * - Create abstract route plans
 * - Save plans as Paths in database
 *
 * Does NOT handle train creation/assembly.
 */
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
                case 1 -> planRouteForPendingFreights();
                case 2 -> viewPendingFreights();
                case 0 -> exit = true;
                default -> System.out.println(RED + "❌ Invalid option!" + RESET);
            }
        }

        System.out.println(GREEN + "\n✓ Returning to main menu..." + RESET);
        MainMenu.menu();
    }

    private void printMainMenu() {
        System.out.println("\n" + CYAN + line(72));
        System.out.println("              ROUTE PLANNER - USLP08 (Freight Manager)");
        System.out.println(line(72) + RESET);
        System.out.println("1. Plan route for pending freights");
        System.out.println("2. View pending freights");
        System.out.println("0. " + RED + "Return to main menu" + RESET);
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    /**
     * USLP08 Main Flow:
     * 1. Get pending freights (no train assigned)
     * 2. Build freight chains
     * 3. Select chain to plan
     * 4. Generate route plan (LOAD/UNLOAD sequence)
     * 5. Save as Path in database
     */
    private void planRouteForPendingFreights() {
        try {
            // Step 1: Get pending freights
            List<Freight> pending = controller.getPendingFreights();

            if (pending.isEmpty()) {
                System.out.println(YELLOW + "\n⚠️  No pending freights found." + RESET);
                System.out.println("   (All freights are already assigned to trains)");
                pause();
                return;
            }

            // Display pending freights
            System.out.println("\n" + CYAN + "PENDING FREIGHTS (no train assigned)" + RESET);
            System.out.println(line(72));
            for (Freight f : pending) {
                System.out.printf("• Freight %d | %s → %s | %d wagon(s)%n",
                        f.getId(),
                        f.getOrigin().getName(),
                        f.getDestination().getName(),
                        f.getWagons().size());
            }
            System.out.println(line(72));

            // Step 2: Build freight chains
            List<FreightChain> chains = controller.buildChains(pending);

            if (chains.isEmpty()) {
                System.out.println(RED + "❌ Could not build any route chain." + RESET);
                pause();
                return;
            }

            // Step 3: Display chains
            System.out.println("\n" + CYAN + "CANDIDATE ROUTE CHAINS" + RESET);
            System.out.println("(Chains connect freights where destination = next origin)");
            System.out.println(line(72));

            for (int i = 0; i < chains.size(); i++) {
                FreightChain c = chains.get(i);
                System.out.printf("%d) %s%n", i + 1, formatChain(c));
            }
            System.out.println("0) Cancel");
            System.out.println(line(72));

            System.out.print("➤ Select chain to plan: ");
            int cidx = readOption() - 1;
            if (cidx == -1) return;
            if (cidx < 0 || cidx >= chains.size()) {
                System.out.println(RED + "❌ Invalid selection." + RESET);
                pause();
                return;
            }

            // Step 4: Generate route plan
            FreightChain selected = chains.get(cidx);
            RoutePlan plan = controller.planRouteForChain(selected);

            displayRoutePlan(plan);

            // Step 5: Save as Path
            System.out.print("\n➤ Save this route as a Path? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println(YELLOW + "⚠️  Route not saved." + RESET);
                pause();
                return;
            }

            String pathName = askNonEmpty("➤ Path name (0 to cancel): ");
            if (pathName == null) {
                System.out.println(YELLOW + "⚠️  Cancelled." + RESET);
                pause();
                return;
            }

            Path path = controller.savePlanAsPath(pathName, plan);

            System.out.println(GREEN + "\n✓ Route saved successfully!" + RESET);
            System.out.println("   • Path ID: " + path.getId());
            System.out.println("   • Path name: " + path.getName());
            System.out.println("   • Stops: " + path.getEndpoints().size());
            System.out.println("\n" + YELLOW + "Note: This Path can now be assigned to a train in USLP09" + RESET);

        } catch (SQLException e) {
            System.err.println(RED + "❌ Database error: " + e.getMessage() + RESET);
        } catch (IllegalArgumentException e) {
            System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
        }

        pause();
    }

    private void viewPendingFreights() {
        try {
            List<Freight> pending = controller.getPendingFreights();

            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                  PENDING FREIGHTS");
            System.out.println("═".repeat(72) + RESET);

            if (pending.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No pending freights." + RESET);
                System.out.println("   All freights are assigned to trains.");
            } else {
                System.out.println(String.format("%-8s | %-20s | %-20s | %s",
                        "ID", "Origin", "Destination", "Wagons"));
                System.out.println(line(72));

                for (Freight f : pending) {
                    System.out.printf("%-8d | %-20s | %-20s | %d%n",
                            f.getId(),
                            truncate(f.getOrigin().getName(), 20),
                            truncate(f.getDestination().getName(), 20),
                            f.getWagons().size());
                }
            }

            System.out.println(CYAN + "═".repeat(72) + RESET);

        } catch (SQLException e) {
            System.err.println(RED + "❌ Database error: " + e.getMessage() + RESET);
        }

        pause();
    }

    private void displayRoutePlan(RoutePlan plan) {
        System.out.println("\n" + CYAN + "ROUTE PLAN (Stations + Operations)" + RESET);
        System.out.println(line(72));

        int step = 1;
        for (RouteOperation op : plan.getOperations()) {
            String type = (op.getType() == RouteOperation.Type.LOAD) ? "LOAD  " : "UNLOAD";
            String arrow = (op.getType() == RouteOperation.Type.LOAD) ? "⬆" : "⬇";
            String color = (op.getType() == RouteOperation.Type.LOAD) ? GREEN : YELLOW;

            System.out.printf("%s%2d. %s %s | %-20s | Freight %d (%s → %s)%s%n",
                    color,
                    step++,
                    arrow,
                    type,
                    truncate(op.getStation().getName(), 20),
                    op.getFreight().getId(),
                    truncate(op.getFreight().getOrigin().getName(), 10),
                    truncate(op.getFreight().getDestination().getName(), 10),
                    RESET
            );
        }

        System.out.println(line(72));
    }

    private String formatChain(FreightChain chain) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chain[").append(chain.getFreights().size()).append(" freight(s)]: ");

        List<Freight> freights = chain.getFreights();
        for (int i = 0; i < freights.size(); i++) {
            Freight f = freights.get(i);
            if (i == 0) {
                sb.append(truncate(f.getOrigin().getName(), 15));
            }
            sb.append(" → ").append(truncate(f.getDestination().getName(), 15));
        }

        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
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