package ui.AssemblyUI;

import controller.AssemblyController.TrainAssemblyController;
import controller.AssemblyController.TrainAssemblyController.LocomotiveWithDetails;
import controller.AssemblyController.TrainAssemblyController.WagonWithDetails;
import controller.FreightController.RoutePlannerController;
import domain.railway.*;
import repository.RailwayRepository;
import ui.MainMenu;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * User Interface for USLP09 - Train Assembly.
 *
 * Creates a train (id chosen by user), assigns locomotives, creates freights (origin->destination + tons),
 * suggests wagons ordered by distance to leader locomotive, and reserves wagons by assigning them to freights.
 */
public class TrainAssemblyUI implements Runnable {

    private final TrainAssemblyController controller;
    private final RailwayRepository railwayRepo;
    private final Scanner scanner;

    // ANSI Colors
    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP = "─";

    public TrainAssemblyUI() {
        this.controller = new TrainAssemblyController();
        this.railwayRepo = new RailwayRepository();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        boolean exit = false;

        while (!exit) {
            printMainMenu();
            int option = readOption();

            switch (option) {
                case 1 -> assembleNewTrain();
                case 2 -> viewAvailableLocomotives();
                case 3 -> viewAvailableWagons();
                case 0 -> exit = true;
                default -> System.out.println(RED + "❌ Invalid option!" + RESET);
            }
        }

        System.out.println(GREEN + "\n✓ Returning to main menu..." + RESET);
        MainMenu.menu();
    }

