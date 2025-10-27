package WarehouseManagementFunctions;

import controller.InventoryPort;
import controller.QuarantineController;
import controller.ReturnsInspectionController;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import domain.ReturnAction;
import repository.InventoryAdapter;
import ui.WarehouseManagementMenu;

import java.time.format.DateTimeFormatter;

/**
 * User interface for inspecting returned goods (USEI05).
 * Loads return entries, processes each automatically, and prints inspection log lines to the console.
 */
public class ReturnsInspectionUI implements Runnable {

    @Override
    public void run() {
        QuarantineController quarantine = new QuarantineController();
        InventoryPort inventory = new InventoryAdapter();
        ReturnsInspectionController controller = new ReturnsInspectionController(quarantine, inventory, null);

        try {
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

        } catch (Exception e) {
            System.out.println("\nError during inspection: " + e.getMessage());
            Runnable war = new WarehouseManagementMenu();
            war.run();
        }
        System.out.println();
        Runnable war = new WarehouseManagementMenu();
        war.run();

    }
}
