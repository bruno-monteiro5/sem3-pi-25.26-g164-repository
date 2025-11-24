package domain;

import java.util.Scanner;
import java.util.Stack;
import ui.UIHelper;

public class Utils {

    private static final Stack<Runnable> runnableStack = new Stack<>();

    /**
     * Shows a numbered list of options and executes the selected one.
     *
     * Format (Style F):
     *   → 1) Build indexes (load CSV)
     *   → 2) Latitude window query
     *   ...
     *   ➤ Select option:
     */
    public static void listAndSelect(Option[] options) {

        final String CYAN   = UIHelper.getCyan();
        final String RED    = UIHelper.getRed();
        final String RESET  = UIHelper.getReset();

        Scanner sc = new Scanner(System.in);
        int choice = -1;

        while (true) {

            // Print menu options (all except last)
            for (int i = 0; i < options.length; i++) {

                boolean isLast = (i == options.length - 1);

                // Last option → RED
                if (isLast) {
                    System.out.printf(" %s→%s %d) %s%s%s%n",
                            CYAN, RESET,
                            i + 1,
                            RED, options[i].toString(), RESET
                    );
                } else {
                    System.out.printf(" %s→%s %d) %s%n",
                            CYAN, RESET,
                            i + 1,
                            options[i].toString()
                    );
                }
            }

            // Unified prompt with cyan arrow
            System.out.print(CYAN + "➤ " + RESET + "Select option: ");
            String line = sc.nextLine().trim();

            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Please enter a valid number.");
                continue;
            }

            if (choice < 1 || choice > options.length) {
                System.out.println("Invalid option. Please select a number between 1 and " + options.length + ".");
                continue;
            }

            break;
        }

        Option selected = options[choice - 1];
        if (selected != null) {
            selected.executeOption();
        }
    }


    public static void start(Runnable initialRunnable) {
        runnableStack.push(initialRunnable);
        initialRunnable.run();
    }

}
