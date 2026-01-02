package controller.FreightController;

import domain.railway.Endpoint;
import domain.railway.Freight;
import domain.railway.Line;
import domain.railway.Path;
import domain.railway.RouteOperation;
import domain.railway.RoutePlan;
import domain.railway.Train;
import repository.RailwayRepository;

import java.sql.SQLException;
import java.util.*;

public class RoutePlannerController {

    private final RailwayRepository railwayRepo;
    private final controller.AssemblyController.TrainAssemblyController assemblyController;

    public RoutePlannerController() {
        this.railwayRepo = new RailwayRepository();
        this.assemblyController = new controller.AssemblyController.TrainAssemblyController();
    }

    /**
     * Plans a full connected path for a train, persists it as a Path, and assigns it.
     *
     * @param trainId          target train
     * @param leaderLocoId     locomotive with position=1
     * @param wagonsPickup     where the wagons are picked up (usually where the selected wagons are parked)
     * @param freights         freights assigned to the train
     * @param pathName         name to save in Path
     */
    public Path autoPlanAndAssignToTrain(int trainId,
                                         int leaderLocoId,
                                         Endpoint wagonsPickup,
                                         List<Freight> freights,
                                         String pathName) throws SQLException {

        if (wagonsPickup == null) throw new IllegalArgumentException("wagonsPickup is null");
        if (freights == null || freights.isEmpty()) throw new IllegalArgumentException("No freights to plan");
        if (pathName == null || pathName.trim().isEmpty()) throw new IllegalArgumentException("Empty path name");

        Endpoint locoStart = assemblyController.getLocomotiveCurrentEndpoint(leaderLocoId);
        if (locoStart == null) throw new IllegalArgumentException("Could not get locomotive current endpoint");

        List<Endpoint> waypoints = buildWaypointSequence(locoStart, wagonsPickup, freights);
        List<Endpoint> connected = expandWithIntermediateEndpoints(waypoints);

        // Define must-stop endpoints: pickup + all load/unload endpoints
        Set<Integer> mustStopIds = new HashSet<>();
        mustStopIds.add(wagonsPickup.getId());
        for (Freight f : freights) {
            mustStopIds.add(f.getOrigin().getId());
            mustStopIds.add(f.getDestination().getId());
        }

        return saveEndpointSequenceAsPathAndAssign(trainId, pathName.trim(), connected, mustStopIds);
    }

    /**
     * Creates the high-level waypoint sequence (no intermediate endpoints yet).
     * Uses a greedy strategy with precedence constraints:
     *   - A freight destination is only eligible after its origin has been visited.
     */
    private List<Endpoint> buildWaypointSequence(Endpoint locoStart,
                                                 Endpoint wagonsPickup,
                                                 List<Freight> freights) {

        List<Endpoint> waypoints = new ArrayList<>();
        waypoints.add(locoStart);

        if (!locoStart.equals(wagonsPickup)) {
            waypoints.add(wagonsPickup);
        }

        // Track freight state
        Map<Integer, Boolean> loaded = new HashMap<>();
        Map<Integer, Boolean> delivered = new HashMap<>();
        for (Freight f : freights) {
            loaded.put(f.getId(), false);
            delivered.put(f.getId(), false);
        }

        Endpoint current = wagonsPickup;

        // Until everything is delivered
        while (delivered.values().stream().anyMatch(v -> !v)) {

            // Candidates: origins not yet loaded + destinations already loaded and not delivered
            List<Candidate> candidates = new ArrayList<>();

            for (Freight f : freights) {
                if (!loaded.get(f.getId())) {
                    candidates.add(new Candidate(f.getOrigin(), f, true));
                } else if (!delivered.get(f.getId())) {
                    candidates.add(new Candidate(f.getDestination(), f, false));
                }
            }

            // Pick next by shortest hop distance; tie-break: LOAD before UNLOAD
            Candidate best = null;
            int bestDist = Integer.MAX_VALUE;

            for (Candidate c : candidates) {
                int d = shortestHopDistance(current.getId(), c.endpoint.getId());
                if (d < 0) continue; // unreachable
                if (d < bestDist) {
                    bestDist = d;
                    best = c;
                } else if (d == bestDist && best != null) {
                    if (c.isLoad && !best.isLoad) best = c;
                }
            }

            if (best == null) {
                throw new IllegalStateException("No reachable next waypoint from " + current.getName());
            }

            if (!current.equals(best.endpoint)) {
                waypoints.add(best.endpoint);
                current = best.endpoint;
            }

            // Apply all loads/unloads at current
            for (Freight f : freights) {
                if (!loaded.get(f.getId()) && f.getOrigin().equals(current)) {
                    loaded.put(f.getId(), true);
                }
                if (loaded.get(f.getId()) && !delivered.get(f.getId()) && f.getDestination().equals(current)) {
                    delivered.put(f.getId(), true);
                }
            }
        }

        return compressConsecutiveDuplicates(waypoints);
    }

