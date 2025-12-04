package controller.Sprint3Loaders;

import domain.Location;
import domain.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StationCsvLoaderS3 {
    public List<Station> load(String path) throws IOException {
        List<Station> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = stripBom(br.readLine());
            if (header == null) return list;

            int idxId    = indexOf(header, "Station id");
            int idxName  = indexOf(header, "Station");
            int idxLat   = indexOf(header, "Lat");
            int idxLon   = indexOf(header, "Lon");
            int idxX     = indexOf(header, "CoordX");
            int idxY     = indexOf(header, "CoordY");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                try {
                    List<String> cols = parseCsvLine(line);

                    int id       = Integer.parseInt(get(cols, idxId));
                    String name  = get(cols, idxName);
                    double lat   = Double.parseDouble(get(cols, idxLat));
                    double lon   = Double.parseDouble(get(cols, idxLon));
                    int coordX   = Integer.parseInt(get(cols, idxX));
                    int coordY   = Integer.parseInt(get(cols, idxY));

                    Location loc = new Location(lat, lon);
                    Station st   = new Station(id, name, loc, coordX, coordY);
                    list.add(st);
                } catch (Exception ignore) {
                }
            }
        }
        return list;
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

}
