package controller;

import domain.Location;
import domain.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads Station objects from a CSV file .
 * Handles quoted fields with commas and ignores invalid lines.
 */
public class StationCsvLoader {

    /**
     * Loads stations from a generic Reader (e.g., StringReader or FileReader).
     */
    public List<Station> load(Reader reader) throws IOException {
        List<Station> list = new ArrayList<Station>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(reader);
            String header = br.readLine();
            if (header == null) return list;
            header = stripBom(header);

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
                if (line != null && line.length() > 0) {
                    try {
                        List<String> cols = parseCsvLine(line);

                        String country   = get(cols, idxCountry);
                        String tzGroup   = normalizeTimeZoneGroup(get(cols, idxTzGroup));
                        String station   = get(cols, idxStation);
                        double latitude  = Double.parseDouble(get(cols, idxLat));
                        double longitude = Double.parseDouble(get(cols, idxLon));
                        boolean isCity   = parseBool(get(cols, idxIsCity));
                        boolean isMain   = parseBool(get(cols, idxIsMain));
                        boolean isAir    = parseBool(get(cols, idxIsAir));

                        Location loc = new Location(latitude, longitude);
                        list.add(new Station(station, country, loc, tzGroup, isCity, isMain, isAir));
                    } catch (Exception ignore) {
                        list.add(null);
                    }
                }
            }
        } finally {
            if (br != null) {
                try { br.close(); } catch (IOException ignore) {}
            }
        }
        return list;
    }

    /**
     * Convenience: loads directly from a CSV file path.
     */
    public List<Station> load(String path) throws IOException {
        FileReader fr = new FileReader(path);
        try {
            return load(fr);
        } finally {
            try { fr.close(); } catch (IOException ignore) {}
        }
    }


    private static int indexOf(String header, String name) {
        List<String> cols = parseCsvLine(header);
        for (int i = 0; i < cols.size(); i++) {
            String h = cols.get(i);
            if (h != null && h.trim().equalsIgnoreCase(name)) return i;
        }
        throw new IllegalArgumentException("CSV missing column: " + name);
    }

    private static String get(List<String> cols, int idx) {
        if (idx >= 0 && idx < cols.size()) {
            String v = cols.get(idx);
            return v == null ? "" : v.trim();
        }
        return "";
    }

    private static boolean parseBool(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.length() == 0) return false;
        return v.equals("1") || v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }

    private static String stripBom(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    /**
     * CSV line parser that honors quotes and commas inside quotes.
     * No regex, no streams, Java SE only.
     */
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        cur.append('\"');
                        i = i + 1;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == '\"') {
                    inQuotes = true;
                } else if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
            i = i + 1;
        }
        out.add(cur.toString());
        return out;
    }

    /**
     * Normalizes values like "('Europe/Lisbon',)" to "Europe/Lisbon".
     */
    private static String normalizeTimeZoneGroup(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last  = s.charAt(s.length() - 1);
            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                s = s.substring(1, s.length() - 1);
            }
        }

        int open = s.indexOf("('");
        int comma = s.indexOf("',");
        if (open != -1 && comma != -1 && comma > open + 2) {
            return s.substring(open + 2, comma);
        }

        if (s.startsWith("('") && s.endsWith("')") && s.length() > 4) {
            return s.substring(2, s.length() - 2);
        }

        return s;
    }
}
