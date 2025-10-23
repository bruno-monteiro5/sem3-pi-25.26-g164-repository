package Classes;

import java.util.Scanner;

public class Utils {

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

}