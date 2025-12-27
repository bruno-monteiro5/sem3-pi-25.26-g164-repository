package ui.FreightUI;

import controller.FreightController.RoutePlannerController;
import domain.railway.Freight;
import domain.railway.RoutePlan;
import domain.railway.RouteOperation;
import domain.railway.Endpoint;
import ui.MainMenu;
import ui.UIHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Text-based user interface for the Route Planner (USLP08).
 * <p>
 * USLP08 – As Freight Manager, I want to use a route planner to define/assign routes
 * in order to accomplish a list of pending freights. A route can be simple or complex.
 * <p>
 * Acceptance Criteria:
 * - A list with the sequence of stations and the cargos to be loaded/unloaded should be presented
 */
public class RoutePlannerUI implements Runnable {
    private final RoutePlannerController controller;
    private final Scanner scanner;

    private static final String RESET  = UIHelper.getReset();
    private static final String CYAN   = UIHelper.getCyan();
    private static final String RED    = UIHelper.getRed();
    private static final String GREEN  = UIHelper.getGreen();
    private static final String YELLOW = UIHelper.getYellow();

    public RoutePlannerUI() {
        this.controller = new RoutePlannerController();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            UIHelper.clear();
            UIHelper.printHeader("ROUTE PLANNER (USLP08)");
            System.out.println();

            // 1. Read number of freights to plan
            int numFreights = readNumberOfFreights();
            if (numFreights <= 0) {
                returnToMainMenu();
                return;
            }

            // 2. Collect details for each freight
            List<Freight> freightList = collectFreights(numFreights);
            if (freightList == null) {
                returnToMainMenu();
                return;
            }

            // 3. Generate the route plan using the controller
            System.out.println("\n" + CYAN + "⏳ Calculating route plan..." + RESET);
            RoutePlan routePlan = controller.planRoute(freightList);

            // 4. Display the route plan result
            displayRoutePlan(routePlan);

        } catch (IllegalArgumentException e) {
            System.out.println(RED + "❌ " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.out.println(RED + "❌ Unexpected error: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            UIHelper.pause();
            returnToMainMenu();
        }
    }

    /**
     * Prompts the user to enter the number of pending freights to include in the route.
     * Allows cancellation by entering 0.
     *
     * @return the number of freights (or 0 if the operation was canceled)
     */
    private int readNumberOfFreights() {
        int num = -1;
        while (true) {
            System.out.print(CYAN + "➤ " + RESET + "Enter the number of pending freights to plan (0 to cancel): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                num = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid number!" + RESET);
                continue;
            }

            if (num < 0) {
                System.out.println(RED + "❌ Number of freights cannot be negative!" + RESET);
                continue;
            }

            break;
        }
        return num;
    }

