package controller.AssemblyController;

import domain.railway.*;
import repository.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for USLP09 - Train Assembly.
 *
 * Responsibilities:
 * - List available locomotives and wagons by location
 * - Filter by status (Free vs In Transit)
 * - Sort by distance from route start point
 * - Assign rolling stock to trains
 *
 */
public class TrainAssemblyController {

    private final LocationRepository locationRepo;
    private final RailwayRepository railwayRepo;

    public TrainAssemblyController() {
        this.railwayRepo = new RailwayRepository();
        this.locationRepo = new LocationRepository(railwayRepo);
    }

    // ==================== LOCOMOTIVES ====================

    /**
     * Gets all locomotives with their current location and status.
     */
    public List<LocomotiveWithDetails> getAvailableLocomotives() throws SQLException {
        List<LocomotiveLocation> locations = locationRepo.getAllLocomotiveLocations();
        List<Locomotive> allLocos = railwayRepo.getAllLocomotives();

        List<LocomotiveWithDetails> result = new ArrayList<>();

        for (LocomotiveLocation loc : locations) {
            Locomotive loco = findLocomotiveById(allLocos, loc.getLocomotiveId());
            if (loco != null) {
                result.add(new LocomotiveWithDetails(loco, loc));
            }
        }

        return result;
    }

    /** Returns the current endpoint where a locomotive is located (from Locomotive_Current_Location). */
    public Endpoint getLocomotiveCurrentEndpoint(int locomotiveId) throws SQLException {
        for (LocomotiveLocation loc : locationRepo.getAllLocomotiveLocations()) {
            if (loc.getLocomotiveId() == locomotiveId) {
                return loc.getCurrentEndpoint();
            }
        }
        return null;
    }


    /**
     * Gets locomotives sorted by distance from route start, showing status.
     *
     * Format per USLP09:
     * - FREE locomotives: sorted by distance ascending
     * - IN TRANSIT locomotives: show final destination
     */
    public List<LocomotiveWithDetails> getLocomotivesByProximity(Endpoint routeStart)
            throws SQLException {

        List<LocomotiveWithDetails> all = getAvailableLocomotives();

        // Separate into free and in-transit
        List<LocomotiveWithDetails> free = all.stream()
                .filter(ld -> ld.location.isFree())
                .collect(Collectors.toList());

        List<LocomotiveWithDetails> inTransit = all.stream()
                .filter(ld -> ld.location.isInTransit())
                .collect(Collectors.toList());

        // Sort free by distance
        free.sort(Comparator.comparingDouble(
                ld -> ld.location.distanceFrom(routeStart)
        ));

        // Combine: free first, then in-transit
        List<LocomotiveWithDetails> result = new ArrayList<>();
        result.addAll(free);
        result.addAll(inTransit);

        return result;
    }

    // ==================== WAGONS ====================

    /**
     * Gets all wagons with their current location and status.
     */
    public List<WagonWithDetails> getAvailableWagons() throws SQLException {
        List<WagonLocation> locations = locationRepo.getAllWagonLocations();
        List<WagonWithDetails> result = new ArrayList<>();

        for (WagonLocation loc : locations) {
            if (!loc.isFree()) continue;

            if (railwayRepo.isWagonAssignedToAnyFreight(loc.getWagonId())) continue;

            Wagon wagon = railwayRepo.getWagonById(loc.getWagonId());
            if (wagon != null) {
                result.add(new WagonWithDetails(wagon, loc));
            }
        }

        return result;
    }


    /**
     * Gets wagons sorted by distance from route start.
     */
    public List<WagonWithDetails> getFreeWagonsByProximity(Endpoint routeStart)
            throws SQLException {

        List<WagonWithDetails> free = getAvailableWagons();

        // Ordenar por proximidade à locomotiva líder
        free.sort((a, b) -> {
            long da = distanceMeters(routeStart.getId(), a.location.getCurrentEndpoint().getId());
            long db = distanceMeters(routeStart.getId(), b.location.getCurrentEndpoint().getId());
            if (da != db) return Long.compare(da, db);

            // tie-break: bigger payload first
            return Double.compare(b.wagon.getModel().getPayload(), a.wagon.getModel().getPayload());
        });


        return free;
    }


    // ==================== HELPER CLASSES ====================

    /**
     * Combines locomotive details with current location.
     */
    public static class LocomotiveWithDetails {
        public final Locomotive locomotive;
        public final LocomotiveLocation location;

