package controller;

import domain.ReturnEntry;
import domain.ReturnReason;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class that loads return entries from a CSV file (USEI05).
 * <p>
 * Expected CSV format (header line is skipped):
 * <pre>
 *   returnId, sku, quantity, reason, timestamp, expiryDate
 * </pre>
 * Notes:
 * <ul>
 *   <li><b>timestamp</b> must be ISO-8601 local date-time (e.g., 2025-10-20T14:30:00)</li>
 *   <li><b>expiryDate</b> may be blank; blank is normalized to {@code null}</li>
 *   <li><b>reason</b> is parsed to {@link ReturnReason} (case-insensitive, hyphens allowed)</li>
 * </ul>
 */
public final class ReturnsCsvLoader {

    private ReturnsCsvLoader() { /* utility */ }

    /**
     * Reads all rows from the CSV and returns a list of {@link ReturnEntry}.
     *
     * @param csvPath path to {@code returns.csv}
     * @return list with one {@code ReturnEntry} per CSV row
     * @throws FileNotFoundException if the file does not exist
     * @throws IllegalArgumentException if a row is malformed
     */
    public static List<ReturnEntry> loadFromFile(String csvPath)
            throws FileNotFoundException {
        List<ReturnEntry> entries = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(csvPath))) {
            if (sc.hasNextLine()) sc.nextLine(); // skip header

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // keep empty trailing fields (expiry may be blank)
                String[] f = line.split(",", -1);

                if (f.length < 5) {
                    throw new IllegalArgumentException("Invalid CSV row (expected >=5 columns): " + line);
                }

                String returnId  = f[0].trim();
                String sku       = f[1].trim();
                int quantity     = Integer.parseInt(f[2].trim());

                // reason may contain hyphens, normalize via enum factory
                ReturnReason reason = ReturnReason.fromString(f[3].trim());

                LocalDateTime ts    = LocalDateTime.parse(f[4].trim());
                String expiry       = (f.length >= 6 && !f[5].trim().isEmpty()) ? f[5].trim() : null;

                entries.add(new ReturnEntry(returnId, sku, quantity, reason, ts, expiry));
            }
        }
        return entries;
    }
}
