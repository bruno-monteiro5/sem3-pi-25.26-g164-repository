package controller;

import domain.Station;
import domain.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StationCsvLoader {

    public List<Station> load(java.io.Reader reader) throws IOException {
        List<Station> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String header = br.readLine();
            if (header == null) return list;

            int idxCountry = indexOf(header, "country");
            int idxTzGroup = indexOf(header, "time_zone_group");
            int idxStation = indexOf(header, "station");
            int idxLat     = indexOf(header, "latitude");
            int idxLon     = indexOf(header, "longitude");
            int idxIsCity  = indexOf(header, "is_city");
            int idxIsMain  = indexOf(header, "is_main_station");
            int idxIsAir   = indexOf(header, "is_airport");

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                try {
                    String country   = get(cols, idxCountry);
                    String tzGroup   = get(cols, idxTzGroup);
                    String station   = get(cols, idxStation);
                    double latitude  = Double.parseDouble(get(cols, idxLat));
                    double longitude = Double.parseDouble(get(cols, idxLon));
                    boolean isCity   = parseBool(get(cols, idxIsCity));
                    boolean isMain   = parseBool(get(cols, idxIsMain));
                    boolean isAir    = parseBool(get(cols, idxIsAir));

                    Location loc = new Location(latitude, longitude);
                    list.add(new Station(station, country, loc, tzGroup, isCity, isMain, isAir));
                } catch (Exception ignore) {
                }
            }
        }
        return list;
    }

    public List<Station> load(String path) throws IOException {
        FileReader fr = new FileReader(path);
        return load(fr);
    }

    private static int indexOf(String header, String name) {
        String[] cols = header.split(",", -1);
        for (int i = 0; i < cols.length; i++)
            if (cols[i].trim().equalsIgnoreCase(name)) return i;
        throw new IllegalArgumentException("CSV missing column: " + name);
    }

    private static String get(String[] cols, int idx) {
        return (idx >= 0 && idx < cols.length) ? cols[idx].trim() : "";
    }

    private static boolean parseBool(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("yes");
    }
}
