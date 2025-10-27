package WarehouseManagementFunctions;

import controller.InventoryPort;
import controller.QuarantineController;
import controller.ReturnsInspectionController;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import repository.InventoryAdapter;
import repository.ReturnsAuditLog;
import ui.WarehouseManagementMenu;

/**
 * User interface for inspecting returned goods (USEI05).
 * <p>
 * This UI loads return entries from a CSV file, queues them for quarantine inspection,
 * and processes each entry automatically by either restocking or discarding.
 * Every decision is registered in an audit log. If restocked, the returned product is
 * inserted back into the warehouse using the InventoryPort adapter.
 */
public class ReturnsInspectionUI implements Runnable {

    /**
     * Executes the automatic inspection process for all returned goods.
     * Loads the CSV file into quarantine, processes each return entry based on its reason,
     * and logs every decision. In case of critical errors (e.g., warehouse not initialized),
     * the process is aborted gracefully and the user is returned to the menu.
     */
    @Override
    public void run() {
        QuarantineController quarantine = new QuarantineController();
        InventoryPort inventory = new InventoryAdapter();
        ReturnsAuditLog auditLog = new ReturnsAuditLog("warehouseMngt/train_station_dataset/returns_audit_log.txt");
        ReturnsInspectionController controller = new ReturnsInspectionController(quarantine, inventory, auditLog);

        try {
            int loaded = quarantine.loadAndEnqueueFromCsv("warehouseMngt/train_station_dataset/returns.csv");
            if (loaded == 0) {
                System.out.println("No return entries found in the CSV file.");
                return;
            }
            System.out.println("Successfully loaded " + loaded + " return entries into quarantine.");

            while (true) {
                ReturnEntry entry = controller.previewNext();
                if (entry == null) {
                    System.out.println("\n All return entries have been inspected.");
                    break;
                }

                System.out.println("\n--- Inspecting Return Entry ---");
                System.out.println("Return ID: " + entry.getReturnId());
                System.out.println("SKU: " + entry.getSku());
                System.out.println("Quantity: " + entry.getQuantity());
                System.out.println("Reason: " + entry.getReason());
                if (entry.getExpiryDate() != null && !entry.getExpiryDate().isBlank()) {
                    System.out.println("Expiry Date: " + entry.getExpiryDate());
                }

                ReturnDecision decision;
                if (entry.getReason() == ReturnReason.DAMAGED || entry.getReason() == ReturnReason.EXPIRED) {
                    decision = controller.discardAllNext();
                    System.out.println(" Discarded all " + decision.getQtyDiscarded() + " unit(s).");
                } else {
                    decision = controller.restockAllNext();
                    System.out.println(" Restocked " + decision.getQtyRestocked() + " unit(s).");
                }
            }

        } catch (Exception e) {
            System.out.println("\nError during inspection: " + e.getMessage());
            System.out.println("Returning to the Warehouse Management Menu.");
            System.out.println();
            Runnable war = new WarehouseManagementMenu();
            war.run();

        }
    }
}
