package ui.AssemblyUI;

import controller.AssemblyController.TrainAssemblyController;
import controller.AssemblyController.TrainAssemblyController.LocomotiveWithDetails;
import controller.AssemblyController.TrainAssemblyController.WagonWithDetails;
import domain.railway.*;
import repository.RailwayRepository;
import ui.MainMenu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * User Interface for USLP09 - Train Assembly.
 *
 * <p>Allows Traffic Manager to:
 * <ul>
 *   <li>Select locomotives and wagons for a train</li>
 *   <li>View rolling stock sorted by distance from route start</li>
 *   <li>Distinguish between FREE and IN TRANSIT equipment</li>
 *   <li>Assign selected equipment to a train</li>
 * </ul>
 *
 * @author G164
 * @version 1.0 - USLP09
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
        System.out.println("║ 1. Assemble a train (select locomotives & wagons)            ║");
        System.out.println("║ 2. View all available locomotives                            ║");
        System.out.println("║ 3. View all available wagons                                 ║");
        System.out.println("║ 0. " + RED + "Return to main menu" + RESET + "                                       ║");
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    private void assembleNewTrain() {
        try {
            System.out.println("\n" + CYAN + "═".repeat(72));
            System.out.println("                    ASSEMBLE NEW TRAIN");
            System.out.println("═".repeat(72) + RESET);

            // Step 1: Select train to assemble
            Train train = selectTrain();
            if (train == null) return;

            System.out.println("\n✓ Selected: " + train);
            System.out.println("  Route: " + train.getStartEndpoint().getName() +
                    " → " + train.getEndEndpoint().getName());

            // Step 2: Select locomotives
            List<Integer> selectedLocoIds = selectLocomotives(train);
            if (selectedLocoIds.isEmpty()) {
                System.out.println(YELLOW + "⚠️  No locomotives selected. Aborting." + RESET);
                return;
            }

            // Step 3: Confirm and assign
            System.out.print("\n➤ Confirm assembly? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println(YELLOW + "⚠️  Assembly cancelled." + RESET);
                return;
            }

            // Assign locomotives to train
            assignLocomotivesToTrain(train.getId(), selectedLocoIds);

            System.out.println(GREEN + "\n✅ Train " + train.getId() +
                    " successfully assembled!" + RESET);
            System.out.println("   • " + selectedLocoIds.size() + " locomotive(s) assigned");

        } catch (SQLException e) {
            System.err.println(RED + "\n❌ Database error: " + e.getMessage() + RESET);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private Train selectTrain() throws SQLException {
        List<Train> trains = railwayRepo.getAllTrains();

        if (trains.isEmpty()) {
            System.out.println(RED + "❌ No trains in database!" + RESET);
            return null;
        }

        System.out.println("\n📋 Available trains:");
        System.out.println(line(72));

        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            System.out.printf("%d. Train %d: %s → %s (Depart: %s on %s)\n",
                    i + 1,
                    t.getId(),
                    t.getStartEndpoint().getName(),
                    t.getEndEndpoint().getName(),
                    t.getDepartureTime(),
                    t.getDate()
            );
        }

        System.out.println("0. Cancel");
        System.out.print("\n➤ Select train: ");

        int choice = readOption() - 1;
        if (choice == -1) return null;

        if (choice < 0 || choice >= trains.size()) {
            System.out.println(RED + "❌ Invalid selection!" + RESET);
            return null;
        }

        return trains.get(choice);
    }

    private List<Integer> selectLocomotives(Train train) throws SQLException {
        List<Integer> selectedIds = new ArrayList<>();

        // Get locomotives sorted by proximity to route start
        List<LocomotiveWithDetails> available =
                controller.getLocomotivesByProximity(train.getStartEndpoint());

        if (available.isEmpty()) {
            System.out.println(RED + "\n❌ No locomotives available!" + RESET);
            return selectedIds;
        }

        System.out.println("\n🚂 AVAILABLE LOCOMOTIVES");
        System.out.println("   (Sorted by distance from route start: " +
                train.getStartEndpoint().getName() + ")");
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
                    ld.location.getTrainId() != null ?
                            "(Train " + ld.location.getTrainId() + ")" : "",
                    RESET,
                    ld.location.getCurrentEndpoint().getName()
            );
        }

        System.out.println(line(72));
        System.out.println("\nEnter locomotive numbers to select (comma-separated).");
        System.out.println("Example: 1,3,5");
        System.out.print("➤ Selection (or 0 to skip): ");

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
                    System.out.println(GREEN + "✓ Selected: " +
                            ld.locomotive.getName() + RESET);
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

        // Remove existing locomotives first
        railwayRepo.removeAllLocomotivesFromTrain(trainId);

        // Assign new ones with positions
        for (int i = 0; i < locomotiveIds.size(); i++) {
            int locoId = locomotiveIds.get(i);
            int position = i + 1; // Position starts at 1

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