package controller.FreightController;

import domain.railway.Endpoint;
import domain.railway.Freight;
import domain.railway.Path;
import domain.railway.RouteOperation;
import domain.railway.RoutePlan;
import repository.RailwayRepository;

import java.sql.SQLException;
import java.util.*;

/**
 * USLP08 - Route Planner Controller
 *
 * Responsibilities:
 * - Plan routes for PENDING freights (no train assigned yet)
 * - Create abstract Paths (sequences of endpoints)
 * - Build freight chains (origin→destination sequences)
 *
 * Does NOT:
 * - Create trains
 * - Assign rolling stock
 * - Schedule departures
 */
public class RoutePlannerController {

    private final RailwayRepository railwayRepo;

    public RoutePlannerController() {
        this.railwayRepo = new RailwayRepository();
    }

    // ==================== USLP08: FREIGHT-BASED PLANNING ====================

    /**
     * Gets all freights that have NO train assigned yet.
     * These are the freights that need route planning.
     */
    public List<Freight> getPendingFreights() throws SQLException {
        return railwayRepo.getPendingFreights();
    }

    /**
     * Builds freight chains from a list of freights.
     * A chain is a sequence where destination[i] = origin[i+1].
     *
     * Example:
     *   Freight A: Lisbon → Porto
     *   Freight B: Porto → Madrid
     *   Chain: [A, B]
     */
    public List<FreightChain> buildChains(List<Freight> freights) {
        if (freights == null || freights.isEmpty()) return new ArrayList<>();

        Map<Integer, Deque<Freight>> outgoing = new HashMap<>();
        Map<Integer, Integer> inDeg = new HashMap<>();
        Map<Integer, Integer> outDeg = new HashMap<>();

        // Build graph: endpoint → outgoing freights
        for (Freight f : freights) {
            int o = f.getOrigin().getId();
            int d = f.getDestination().getId();

            outgoing.computeIfAbsent(o, k -> new ArrayDeque<>()).add(f);

            outDeg.put(o, outDeg.getOrDefault(o, 0) + 1);
            inDeg.put(d, inDeg.getOrDefault(d, 0) + 1);

            inDeg.putIfAbsent(o, inDeg.getOrDefault(o, 0));
            outDeg.putIfAbsent(d, outDeg.getOrDefault(d, 0));
        }

        // Prioritize starting points (more outgoing than incoming)
        List<Integer> starters = new ArrayList<>(outgoing.keySet());
        starters.sort((a, b) -> {
            int da = outDeg.getOrDefault(a, 0) - inDeg.getOrDefault(a, 0);
            int db = outDeg.getOrDefault(b, 0) - inDeg.getOrDefault(b, 0);
            return Integer.compare(db, da);
        });

        List<FreightChain> chains = new ArrayList<>();

        // Greedy chain extraction
        java.util.function.Function<Integer, List<Freight>> consumeFrom = (startId) -> {
            List<Freight> chain = new ArrayList<>();
            Integer cur = startId;

            while (true) {
                Deque<Freight> q = outgoing.get(cur);
                if (q == null || q.isEmpty()) break;

                Freight next = q.removeFirst();
                chain.add(next);
                cur = next.getDestination().getId();
            }
            return chain;
        };

        // Extract chains from high-priority starters
        for (Integer s : starters) {
            Deque<Freight> q = outgoing.get(s);
            if (q == null || q.isEmpty()) continue;

            List<Freight> ch = consumeFrom.apply(s);
            if (!ch.isEmpty()) chains.add(new FreightChain(ch));
        }

        // Mop up remaining freights
        for (Integer s : new ArrayList<>(outgoing.keySet())) {
            Deque<Freight> q = outgoing.get(s);
            if (q == null || q.isEmpty()) continue;

            List<Freight> ch = consumeFrom.apply(s);
            if (!ch.isEmpty()) chains.add(new FreightChain(ch));
        }

        // Sort by chain length (longest first)
        chains.sort((c1, c2) -> Integer.compare(c2.getFreights().size(), c1.getFreights().size()));
        return chains;
    }