    private static class Candidate {
        final Endpoint endpoint;
        final Freight freight;
        final boolean isLoad;

        Candidate(Endpoint endpoint, Freight freight, boolean isLoad) {
            this.endpoint = endpoint;
            this.freight = freight;
            this.isLoad = isLoad;
        }
    }

    /**
     * Expands each leg between consecutive waypoints to ensure the final Path is connected.
     */
    private List<Endpoint> expandWithIntermediateEndpoints(List<Endpoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) return List.of();

        List<Endpoint> full = new ArrayList<>();

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Endpoint a = waypoints.get(i);
            Endpoint b = waypoints.get(i + 1);

            List<Endpoint> leg = shortestPathEndpoints(a.getId(), b.getId());
            if (leg.isEmpty()) {
                throw new IllegalStateException("No path between " + a.getName() + " and " + b.getName());
            }

            if (full.isEmpty()) {
                full.addAll(leg);
            } else {
                for (int j = 0; j < leg.size(); j++) {
                    Endpoint e = leg.get(j);
                    if (j == 0 && full.get(full.size() - 1).equals(e)) continue;
                    full.add(e);
                }
            }
        }

        return compressConsecutiveDuplicates(full);
    }

    /**
     * Saves a raw endpoint sequence as a Path and assigns it to a train.
     */
    private Path saveEndpointSequenceAsPathAndAssign(int trainId,
                                                     String pathName,
                                                     List<Endpoint> orderedEndpoints,
                                                     Set<Integer> mustStopIds) throws SQLException {

        if (orderedEndpoints == null || orderedEndpoints.isEmpty()) {
            throw new IllegalArgumentException("Empty endpoint sequence");
        }

        Path path = railwayRepo.createPath(pathName);

        int seq = 1;
        Endpoint last = null;
        for (Endpoint e : orderedEndpoints) {
            if (last != null && last.equals(e)) continue;
            boolean mustStop = mustStopIds != null && mustStopIds.contains(e.getId());
            railwayRepo.addEndpointToPath(path.getId(), seq++, e.getId(), mustStop);
            last = e;
        }

        railwayRepo.updateTrainPath(trainId, path.getId());
        return path;
    }

    private List<Endpoint> compressConsecutiveDuplicates(List<Endpoint> endpoints) {
        List<Endpoint> out = new ArrayList<>();
        Endpoint last = null;
        for (Endpoint e : endpoints) {
            if (last == null || !last.equals(e)) out.add(e);
            last = e;
        }
        return out;
    }

    // -------------------- shortest path on Endpoint graph (Lines) --------------------

    private Map<Integer, List<Integer>> cachedAdj = null;

    private Map<Integer, List<Integer>> adjacency() {
        if (cachedAdj != null) return cachedAdj;

        Map<Integer, List<Integer>> adj = new HashMap<>();
        try {
            for (Line l : railwayRepo.getAllLines()) {
                adj.computeIfAbsent(l.getEndpointA(), k -> new ArrayList<>()).add(l.getEndpointB());
                adj.computeIfAbsent(l.getEndpointB(), k -> new ArrayList<>()).add(l.getEndpointA());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        cachedAdj = adj;
        return adj;
    }

    /** Returns hop count distance, or -1 if unreachable. */
    private int shortestHopDistance(int startId, int endId) {
        if (startId == endId) return 0;

        Map<Integer, List<Integer>> adj = adjacency();
        ArrayDeque<Integer> q = new ArrayDeque<>();
        Map<Integer, Integer> dist = new HashMap<>();
        q.add(startId);
        dist.put(startId, 0);

        while (!q.isEmpty()) {
            int u = q.removeFirst();
            int du = dist.get(u);
            for (int v : adj.getOrDefault(u, List.of())) {
                if (dist.containsKey(v)) continue;
                dist.put(v, du + 1);
                if (v == endId) return du + 1;
                q.addLast(v);
            }
        }
        return -1;
    }

    /** Returns list of endpoints (objects) from start to end, inclusive, using BFS. */
    private List<Endpoint> shortestPathEndpoints(int startId, int endId) {
        if (startId == endId) {
            try {
                return List.of(railwayRepo.getEndpointById(startId));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        Map<Integer, List<Integer>> adj = adjacency();

        ArrayDeque<Integer> q = new ArrayDeque<>();
        Map<Integer, Integer> prev = new HashMap<>();
        Set<Integer> vis = new HashSet<>();
        q.add(startId);
        vis.add(startId);

        boolean found = false;
        while (!q.isEmpty() && !found) {
            int u = q.removeFirst();
            for (int v : adj.getOrDefault(u, List.of())) {
                if (vis.contains(v)) continue;
                vis.add(v);
                prev.put(v, u);
                if (v == endId) {
                    found = true;
                    break;
                }
                q.addLast(v);
            }
        }

        if (!found) return List.of();

        // Reconstruct
        List<Integer> ids = new ArrayList<>();
        int cur = endId;
        ids.add(cur);
        while (cur != startId) {
            cur = prev.get(cur);
            ids.add(cur);
        }
        Collections.reverse(ids);

        List<Endpoint> res = new ArrayList<>();
        try {
            for (int id : ids) res.add(railwayRepo.getEndpointById(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    // ==================== EXISTING (manual) USLP08 (chains) ====================

    public List<Train> getTrainsWithUnassignedPath() throws SQLException {
        return railwayRepo.getTrainsWithUnassignedPath();
    }

    public List<Freight> getFreightsByTrain(int trainId) throws SQLException {
        return railwayRepo.getFreightsByTrain(trainId);
    }

    public static class FreightChain {
        private final List<Freight> freights;

        public FreightChain(List<Freight> freights) {
            this.freights = freights;
        }

        public List<Freight> getFreights() { return freights; }
        public Endpoint getStart() { return freights.get(0).getOrigin(); }
        public Endpoint getEnd() { return freights.get(freights.size() - 1).getDestination(); }

        @Override
        public String toString() {
            return "Chain[" + freights.size() + "] " + getStart().getName() + " -> " + getEnd().getName();
        }
    }

    public List<FreightChain> buildChains(List<Freight> freights) {
        if (freights == null || freights.isEmpty()) return new ArrayList<>();

        Map<Integer, Deque<Freight>> outgoing = new HashMap<>();
        Map<Integer, Integer> inDeg = new HashMap<>();
        Map<Integer, Integer> outDeg = new HashMap<>();

        for (Freight f : freights) {
            int o = f.getOrigin().getId();
            int d = f.getDestination().getId();

            outgoing.computeIfAbsent(o, k -> new ArrayDeque<>()).add(f);

            outDeg.put(o, outDeg.getOrDefault(o, 0) + 1);
            inDeg.put(d, inDeg.getOrDefault(d, 0) + 1);

            inDeg.putIfAbsent(o, inDeg.getOrDefault(o, 0));
            outDeg.putIfAbsent(d, outDeg.getOrDefault(d, 0));
        }

        List<Integer> starters = new ArrayList<>(outgoing.keySet());
        starters.sort((a, b) -> {
            int da = outDeg.getOrDefault(a, 0) - inDeg.getOrDefault(a, 0);
            int db = outDeg.getOrDefault(b, 0) - inDeg.getOrDefault(b, 0);
            return Integer.compare(db, da);
        });

        List<FreightChain> chains = new ArrayList<>();

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

        for (Integer s : starters) {
            Deque<Freight> q = outgoing.get(s);
            if (q == null || q.isEmpty()) continue;

            List<Freight> ch = consumeFrom.apply(s);
            if (!ch.isEmpty()) chains.add(new FreightChain(ch));
        }

        for (Integer s : new ArrayList<>(outgoing.keySet())) {
            Deque<Freight> q = outgoing.get(s);
            if (q == null || q.isEmpty()) continue;

            List<Freight> ch = consumeFrom.apply(s);
            if (!ch.isEmpty()) chains.add(new FreightChain(ch));
        }

        chains.sort((c1, c2) -> Integer.compare(c2.getFreights().size(), c1.getFreights().size()));
        return chains;
    }

    public RoutePlan planRouteForChain(FreightChain chain) {
        if (chain == null || chain.getFreights().isEmpty()) {
            throw new IllegalArgumentException("Empty chain");
        }

        List<Freight> freights = chain.getFreights();
        RoutePlan plan = new RoutePlan();

        Freight first = freights.get(0);
        plan.addOperation(new RouteOperation(first.getOrigin(), first, RouteOperation.Type.LOAD));

        for (int i = 0; i < freights.size(); i++) {
            Freight cur = freights.get(i);
            plan.addOperation(new RouteOperation(cur.getDestination(), cur, RouteOperation.Type.UNLOAD));

            if (i < freights.size() - 1) {
                Freight next = freights.get(i + 1);
                plan.addOperation(new RouteOperation(cur.getDestination(), next, RouteOperation.Type.LOAD));
            }
        }

        return plan;
    }

    public Path savePlanAsPathAndAssignToTrain(int trainId, String pathName, RoutePlan plan) throws SQLException {
        if (pathName == null || pathName.trim().isEmpty()) throw new IllegalArgumentException("Empty path name");
        if (plan == null || plan.getOperations().isEmpty()) throw new IllegalArgumentException("Empty plan");

        Path path = railwayRepo.createPath(pathName.trim());

        List<Endpoint> stops = extractStopsInOrder(plan);
        int seq = 1;
        for (Endpoint stop : stops) {
            railwayRepo.addEndpointToPath(path.getId(), seq++, stop.getId(), true); // must_stop='Y'
        }

        railwayRepo.updateTrainPath(trainId, path.getId());
        return path;
    }

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



    /**
     * Generates an automatic connected route (preview only). Does NOT persist anything.
     */
    public List<Endpoint> autoPlanPreview(int leaderLocoId,
                                          Endpoint wagonsPickup,
                                          List<Freight> freights) throws SQLException {

        if (wagonsPickup == null) throw new IllegalArgumentException("wagonsPickup is null");
        if (freights == null || freights.isEmpty()) throw new IllegalArgumentException("No freights to plan");

        Endpoint locoStart = assemblyController.getLocomotiveCurrentEndpoint(leaderLocoId);
        if (locoStart == null) throw new IllegalArgumentException("Could not get locomotive current endpoint");

        List<Endpoint> waypoints = buildWaypointSequence(locoStart, wagonsPickup, freights);
        return expandWithIntermediateEndpoints(waypoints);
    }


    /**
     * Persists the given connected route as a Path and assigns it to the train.
     * It also sets must_stop='Y' for pickup + all freight origins/destinations.
     */
    public Path saveConnectedPathAndAssign(int trainId,
                                           String pathName,
                                           List<Endpoint> connectedRoute,
                                           Endpoint wagonsPickup,
                                           List<Freight> freights) throws SQLException {

        if (pathName == null || pathName.trim().isEmpty()) throw new IllegalArgumentException("Empty path name");
        if (connectedRoute == null || connectedRoute.isEmpty()) throw new IllegalArgumentException("Empty route");
        if (wagonsPickup == null) throw new IllegalArgumentException("wagonsPickup is null");
        if (freights == null || freights.isEmpty()) throw new IllegalArgumentException("Empty freights");

        // must-stop endpoints: pickup + all load/unload endpoints
        Set<Integer> mustStopIds = new HashSet<>();
        mustStopIds.add(wagonsPickup.getId());
        for (Freight f : freights) {
            mustStopIds.add(f.getOrigin().getId());
            mustStopIds.add(f.getDestination().getId());
        }

        return saveEndpointSequenceAsPathAndAssign(trainId, pathName.trim(), connectedRoute, mustStopIds);
    }

    /**
     * Parses user input "1 2 3 4" (endpoint IDs) into a list of Endpoint objects.
     */
    public List<Endpoint> parseEndpointsFromIds(String input) throws SQLException {
        if (input == null || input.trim().isEmpty()) return List.of();

        String[] parts = input.trim().split("\\s+");
        List<Endpoint> res = new ArrayList<>();

        for (String p : parts) {
            int id;
            try {
                id = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid endpoint id: " + p);
            }
            Endpoint e = railwayRepo.getEndpointById(id);
            if (e == null) throw new IllegalArgumentException("Endpoint not found: id=" + id);
            res.add(e);
        }
        return compressConsecutiveDuplicates(res);
    }

    /**
     * Ensures the route is physically connected by expanding each leg using BFS over Lines.
     * If the user route has "gaps", this fills intermediate endpoints automatically.
     */
    public List<Endpoint> ensureConnected(List<Endpoint> route) {
        if (route == null || route.isEmpty()) return List.of();
        return expandWithIntermediateEndpoints(compressConsecutiveDuplicates(route));
    }

}
