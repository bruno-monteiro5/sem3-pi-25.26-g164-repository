package ui.InfrastructureIndex;

import controller.Search2DTreeController;

import java.util.Scanner;

public class Search2DTreeUI implements Runnable {
    public Search2DTreeController treeController;

    @Override
    public void run(){
        String search = "";
        this.treeController = new Search2DTreeController();
        Search2DTreeController tree = new Search2DTreeController();
        System.out.println("Please select a minimum latitude");
        Scanner sc = new Scanner(System.in);
        double minLat = sc.nextDouble();
        while(minLat<-90 || minLat>90) {
            System.out.println("Invalid latitude. Please try again.");
            minLat = sc.nextDouble();
        }
        System.out.println("Please select a maximum latitude");
        double maxLat = sc.nextDouble();
        while(maxLat<-90 || maxLat>90 && maxLat<minLat) {
            System.out.println("Invalid latitude. Please try again");
            maxLat = sc.nextDouble();
        }
        System.out.println("Please select a minimum longitude");
        double minLon = sc.nextDouble();
        while(minLon<-180 || minLon>180) {
            System.out.println("Invalid longitude. Please try again");
            minLon = sc.nextDouble();
        }
        System.out.println("Please select a maximum longitude");
        double maxLon = sc.nextDouble();
        while(maxLon<-180 || maxLon>180 && maxLon<minLon) {
            System.out.println("Invalid longitude. Please try again");
            maxLon = sc.nextDouble();
        }
        System.out.println("Do you wish to add additional filters? (y/n)");
        String awnser = sc.nextLine().trim().toLowerCase();
        while(!awnser.equals("y") && !awnser.equals("n")) {
            System.out.println("Invalid input. Please enter 'y' or 'n'");
            awnser = sc.nextLine().trim().toLowerCase();
        }
        if(awnser.equals("y")) {
            System.out.println("Should the station be in a city?(y/n/a)");
            String city = sc.nextLine().trim().toLowerCase();
            int isCity = -1;
            while(!city.equals("y") && !city.equals("n") && !city.equals("a")) {
                System.out.println("Invalid input. Please enter 'y' or 'n'");
                city = sc.nextLine().trim().toLowerCase();
            }
            if(city.equals("y")){
                isCity = 0;
            }else if(city.equals("n")){
                isCity = 1;
            }else{
                isCity = 2;
            }
            System.out.println("Should it be a main station?(y/n/a)");
            city = sc.nextLine().trim().toLowerCase();
            int isMain = -1;
            while (!city.equals("y") && !city.equals("n") && !city.equals("a") ) {
                System.out.println("Invalid input. Please enter 'y' or 'n' or 'a'");
                city = sc.nextLine().trim().toLowerCase();
            }
            if(city.equals("y")){
                isMain = 0;
            }else if(city.equals("n")){
                isMain = 1;
            }else {
                isMain = 2;
            }
            System.out.println("Which country should it be in?(PT/ES/all)");
            String country = sc.nextLine().toLowerCase();
            while(!country.equals("pt") && !country.equals("es") && !country.equals("all")) {
                System.out.println("Invalid input. Please enter 'PT', 'ES' or 'all'");
                country = sc.nextLine().toLowerCase();
            }
            search = treeController.searchInArea(minLat, maxLat, minLon, maxLon, isCity, isMain, country);


        }else {
            search = treeController.searchInArea(minLat, maxLat, minLon, maxLon);
        }
        BuildIndexesUI run = new BuildIndexesUI();
        run.run();
    }
}
