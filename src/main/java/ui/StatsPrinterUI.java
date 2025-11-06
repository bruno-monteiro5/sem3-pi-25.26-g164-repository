package ui;

import domain.Aisle;
import domain.Bay;
import domain.Box;
import domain.Warehouse;
import repository.WarehouseRepository;

import java.util.*;

public class StatsPrinterUI implements Runnable {

    private static final boolean COLOR_ENABLED = true;
    private static final String COLOR_GREEN = COLOR_ENABLED ? "\u001B[32m" : "";
    private static final String COLOR_CYAN  = COLOR_ENABLED ? "\u001B[36m" : "";
    private static final String COLOR_RED   = COLOR_ENABLED ? "\u001B[31m" : "";
    private static final String COLOR_RESET = COLOR_ENABLED ? "\u001B[0m"  : "";
    private static final String SEP_CHAR    = "─";
    private static final String COL_DIV     = "│";
    private static final String BULLET      = "•";

    private final Scanner input = new Scanner(System.in);

    @Override
    public void run() {
        Warehouse warehouse = WarehouseRepository.getCurrentWarehouse();
        if (warehouse == null) {
            System.out.println(COLOR_RED + "[ERROR] No warehouse is loaded." + COLOR_RESET);
            pauseForKey();
            new WarehouseManagementMenu().run();
            return;
        }
        showWarehouseOverview(warehouse);
    }

    private void showWarehouseOverview(Warehouse warehouse) {
        int choice;
        do {
            clearScreen();
            printBanner("WAREHOUSE STATS");
            printBreadcrumbs(new String[]{"Home", "Stats"});

            int[] totals = aggregateWarehouse(warehouse);
            System.out.println();
            System.out.println(lineOf(78));
            System.out.printf("  Warehouse: %s%n", warehouse.getWarehouseID());
            System.out.printf("  Aisles: %d   Boxes: %d   Units: %d%n",
                    warehouse.getAisles().size(), totals[0], totals[1]);
            System.out.println(lineOf(78));

            System.out.println();
            System.out.println(COLOR_CYAN + "Aisles" + COLOR_RESET);
            System.out.println(lineOf(78));
            List<Aisle> aisles = warehouse.getAisles();
            for (Aisle aisle : aisles) {
                int[] aisleAgg = aggregateAisle(aisle);
                System.out.printf("  %s Aisle %s  Bays: %d   Boxes: %d   Units: %d%n",
                        COL_DIV, labelForAisle(aisle), aisle.getBays().size(), aisleAgg[0], aisleAgg[1]);
            }
            System.out.println(lineOf(78));

            System.out.println();
            System.out.println("  0 " + COL_DIV + " Back");
            choice = askInt("Select aisle number (1-" + aisles.size() + "): ", 0, aisles.size());
            if (choice > 0) showAisleBays(warehouse, aisles.get(choice - 1));
        } while (choice != 0);

        new WarehouseManagementMenu().run();
    }

    private void showAisleBays(Warehouse warehouse, Aisle aisle) {
        int choice;
        do {
            clearScreen();
            printBanner("WAREHOUSE STATS");
            printBreadcrumbs(new String[]{"Home", "Stats", "Aisle " + labelForAisle(aisle)});

            List<Bay> bays = aisle.getBays();
            int maxBoxCount = 1;
            for (Bay b : bays) {
                int count = countBoxes(b);
                if (count > maxBoxCount) maxBoxCount = count;
            }

            System.out.println();
            System.out.println(COLOR_CYAN + "Bays" + COLOR_RESET);
            System.out.println(lineOf(78));
            for (Bay bay : bays) {
                int[] bayAgg = aggregateBay(bay);
                String bar = COLOR_GREEN + relativeBar(20, bayAgg[0], maxBoxCount) + COLOR_RESET;
                System.out.printf("  %s Bay %-3s  Boxes: %-4d  Units: %-5d  %s%n",
                        COL_DIV, labelForBay(bay), bayAgg[0], bayAgg[1], bar);
            }
            System.out.println(lineOf(78));

            System.out.println();
            System.out.println("  0 " + COL_DIV + " Back");
            choice = askInt("Select bay number (1-" + bays.size() + "): ", 0, bays.size());
            if (choice > 0) showBayBoxes(warehouse, aisle, bays.get(choice - 1));
        } while (choice != 0);
    }

