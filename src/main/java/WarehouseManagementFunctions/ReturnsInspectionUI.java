package WarehouseManagementFunctions;

import controller.InventoryPort;
import controller.QuarantineController;
import controller.ReturnsInspectionController;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import repository.InventoryAdapter;
import repository.ReturnsAuditLog;

/**
 * User interface for inspecting returned goods (USEI05).
 * <p>
 * This UI loads return entries from a CSV file, queues them for quarantine inspection,
 * and processes each entry automatically by either restocking or discarding.
 * Every decision taken is registered in an audit log, and if restocked,
 * the product is inserted back into the warehouse through an InventoryPort adapter.
 */
public class ReturnsInspectionUI implements Runnable {

    /**
     * Runs the user interface for processing returned items.
     * <p>
     * - Loads return entries from CSV into quarantine.
     * - Automatically inspects each return: restocks or discards based on reason.
     * - Records audit log for every decision.
     */
    @Override
    public void run() {
        QuarantineController quarantine = new QuarantineController();
        InventoryPort inventory = new InventoryAdapter();
        ReturnsAuditLog auditLog = new ReturnsAuditLog("warehouseMngt/train_station_dataset/returns_audit_log.txt");
        ReturnsInspectionController controller = new ReturnsInspectionController(quarantine, inventory, auditLog);

        // Load return entries from CSV
        try {
            int loaded = quarantine.loadAndEnqueueFromCsv("warehouseMngt/train_station_dataset/returns.csv");
            if (loaded == 0) {
                System.out.println("No return entries found in CSV.");
                return;
            }
            System.out.println("Loaded " + loaded + " return entries into quarantine.");
        } catch (Exception e) {
            System.out.println("Error loading returns from CSV: " + e.getMessage());
            return;
        }

        // Process entries one by one
        while (true) {
            ReturnEntry entry = controller.previewNext();
            if (entry == null) {
                System.out.println("\n✅ All returns have been inspected.");
                break;
            }

            System.out.println("\n--- Next Return Entry ---");
            System.out.println("Return ID: " + entry.getReturnId());
            System.out.println("SKU: " + entry.getSku());
            System.out.println("Quantity: " + entry.getQuantity());
            System.out.println("Reason: " + entry.getReason());
            if (entry.getExpiryDate() != null && !entry.getExpiryDate().isBlank()) {
                System.out.println("Expiry Date: " + entry.getExpiryDate());
            }

            try {
                ReturnDecision decision;
                if (entry.getReason() == ReturnReason.DAMAGED || entry.getReason() == ReturnReason.EXPIRED) {
                    decision = controller.discardAllNext();
                    System.out.println("✅ Discarded all " + decision.getQtyDiscarded() + " unit(s).");
                } else {
                    decision = controller.restockAllNext();
                    System.out.println("✅ Restocked " + decision.getQtyRestocked() + " unit(s).");
                }
            } catch (Exception ex) {
                System.out.println("❌ Error processing return: " + ex.getMessage());
            }
        }
    }
}
