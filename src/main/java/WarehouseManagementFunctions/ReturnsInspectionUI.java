package WarehouseManagementFunctions;

import controller.InventoryPort;
import controller.QuarantineController;
import controller.ReturnsCsvLoader;
import controller.ReturnsInspectionController;
import domain.ReturnDecision;
import domain.ReturnEntry;
import repository.InventoryAdapter;
import repository.ReturnsAuditLog;

import java.util.List;
import java.util.Scanner;

/**
 * User interface for inspecting returned goods (USEI05).
 * <p>
 * This UI loads return entries from a CSV file, queues them for quarantine inspection,
 * and allows the user to interactively choose whether to restock, discard, or partially
 * restock each return.
 * <p>
 * Every decision taken is registered in an audit log, and if restocked, the product is
 * inserted back into the warehouse through an InventoryPort adapter.
 */
public class ReturnsInspectionUI implements Runnable {

    /**
     * Runs the user interface for processing returned items.
     * <p>
     * - Loads return entries from CSV into quarantine.
     * - Iteratively prompts user for decision on each return.
     * - Records audit log for every decision.
     * - Uses InventoryAdapter to update warehouse stock if restocked.
     */
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        QuarantineController quarantine = new QuarantineController();
        InventoryPort inventory = new InventoryAdapter();
        ReturnsAuditLog auditLog = new ReturnsAuditLog("warehouseMngt/train_station_dataset/returns_audit_log.txt");
        ReturnsInspectionController controller = new ReturnsInspectionController(quarantine, inventory, auditLog);

        // Load return entries from CSV
        try {
            List<ReturnEntry> entries = ReturnsCsvLoader.loadFromFile("warehouseMngt/train_station_dataset/returns.csv");
            int loaded = quarantine.enqueueAll(entries);
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

            System.out.println("\nSelect inspection action:");
            System.out.println("1 - Restock all units");
            System.out.println("2 - Discard all units");
            System.out.println("3 - Partial restock");
            System.out.print("Your choice: ");
            String option = sc.nextLine().trim();

            try {
                ReturnDecision decision;
                switch (option) {
                    case "1":
                        decision = controller.restockAllNext();
                        System.out.println("✅ Restocked " + decision.getQtyRestocked() + " unit(s).");
                        break;
                    case "2":
                        decision = controller.discardAllNext();
                        System.out.println("✅ Discarded all " + decision.getQtyDiscarded() + " unit(s).");
                        break;
                    case "3":
                        System.out.print("Enter quantity to restock (0 to " + entry.getQuantity() + "): ");
                        String qtyStr = sc.nextLine().trim();
                        int qtyToRestock = Integer.parseInt(qtyStr);
                        if (qtyToRestock < 0 || qtyToRestock > entry.getQuantity()) {
                            System.out.println("❌ Invalid quantity. Try again.");
                            continue;
                        }
                        decision = controller.partialRestockNext(qtyToRestock);
                        System.out.println("✅ Restocked: " + decision.getQtyRestocked() +
                                " | Discarded: " + decision.getQtyDiscarded());
                        break;
                    default:
                        System.out.println("❌ Invalid option. Please enter 1, 2, or 3.");
                        continue;
                }
            } catch (Exception ex) {
                System.out.println("❌ Error processing return: " + ex.getMessage());
            }
        }
    }
}