        public LocomotiveWithDetails(Locomotive locomotive, LocomotiveLocation location) {
            this.locomotive = locomotive;
            this.location = location;
        }

        @Override
        public String toString() {
            String status = location.isFree() ? "FREE" : "IN TRANSIT";
            String trainInfo = location.getTrainId() != null
                    ? " (Train " + location.getTrainId() + ")"
                    : "";

            return String.format("%s | %s @ %s%s",
                    locomotive.getName(),
                    status,
                    location.getCurrentEndpoint().getName(),
                    trainInfo);
        }
    }

    /**
     * Combines wagon details with current location.
     */
    public static class WagonWithDetails {
        public final Wagon wagon;
        public final WagonLocation location;

        public WagonWithDetails(Wagon wagon, WagonLocation location) {
            this.wagon = wagon;
            this.location = location;
        }

        @Override
        public String toString() {
            String status = location.isFree() ? "FREE" : "IN TRANSIT";
            String trainInfo = location.getTrainId() != null
                    ? " (Train " + location.getTrainId() + ")"
                    : "";

            return String.format("Wagon %d (%s) | %s @ %s%s",
                    wagon.getId(),
                    wagon.getModel().getType(),
                    status,
                    location.getCurrentEndpoint().getName(),
                    trainInfo);
        }
    }

    // ==================== HELPERS ====================

    private Locomotive findLocomotiveById(List<Locomotive> locos, int id) {
        return locos.stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // ==================== REAL DISTANCE (Dijkstra over Lines, weight = SUM(segment.length)) ====================

    private static class Edge {
        final int to;
        final long w;
        Edge(int to, long w) { this.to = to; this.w = w; }
    }

    private Map<Integer, List<Edge>> cachedWeightedAdj = null;
    private Map<Integer, Long> cachedLineLength = null;

    /**
     * Builds weighted adjacency: endpointA <-> endpointB with weight = total line length.
     */
    private Map<Integer, List<Edge>> weightedAdjacency() throws SQLException {
        if (cachedWeightedAdj != null) return cachedWeightedAdj;

        // cache line lengths to avoid N queries per Dijkstra
        cachedLineLength = new HashMap<>();

        Map<Integer, List<Edge>> adj = new HashMap<>();
        List<Line> lines = railwayRepo.getAllLines();

        for (Line l : lines) {
            long len = lineTotalLength(l.getId());
            int a = l.getEndpointA();
            int b = l.getEndpointB();

            adj.computeIfAbsent(a, k -> new ArrayList<>()).add(new Edge(b, len));
            adj.computeIfAbsent(b, k -> new ArrayList<>()).add(new Edge(a, len));
        }

        cachedWeightedAdj = adj;
        return adj;
    }

    /**
     * Total line length = SUM(Line_Segment.length) for that line.
     */
    private long lineTotalLength(int lineId) throws SQLException {
        if (cachedLineLength != null && cachedLineLength.containsKey(lineId)) {
            return cachedLineLength.get(lineId);
        }

        long sum = 0L;
        for (LineSegment s : railwayRepo.getLineSegments(lineId)) {
            sum += s.getLength();
        }

        if (cachedLineLength != null) cachedLineLength.put(lineId, sum);
        return sum;
    }

    /**
     * Returns shortest distance between endpoints in meters (or whatever unit Line_Segment.length is).
     * If unreachable, returns a very large number (push to end of sorting).
     */
    private long distanceMeters(int startId, int endId) {
        if (startId == endId) return 0L;

        final long INF = Long.MAX_VALUE / 4;

        Map<Integer, List<Edge>> adj;
        try {
            adj = weightedAdjacency();
        } catch (SQLException e) {
            return INF;
        }

        // Dijkstra
        Map<Integer, Long> dist = new HashMap<>();
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[0]));
        dist.put(startId, 0L);
        pq.add(new long[]{0L, startId});

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            long d = cur[0];
            int u = (int) cur[1];

            if (u == endId) return d;
            if (d != dist.getOrDefault(u, INF)) continue;

            for (Edge e : adj.getOrDefault(u, List.of())) {
                long nd = d + e.w;
                long best = dist.getOrDefault(e.to, INF);
                if (nd < best) {
                    dist.put(e.to, nd);
                    pq.add(new long[]{nd, e.to});
                }
            }
        }

        return INF;
    }

    /**
     * If your lines/segments can change at runtime, call this to rebuild caches.
     */
    public void clearDistanceCache() {
        cachedWeightedAdj = null;
        cachedLineLength = null;
    }

}