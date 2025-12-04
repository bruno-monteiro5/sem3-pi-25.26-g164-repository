package controller.Sprint3Loaders;

import domain.Line;
import domain.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineCsvLoader {
    public List<Line> load(String path, Map<Integer, Station> stationsById) throws IOException {
        List<Line> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = stripBom(br.readLine());
            if (header == null) return list;

            int idxDep   = indexOf(header, "departure_stid");
            int idxArr   = indexOf(header, "arrival_stid");
            int idxDist  = indexOf(header, "dist");
            int idxCap   = indexOf(header, "capacity");
            int idxCost  = indexOf(header, "cost");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                try {
                    List<String> cols = parseCsvLine(line);

                    int depId   = Integer.parseInt(get(cols, idxDep));
                    int arrId   = Integer.parseInt(get(cols, idxArr));
                    double dist = Double.parseDouble(get(cols, idxDist));
                    int cap     = Integer.parseInt(get(cols, idxCap));
                    double cost = Double.parseDouble(get(cols, idxCost));

                    Station origin      = stationsById.get(depId);
                    Station destination = stationsById.get(arrId);
                    if (origin != null && destination != null) {
                        list.add(new Line(origin, destination, dist, cap, cost));
                    }
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
