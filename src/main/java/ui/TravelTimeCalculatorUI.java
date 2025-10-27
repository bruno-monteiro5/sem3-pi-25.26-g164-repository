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

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    public TravelTimeCalculatorUI() {
        this.controller = new TravelTimeController();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            System.out.println();
            System.out.println(CYAN + line(80));
            System.out.println(center("TRAVEL TIME CALCULATOR (USLP03)", 80));
            System.out.println(line(80) + RESET);

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
            System.err.println(RED + "\n[DATABASE ERROR] " + e.getMessage() + RESET);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(RED + "\n[ERROR] " + e.getMessage() + RESET);
        }

        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
        returnToMainMenu();
    }

    private Line selectLine() throws SQLException {
        List<Line> lines = controller.getAvailableLines();

        if (lines.isEmpty()) {
            System.out.println(RED + "No lines available in the database." + RESET);
            return null;
        }

        System.out.println(CYAN + "\nAVAILABLE RAILWAY LINES" + RESET);
        System.out.println(line(80));
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
            System.out.println(RED + "No locomotives available in the database." + RESET);
            return null;
        }

        System.out.println(CYAN + "\nAVAILABLE LOCOMOTIVES" + RESET);
        System.out.println(line(80));
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

            System.out.println();
            System.out.println(CYAN + line(80));
            System.out.println(center("TRAVEL TIME CALCULATION RESULTS", 80));
            System.out.println(line(80) + RESET);

            System.out.printf("\nRoute: %s → %s\n",
                    line.getEndpointAName(), line.getEndpointBName());
            System.out.printf("Line: %s\n", line.getName());
            System.out.printf("Locomotive: %s\n\n", locomotive.getName());

            System.out.println(line(80));
            System.out.printf("%-10s | %-12s | %-15s | %-12s | %-16s\n",
                    "Segment", "Length (m)", "Speed Limit", "Used Speed", "Time (hh:mm:ss)");
            System.out.println(line(80));

            for (TravelTimeResult.SegmentTime st : result.getSegmentTimes()) {
                String segHms = formatHMS(st.getTimeMinutes());
                System.out.printf("%-10d | %,12d | %12d km/h | %9d km/h | %16s\n",
                        st.getSegment().getId(),
                        st.getSegment().getLength(),
                        st.getSegment().getSpeedLimit(),
                        st.getSpeedUsed(),
                        segHms
                );
            }

            System.out.println(line(80));
            System.out.printf("\n" + GREEN + "✓ Total Distance: %.2f km" + RESET + "%n", result.getTotalDistanceKm());
            String totalHms = formatHMS(result.getTotalTimeMinutes());
            System.out.printf(GREEN + "✓ Total Time: %s (%.2f min)" + RESET + "%n",
                    totalHms, result.getTotalTimeMinutes());

            System.out.println("\n" + line(80));

        } catch (IllegalArgumentException e) {
            System.err.println(RED + "\n[VALIDATION ERROR] " + e.getMessage() + RESET);
        } catch (SQLException e) {
            System.err.println(RED + "\n[DATABASE ERROR] " + e.getMessage() + RESET);
        }
    }

    private int getValidChoice(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            System.out.printf("Enter your choice (%d-%d): ", min, max);
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
                if (choice < min || choice > max) {
                    System.out.println(RED + "Invalid choice. Please try again." + RESET);
                }
            } else {
                System.out.println(RED + "Invalid input. Please enter a number." + RESET);
                scanner.nextLine();
            }
        }
        return choice;
    }

    private static String formatHMS(double minutes) {
        long totalSeconds = Math.round(minutes * 60.0);
        long hours = totalSeconds / 3600;
        long minutesPart = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutesPart, seconds);
    }

    private void returnToMainMenu() {
        new MainMenu().run();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < pad; i++) left.append(' ');
        return left + s;
    }
}