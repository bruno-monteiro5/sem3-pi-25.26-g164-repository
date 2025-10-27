package ui;

import domain.Aisle;
import domain.Bay;
import domain.Box;
import domain.Warehouse;
import repository.WarehouseRepository;

import java.util.*;

public class StatsPrinterUI implements Runnable  {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
        if (warehouse == null) {
            System.out.println("There are no warehouses loaded.");
            pause();
            return;
        }
        browseStats(warehouse);
    }

    private void browseStats(Warehouse warehouse) {
        int opt;
        do {
            clear();
            System.out.println("=== WAREHOUSE STATS ===\n");
            System.out.printf("Warehouse: %s%n", warehouse.getWarehouseID());

            int totalBoxes = 0, totalUnits = 0;
            for (Aisle aisle : warehouse.getAisles()) {
                for (Bay bay : aisle.getBays()) {
                    for (Box box : bay.getInventory()) {
                        totalBoxes++;
                        totalUnits += safeQuantity(box);
                    }
                }
            }
            System.out.printf("Aisles: %d | Boxes: %d | Units: %d%n%n",
                    warehouse.getAisles().size(), totalBoxes, totalUnits);

            List<Aisle> aisles = warehouse.getAisles();
            for (int i = 0; i < aisles.size(); i++) {
                Aisle a = aisles.get(i);
                int[] agg = aggregateAisleStats(a);
                System.out.printf("  %d - Aisle %s  (Bays: %d | Boxes: %d | Units: %d)%n",
                        i + 1, aisleLabel(a), a.getBays().size(), agg[0], agg[1]);
            }

            System.out.println("\n0 - Back");
            opt = askInt("Select aisle : ", 0, aisles.size());
            if (opt > 0) browseAisle(warehouse, aisles.get(opt - 1));

        } while (opt != 0);
    }

    private void browseAisle(Warehouse w, Aisle a) {
        int opt;
        do {
            clear();
            System.out.printf("--- %s | Aisle %s ---%n%n", w.getWarehouseID(), aisleLabel(a));

            List<Bay> bays = a.getBays();
            for (int i = 0; i < bays.size(); i++) {
                Bay b = bays.get(i);
                int[] agg = aggregateBay(b);
                System.out.printf(" %3d - Bay %s  (Boxes: %d | Units: %d)%n",
                        i + 1, bayLabel(b), agg[0], agg[1]);
            }

            System.out.println("   0 - Back");
            opt = askInt("Select bay: ", 0, bays.size());
            if (opt > 0) showBayDetails(w, a, bays.get(opt - 1));
        } while (opt != 0);
    }

    private void showBayDetails(Warehouse w, Aisle a, Bay b) {
        clear();
        System.out.printf("--- Bay Details: %s | Aisle %s | Bay %s ---%n%n",
                w.getWarehouseID(), aisleLabel(a), bayLabel(b));

        List<Box> inv = new ArrayList<>(b.getInventory());
        Collections.sort(inv, new Comparator<Box>() {
            @Override
            public int compare(Box b1, Box b2) {
                String id1 = safeBoxId(b1);
                String id2 = safeBoxId(b2);
                return id1.compareTo(id2);
            }
        });

        System.out.println("Boxes:");
        System.out.println("  ────────────────────────────────────────────────────────────────────────");
        System.out.println("  Box ID       SKU          Qty          Expiry");
        System.out.println("  ────────────────────────────────────────────────────────────────────────");
        for (Box box : inv) {
            System.out.printf("  %-11s %-10s %7d %15s%n",
                    safeBoxId(box),
                    safeSku(box),
                    safeQuantity(box),
                    safeExpiry(box));
        }
        System.out.println("  ────────────────────────────────────────────────────────────────────────");

        Map<String, Integer> skuSum = new TreeMap<>();
        for (Box box : inv) skuSum.merge(safeSku(box), safeQuantity(box), Integer::sum);

        System.out.println("\nSKU Summary:");
        for (Map.Entry<String, Integer> e : skuSum.entrySet()) {
            System.out.printf("  %s: %d units%n", e.getKey(), e.getValue());
        }
        pause();
    }

    private int[] aggregateAisleStats(Aisle a) {
        int boxes = 0, units = 0;
        for (Bay b : a.getBays()) {
            for (Box box : b.getInventory()) {
                boxes++;
                units += safeQuantity(box);
            }
        }
        return new int[]{boxes, units};
    }

    private int[] aggregateBay(Bay b) {
        int boxes = 0, units = 0;
        for (Box box : b.getInventory()) {
            boxes++;
            units += safeQuantity(box);
        }
        return new int[]{boxes, units};
    }

    private int safeQuantity(Box box) {
        try { return box.getQuantity(); }
        catch (Exception e) { return 0; }
    }

    private String safeSku(Box box) {
        try { return box.getProduct().getSkuID(); }
        catch (Exception e) { return "UNKNOWN"; }
    }

    private String safeBoxId(Box box) {
        String id = box.getBoxID();
        return (id == null || id.isBlank()) ? "BOX?" : id;
    }

    private String safeExpiry(Box box) {
        String v = box.getExpDate(); // <-- o teu getter real
        return (v == null || v.isBlank()) ? "N/A" : v;
    }

    private String aisleLabel(Aisle a) {
        try { return String.valueOf(Aisle.class.getMethod("getAisleID").invoke(a)); }
        catch (Exception ignore) { return String.valueOf(a.hashCode()); }
    }
    private String bayLabel(Bay b) {
        try { return String.valueOf(Bay.class.getMethod("getBayID").invoke(b)); }
        catch (Exception ignore) { return String.valueOf(b.hashCode()); }
    }

    private int askInt(String msg, int min, int max) {
        int val = Integer.MIN_VALUE;
        while (val < min || val > max) {
            System.out.print(msg);
            String s = scanner.nextLine().trim();
            try { val = Integer.parseInt(s); }
            catch (NumberFormatException ignore) {
                System.out.printf("Invalid number (%d..%d)%n", min, max);
            }
        }
        return val;
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
