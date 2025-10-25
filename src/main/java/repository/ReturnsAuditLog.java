package repository;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Appends one human-readable audit line per USEI05 inspection to a text file.
 * <p>
 * Log format (single line per decision):
 * <pre>
 *   2025-10-24T12:34:56 | returnId=R001 | sku=SKU-A | action=Restocked|Discarded|Partial | qty=10 | qtyRestocked=... | qtyDiscarded=...
 * </pre>
 * The file is opened in append mode at every call; the method is safe to call repeatedly.
 */
public final class ReturnsAuditLog {

    /** Destination text file path (e.g., "warehouseMngt/returns_log.txt"). */
    private final String logPath;

    /** ISO-8601 local date-time, consistent with the rest of the project. */
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Creates a new audit logger that writes to the given path.
     *
     * @param logPath destination path; if the file does not exist it will be created
     */
    public ReturnsAuditLog(String logPath) {
        this.logPath = logPath;
    }

    /**
     * Writes one audit line for the given inspection decision.
     *
     * @param decision the inspection result to log (non-null)
     * @throws IOException if the underlying file cannot be written
     */
    public void append(ReturnDecision decision) throws IOException {
        if (decision == null) throw new IllegalArgumentException("decision is required.");

        ReturnEntry e = decision.getEntry();
        String actionLabel = labelFor(decision.getAction());

        String line = String.format(
                "%s | returnId=%s | sku=%s | action=%s | qty=%d | qtyRestocked=%d | qtyDiscarded=%d%n",
                decision.getDecidedAt().format(TS),
                e.getReturnId(),
                e.getSku(),
                actionLabel,
                e.getQuantity(),
                decision.getQtyRestocked(),
                decision.getQtyDiscarded()
        );

        // try-with-resources ensures the handle is closed; opened in append mode
        try (FileWriter fw = new FileWriter(logPath, true)) {
            fw.write(line);
        }
    }

    /** Maps {@link ReturnAction} to the human-readable token required in the log. */
    private static String labelFor(ReturnAction action) {
        return switch (action) {
            case RESTOCK_ALL -> "Restocked";
            case DISCARD_ALL -> "Discarded";
            case PARTIAL     -> "Partial";
        };
    }

    /** @return the current destination path (useful for diagnostics/tests). */
    public String getLogPath() {
        return logPath;
    }
}