    private void showBayBoxes(Warehouse warehouse, Aisle aisle, Bay bay) {
        clearScreen();
        printBanner("WAREHOUSE STATS");
        printBreadcrumbs(new String[]{"Home", "Stats", "Aisle " + labelForAisle(aisle), "Bay " + labelForBay(bay)});
        System.out.println(lineOf(78));

        List<Box> boxes = new ArrayList<>(bay.getInventory());
        Collections.sort(boxes, new Comparator<Box>() {
            @Override
            public int compare(Box a, Box b) {
                String id1 = safeBoxId(a);
                String id2 = safeBoxId(b);
                return id1.compareTo(id2);
            }
        });

        System.out.println();
        System.out.println(COLOR_CYAN + "Boxes" + COLOR_RESET);
        System.out.println("  " + lineOf(75));
        System.out.println("  " + padFixed("Box ID", 12) + padFixed("SKU", 12) + padRightFixed("Qty", 8) + padRightFixed("Expiry", 18));
        System.out.println("  " + lineOf(75));

        for (Box bx : boxes) {
            String id = safeBoxId(bx);
            String sku = safeSku(bx);
            String qty = String.valueOf(safeQuantity(bx));
            String exp = safeExpiry(bx);
            System.out.println("  " + padFixed(id, 12) + padFixed(sku, 12) + padRightFixed(qty, 8) + padRightFixed(exp, 18));
        }
        System.out.println("  " + lineOf(75));

        Map<String, Integer> perSku = new TreeMap<>();
        for (Box bx : boxes) {
            String sku = safeSku(bx);
            int add = safeQuantity(bx);
            perSku.put(sku, perSku.getOrDefault(sku, 0) + add);
        }

        System.out.println();
        System.out.println(COLOR_CYAN + "SKU Summary" + COLOR_RESET);
        System.out.println(lineOf(78));
        for (Map.Entry<String, Integer> e : perSku.entrySet()) {
            System.out.printf("  %-10s : %d%n", e.getKey(), e.getValue());
        }
        System.out.println(lineOf(78));

        System.out.println();
        System.out.println("  0 " + COL_DIV + " Back");
        pauseForKey();
    }

    private int[] aggregateWarehouse(Warehouse warehouse) {
        int totalBoxes = 0, totalUnits = 0;
        for (Aisle aisle : warehouse.getAisles()) {
            int[] agg = aggregateAisle(aisle);
            totalBoxes += agg[0];
            totalUnits += agg[1];
        }
        return new int[]{totalBoxes, totalUnits};
    }

    private int[] aggregateAisle(Aisle aisle) {
        int totalBoxes = 0, totalUnits = 0;
        for (Bay bay : aisle.getBays()) {
            int[] agg = aggregateBay(bay);
            totalBoxes += agg[0];
            totalUnits += agg[1];
        }
        return new int[]{totalBoxes, totalUnits};
    }

    private int[] aggregateBay(Bay bay) {
        int totalBoxes = 0, totalUnits = 0;
        for (Box box : bay.getInventory()) {
            totalBoxes++;
            totalUnits += safeQuantity(box);
        }
        return new int[]{totalBoxes, totalUnits};
    }

    private int countBoxes(Bay bay) {
        List<Box> inv = bay.getInventory();
        return inv == null ? 0 : inv.size();
    }

    private int safeQuantity(Box box) {
        try { return box.getQuantity(); } catch (Exception e) { return 0; }
    }

    private String safeSku(Box box) {
        try { return box.getProduct().getSkuID(); } catch (Exception e) { return "UNKNOWN"; }
    }

    private String safeBoxId(Box box) {
        try {
            String id = box.getBoxID();
            return (id == null || id.trim().isEmpty()) ? "BOX?" : id;
        } catch (Exception e) {
            return "BOX?";
        }
    }

    private String safeExpiry(Box box) {
        try {
            String s = box.getExpDate();
            return (s == null || s.trim().isEmpty()) ? "N/A" : s;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String labelForAisle(Aisle aisle) {
        try { return String.valueOf(Aisle.class.getMethod("getAisleID").invoke(aisle)); }
        catch (Exception ignore) { return String.valueOf(aisle.hashCode()); }
    }

    private String labelForBay(Bay bay) {
        try { return String.valueOf(Bay.class.getMethod("getBayID").invoke(bay)); }
        catch (Exception ignore) { return String.valueOf(bay.hashCode()); }
    }

    private int askInt(String prompt, int min, int max) {
        int val = Integer.MIN_VALUE;
        do {
            System.out.print(prompt);
            String s = input.nextLine().trim();
            try {
                val = Integer.parseInt(s);
                if (val < min || val > max) {
                    System.out.println(COLOR_RED + "Invalid option. Please choose between " + min + " and " + max + "." + COLOR_RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(COLOR_RED + "Invalid number. Please enter digits only." + COLOR_RESET);
                val = Integer.MIN_VALUE;
            }
        } while (val < min || val > max);
        return val;
    }

    private void pauseForKey() {
        System.out.print("\nPress Enter to continue...");
        input.nextLine();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String lineOf(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(SEP_CHAR);
        return sb.toString();
    }

    private String padFixed(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private String padRightFixed(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder();
        while (sb.length() < width - s.length()) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    private String relativeBar(int width, int value, int max) {
        if (max <= 0) max = 1;
        int fill = (int) Math.round((value * 1.0 / max) * width);
        if (fill < 0) fill = 0;
        if (fill > width) fill = width;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append(i < fill ? '█' : '░');
        return sb.toString();
    }

    private void printBanner(String title) {
        String line = COLOR_CYAN + lineOf(78) + COLOR_RESET; // design comentado
        System.out.println(line);
        System.out.println(center(title, 78));
        System.out.println(line);
    }

    private static String center(String s, int width) { // design comentado
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < pad; i++) left.append(' ');
        return left + s;
    }

    private void printBreadcrumbs(String[] crumbs) {
        StringBuilder sb = new StringBuilder();
        sb.append(BULLET).append(" ");
        for (int i = 0; i < crumbs.length; i++) {
            sb.append(crumbs[i]);
            if (i < crumbs.length - 1) sb.append(" ").append(COL_DIV).append(" ");
        }
        System.out.println(sb.toString());
    }
}