    private void printMainMenu() {
        System.out.println("\n" + CYAN + line(72));
        System.out.println("                    TRAIN ASSEMBLY - USLP09");
        System.out.println(line(72) + RESET);
        System.out.println("║ 1. Create & assemble a train (USLP09)                        ║");
        System.out.println("║ 2. View all available locomotives                            ║");
        System.out.println("║ 3. View all available wagons                                 ║");
        System.out.println("║ 0. " + RED + "Return to main menu" + RESET + "                                       ║");
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    private void assembleNewTrain() {
        try {
            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                    CREATE & ASSEMBLE TRAIN");
            System.out.println("═".repeat(72) + RESET);

            // Step 1: Create train (id/date/time by user; operator + max_length fixed; path placeholder)
            TrainCreation creation = createTrain();
            if (creation == null) return;

            int trainId = creation.trainId;
            LocalDate trainDate = creation.date;

            System.out.println(GREEN + "\n✓ Train created with id: " + trainId + RESET);

            // Step 2: Select locomotives (leader = first selected)
            List<Integer> selectedLocoIds = selectLocomotives();
            if (selectedLocoIds.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No locomotives selected. Aborting." + RESET);
                return;
            }

            int leaderLocoId = selectedLocoIds.get(0);
            Endpoint leaderEndpoint = controller.getLocomotiveCurrentEndpoint(leaderLocoId);
            if (leaderEndpoint == null) {
                System.out.println(RED + "❌ Could not determine leader locomotive location. Aborting." + RESET);
                return;
            }

            // Step 3: Define freights (origin → destination, tons)
            List<FreightRequest> freightRequests = defineFreights();
            if (freightRequests.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No freights defined. Aborting." + RESET);
                return;
            }

            // Step 4: Suggest wagons (FREE + not in Wagon_Freight), ordered by distance to leader locomotive
            List<WagonWithDetails> availableWagons = controller.getFreeWagonsByProximity(leaderEndpoint);
            if (availableWagons.isEmpty()) {
                System.out.println(RED + "❌ No FREE wagons available for reservation." + RESET);
                return;
            }

            List<WagonWithDetails> selectedWagons = proposeWagons(availableWagons, freightRequests);
            if (selectedWagons.isEmpty()) {
                System.out.println(RED + "❌ Not enough wagons to satisfy requested tonnage." + RESET);
                return;
            }

            selectedWagons = allowWagonAdjustments(availableWagons, selectedWagons, freightRequests);
            if (selectedWagons.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No wagons selected. Aborting." + RESET);
                return;
            }

            // Step 5: Confirm and persist links/reservations
            System.out.print("\n➤ Confirm assembly? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println(YELLOW + "⚠️  Assembly cancelled." + RESET);
                return;
            }

            assignLocomotivesToTrain(trainId, selectedLocoIds);
            List<Freight> createdFreights = persistFreightsAndWagons(trainId, trainDate, freightRequests, selectedWagons);

            // >>> USLP08 AUTO (plan + show + confirm/alter) <<<
            try {
                Endpoint pickup = selectedWagons.get(0).location.getCurrentEndpoint();

                RoutePlannerController planner = new RoutePlannerController();
                List<Endpoint> proposed = planner.autoPlanPreview(
                        leaderLocoId,
                        pickup,
                        createdFreights
                );

                System.out.println("\n" + CYAN + "═".repeat(72) + RESET);
                System.out.println(CYAN + "              USLP08 - PROPOSED ROUTE (PREVIEW)" + RESET);
                System.out.println(CYAN + "═".repeat(72) + RESET);

                // SHOW ROUTE + ACTIONS (pickup/load/unload)
                printRouteWithActions(proposed, pickup, createdFreights);

                System.out.print("\n➤ Accept this route? (y = accept / e = edit / n = skip): ");
                String opt = scanner.nextLine();
                opt = (opt == null) ? "" : opt.trim().toLowerCase();

                if (opt.equals("y")) {
                    String autoPathName = "AUTO_TRAIN_" + trainId;
                    planner.saveConnectedPathAndAssign(trainId, autoPathName, proposed, pickup, createdFreights);
                    System.out.println(GREEN + "✅ USLP08: Route saved and assigned to Train." + RESET);

                } else if (opt.equals("e")) {
                    System.out.println("\nWrite the route as endpoint IDs separated by spaces (example: 7 6 5 4 3 2 1 2 3 4 5 6 7)");
                    System.out.print("➤ Route IDs: ");
                    String line = scanner.nextLine().trim();

                    List<Endpoint> manual = planner.parseEndpointsFromIds(line);
                    List<Endpoint> fixed = planner.ensureConnected(manual);

                    System.out.println("\nFinal connected route to save:");
                    printRouteWithActions(fixed, pickup, createdFreights);

                    System.out.print("\n➤ Save this route? (y/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        String pathName = "MANUAL_TRAIN_" + trainId;
                        planner.saveConnectedPathAndAssign(trainId, pathName, fixed, pickup, createdFreights);
                        System.out.println(GREEN + "✅ USLP08: Manual route saved and assigned to Train." + RESET);
                    } else {
                        System.out.println(YELLOW + "⚠️ Route not saved. Train remains with UNASSIGNED path." + RESET);
                    }

                } else {
                    System.out.println(YELLOW + "⚠️ Skipped route planning. Train remains with UNASSIGNED path." + RESET);
                }

            } catch (SQLException ex) {
                System.out.println(RED + "❌ USLP08 DB error: " + ex.getMessage() + RESET);
                System.out.println("   Train may remain with UNASSIGNED path.");
            } catch (RuntimeException ex) {
                // This is likely UI/input/framework related (often happens after DB already committed).
                System.out.println(YELLOW + "⚠️ USLP08 planning/UI error: " + ex.getMessage() + RESET);
                System.out.println("   Note: DB may still have been updated correctly.");
                ex.printStackTrace(); // remove after you identify the source
            }
            // <<< END USLP08 AUTO

            System.out.println(GREEN + "\n✅ Train " + trainId + " successfully assembled!" + RESET);
            System.out.println("   • " + selectedLocoIds.size() + " locomotive(s) assigned");
            System.out.println("   • " + freightRequests.size() + " freight(s) created & assigned");
            System.out.println("   • " + selectedWagons.size() + " wagon(s) reserved (via Wagon_Freight)");

        } catch (SQLException e) {
            System.err.println(RED + "\n❌ Database error: " + e.getMessage() + RESET);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ==================== ROUTE PRINTING WITH ACTIONS  ====================

    private void printRouteWithActions(List<Endpoint> route, Endpoint pickup, List<Freight> freights) {
        Set<Integer> pickupShownAtEndpoint = new HashSet<>();
        Set<Integer> loadShownFreightIds = new HashSet<>();
        Set<Integer> unloadShownFreightIds = new HashSet<>();

        for (int i = 0; i < route.size(); i++) {
            Endpoint e = route.get(i);

            List<String> acts = new ArrayList<>();

            if (pickup != null && e.getId() == pickup.getId() && !pickupShownAtEndpoint.contains(e.getId())) {
                acts.add("PICKUP WAGONS");
                pickupShownAtEndpoint.add(e.getId());
            }

            if (freights != null) {
                for (Freight f : freights) {

                    if (e.getId() == f.getOrigin().getId() && !loadShownFreightIds.contains(f.getId())) {
                        acts.add("LOAD Freight " + f.getId() + " (" +
                                f.getOrigin().getName() + "→" + f.getDestination().getName() + ")");
                        loadShownFreightIds.add(f.getId());
                    }

                    if (e.getId() == f.getDestination().getId() && !unloadShownFreightIds.contains(f.getId())) {
                        acts.add("UNLOAD Freight " + f.getId() + " (" +
                                f.getOrigin().getName() + "→" + f.getDestination().getName() + ")");
                        unloadShownFreightIds.add(f.getId());
                    }
                }
            }

            if (acts.isEmpty()) {
                System.out.printf("  %2d) %s (id=%d)%n", (i + 1), e.getName(), e.getId());
            } else {
                System.out.printf("  %2d) %s (id=%d)  <-- %s%n",
                        (i + 1), e.getName(), e.getId(), String.join(" | ", acts));
            }
        }
    }


    // ==================== TRAIN + FREIGHTS & WAGONS (USLP09) ====================

    private static final String DEFAULT_OPERATOR_VAT = "PT509017800"; // Medway
    private static final int DEFAULT_MAX_LENGTH = 740000;

    private static class TrainCreation {
        final int trainId;
        final java.time.LocalDate date;
        final java.time.LocalTime time;

        TrainCreation(int trainId, java.time.LocalDate date, java.time.LocalTime time) {
            this.trainId = trainId;
            this.date = date;
            this.time = time;
        }
    }

    private TrainCreation createTrain() throws SQLException {
        System.out.println("\n" + CYAN + "🧩 CREATE TRAIN" + RESET);

        int trainId;
        while (true) {
            System.out.print("➤ Train id (unique integer, 0 to cancel): ");
            trainId = readOption();
            if (trainId == 0) return null;
            if (trainId < 0) {
                System.out.println(RED + "❌ Invalid id." + RESET);
                continue;
            }
            if (railwayRepo.trainExists(trainId)) {
                System.out.println(RED + "❌ Train id already exists. Choose another." + RESET);
                continue;
            }
            break;
        }

        java.time.LocalDate date;
        while (true) {
            System.out.print("➤ Departure date (YYYY-MM-DD, 0 to cancel): ");
            String s = scanner.nextLine().trim();
            if (s.equals("0")) return null;
            try {
                date = java.time.LocalDate.parse(s);
                break;
            } catch (Exception e) {
                System.out.println(RED + "❌ Invalid date format." + RESET);
            }
        }

        java.time.LocalTime time;
        while (true) {
            System.out.print("➤ Departure time (HH:MM:SS or HH:MM, 0 to cancel): ");
            String s = scanner.nextLine().trim();
            if (s.equals("0")) return null;
            try {
                if (s.matches("\\d{2}:\\d{2}")) s = s + ":00";
                time = java.time.LocalTime.parse(s);
                break;
            } catch (Exception e) {
                System.out.println(RED + "❌ Invalid time format." + RESET);
            }
        }

        // Path is NOT NULL -> use placeholder Path('UNASSIGNED') for now
        int unassignedPathId = railwayRepo.ensureUnassignedPath();
        railwayRepo.createTrainWithId(trainId, DEFAULT_OPERATOR_VAT, date, time, unassignedPathId, DEFAULT_MAX_LENGTH);

        return new TrainCreation(trainId, date, time);
    }

    private static class FreightRequest {
        final Endpoint origin;
        final Endpoint destination;
        final int tons;

        FreightRequest(Endpoint origin, Endpoint destination, int tons) {
            this.origin = origin;
            this.destination = destination;
            this.tons = tons;
        }
    }

    private List<FreightRequest> defineFreights() throws SQLException {
        List<FreightRequest> requests = new ArrayList<>();
        List<Endpoint> endpoints = railwayRepo.getAllEndpoints();

        System.out.println("\n" + CYAN + "📦 DEFINE FREIGHTS" + RESET);
        System.out.print("➤ How many freights do you want to create? ");
        int n = readOption();
        if (n <= 0) return requests;

        for (int i = 1; i <= n; i++) {
            System.out.println("\n" + line(72));
            System.out.println("Freight " + i + ":");
            Endpoint origin = selectEndpoint("Origin", endpoints);
            if (origin == null) return new ArrayList<>();
            Endpoint dest = selectEndpoint("Destination", endpoints);
            if (dest == null) return new ArrayList<>();

            if (origin.equals(dest)) {
                System.out.println(RED + "❌ Origin and destination cannot be the same." + RESET);
                i--;
                continue;
            }

            System.out.print("➤ Required payload (tons): ");
            int tons = readOption();
            if (tons <= 0) {
                System.out.println(RED + "❌ Tons must be > 0." + RESET);
                i--;
                continue;
            }

            requests.add(new FreightRequest(origin, dest, tons));
            System.out.println(GREEN + "✓ Added freight: " + origin.getName() + " → " + dest.getName() + " (" + tons + "t)" + RESET);
        }

        System.out.println("\n✓ Total freights defined: " + requests.size());
        return requests;
    }

    private Endpoint selectEndpoint(String label, List<Endpoint> endpoints) {
        System.out.println("\n" + label + " endpoint:");
        for (int i = 0; i < endpoints.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, endpoints.get(i).getName());
        }
        System.out.println("0. Cancel");
        System.out.print("➤ Select " + label.toLowerCase() + ": ");
        int choice = readOption() - 1;
        if (choice == -1) return null;
        if (choice < 0 || choice >= endpoints.size()) {
            System.out.println(RED + "❌ Invalid selection." + RESET);
            return null;
        }
        return endpoints.get(choice);
    }

    private List<WagonWithDetails> proposeWagons(List<WagonWithDetails> available, List<FreightRequest> requests) {
        int totalTons = requests.stream().mapToInt(r -> r.tons).sum();

        List<WagonWithDetails> selected = new ArrayList<>();
        double cap = 0.0;

        for (WagonWithDetails wd : available) {
            selected.add(wd);
            cap += wd.wagon.getModel().getPayload();
            if (cap >= totalTons) break;
        }

        System.out.println("\n" + CYAN + "🛤️  WAGON SUGGESTION" + RESET);
        System.out.println("Requested payload: " + totalTons + "t");
        System.out.println("Suggested wagons (sorted by distance to leader locomotive):");
        printWagonSelection(selected);
        System.out.printf("Total payload capacity selected: %.1ft\n", cap);

        if (cap < totalTons) return new ArrayList<>();
        return selected;
    }

    private List<WagonWithDetails> allowWagonAdjustments(List<WagonWithDetails> available,
                                                         List<WagonWithDetails> current,
                                                         List<FreightRequest> requests) {
        int totalTons = requests.stream().mapToInt(r -> r.tons).sum();

        System.out.print("\n➤ Do you want to adjust the wagon selection? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return current;

        List<WagonWithDetails> selected = new ArrayList<>(current);

        boolean done = false;
        while (!done) {
            System.out.println("\n" + CYAN + "AVAILABLE WAGONS (FREE & unassigned)" + RESET);
            for (int i = 0; i < available.size(); i++) {
                WagonWithDetails wd = available.get(i);
                boolean isSel = selected.stream().anyMatch(s -> s.wagon.getId() == wd.wagon.getId());
                System.out.printf("%2d. Wagon %d | payload=%.1ft | @ %s %s\n",
                        i + 1,
                        wd.wagon.getId(),
                        wd.wagon.getModel().getPayload(),
                        wd.location.getCurrentEndpoint().getName(),
                        isSel ? GREEN + "[SELECTED]" + RESET : ""
                );
            }

            System.out.println("\nOptions:");
            System.out.println("1) Add wagon by number");
            System.out.println("2) Remove wagon by number");
            System.out.println("0) Finish");
            System.out.print("➤ Choice: ");
            int opt = readOption();

            switch (opt) {
                case 1 -> {
                    System.out.print("➤ Enter wagon number to ADD: ");
                    int idx = readOption() - 1;
                    if (idx >= 0 && idx < available.size()) {
                        WagonWithDetails w = available.get(idx);
                        boolean already = selected.stream().anyMatch(s -> s.wagon.getId() == w.wagon.getId());
                        if (!already) selected.add(w);
                    }
                }
                case 2 -> {
                    System.out.print("➤ Enter wagon number to REMOVE: ");
                    int idx = readOption() - 1;
                    if (idx >= 0 && idx < available.size()) {
                        int wagonId = available.get(idx).wagon.getId();
                        selected.removeIf(s -> s.wagon.getId() == wagonId);
                    }
                }
                case 0 -> done = true;
                default -> System.out.println(RED + "❌ Invalid option." + RESET);
            }

            double cap = selected.stream().mapToDouble(w -> w.wagon.getModel().getPayload()).sum();
            System.out.printf("\nCurrent capacity: %.1ft / required: %dt\n", cap, totalTons);
            if (cap < totalTons) {
                System.out.println(YELLOW + "⚠️  Warning: capacity is below required tons." + RESET);
            }
        }

        return selected;
    }

    private void printWagonSelection(List<WagonWithDetails> selection) {
        if (selection.isEmpty()) {
            System.out.println(RED + "(none)" + RESET);
            return;
        }
        for (WagonWithDetails wd : selection) {
            System.out.printf("  • Wagon %d | payload=%.1ft | @ %s\n",
                    wd.wagon.getId(),
                    wd.wagon.getModel().getPayload(),
                    wd.location.getCurrentEndpoint().getName());
        }
    }

    private List<Freight> persistFreightsAndWagons(int trainId,
                                                   LocalDate trainDate,
                                                   List<FreightRequest> requests,
                                                   List<WagonWithDetails> selectedWagons) throws SQLException {

        List<Freight> created = new ArrayList<>();

        int wagonCursor = 0;
        for (FreightRequest req : requests) {
            int freightId = railwayRepo.createFreight(trainDate, req.origin.getId(), req.destination.getId());
            railwayRepo.assignFreightToTrain(freightId, trainId);

            created.add(new Freight(freightId, trainDate, req.origin, req.destination));

            double cap = 0.0;
            while (cap < req.tons && wagonCursor < selectedWagons.size()) {
                WagonWithDetails wd = selectedWagons.get(wagonCursor++);
                railwayRepo.assignWagonToFreight(wd.wagon.getId(), freightId);
                cap += wd.wagon.getModel().getPayload();
            }

            if (cap < req.tons) {
                throw new SQLException("Not enough wagons to allocate freight " +
                        req.origin.getName() + "→" + req.destination.getName());
            }
        }

        return created;
    }


    private List<Integer> selectLocomotives() throws SQLException {
        List<Integer> selectedIds = new ArrayList<>();

        // We don't have a route/path yet in USLP09 train creation.
        // Show all locos; only FREE can be selected.
        List<LocomotiveWithDetails> available = controller.getAvailableLocomotives();

        if (available.isEmpty()) {
            System.out.println(RED + "\n❌ No locomotives available!" + RESET);
            return selectedIds;
        }

        System.out.println("\n🚂 AVAILABLE LOCOMOTIVES");
        System.out.println("   (Only FREE locomotives can be assigned)");
        System.out.println(line(72));

        for (int i = 0; i < available.size(); i++) {
            LocomotiveWithDetails ld = available.get(i);
            String statusColor = ld.location.isFree() ? GREEN : YELLOW;
            String status = ld.location.isFree() ? "FREE" : "IN TRANSIT";

            System.out.printf("%s%2d. %-20s | %s %-12s%s | @ %s\n",
                    statusColor,
                    i + 1,
                    ld.locomotive.getName(),
                    status,
                    ld.location.getTrainId() != null ? "(Train " + ld.location.getTrainId() + ")" : "",
                    RESET,
                    ld.location.getCurrentEndpoint().getName()
            );
        }

        System.out.println(line(72));
        System.out.println("\nEnter locomotive numbers to select (comma-separated).");
        System.out.println("Leader locomotive will be the FIRST selected.");
        System.out.print("➤ Selection (or 0 to cancel): ");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) return selectedIds;

        String[] parts = input.split(",");
        for (String part : parts) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < available.size()) {
                    LocomotiveWithDetails ld = available.get(index);

                    if (ld.location.isInTransit()) {
                        System.out.println(YELLOW + "⚠️  Locomotive " +
                                ld.locomotive.getName() + " is IN TRANSIT - cannot assign." + RESET);
                        continue;
                    }

                    selectedIds.add(ld.locomotive.getId());
                    System.out.println(GREEN + "✓ Selected: " + ld.locomotive.getName() + RESET);
                } else {
                    System.out.println(RED + "❌ Invalid number: " + part + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid input: " + part + RESET);
            }
        }

        return selectedIds;
    }

    private void assignLocomotivesToTrain(int trainId, List<Integer> locomotiveIds)
            throws SQLException {

        railwayRepo.removeAllLocomotivesFromTrain(trainId);

        for (int i = 0; i < locomotiveIds.size(); i++) {
            int locoId = locomotiveIds.get(i);
            int position = i + 1; // leader = position 1
            railwayRepo.assignLocomotiveToTrain(locoId, trainId, position);
        }
    }

    private void viewAvailableLocomotives() {
        try {
            List<LocomotiveWithDetails> locomotives = controller.getAvailableLocomotives();

            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                  ALL LOCOMOTIVES");
            System.out.println("═".repeat(72) + RESET);

            if (locomotives.isEmpty()) {
                System.out.println(RED + "❌ No locomotives found!" + RESET);
            } else {
                for (LocomotiveWithDetails ld : locomotives) {
                    String statusColor = ld.location.isFree() ? GREEN : YELLOW;
                    System.out.println(statusColor + ld + RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void viewAvailableWagons() {
        try {
            List<WagonWithDetails> wagons = controller.getAvailableWagons();

            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                     ALL WAGONS");
            System.out.println("═".repeat(72) + RESET);

            if (wagons.isEmpty()) {
                System.out.println(RED + "❌ No wagons found!" + RESET);
            } else {
                for (WagonWithDetails wd : wagons) {
                    String statusColor = wd.location.isFree() ? GREEN : YELLOW;
                    System.out.println(statusColor + wd + RESET);
                }
            }

        } catch (SQLException e) {
            System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
}
