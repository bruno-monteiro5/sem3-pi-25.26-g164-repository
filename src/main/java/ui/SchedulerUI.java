package ui;

import domain.railway.*;
import domain.scheduling.Schedule;
import domain.scheduling.TrainSchedule;
import repository.RailwayRepository;
import service.TrainScheduler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Interface de utilizador text-based para o Train Scheduler
 * USLP07 - As Freight Manager, I want to use a scheduler
 */
public class SchedulerUI {

    private final RailwayNetwork network;
    private final TrainScheduler scheduler;
    private final Scanner scanner;

    public SchedulerUI(RailwayNetwork network) {
        this.network = network;
        this.scheduler = new TrainScheduler(network);
        this.scanner = new Scanner(System.in);
    }


    public void run() {
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
                case 0 -> exit = true;
                default -> System.out.println("❌ Invalid option!");
            }



        }

        System.out.println("\n👋 Goodbye!");
    }

    private void printMainMenu() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("║" + " ".repeat(20) + "TRAIN SCHEDULER - USLP07" + " ".repeat(25) + "║");
        System.out.println("═".repeat(70));
        System.out.println("║ 1. Schedule a single train                                      ║");
        System.out.println("║ 2. Schedule multiple trains (manual selection)                  ║");
        System.out.println("║ 3. Schedule ALL trains                                          ║");
        System.out.println("║ 4. View train details                                           ║");
        System.out.println("║ 5. Define/change train route (Path)                             ║");
        System.out.println("║ 6. Create new path                                              ║");
        System.out.println("║ 7. Edit train departure date & time                             ║");
        System.out.println("║ 0. Exit                                                         ║");
        System.out.println("═".repeat(70));
        System.out.print("➤ Select option: ");
    }




    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void scheduleSingleTrain() {
        try {
            // 1. Buscar todos os comboios da rede em memória
            List<Train> trains = network.getTrains();

            if (trains.isEmpty()) {
                System.out.println("❌ No trains in network!");
                return;
            }

            // 2. Mostrar lista de comboios
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

            // 3. Escolher um
            System.out.print("\n➤ Select train number: ");
            int choice = readOption() - 1;

            if (choice < 0 || choice >= trains.size()) {
                System.out.println("❌ Invalid selection!");
                return;
            }

            Train selected = trains.get(choice);

            System.out.println("\n⏳ Calculating schedule for train " + selected.getId() + "...\n");

            // 4. Agendar (usa o TrainScheduler)
            TrainSchedule schedule = scheduler.scheduleTrainReschedule(selected);

            // 5. Mostrar resultado
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

            // 1. Mostrar todos os comboios disponíveis
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

            // 2. Escolher vários (tipo "1,2,4")
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

            // 3. Agendar todos
            Schedule schedule = scheduler.scheduleMultipleTrains(selectedTrains);

            // 4. Mostrar visão geral
            System.out.println(schedule);

            // 5. Opcional: mostrar detalhes
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
            // Aqui em princípio já não deves ter SQLException, mas deixo genérico
            System.err.println("❌ Error while showing train details: " + e.getMessage());
        }
    }


    /**
     * Permite ao Freight Manager definir/alterar a rota (Path) de um Train,
     * SEM alterar a base de dados. Tudo é feito apenas em memória na RailwayNetwork.
     */
    private void defineTrainRoute() {
        // 1. Ir buscar os trains à rede em memória
        List<Train> trains = network.getTrains();

        if (trains.isEmpty()) {
            System.out.println("❌ No trains in network!");
            return;
        }

        // 2. Mostrar lista de comboios
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
            return; // user escolheu 0
        }
        if (trainChoice < 0 || trainChoice >= trains.size()) {
            System.out.println("❌ Invalid train selection!");
            return;
        }

        Train train = trains.get(trainChoice);

        // 4. Mostrar paths disponíveis (inclui os criados em memória)
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

        // 5. Escolher path
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

        // 6. Atribuir path ao train em memória
        network.assignPathToTrain(train, selectedPath);

        System.out.printf("✅ Train %d route updated IN MEMORY to path '%s'.\n",
                train.getId(), selectedPath.getName());
        System.out.println("   New start: " + train.getStartEndpoint().getName());
        System.out.println("   New end:   " + train.getEndEndpoint().getName());
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

            // Se já houver endpoints no path, validar ligação com o último
            var ordered = path.getOrderedEndpoints();
            if (!ordered.isEmpty()) {
                Endpoint lastEndpoint = ordered.get(ordered.size() - 1).getEndpoint();

                // ⚠️ Aqui é onde impedimos caminhos impossíveis:
                // usamos a RailwayNetwork para ver se há segmento entre lastEndpoint e endpoint
                LineSegment segment = network.findSegmentBetween(lastEndpoint.getId(), endpoint.getId());

                if (segment == null) {
                    System.out.printf(
                            "❌ There is NO line connecting '%s' and '%s'. Path step rejected.\n",
                            lastEndpoint.getName(), endpoint.getName()
                    );
                    System.out.println("   Please choose another endpoint that is directly connected.");
                    // Podes opcionalmente aqui sugerir endpoints ligados (ver secção 2)
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

        // Paths disponíveis (incluindo novos em memória)
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
     * Permite alterar a data e a hora de partida de um train em memória.
     * Útil para criar cenários artificiais (ex.: cruzamentos em sidings)
     * sem tocar na base de dados.
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



    public static void main(String[] args) {
        try {
            RailwayRepository repository = new RailwayRepository();
            RailwayNetwork network = RailwayNetwork.loadFromDatabase(repository);

            SchedulerUI ui = new SchedulerUI(network);
            ui.run();
        } catch (SQLException e) {
            System.err.println("❌ Failed to load railway data: " + e.getMessage());
        }
    }

}