package domain;

import java.util.*;

/**
 * Builds and maintains sorted indexes over {@link Station} to enable fast queries.
 *
 * <p><b>Theory (as in the Trees and JCF slides):</b>
 * Each index below is a search tree: in Java, {@link TreeMap} / {@link TreeSet}
 * are balanced binary search trees (Red-Black), with O(log n) insertion/search.
 * Range ("window") queries are implemented with {@link NavigableMap#subMap(Object, boolean, Object, boolean)},
 * as described in the slides about range queries in Trees.</p>
 *
 * <p><b>Indexes:</b>
 * <ul>
 *   <li>byLat → key = latitude (double)</li>
 *   <li>byLon → key = longitude (double)</li>
 *   <li>byTz  → key = time-zone group (String)</li>
 * </ul>
 * Each key maps to a {@link TreeSet} of stations kept in ascending order
 * according to the required criteria.</p>
 */
public class StationIndex {


    /**
     * Order stations by name, then country, then longitude.
     * Used within each latitude bucket to ensure deterministic ordering.
     */
    private static final Comparator<Station> NAME_COUNTRY_LON = new Comparator<Station>() {
        @Override
        public int compare(Station a, Station b) {
            int c = a.getName().compareTo(b.getName());
            if (c != 0) return c;
            c = a.getCountry().compareTo(b.getCountry());
            if (c != 0) return c;
            double alo = a.getLocation().getLongitude();
            double blo = b.getLocation().getLongitude();
            return Double.compare(alo, blo);
        }
    };

    /**
     * Order stations by name, then country, then latitude.
     * Used within each longitude bucket to ensure deterministic ordering.
     */
    private static final Comparator<Station> NAME_COUNTRY_LAT = new Comparator<Station>() {
        @Override
        public int compare(Station a, Station b) {
            int c = a.getName().compareTo(b.getName());
            if (c != 0) return c;
            c = a.getCountry().compareTo(b.getCountry());
            if (c != 0) return c;
            double ala = a.getLocation().getLatitude();
            double bla = b.getLocation().getLatitude();
            return Double.compare(ala, bla);
        }
    };

    /**
     * Order stations by country, then name, then lat/lon.
     * Used within each time-zone group (as required).
     */
    private static final Comparator<Station> COUNTRY_NAME_LATLON = new Comparator<Station>() {
        @Override
        public int compare(Station a, Station b) {
            int c = a.getCountry().compareTo(b.getCountry());
            if (c != 0) return c;
            c = a.getName().compareTo(b.getName());
            if (c != 0) return c;
            c = Double.compare(a.getLocation().getLatitude(), b.getLocation().getLatitude());
            if (c != 0) return c;
            return Double.compare(a.getLocation().getLongitude(), b.getLocation().getLongitude());
        }
    };


    private final NavigableMap<Double, NavigableSet<Station>> byLat = new TreeMap<>();
    private final NavigableMap<Double, NavigableSet<Station>> byLon = new TreeMap<>();
    private final Map<String, NavigableSet<Station>> byTz = new HashMap<>();


    /**
     * Inserts one station into all indexes.
     * Uses only Map methods shown in the slides: containsKey, put, get.
     */
    public void addStation(Station s) {
        double lat = s.getLocation().getLatitude();
        double lon = s.getLocation().getLongitude();

        // Latitude index
        if (!byLat.containsKey(lat)) {
            byLat.put(lat, new TreeSet<>(NAME_COUNTRY_LON));
        }
        byLat.get(lat).add(s);

        // Longitude index
        if (!byLon.containsKey(lon)) {
            byLon.put(lon, new TreeSet<>(NAME_COUNTRY_LAT));
        }
        byLon.get(lon).add(s);

        // Time-zone index
        String group = s.getTimeZoneGroup();
        if (!byTz.containsKey(group)) {
            byTz.put(group, new TreeSet<>(COUNTRY_NAME_LATLON));
        }
        byTz.get(group).add(s);
    }

    /**
     * Bulk insertion of all stations after reading the CSV file.
     * Equivalent to multiple calls to {@link #addStation(Station)}.
     */
    public void addAll(Collection<Station> stations) {
        for (Station s : stations) {
            addStation(s);
        }
    }


    /**
     * Returns all stations with latitude in the interval [minLat, maxLat].
     * Ordered by latitude ↑, then name → country (as required).
     * Uses subMap() (TreeMap range view).
     */
    public List<Station> queryLatitudeWindow(double minLat, double maxLat) {
        List<Station> result = new ArrayList<Station>();
        SortedMap<Double, NavigableSet<Station>> sub =
                byLat.subMap(minLat, true, maxLat, true);
        for (NavigableSet<Station> set : sub.values()) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * Returns all stations with longitude in the interval [minLon, maxLon].
     * Ordered by longitude ↑, then name → country (as required).
     */
    public List<Station> queryLongitudeWindow(double minLon, double maxLon) {
        List<Station> result = new ArrayList<Station>();
        SortedMap<Double, NavigableSet<Station>> sub =
                byLon.subMap(minLon, true, maxLon, true);
        for (NavigableSet<Station> set : sub.values()) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * Returns all stations of a given time-zone group,
     * ordered by country ↑ and name ↑ (as required).
     */
    public List<Station> queryByTzGroup(String group) {
        NavigableSet<Station> set = byTz.get(group);
        if (set == null) {
            return new ArrayList<Station>();
        }
        return new ArrayList<Station>(set);
    }

    /**
     * Returns all stations of the given time-zone groups (union),
     * keeping each group's internal order.
     */
    public List<Station> queryByTzWindow(Collection<String> groups) {
        List<Station> out = new ArrayList<Station>();
        for (String g : groups) {
            NavigableSet<Station> set = byTz.get(g);
            if (set != null) out.addAll(set);
        }
        return out;
    }

    /**
     * Returns all stations of a given time-zone group and a specific country,
     * preserving order by name within the group.
     */
    public List<Station> queryByTzAndCountry(String group, String country) {
        NavigableSet<Station> set = byTz.get(group);
        if (set == null) return new ArrayList<Station>();
        List<Station> out = new ArrayList<Station>();
        for (Station s : set) {
            if (s.getCountry().equals(country)) out.add(s);
        }
        return out;
    }


    public int sizeLatKeys() {
        return byLat.size();
    }

    public int sizeLonKeys() {
        return byLon.size();
    }

    public int sizeTzGroups() {
        return byTz.size();
    }
}