    /**
     * Collects freight information from the user.
     * For the first freight, asks for both origin and destination.
     * For subsequent freights, the origin is the previous freight's destination (chaining).
     *
     * @param numFreights number of freights to collect
     * @return list of Freight objects, or null if user cancels
     */
    private List<Freight> collectFreights(int numFreights) {
        List<Freight> freightList = new ArrayList<Freight>();

        for (int i = 1; i <= numFreights; i++) {
            System.out.println();
            System.out.println(CYAN + UIHelper.line(72));
            System.out.println("Freight " + i + " of " + numFreights);
            System.out.println(UIHelper.line(72) + RESET);

            if (i == 1) {
                // First freight: ask origin and destination
                System.out.print(CYAN + "➤ " + RESET + "Enter origin station name: ");
                String originName = scanner.nextLine().trim();

                if (originName.isEmpty()) {
                    System.out.println(RED + "❌ Origin station cannot be empty!" + RESET);
                    return null;
                }

                System.out.print(CYAN + "➤ " + RESET + "Enter destination station name: ");
                String destName = scanner.nextLine().trim();

                if (destName.isEmpty()) {
                    System.out.println(RED + "❌ Destination station cannot be empty!" + RESET);
                    return null;
                }

                // Create endpoints (using simple IDs for in-memory demo)
                Endpoint origin = new Endpoint(i * 2 - 1, originName, 0);
                Endpoint destination = new Endpoint(i * 2, destName, 0);
                Freight freight = new Freight(i, LocalDate.now(), origin, destination);
                freightList.add(freight);

                System.out.println(GREEN + "✓ Freight " + i + " added: " + originName + " → " + destName + RESET);

            } else {
                // Subsequent freight: origin is the previous freight's destination
                Freight prevFreight = freightList.get(freightList.size() - 1);
                Endpoint chainOrigin = prevFreight.getDestination();

                System.out.println("Origin: " + YELLOW + chainOrigin.getName() + RESET + " (from previous freight)");
                System.out.print(CYAN + "➤ " + RESET + "Enter destination station name: ");
                String destName = scanner.nextLine().trim();

                if (destName.isEmpty()) {
                    System.out.println(RED + "❌ Destination station cannot be empty!" + RESET);
                    return null;
                }

                // Create new endpoint for this freight's destination
                Endpoint destination = new Endpoint(i * 2, destName, 0);
                Freight freight = new Freight(i, LocalDate.now(), chainOrigin, destination);
                freightList.add(freight);

                System.out.println(GREEN + "✓ Freight " + i + " added: " + chainOrigin.getName() + " → " + destName + RESET);
            }
        }

        return freightList;
    }

    /**
     * Displays the route plan results: the sequence of stations and the freights to load/unload at each.
     *
     * @param routePlan the RoutePlan to display
     */
    private void displayRoutePlan(RoutePlan routePlan) {
        UIHelper.clear();
        UIHelper.printHeader("ROUTE PLANNING RESULTS");
        System.out.println();

        List<RouteOperation> ops = routePlan.getOperations();

        if (ops.isEmpty()) {
            System.out.println(YELLOW + "No operations in route plan." + RESET);
            return;
        }

        System.out.println(GREEN + "✓ Route plan generated successfully" + RESET);
        System.out.println();
        System.out.println("Sequence of stations and cargo operations:");
        System.out.println(UIHelper.line(72));

        // Group operations by station for better readability
        int stopNumber = 1;
        int i = 0;

        while (i < ops.size()) {
            RouteOperation currentOp = ops.get(i);
            String stationName = currentOp.getStation().getName();

            // Collect all operations at this station
            List<String> stationOps = new ArrayList<String>();

            while (i < ops.size() && ops.get(i).getStation().equals(currentOp.getStation())) {
                RouteOperation op = ops.get(i);
                String opStr = formatOperation(op);
                stationOps.add(opStr);
                i++;
            }

            // Print station stop with all operations
            System.out.printf("%s%d. %s%s\n", CYAN, stopNumber, stationName, RESET);

            for (int j = 0; j < stationOps.size(); j++) {
                String op = stationOps.get(j);
                System.out.println("   " + op);
            }

            if (i < ops.size()) {
                System.out.println("   " + YELLOW + "↓" + RESET);
            }

            stopNumber++;
        }

        System.out.println(UIHelper.line(72));
        System.out.printf("\nTotal stops: %d\n", stopNumber - 1);
    }

    /**
     * Formats a RouteOperation for display.
     *
     * @param op the operation to format
     * @return formatted string representation
     */
    private String formatOperation(RouteOperation op) {
        String symbol;
        String color;

        if (op.getType() == RouteOperation.Type.LOAD) {
            symbol = "↑";
            color = GREEN;
        } else {
            symbol = "↓";
            color = RED;
        }

        return String.format("%s%s %s Freight %d%s (%s → %s)",
                color,
                symbol,
                op.getType() == RouteOperation.Type.LOAD ? "Load" : "Unload",
                op.getFreight().getId(),
                RESET,
                op.getFreight().getOrigin().getName(),
                op.getFreight().getDestination().getName());
    }

    /**
     * Returns to the main menu by running the MainMenu.
     */
    private void returnToMainMenu() {
        new MainMenu().run();
    }
}