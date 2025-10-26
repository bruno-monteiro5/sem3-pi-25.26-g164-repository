package domain;

import java.util.Scanner;
import java.util.Stack;

public class Utils {

    private static final Stack<Runnable> runnableStack = new Stack<>();

    public static void listAndSelect(Option[] options) {
        Scanner sc = new Scanner(System.in);
        int choice = -1;

        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + "-" + options[i].toString());
        }

        while (choice < 0 || choice >= options.length) {
            System.out.println("Enter your choice:");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and " + options.length);
                sc.next();
                continue;
            }

            choice = sc.nextInt() - 1;
            if (choice < 0 || choice >= options.length) {
                System.out.println("Invalid option. Please select a number between 1 and " + options.length);
            }
        }

        options[choice].executeOption();
    }

    public static Runnable pushRunnable(Runnable runnable) {
        return () -> {
            Utils.pushRunnable(runnable);
            runnable.run();
        };
    }

    public static void finishCurrentAndRunPrevious() {
        if (!runnableStack.isEmpty()) {
            runnableStack.pop(); // Remove o atual
        }
        if (!runnableStack.isEmpty()) {
            runnableStack.peek().run(); // Executa o anterior, se existir
        } else {
            System.out.println("Não há processos restantes na pilha.");
        }
    }

    public static void start(Runnable initialRunnable) {
        runnableStack.push(initialRunnable);
        initialRunnable.run();
    }


}