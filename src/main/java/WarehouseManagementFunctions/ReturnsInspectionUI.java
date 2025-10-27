package WarehouseManagementFunctions;

import controller.InventoryPort;
import controller.QuarantineController;
import controller.ReturnsInspectionController;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnAction;
import repository.InventoryAdapter;
import ui.WarehouseManagementMenu;

import java.time.format.DateTimeFormatter;

/**
 * User interface for inspecting returned goods (USEI05).
 * Loads return entries, processes each automatically, and prints inspection log lines to the console.
 */
public class ReturnsInspectionUI implements Runnable {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String SEP    = "─";

    @Override
    public void run() {
        QuarantineController quarantine = new QuarantineController();
        InventoryPort inventory = new InventoryAdapter();
        ReturnsInspectionController controller = new ReturnsInspectionController(quarantine, inventory, null);

        try {
            System.out.println();
            System.out.println(CYAN + line(72));
            System.out.println(center("RETURNS INSPECTION (USEI05)", 72));
            System.out.println(line(72) + RESET);

            int loaded = quarantine.loadAndEnqueueFromCsv("warehouseMngt/train_station_dataset/returns.csv");
            if (loaded == 0) return;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while (true) {
                ReturnEntry entry = controller.previewNext();
                if (entry == null) break;

                ReturnDecision decision;
                switch (entry.getReason()) {
                    case EXPIRED, DAMAGED -> decision = controller.discardAllNext();
                    case CYCLE_COUNT -> {
                        int half = entry.getQuantity() / 2;
                        decision = controller.partialRestockNext(half);
                    }
                    case CUSTOMER_REMORSE -> decision = controller.restockAllNext();
                    default -> throw new IllegalStateException("Unexpected return reason: " + entry.getReason());
                }

                StringBuilder sb = new StringBuilder();
                sb.append(decision.getTimestamp().format(formatter))
                        .append(" | returnId=").append(decision.getReturnId())
                        .append(" | sku=").append(decision.getSku())
                        .append(" | action=");

                if (decision.getAction() == ReturnAction.RESTOCK_ALL) {
                    sb.append("Restocked");
                } else if (decision.getAction() == ReturnAction.DISCARD_ALL) {
                    sb.append("Discarded");
                } else if (decision.getAction() == ReturnAction.PARTIAL) {
                    sb.append("Partially Restocked")
                            .append(" | qtyRestocked=").append(decision.getQtyRestocked())
                            .append(" | qtyDiscarded=").append(decision.getQtyDiscarded());
                }

                System.out.println(sb.toString());
            }

            System.out.println(CYAN + line(72) + RESET);
            System.out.println(GREEN + "Returns inspection finished." + RESET);
            System.out.println(line(72));

        } catch (Exception e) {
            System.out.println(RED + "\nError during inspection: " + e.getMessage() + RESET);
            Runnable war = new WarehouseManagementMenu();
            war.run();
            return;
        }

        System.out.println();
        Runnable war = new WarehouseManagementMenu();
        war.run();
    }

    private static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }
    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < pad; i++) left.append(' ');
        return left + s;
    }
}
