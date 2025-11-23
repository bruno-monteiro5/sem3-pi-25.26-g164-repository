package ui.InfrastructureIndex;

import controller.Search2DTreeController;

import java.util.Scanner;

public class Search2DTreeUI implements Runnable {
    public Search2DTreeController treeController;

    @Override
    public void run(){
        String search = "";
        this.treeController = new Search2DTreeController();
        Scanner sc = new Scanner(System.in);

// Get latitude range – MIN
        double minLat = 0.0;
        while (true) {
            System.out.print("Minimum latitude (-90 to 90): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Latitude is required. Please enter a number.");
                continue;
            }

            try {
                minLat = Double.parseDouble(text);
                if (minLat < -90.0 || minLat > 90.0) {
                    System.out.println("Latitude must be between -90 and 90 degrees. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid decimal value.");
            }
        }

// Get latitude range – MAX
        double maxLat = 0.0;
        while (true) {
            System.out.print("Maximum latitude (-90 to 90, >= minimum latitude): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Latitude is required. Please enter a number.");
                continue;
            }

            try {
                maxLat = Double.parseDouble(text);
                if (maxLat < -90.0 || maxLat > 90.0) {
                    System.out.println("Latitude must be between -90 and 90 degrees. Please try again.");
                } else if (maxLat < minLat) {
                    System.out.println("Maximum latitude must be greater or equal to minimum latitude. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid decimal value.");
            }
        }

// Get longitude range – MIN
        double minLon = 0.0;
        while (true) {
            System.out.print("Minimum longitude (-180 to 180): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Longitude is required. Please enter a number.");
                continue;
            }

            try {
                minLon = Double.parseDouble(text);
                if (minLon < -180.0 || minLon > 180.0) {
                    System.out.println("Longitude must be between -180 and 180 degrees. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid decimal value.");
            }
        }

// Get longitude range – MAX
        double maxLon = 0.0;
        while (true) {
            System.out.print("Maximum longitude (-180 to 180, >= minimum longitude): ");
            String text = sc.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println("Longitude is required. Please enter a number.");
                continue;
            }

            try {
                maxLon = Double.parseDouble(text);
                if (maxLon < -180.0 || maxLon > 180.0) {
                    System.out.println("Longitude must be between -180 and 180 degrees. Please try again.");
                } else if (maxLon < minLon) {
                    System.out.println("Maximum longitude must be greater or equal to minimum longitude. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid decimal value.");
            }
        }


        // Ask about additional filters
        System.out.println("Do you wish to add additional filters? (y/n)");
        String answer = sc.nextLine().trim().toLowerCase();
        while(!answer.equals("y") && !answer.equals("n")) {
            System.out.println("Invalid input. Please enter 'y' or 'n':");
            answer = sc.nextLine().trim().toLowerCase();
        }

        if(answer.equals("y")) {
            // Filter: isCity
            System.out.println("Should the station be in a city? (y/n/a for any)");
            String cityInput = sc.nextLine().trim().toLowerCase();
            while(!cityInput.equals("y") && !cityInput.equals("n") && !cityInput.equals("a")) {
                System.out.println("Invalid input. Please enter 'y', 'n', or 'a':");
                cityInput = sc.nextLine().trim().toLowerCase();
            }

            int isCity;
            if(cityInput.equals("y")){
                isCity = 1; // Filter for city = true
            } else if(cityInput.equals("n")){
                isCity = 0; // Filter for city = false
            } else {
                isCity = 2; // No filter (any)
            }

            // Filter: isMainStation
            System.out.println("Should it be a main station? (y/n/a for any)");
            String mainInput = sc.nextLine().trim().toLowerCase();
            while (!mainInput.equals("y") && !mainInput.equals("n") && !mainInput.equals("a")) {
                System.out.println("Invalid input. Please enter 'y', 'n', or 'a':");
                mainInput = sc.nextLine().trim().toLowerCase();
            }

            int isMain;
            if(mainInput.equals("y")){
                isMain = 1; // Filter for main = true
            } else if(mainInput.equals("n")){
                isMain = 0; // Filter for main = false
            } else {
                isMain = 2; // No filter (any)
            }

            // Filter: country
            System.out.println("Which country should it be in? (PT/ES/all)");
            String country = sc.nextLine().trim().toLowerCase();
            while(!country.equals("pt") && !country.equals("es") && !country.equals("all")) {
                System.out.println("Invalid input. Please enter 'PT', 'ES', or 'all':");
                country = sc.nextLine().trim().toLowerCase();
            }

            // Execute search with filters
            search = treeController.searchInArea(minLat, maxLat, minLon, maxLon, isCity, isMain, country);

        } else {
            // Execute search without filters
            search = treeController.searchInArea(minLat, maxLat, minLon, maxLon);
        }

        // Display results
        if(search.isEmpty()) {
            System.out.println("\nNo stations found matching the criteria.");
        } else {
            System.out.println("\n=== Search Results ===");
            System.out.println(search);
        }

        sc.close();

        BuildIndexesUI run = new BuildIndexesUI();
        run.run();
    }

    /**
     * Sample queries for testing:
     *
     * Query 1: All stations in Portugal mainland
     *   minLat: 37.0, maxLat: 42.0, minLon: -9.5, maxLon: -6.0
     *   No filters
     *
     * Query 2: Main stations in city areas in Portugal
     *   minLat: 38.0, maxLat: 41.5, minLon: -9.0, maxLon: -7.0
     *   isCity: y, isMainStation: y, country: pt
     *
     * Query 3: Non-city stations in Spain (Barcelona area)
     *   minLat: 41.0, maxLat: 42.0, minLon: 1.5, maxLon: 2.5
     *   isCity: n, isMainStation: a, country: es
     *
     * Query 4: All main stations in Iberian Peninsula
     *   minLat: 36.0, maxLat: 43.0, minLon: -10.0, maxLon: 4.0
     *   isCity: a, isMainStation: y, country: all
     *
     * Query 5: City stations in Lisbon metropolitan area
     *   minLat: 38.6, maxLat: 39.0, minLon: -9.3, maxLon: -9.0
     *   isCity: y, isMainStation: a, country: pt
     *
     * Temporal Complexity Analysis:
     * - Without pruning: O(n) - visits all nodes in tree
     * - With KD-Tree pruning: O(√n + k) average case
     *   where n = total stations, k = number of results
     * - Worst case (region covers entire space): O(n)
     * - Best case (small region, few matches): O(log n + k)
     */
}