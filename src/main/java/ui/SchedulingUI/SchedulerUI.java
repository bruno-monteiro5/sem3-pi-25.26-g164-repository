package ui.SchedulingUI;

import com.sun.tools.javac.Main;
import domain.railway.*;
import domain.scheduling.Schedule;
import domain.scheduling.TrainSchedule;
import repository.RailwayRepository;
import controller.SchedulingController.TrainScheduler;
import ui.MainMenu;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Text-based user interface for the Train Scheduler
 * USLP07 - As Freight Manager, I want to use a scheduler
 */
public class SchedulerUI implements Runnable{

    private RailwayNetwork network;
    private TrainScheduler scheduler;
    private final Scanner scanner;

    // ANSI colors (matching MainMenu style)
    private static final boolean COLOR_ON = true;
    private static final String RESET = COLOR_ON ? "\u001B[0m" : "";
    private static final String CYAN = COLOR_ON ? "\u001B[36m" : "";
    private static final String RED = COLOR_ON ? "\u001B[31m" : "";
    private static final String GREEN = COLOR_ON ? "\u001B[32m" : "";
    private static final String SEP = "─";

    public SchedulerUI() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main entry point when called from menu.
     * Loads network if not already loaded.
     */
    @Override
    public void run() {
        // Lazy load: only load network when first entering scheduler
        if (network == null) {
            if (!loadNetwork()) {
                return; // Failed to load, return to main menu
            }
        }

        boolean exit = false;

        while (!exit) {
            printMainMenu();
            int option = readOption();

            switch (option) {
                case 1 -> scheduleSingleTrain();
                case 2 -> scheduleMultipleTrains();
                case 3 -> scheduleAllTrains();
                case 4 -> viewTrainDetails();
                case 5 -> defineTrainRoute();
                case 6 -> createNewPathInMemory();
                case 7 -> editTrainDeparture();
                case 8 -> reloadNetwork();
                case 0 -> exit = true;
                default -> System.out.println(RED + "❌ Invalid option!" + RESET);
            }
        }

        // Return to main menu (don't exit application)
        System.out.println(GREEN + "\n✓ Returning to main menu..." + RESET);
        MainMenu.menu();
    }

    /**
     * Loads the railway network from database.
     *
     * @return true if successful, false otherwise
     */
    private boolean loadNetwork() {
        System.out.println(CYAN + "\n⏳ Loading railway network from database..." + RESET);
        long startTime = System.currentTimeMillis();

        try {
            RailwayRepository repository = new RailwayRepository();
            network = RailwayNetwork.loadFromDatabase(repository);
            scheduler = new TrainScheduler(network);

            long duration = System.currentTimeMillis() - startTime;

            System.out.printf(GREEN + "✅ Network loaded successfully in %.2f seconds\n" + RESET,
                    duration / 1000.0);
            System.out.printf("   • %d endpoints\n", network.getEndpoints().size());
            System.out.printf("   • %d trains\n", network.getTrains().size());
            System.out.printf("   • %d paths\n", network.getPaths().size());

            return true;

        } catch (SQLException e) {
            System.err.println(RED + "❌ Failed to load network: " + e.getMessage() + RESET);
            System.out.println(RED + "⚠️  Returning to main menu..." + RESET);
            return false;
        }
    }

