package ui.AssemblyUI;

import controller.AssemblyController.TrainAssemblyController;
import controller.AssemblyController.TrainAssemblyController.LocomotiveWithDetails;
import controller.AssemblyController.TrainAssemblyController.WagonWithDetails;
import domain.railway.*;
import repository.RailwayRepository;
import ui.MainMenu;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * USLP09 - Train Assembly UI
 *
 * Allows Traffic Managers to:
 * - Create a new train
 * - Select an EXISTING Path (created in USLP08 or manually)
 * - Assign locomotives (sorted by distance from route start)
 * - Assign wagons (sorted by distance from route start)
 *
 * Prerequisites:
 * - Path must already exist in database (created via USLP08)
 * - Freights must already be created
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
                case 4 -> viewExistingPaths();
                case 0 -> exit = true;
                default -> System.out.println(RED + "❌ Invalid option!" + RESET);
            }
        }

        System.out.println(GREEN + "\n✓ Returning to main menu..." + RESET);
        MainMenu.menu();
    }

    private void printMainMenu() {
        System.out.println("\n" + CYAN + line(72));
        System.out.println("            TRAIN ASSEMBLY - USLP09 (Traffic Manager)");
        System.out.println(line(72) + RESET);
        System.out.println("1. Create & assemble a train (USLP09)                        ");
        System.out.println("2. View available locomotives                                ");
        System.out.println("3. View available wagons                                     ");
        System.out.println("4. View existing paths                                       ");
        System.out.println("0. " + RED + "Return to main menu" + RESET + "                                       ║");
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    /**
     * USLP09 Main Flow:
     * 1. Create train (id/date/time)
     * 2. Select EXISTING Path (from USLP08 or manual)
     * 3. Select locomotives (sorted by distance from route start)
     * 4. Assign freights to train
     * 5. Persist to database
     */
    private void assembleNewTrain() {
        try {
            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                    CREATE & ASSEMBLE TRAIN (USLP09)");
            System.out.println("═".repeat(72) + RESET);

            // Step 1: Create train
            TrainCreation creation = createTrain();
            if (creation == null) return;

            int trainId = creation.trainId;
            LocalDate trainDate = creation.date;

            System.out.println(GREEN + "\n✓ Train " + trainId + " created" + RESET);

            // Step 2: Select EXISTING Path
            Path selectedPath = selectExistingPath();
            if (selectedPath == null) {
                System.out.println(YELLOW + "⚠️  No path selected. Aborting." + RESET);
                return;
            }

            System.out.println(GREEN + "✓ Path selected: " + selectedPath.getName() + RESET);

            // Step 3: Assign path to train
            railwayRepo.updateTrainPath(trainId, selectedPath.getId());

            Endpoint routeStart = selectedPath.getFirstEndpoint();
            Endpoint routeEnd = selectedPath.getLastEndpoint();

            System.out.println("   Route: " + routeStart.getName() + " → " + routeEnd.getName());

            // Step 4: Select locomotives (sorted by distance from route start)
            List<Integer> selectedLocoIds = selectLocomotivesByDistance(routeStart);
            if (selectedLocoIds.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No locomotives selected. Aborting." + RESET);
                return;
            }

            assignLocomotivesToTrain(trainId, selectedLocoIds);

            System.out.println(GREEN + "\n✓ Locomotives assigned:" + RESET);
            for (int locoId : selectedLocoIds) {
                Locomotive loco = railwayRepo.getLocomotiveById(locoId);
                System.out.println("   • " + loco.getName());
            }

            // Step 5: Assign freights (optional - freights may already exist)
            System.out.print("\n➤ Assign freights to this train? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                assignFreightsToTrain(trainId);
            }

            // Step 6: Final summary
            System.out.println(GREEN + "\n✅ Train " + trainId + " successfully assembled!" + RESET);
            System.out.println("   • Path: " + selectedPath.getName());
            System.out.println("   • Locomotives: " + selectedLocoIds.size());
            System.out.println("   • Departure: " + trainDate + " " + creation.time);
            System.out.println("\n" + YELLOW + "Note: Train is ready for scheduling (USLP10)" + RESET);

        } catch (SQLException e) {
            System.err.println(RED + "\n❌ Database error: " + e.getMessage() + RESET);
            e.printStackTrace();
        }

        pause();
    }

    /**
     * Selects an existing Path from the database.
     * Shows Path details (endpoints, stops).
     */
    private Path selectExistingPath() throws SQLException {
        List<Path> paths = railwayRepo.getAllPaths();

        if (paths.isEmpty()) {
            System.out.println(RED + "\n❌ No paths found in database." + RESET);
            System.out.println("   Create a path first using USLP08 (Route Planner)");
            return null;
        }

        System.out.println("\n" + CYAN + "EXISTING PATHS" + RESET);
        System.out.println(line(72));

        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);
            String start = p.getFirstEndpoint() != null ? p.getFirstEndpoint().getName() : "?";
            String end = p.getLastEndpoint() != null ? p.getLastEndpoint().getName() : "?";
            int stops = (int) p.getEndpoints().stream().filter(Path.PathEndpoint::isMustStop).count();

            System.out.printf("%d) %-30s | %s → %s | %d stop(s)%n",
                    i + 1,
                    truncate(p.getName(), 30),
                    truncate(start, 15),
                    truncate(end, 15),
                    stops);
        }
        System.out.println("0) Cancel");
        System.out.println(line(72));

        System.out.print("➤ Select path: ");
        int idx = readOption() - 1;
        if (idx == -1) return null;
        if (idx < 0 || idx >= paths.size()) {
            System.out.println(RED + "❌ Invalid selection." + RESET);
            return null;
        }

        Path selected = paths.get(idx);

        // Show path details
        System.out.println("\n" + CYAN + "PATH DETAILS: " + selected.getName() + RESET);
        System.out.println(line(72));
        for (Path.PathEndpoint pe : selected.getOrderedEndpoints()) {
            String stop = pe.isMustStop() ? GREEN + "[STOP]" + RESET : YELLOW + "[PASS]" + RESET;
            System.out.printf("  %2d. %-30s %s%n", pe.getSequence(), pe.getEndpoint().getName(), stop);
        }
        System.out.println(line(72));

        System.out.print("➤ Confirm this path? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            return null;
        }

        return selected;
    }

    /**
     * Selects locomotives sorted by distance from route start (USLP09 requirement).
     */
    private List<Integer> selectLocomotivesByDistance(Endpoint routeStart) throws SQLException {
        List<Integer> selectedIds = new ArrayList<>();

        // Get locomotives sorted by proximity to route start
        List<LocomotiveWithDetails> sorted = controller.getLocomotivesByProximity(routeStart);

        if (sorted.isEmpty()) {
            System.out.println(RED + "\n❌ No locomotives available!" + RESET);
            return selectedIds;
        }

        System.out.println("\n🚂 AVAILABLE LOCOMOTIVES (sorted by distance from route start)");
        System.out.println("   Route starts at: " + routeStart.getName());
        System.out.println(line(72));

        for (int i = 0; i < sorted.size(); i++) {
            LocomotiveWithDetails ld = sorted.get(i);
            String statusColor = ld.location.isFree() ? GREEN : YELLOW;
            String status = ld.location.isFree() ? "FREE" : "IN TRANSIT";

            double dist = ld.location.distanceFrom(routeStart);
            String distStr = dist == 0 ? "AT START" : String.format("%.0f km", dist / 1000);

            System.out.printf("%s%2d. %-20s | %s | @ %-20s | %s%s%n",
                    statusColor,
                    i + 1,
                    truncate(ld.locomotive.getName(), 20),
                    status,
                    truncate(ld.location.getCurrentEndpoint().getName(), 20),
                    distStr,
                    RESET);
        }

        System.out.println(line(72));
        System.out.println("\nEnter locomotive numbers (comma-separated, 0 to cancel):");
        System.out.print("➤ Selection: ");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) return selectedIds;

        String[] parts = input.split(",");
        for (String part : parts) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < sorted.size()) {
                    LocomotiveWithDetails ld = sorted.get(index);

                    if (ld.location.isInTransit()) {
                        System.out.println(YELLOW + "⚠️  " + ld.locomotive.getName() +
                                " is IN TRANSIT - cannot assign." + RESET);
                        continue;
                    }

                    selectedIds.add(ld.locomotive.getId());
                    System.out.println(GREEN + "✓ Selected: " + ld.locomotive.getName() + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid input: " + part + RESET);
            }
        }

        return selectedIds;
    }

    /**
     * Assigns freights to train.
     * Shows available freights and allows selection.
     */
    private void assignFreightsToTrain(int trainId) throws SQLException {
        List<Freight> pending = railwayRepo.getPendingFreights();

        if (pending.isEmpty()) {
            System.out.println(YELLOW + "\n⚠️  No pending freights available." + RESET);
            return;
        }

        System.out.println("\n" + CYAN + "PENDING FREIGHTS" + RESET);
        System.out.println(line(72));

        for (int i = 0; i < pending.size(); i++) {
            Freight f = pending.get(i);
            System.out.printf("%d) Freight %d | %s → %s | %d wagon(s)%n",
                    i + 1,
                    f.getId(),
                    truncate(f.getOrigin().getName(), 20),
                    truncate(f.getDestination().getName(), 20),
                    f.getWagons().size());
        }
        System.out.println("0) Skip");
        System.out.println(line(72));

        System.out.println("Enter freight numbers (comma-separated):");
        System.out.print("➤ Selection: ");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) return;

        String[] parts = input.split(",");
        int assigned = 0;

        for (String part : parts) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < pending.size()) {
                    Freight f = pending.get(index);
                    railwayRepo.assignFreightToTrain(f.getId(), trainId);
                    assigned++;
                    System.out.println(GREEN + "✓ Assigned Freight " + f.getId() + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "❌ Invalid input: " + part + RESET);
            }
        }

        System.out.println("\n✓ Total freights assigned: " + assigned);
    }

    private void viewExistingPaths() {
        try {
            List<Path> paths = railwayRepo.getAllPaths();

            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                     EXISTING PATHS");
            System.out.println("═".repeat(72) + RESET);

            if (paths.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No paths in database." + RESET);
                System.out.println("   Create paths using USLP08 (Route Planner)");
            } else {
                for (Path p : paths) {
                    String start = p.getFirstEndpoint() != null ? p.getFirstEndpoint().getName() : "?";
                    String end = p.getLastEndpoint() != null ? p.getLastEndpoint().getName() : "?";

                    System.out.println("\n• " + GREEN + p.getName() + RESET + " (ID: " + p.getId() + ")");
                    System.out.println("  Route: " + start + " → " + end);
                    System.out.println("  Stops: " + p.getEndpoints().size() + " total, " +
                            p.getStopEndpoints().size() + " must-stop");
                }
            }

            System.out.println(CYAN + "\n═".repeat(72) + RESET);

        } catch (SQLException e) {
            System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
        }

        pause();
    }

    // ==================== HELPER METHODS (REUSED FROM ORIGINAL) ====================

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
            System.out.print("➤ Train ID (unique integer, 0 to cancel): ");
            trainId = readOption();
            if (trainId == 0) return null;
            if (trainId < 0) {
                System.out.println(RED + "❌ Invalid ID." + RESET);
                continue;
            }
            if (railwayRepo.trainExists(trainId)) {
                System.out.println(RED + "❌ Train ID already exists." + RESET);
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

        // Create train with UNASSIGNED path (will be updated later)
        int unassignedPathId = railwayRepo.ensureUnassignedPath();
        railwayRepo.createTrainWithId(trainId, DEFAULT_OPERATOR_VAT, date, time, unassignedPathId, DEFAULT_MAX_LENGTH);

        return new TrainCreation(trainId, date, time);
    }

    private void assignLocomotivesToTrain(int trainId, List<Integer> locomotiveIds) throws SQLException {
        railwayRepo.removeAllLocomotivesFromTrain(trainId);

        for (int i = 0; i < locomotiveIds.size(); i++) {
            int locoId = locomotiveIds.get(i);
            int position = i + 1;
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

        pause();
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

        pause();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
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