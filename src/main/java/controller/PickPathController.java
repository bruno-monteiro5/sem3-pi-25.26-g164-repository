package controller;

import domain.Allocation;
import domain.PickStop;

import java.util.*;

public class PickPathController {

    /** Junta allocations no mesmo (aisle,bay) somando qty. */
    public List<PickStop> mergeStops(List<Allocation> allocations) {
        Map<String, PickStop> map = new LinkedHashMap<>();
        for (Allocation a : allocations) {
            String key = a.getAisle() + ":" + a.getBay();
            map.compute(key, (k, old) -> {
                if (old == null) return new PickStop(a.getAisle().getAisleID(), a.getBay().getBayID(), a.getQuantity());
                old.addQty(a.getQuantity());
                return old;
            });
        }
        return new ArrayList<>(map.values());
    }

    /** Distância D do enunciado. */
    public int distance(int a1, int b1, int a2, int b2) {
        if (a1 == a2) {
            return Math.abs(b1 - b2);
        } else {
            return b1 + Math.abs(a1 - a2) * 3 + b2; // desce ao corredor, move horizontal, sobe
        }
    }

    //strategy A
    public PickPathResult sequenceStrategyA(List<PickStop> stops) {
        List<PickStop> ordered = new ArrayList<>(stops);
        Collections.sort(ordered, new Comparator<PickStop>() {
            @Override
            public int compare(PickStop p1, PickStop p2) {
                if (p1.getAisle() != p2.getAisle()) {
                    return p1.getAisle() - p2.getAisle();
                }
                return p1.getBay() - p2.getBay();
            }
        });
        int total = totalDistance(ordered);
        return new PickPathResult(ordered, total);
    }

    //strategy B
    public PickPathResult sequenceStrategyB(List<PickStop> stops) {
        List<PickStop> remaining = new ArrayList<>(stops);
        List<PickStop> route = new ArrayList<PickStop>();
        int ca = 0, cb = 0; // start at (0,0)

        while (!remaining.isEmpty()) {
            PickStop best = null;
            int bestD = Integer.MAX_VALUE;

            for (PickStop s : remaining) {
                int d = distance(ca, cb, s.getAisle(), s.getBay());
                if (best == null || d < bestD ||
                        (d == bestD && (s.getAisle() < best.getAisle() ||
                                (s.getAisle() == best.getAisle() && s.getBay() < best.getBay())))) {
                    bestD = d;
                    best = s;
                }
            }

            route.add(best);
            ca = best.getAisle();
            cb = best.getBay();
            remaining.remove(best);
        }

        int total = totalDistance(route);
        return new PickPathResult(route, total);
    }


    /** Distância total desde (0,0) seguindo a sequência fornecida (sem regresso). */
    public int totalDistance(List<PickStop> ordered) {
        int total = 0;
        int ca = 0, cb = 0;
        for (PickStop s : ordered) {
            total += distance(ca, cb, s.getAisle(), s.getBay());
            ca = s.getAisle();
            cb = s.getBay();
        }
        return total;
    }

    /** Resultado para impressão/consumo por UI. */
    public static class PickPathResult {
        private final List<PickStop> path;
        private final int totalDistance;

        public PickPathResult(List<PickStop> path, int totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }

        public List<PickStop> getPath() { return path; }
        public int getTotalDistance() { return totalDistance; }
    }
}