    /**
     * Reloads the network from database, discarding in-memory changes.
     */
    private void reloadNetwork() {
        System.out.println(RED + "\n⚠️  This will discard any in-memory changes (new paths, etc)." + RESET);
        System.out.print("➤ Are you sure? (y/n): ");

        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            network = null;
            scheduler = null;
            loadNetwork();
        } else {
            System.out.println(GREEN + "✓ Reload cancelled." + RESET);
        }
    }

    private void printMainMenu() {
        System.out.println("\n" + CYAN + line(72));
        System.out.println("                    TRAIN SCHEDULER - USLP07");
        System.out.println(line(72) + RESET);
        System.out.println("║ 1. Schedule a single train                                    ║");
        System.out.println("║ 2. Schedule multiple trains (manual selection)                ║");
        System.out.println("║ 3. Schedule ALL trains                                        ║");
        System.out.println("║ 4. View train details                                         ║");
        System.out.println("║ 5. Define/change train route (Path)                           ║");
        System.out.println("║ 6. Create new path                                            ║");
        System.out.println("║ 7. Edit train departure date & time                           ║");
        System.out.println("║ 8. Reload network from database                               ║");
        System.out.println("║ 0. " + RED + "Return to main menu" + RESET + "                                       ║");
        System.out.println(CYAN + line(72) + RESET);
        System.out.print("➤ Select option: ");
    }

    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }

    private void scheduleSingleTrain() {
        try {
            // 1. Get all trains from the network
            List<Train> trains = network.getTrains();

            if (trains.isEmpty()) {
                System.out.println("❌ No trains in network!");
                return;
            }

            // 2. Show list of trains
            System.out.println("\n📋 Available trains:");
            for (int i = 0; i < trains.size(); i++) {
                Train t = trains.get(i);
                System.out.printf(
                        "%d. Train %d: %s → %s (Departure: %s on %s)\n",
                        i + 1,
                        t.getId(),
                        t.getStartEndpoint().getName(),
                        t.getEndEndpoint().getName(),
                        t.getDepartureTime(),
                        t.getDate()
                );
            }

            // 3. Choose one
            System.out.print("\n➤ Select train number: ");
            int choice = readOption() - 1;

            if (choice < 0 || choice >= trains.size()) {
                System.out.println("❌ Invalid selection!");
                return;
            }

            Train selected = trains.get(choice);

            System.out.println("\n⏳ Calculating schedule for train " + selected.getId() + "...\n");

            // 4. Schedule (use TrainScheduler)
            TrainSchedule schedule = scheduler.scheduleTrainReschedule(selected);

            // 5. Show result
            System.out.println(schedule.getDetailedReport());

        } catch (SQLException e) {
            System.err.println("❌ Scheduler error: " + e.getMessage());
        }
    }


    private void scheduleMultipleTrains() {
        try {
            List<Train> allTrains = network.getTrains();

            if (allTrains.isEmpty()) {
                System.out.println("❌ No trains in network!");
                return;
            }

            // 1. Show all trains available
            System.out.println("\n📋 Available trains:");
            for (int i = 0; i < allTrains.size(); i++) {
                Train t = allTrains.get(i);
                System.out.printf(
                        "%d. Train %d: %s → %s (Departure: %s on %s)\n",
                        i + 1,
                        t.getId(),
                        t.getStartEndpoint().getName(),
                        t.getEndEndpoint().getName(),
                        t.getDepartureTime(),
                        t.getDate()
                );
            }

            // 2. Choose various (like "1", "2", "4")
            System.out.print("\n➤ Enter train numbers separated by comma (e.g., 1,2,3): ");
            String input = scanner.nextLine();
            String[] parts = input.split(",");

            List<Train> selectedTrains = new java.util.ArrayList<>();
            for (String part : parts) {
                try {
                    int index = Integer.parseInt(part.trim()) - 1;
                    if (index >= 0 && index < allTrains.size()) {
                        selectedTrains.add(allTrains.get(index));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("⚠️  Ignoring invalid number: " + part);
                }
            }

            if (selectedTrains.isEmpty()) {
                System.out.println("❌ No valid trains selected!");
                return;
            }

            System.out.println("\n⏳ Calculating schedule for " + selectedTrains.size() + " trains...\n");

            // 3. Schedule all
            Schedule schedule = scheduler.scheduleMultipleTrains(selectedTrains);

            // 4. Show general vision
            System.out.println(schedule);

            // 5. Optional: show details
            System.out.print("\n➤ Show detailed report for all trains? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                for (TrainSchedule ts : schedule.getTrainSchedules()) {
                    System.out.println("\n" + ts.getDetailedReport());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Scheduler error: " + e.getMessage());
        }
    }


    private void scheduleAllTrains() {
        try {
            List<Train> trains = network.getTrains();

            if (trains.isEmpty()) {
                System.out.println("❌ No trains in network!");
                return;
            }

            System.out.println("\n⏳ Calculating schedule for ALL trains in network (" +
                    trains.size() + " trains)...\n");

            Schedule schedule = scheduler.scheduleMultipleTrains(trains);

            System.out.println(schedule);

            System.out.print("\n➤ Show detailed report for all trains? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                for (TrainSchedule ts : schedule.getTrainSchedules()) {
                    System.out.println("\n" + ts.getDetailedReport());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Scheduler error: " + e.getMessage());
        }
    }


    private void viewTrainDetails() {
        try {
            List<Train> trains = network.getTrains();

            if (trains.isEmpty()) {
                System.out.println("❌ No trains in network!");
                return;
            }

            System.out.println("\n📋 Available trains:");
            for (int i = 0; i < trains.size(); i++) {
                Train t = trains.get(i);
                System.out.printf("%d. Train %d\n", i + 1, t.getId());
            }

            System.out.print("\n➤ Select train number: ");
            int choice = readOption() - 1;

            if (choice < 0 || choice >= trains.size()) {
                System.out.println("❌ Invalid selection!");
                return;
            }

            Train train = trains.get(choice);

            System.out.println("\n════════════════════════════════════════════");
            System.out.println("Train " + train.getId());
            System.out.println("Operator: " + train.getOperatorVat());
            System.out.println("Date: " + train.getDate());
            System.out.println("Departure: " + train.getDepartureTime());
            System.out.println("Route: " + train.getStartEndpoint().getName() +
                    " → " + train.getEndEndpoint().getName());

            System.out.println("\nLocomotives:");
            train.getLocomotives().forEach(l -> System.out.println("  • " + l));

            System.out.println("\nFreights:");
            train.getFreights().forEach(f -> System.out.println("  • " + f));

            System.out.println("\nPath: " + train.getPath().getName());
            train.getPath().getOrderedEndpoints()
                    .forEach(e -> System.out.println("  " + e));

        } catch (Exception e) {
            // From the get go, there should not be a SQLException here. But i leave a generic format
            System.err.println("❌ Error while showing train details: " + e.getMessage());
        }
    }


    /**
     * Allows the Freight Manager to define/change the route (Path) of a Train,
     * WITHOUT changing the database. Everything is done in memory in the RailwayNetwork.
     */
    private void defineTrainRoute() {
        // 1. Go get the trains from the network's memory
        List<Train> trains = network.getTrains();

        if (trains.isEmpty()) {
            System.out.println("❌ No trains in network!");
            return;
        }

        // 2. Show the list of trains
        System.out.println("\n📋 Available trains:");
        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            String pathName = (t.getPath() != null) ? t.getPath().getName() : "<no path>";
            System.out.printf(
                    "%d. Train %d: %s → %s (Departure: %s on %s) [Path: %s]\n",
                    i + 1,
                    t.getId(),
                    t.getStartEndpoint().getName(),
                    t.getEndEndpoint().getName(),
                    t.getDepartureTime(),
                    t.getDate(),
                    pathName
            );
        }

        // 3. Escolher o comboio
        System.out.print("\n➤ Select train number (0 to cancel): ");
        int trainChoice = readOption() - 1;
        if (trainChoice == -1) {
            return; // user chose 0
        }
        if (trainChoice < 0 || trainChoice >= trains.size()) {
            System.out.println("❌ Invalid train selection!");
            return;
        }

        Train train = trains.get(trainChoice);

        // 4. Show available paths (including the new ones created in memory)
        List<Path> paths = network.getPaths();
        if (paths.isEmpty()) {
            System.out.println("❌ No paths in network! Create one first (option 6).");
            return;
        }

        System.out.println("\n📋 Available paths:");
        for (Path p : paths) {
            int endpointCount = p.getOrderedEndpoints().size();
            System.out.printf("  %d. %s (%d endpoints)\n",
                    p.getId(), p.getName(), endpointCount);
        }

        // 5. Pick a path
        System.out.print("\n➤ Enter path ID to assign (0 to cancel): ");
        int pathId = readOption();
        if (pathId == 0) {
            return;
        }

        Path selectedPath = network.getPathById(pathId);
        if (selectedPath == null) {
            System.out.println("❌ Invalid path ID!");
            return;
        }

        if (selectedPath.getOrderedEndpoints().isEmpty()) {
            System.out.println("❌ Selected path has no endpoints defined!");
            return;
        }

        // 6. Attribute path to the train in the memory
        try {
            network.assignPathToTrain(train, selectedPath);
            System.out.printf("✅ Train %d route updated IN MEMORY to path '%s'.\n",
                    train.getId(), selectedPath.getName());
            System.out.println("   New start: " + train.getStartEndpoint().getName());
            System.out.println("   New end:   " + train.getEndEndpoint().getName());

        } catch (IllegalArgumentException e) {
            System.out.println(RED + "❌ VALIDATION FAILED: " + e.getMessage() + RESET);
            System.out.println("   Train route NOT changed.");

            // Suggestions of action
            if (e.getMessage().contains("GAUGE")) {
                System.out.println("\n💡 Possible solutions:");
                System.out.println("   • Add locomotives supporting gauge " );
                System.out.println("   • Choose a different path");
            }
        }
    }


    private void createNewPathInMemory() {
        System.out.print("\n➤ Enter path name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("❌ Path name cannot be empty!");
            return;
        }

        Path path = network.createInMemoryPath(name);
        System.out.printf("✅ Path '%s' created with id %d (in memory).\n",
                path.getName(), path.getId());

        var endpoints = network.getEndpoints();
        if (endpoints.isEmpty()) {
            System.out.println("❌ No endpoints in network!");
            return;
        }

        System.out.println("\nAvailable endpoints:");
        endpoints.forEach(e ->
                System.out.printf("  %d. %s\n", e.getId(), e.getName())
        );

        System.out.println("\nNow define the route (sequence of endpoints).");
        System.out.println("Rules:");
        System.out.println("  • The first endpoint can be any.");
        System.out.println("  • Every NEXT endpoint must be connected to the previous one by a line.");
        System.out.println("Enter endpoint IDs in order. Enter 0 to finish.");

        while (true) {
            System.out.printf("➤ Endpoint ID for next position (0 to finish): ");
            int endpointId = readOption();
            if (endpointId == 0) break;

            Endpoint endpoint = network.getEndpointById(endpointId);
            if (endpoint == null) {
                System.out.println("❌ Invalid endpoint ID!");
                continue;
            }

            // If there is endpoints on the path, validate the connection to the last one
            var ordered = path.getOrderedEndpoints();
            if (!ordered.isEmpty()) {
                Endpoint lastEndpoint = ordered.get(ordered.size() - 1).getEndpoint();

                // ⚠️ Here is where we stop impossible paths:
                // we use the RailwayNetwork to check if there is a segment between lastEndpoint and endpoint
                LineSegment segment = network.findSegmentBetween(lastEndpoint.getId(), endpoint.getId());

                if (segment == null) {
                    System.out.printf(
                            "❌ There is NO line connecting '%s' and '%s'. Path step rejected.\n",
                            lastEndpoint.getName(), endpoint.getName()
                    );
                    System.out.println("   Please choose another endpoint that is directly connected.");
                    // You can, optionally, suggest here connected endpoints (see section 2)
                    continue;
                }
            }

            System.out.print("  Must stop here? (y/n): ");
            boolean mustStop = scanner.nextLine().trim().equalsIgnoreCase("y");

            network.addEndpointToPath(path, endpoint, mustStop);
            System.out.printf("   ➕ Added '%s' to path '%s'.\n",
                    endpoint.getName(), path.getName());
        }

        int size = path.getOrderedEndpoints().size();
        if (size < 2) {
            System.out.printf("⚠️ Path '%s' has only %d endpoint(s). It may not be useful.\n",
                    path.getName(), size);
        } else {
            System.out.printf("✅ Path '%s' defined with %d endpoint(s).\n",
                    path.getName(), size);
        }
    }


    private void defineTrainRouteInMemory() {
        List<Train> trains = network.getTrains();
        if (trains.isEmpty()) {
            System.out.println("❌ No trains in network!");
            return;
        }

        System.out.println("\nAvailable trains:");
        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            System.out.printf("%d. Train %d: %s → %s (Departure: %s on %s) [Path: %s]\n",
                    i + 1,
                    t.getId(),
                    t.getStartEndpoint().getName(),
                    t.getEndEndpoint().getName(),
                    t.getDepartureTime(),
                    t.getDate(),
                    t.getPath().getName());
        }

        System.out.print("\n➤ Select train number (0 to cancel): ");
        int trainChoice = readOption() - 1;
        if (trainChoice == -1) return;
        if (trainChoice < 0 || trainChoice >= trains.size()) {
            System.out.println("❌ Invalid selected train!");
            return;
        }

        Train train = trains.get(trainChoice);

        // Available paths (including new ones in memory)
        var paths = network.getPaths();
        if (paths.isEmpty()) {
            System.out.println("❌ No paths in network! Create one first (option 6).");
            return;
        }

        System.out.println("\nAvailable paths:");
        for (Path p : paths) {
            System.out.printf("  %d. %s (%d endpoints)\n",
                    p.getId(),
                    p.getName(),
                    p.getOrderedEndpoints().size());
        }

        System.out.print("\n➤ Enter path ID to assign (0 to cancel): ");
        int pathId = readOption();
        if (pathId == 0) return;

        Path selectedPath = network.getPathById(pathId);
        if (selectedPath == null) {
            System.out.println("❌ Invalid path ID!");
            return;
        }

        if (selectedPath.getOrderedEndpoints().isEmpty()) {
            System.out.println("❌ Selected path has no endpoints!");
            return;
        }

        network.assignPathToTrain(train, selectedPath);

        System.out.printf("✅ Train %d route updated IN MEMORY to path '%s'.\n",
                train.getId(), selectedPath.getName());
        System.out.println("   New start: " + train.getStartEndpoint().getName());
        System.out.println("   New end:   " + train.getEndEndpoint().getName());
    }

    /**
     * Allows to alter the date and time of departure of a train in memory.
     * Useful for artificial scenarios (i.e. crossings on siding)
     * without touching the database.
     */
    private void editTrainDeparture() {
        List<Train> trains = network.getTrains();

        if (trains.isEmpty()) {
            System.out.println("❌ No trains in network!");
            return;
        }

        System.out.println("\n📋 Available trains:");
        for (int i = 0; i < trains.size(); i++) {
            Train t = trains.get(i);
            System.out.printf(
                    "%d. Train %d: %s → %s (Departure: %s on %s)\n",
                    i + 1,
                    t.getId(),
                    t.getStartEndpoint().getName(),
                    t.getEndEndpoint().getName(),
                    t.getDepartureTime(),
                    t.getDate()
            );
        }

        System.out.print("\n➤ Select train number (0 to cancel): ");
        int choice = readOption() - 1;
        if (choice == -1) {
            return; // cancel
        }
        if (choice < 0 || choice >= trains.size()) {
            System.out.println("❌ Invalid train selection!");
            return;
        }

        Train train = trains.get(choice);

        System.out.println("\nCurrent values:");
        System.out.println("  Date: " + train.getDate());
        System.out.println("  Time: " + train.getDepartureTime());

        // Ler nova data
        LocalDate newDate = train.getDate();
        LocalTime newTime = train.getDepartureTime();

        System.out.print("\n➤ Enter new date (YYYY-MM-DD) or leave blank to keep [" +
                train.getDate() + "]: ");
        String dateInput = scanner.nextLine().trim();
        if (!dateInput.isEmpty()) {
            try {
                newDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Keeping previous date.");
            }
        }

        System.out.print("➤ Enter new time (HH:MM) or leave blank to keep [" +
                train.getDepartureTime() + "]: ");
        String timeInput = scanner.nextLine().trim();
        if (!timeInput.isEmpty()) {
            try {
                newTime = LocalTime.parse(timeInput);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid time format. Keeping previous time.");
            }
        }

        train.setDate(newDate);
        train.setDepartureTime(newTime);

        System.out.println("\n✅ Train " + train.getId() + " updated IN MEMORY:");
        System.out.println("   New date: " + train.getDate());
        System.out.println("   New time: " + train.getDepartureTime());
    }
}