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
 * Utility class that loads return entries from a CSV file for USEI05.
 * <p>
 * Expected CSV format (header line is skipped):
 * <pre>
 * returnId, sku, quantity, reason, timestamp, expiryDate
 * </pre>
 * <ul>
 *   <li>{@code timestamp} must be an ISO-8601 local date-time (e.g., {@code 2025-10-20T14:30:00})</li>
 *   <li>{@code expiryDate} may be blank; blank values are normalized to {@code null}</li>
 *   <li>{@code reason} is mapped to {@link ReturnReason} (case-insensitive, hyphens allowed)</li>
 * </ul>
 */
public final class ReturnsCsvLoader {

    private ReturnsCsvLoader() {
    }

    /**
     * Reads all rows from the given CSV file and creates a list of {@link ReturnEntry} instances.
     * <p>
     * This method ignores the first line of the file (assumed to be a header)
     * and validates that each data line has at least five columns. It allows
     * an optional sixth column representing an expiry date, which may be blank.
     *
     * @param csvPath absolute or relative path to {@code returns.csv}
     * @return a list containing one {@link ReturnEntry} per CSV row
     * @throws FileNotFoundException    if the specified file does not exist
     * @throws IllegalArgumentException if a CSV row is malformed or incomplete
     */
    public static List<ReturnEntry> loadFromFile(String csvPath)
            throws FileNotFoundException {
        List<ReturnEntry> entries = new ArrayList<>();
        try (Scanner sc = new Scanner(new File(csvPath))) {
            if (sc.hasNextLine()) sc.nextLine();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] f = line.split(",", -1);

                if (f.length < 5) {
                    throw new IllegalArgumentException(
                            "Invalid CSV row (expected >= 5 columns): " + line);
                }

                String returnId = f[0].trim();
                String sku = f[1].trim();
                int quantity = Integer.parseInt(f[2].trim());
                ReturnReason reason = ReturnReason.fromString(f[3].trim());
                LocalDateTime ts = LocalDateTime.parse(f[4].trim());
                String expiry = (f.length >= 6 && !f[5].trim().isEmpty()) ? f[5].trim() : null;

                entries.add(new ReturnEntry(returnId, sku, quantity, reason, ts, expiry));
            }
        }
        return entries;
    }
}