    /**
     * Creates a route plan from a freight chain.
     *
     * Plan structure:
     *   LOAD Freight A at origin
     *   UNLOAD Freight A at destination
     *   LOAD Freight B at destination (if chained)
     *   UNLOAD Freight B at destination
     *   ...
     */
    public RoutePlan planRouteForChain(FreightChain chain) {
        if (chain == null || chain.getFreights().isEmpty()) {
            throw new IllegalArgumentException("Empty chain");
        }

        List<Freight> freights = chain.getFreights();
        RoutePlan plan = new RoutePlan();

        // First freight: LOAD at origin
        Freight first = freights.get(0);
        plan.addOperation(new RouteOperation(first.getOrigin(), first, RouteOperation.Type.LOAD));

        // For each freight: UNLOAD at destination
        for (int i = 0; i < freights.size(); i++) {
            Freight cur = freights.get(i);
            plan.addOperation(new RouteOperation(cur.getDestination(), cur, RouteOperation.Type.UNLOAD));

            // If not last: LOAD next freight at current destination
            if (i < freights.size() - 1) {
                Freight next = freights.get(i + 1);
                plan.addOperation(new RouteOperation(cur.getDestination(), next, RouteOperation.Type.LOAD));
            }
        }

        return plan;
    }

    /**
     * Saves a route plan as a Path in the database.
     * All endpoints in the plan become must_stop='Y'.
     *
     * @param pathName Name for the new path
     * @param plan Route plan to save
     * @return Created Path object
     */
    public Path savePlanAsPath(String pathName, RoutePlan plan) throws SQLException {
        if (pathName == null || pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty path name");
        }
        if (plan == null || plan.getOperations().isEmpty()) {
            throw new IllegalArgumentException("Empty plan");
        }

        Path path = railwayRepo.createPath(pathName.trim());

        // Extract unique endpoints from plan (preserving order)
        List<Endpoint> stops = extractStopsInOrder(plan);

        // DEBUG
        System.out.println("DEBUG: Extracted " + stops.size() + " stops from plan");
        for (Endpoint e : stops) {
            System.out.println("  - " + e.getName() + " (id=" + e.getId() + ")");
        }

        int seq = 1;
        for (Endpoint stop : stops) {
            System.out.println("DEBUG: Inserting stop " + seq + ": " + stop.getName());
            railwayRepo.addEndpointToPath(path.getId(), seq++, stop.getId(), true); // must_stop='Y'
        }

        // CRITICAL FIX: Reload path from database to get endpoints
        Path reloadedPath = railwayRepo.getPathById(path.getId());

        System.out.println("DEBUG: Path reloaded with " +
                (reloadedPath != null ? reloadedPath.getEndpoints().size() : 0) +
                " endpoints");

        return reloadedPath != null ? reloadedPath : path;
    }

    /**
     * Extracts ordered list of endpoints from a route plan.
     * Removes consecutive duplicates.
     */
    private List<Endpoint> extractStopsInOrder(RoutePlan plan) {
        List<Endpoint> stops = new ArrayList<>();
        Endpoint last = null;

        for (RouteOperation op : plan.getOperations()) {
            Endpoint s = op.getStation();
            if (last == null || !last.equals(s)) {
                stops.add(s);
                last = s;
            }
        }
        return stops;
    }

    // ==================== HELPER CLASSES ====================

    /**
     * Represents a sequence of connected freights.
     * Example: A→B→C means freight1 ends where freight2 starts.
     */
    public static class FreightChain {
        private final List<Freight> freights;

        public FreightChain(List<Freight> freights) {
            this.freights = freights;
        }

        public List<Freight> getFreights() {
            return freights;
        }

        public Endpoint getStart() {
            return freights.get(0).getOrigin();
        }

        public Endpoint getEnd() {
            return freights.get(freights.size() - 1).getDestination();
        }

        @Override
        public String toString() {
            return "Chain[" + freights.size() + "] " +
                    getStart().getName() + " → " + getEnd().getName();
        }
    }
}