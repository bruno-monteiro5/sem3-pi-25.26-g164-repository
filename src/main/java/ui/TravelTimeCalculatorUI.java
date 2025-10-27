package ui;

import controller.TravelTimeController;
import domain.railway.*;
import domain.Utils;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class TravelTimeCalculatorUI implements Runnable {
    private final TravelTimeController controller;
    private final Scanner scanner;

    public TravelTimeCalculatorUI() {
        this.controller = new TravelTimeController();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  TRAVEL TIME CALCULATOR (USLP03)");
            System.out.println("=".repeat(80) + "\n");

            // 1. Selecionar linha
            Line selectedLine = selectLine();
            if (selectedLine == null) {
                returnToMainMenu();
                return;
            }

            // 2. Selecionar locomotiva
            Locomotive selectedLocomotive = selectLocomotive();
            if (selectedLocomotive == null) {
                returnToMainMenu();
                return;
            }

            // 3. Calcular e apresentar resultados
            calculateAndDisplay(selectedLine, selectedLocomotive);

        } catch (SQLException e) {
            System.err.println("\n[ERROR] Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n[ERROR] " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
        returnToMainMenu();
    }

    private Line selectLine() throws SQLException {
        List<Line> lines = controller.getAvailableLines();

        if (lines.isEmpty()) {
            System.out.println("No lines available in the database.");
            return null;
        }

        System.out.println("═══ AVAILABLE RAILWAY LINES ═══\n");
        for (int i = 0; i < lines.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, lines.get(i));
        }
        System.out.println("0. Cancel\n");

        int choice = getValidChoice(0, lines.size());
        return (choice == 0) ? null : lines.get(choice - 1);
    }

    private Locomotive selectLocomotive() throws SQLException {
        List<Locomotive> locomotives = controller.getAvailableLocomotives();

        if (locomotives.isEmpty()) {
            System.out.println("No locomotives available in the database.");
            return null;
        }

        System.out.println("\n═══ AVAILABLE LOCOMOTIVES ═══\n");
        for (int i = 0; i < locomotives.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, locomotives.get(i));
        }
        System.out.println("0. Cancel\n");

        int choice = getValidChoice(0, locomotives.size());
        return (choice == 0) ? null : locomotives.get(choice - 1);
    }

    private void calculateAndDisplay(Line line, Locomotive locomotive) {
        try {
            TravelTimeResult result = controller.calculateTravelTime(line, locomotive);

            System.out.println("\n" + "═".repeat(80));
            System.out.println("  TRAVEL TIME CALCULATION RESULTS");
            System.out.println("═".repeat(80));

            System.out.printf("\nRoute: %s → %s\n",
                    line.getEndpointAName(), line.getEndpointBName());
            System.out.printf("Line: %s\n", line.getName());
            System.out.printf("Locomotive: %s\n\n", locomotive.getName());

            System.out.println("─".repeat(80));
            System.out.printf("%-10s | %-12s | %-15s | %-12s | %-10s\n",
                    "Segment", "Length (m)", "Speed Limit", "Used Speed", "Time (min)");
            System.out.println("─".repeat(80));

            for (TravelTimeResult.SegmentTime st : result.getSegmentTimes()) {
                System.out.printf("%-10d | %,12d | %12d km/h | %9d km/h | %10.2f\n",
                        st.getSegment().getId(),
                        st.getSegment().getLength(),
                        st.getSegment().getSpeedLimit(),
                        st.getSpeedUsed(),
                        st.getTimeMinutes()
                );
            }

            System.out.println("─".repeat(80));
            System.out.printf("\n✓ Total Distance: %.2f km\n", result.getTotalDistanceKm());
            System.out.printf("✓ Total Time: %.2f minutes (%.2f hours)\n",
                    result.getTotalTimeMinutes(), result.getTotalTimeMinutes() / 60);
            System.out.println("\n" + "═".repeat(80));

        } catch (IllegalArgumentException e) {
            System.err.println("\n[VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("\n[DATABASE ERROR] " + e.getMessage());
        }
    }

    private int getValidChoice(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            System.out.printf("Enter your choice (%d-%d): ", min, max);
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                if (choice < min || choice > max) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // clear invalid input
            }
        }
        return choice;
    }

    private void returnToMainMenu() {
        new MainMenu().run();
    }
}