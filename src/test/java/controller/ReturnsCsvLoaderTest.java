package controller;

import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReturnsCsvLoader.
 * Uses the real project dataset (returns.csv) for integration coverage,
 * and @TempDir only for error/edge cases.
 */
class ReturnsCsvLoaderTest {

    /** Path in the repository to the official dataset file. */
    private static final String OFFICIAL_RETURNS_CSV =
            "warehouseMngt/train_station_dataset/returns.csv";

    @Test
    void testLoadOfficialReturnsCsv() throws Exception {
        // Ensure the file exists (so failures are meaningful)
        assertTrue(Files.exists(Path.of(OFFICIAL_RETURNS_CSV)),
                "Official returns.csv not found at: " + OFFICIAL_RETURNS_CSV);

        List<ReturnEntry> entries = ReturnsCsvLoader.loadFromFile(OFFICIAL_RETURNS_CSV);
        assertNotNull(entries);
        assertTrue(entries.size() > 0, "Official returns.csv should have entries");

        // Sanity-check a couple of fields of the first row
        ReturnEntry first = entries.get(0);
        assertNotNull(first.getReturnId());
        assertNotNull(first.getSku());
        assertTrue(first.getQuantity() > 0);
        assertNotNull(first.getReason());
        assertNotNull(first.getTimestamp());
        // timestamp is a LocalDateTime; compare as object (not by toString formatting)
        assertTrue(first.getTimestamp().isBefore(LocalDateTime.now().plusYears(50)));
    }

    @Test
    void testMalformedRowThrows(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("bad.csv");
        try (PrintWriter pw = new PrintWriter(csv.toFile())) {
            pw.println("returnId,sku,quantity,reason,timestamp,expiryDate");
            pw.println("RET-001,ABC,5"); // too few columns → should throw
        }
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ReturnsCsvLoader.loadFromFile(csv.toString()));
        assertTrue(ex.getMessage().toLowerCase().contains("expected >=5"),
                "Should mention invalid column count");
    }

    @Test
    void testMissingFileThrows() {
        assertThrows(java.io.FileNotFoundException.class, () ->
                ReturnsCsvLoader.loadFromFile("this/file/does/not/exist.csv"));
    }
}
