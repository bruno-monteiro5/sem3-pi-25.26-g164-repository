package controller;

import domain.KDNode;
import domain.KDTree2D;
import domain.Station;
import repository.KDTreeRepository;

public class Search2DTreeController {
    public KDTreeRepository kdTreeRepository = KDTreeRepository.getInstance();

    public Search2DTreeController() {
        this.kdTreeRepository = KDTreeRepository.getInstance();
    }

    /**
     * Returns the requested geographical search
     */
    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        KDTree2D kdTree = kdTreeRepository.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            return "2D-Tree not built yet. Please build it first.";
        }

        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, kdTree.getRoot(), s);
    }

    public String searchInArea(double minLatitude, double maxLatitude,
                               double minLongitude, double maxLongitude,
                               KDNode node, String s) {
        for(Station station : node.getStations()){
            if(station.getLocation().getLatitude() >= minLatitude &&
                    station.getLocation().getLatitude() <= maxLatitude &&
                    station.getLocation().getLongitude() >= minLongitude &&
                    station.getLocation().getLongitude() <= maxLongitude){
                s += station.toString() + "\n";
            }
        }

        if(node.getLeft() != null){
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                    node.getLeft(), s);
        }
        if(node.getRight() != null){
            s = searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude,
                    node.getRight(), s);
        }

        return s;
    }

    /**
     * Return the requested geographical search with the extra filters
     */
    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int isCity, int isMain, String country) {
        KDTree2D kdTree = kdTreeRepository.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            return "2D-Tree not built yet. Please build it first.";
        }

        String s = "";
        return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, isCity, isMain, country, s, kdTree.getRoot());
    }

    public String searchInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude, int isCityNum, int isMainNum, String country, String s, KDNode node){
        boolean isCityDelimited = (isCityNum == 1 || isCityNum == 0);
        boolean isMainDelimited = (isMainNum == 1 || isMainNum == 0);
        boolean isCity = false;
        boolean isMain = false;
        if(isCityDelimited){
            isCity = (isCityNum == 0);
        }
        if(isMainDelimited){
            isMain = (isMainNum == 0);
        }
        for(Station station : node.getStations()) {
            if (isCityDelimited && isMainDelimited && !country.equals("all")) {
                if (station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isCity() == isCity &&
                        station.isMainStation() == isMain &&
                        station.getCountry().toLowerCase().equals(country)) {
                    s += station.toString();
                }
            }else if(isCityDelimited && isMainDelimited && country.equals("all")){
                if(station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isCity() == isCity &&
                        station.isMainStation() == isMain){
                    s += station.toString();
                }
            } else if(isCityDelimited && !country.equals("all")){
                if(station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isCity() == isCity &&
                        station.getCountry().toLowerCase().equals(country)){
                    s += station.toString();
                }
            } else if(isCityDelimited && country.equals("all")){
                if(station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isCity() == isCity){
                    s += station.toString();
                }
            } else if(isMainDelimited && !country.equals("all")){
                if(station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isMainStation() == isMain &&
                        station.getCountry().toLowerCase().equals(country)){
                    s += station.toString();
                }
            } else{
                if(station.getLocation().getLatitude() >= minLatitude &&
                        station.getLocation().getLatitude() <= maxLatitude &&
                        station.getLocation().getLongitude() >= minLongitude &&
                        station.getLocation().getLongitude() <= maxLongitude &&
                        station.isMainStation() == isMain){
                    s += station.toString();
                }
            }
        }
        if(node.getLeft()!= null){
            return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, isCityNum, isMainNum, country, s, node.getLeft());
        }
        if(node.getRight()!= null){
            return searchInArea(minLatitude, maxLatitude, minLongitude, maxLongitude, isCityNum, isMainNum, country, s, node.getRight());
        }
        return s;
    }
}
