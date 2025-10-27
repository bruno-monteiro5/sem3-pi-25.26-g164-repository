package controller;

import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ReturnsCsvLoader}.
 * <p>
 * Covers all branches of {@link ReturnsCsvLoader#loadFromFile(String)}:
 * <ul>
 *   <li>Valid CSV with 6 columns (expiry present);</li>
 *   <li>Valid CSV with 5 columns (expiry absent);</li>
 *   <li>Malformed CSV rows with fewer than 5 columns;</li>
 *   <li>Missing file path;</li>
 *   <li>Header-skipping behavior.</li>
 * </ul>
 */
class ReturnsCsvLoaderTest {

    /**
     * Creates a temporary CSV file with the given content and returns its path.
     */
    private Path createTempCsv(String content) throws IOException {
        Path file = Files.createTempFile("returns-", ".csv");
        Files.writeString(file, content);
        return file;
    }

    /**
     * Verifies that a valid CSV with expiry dates is parsed correctly.
     */
    @Test
    void loadFromFile_validCsvWithExpiry_parsesAllRows() throws Exception {
        String csv = "returnId,sku,quantity,reason,timestamp,expiry\n" +
                "RET-01,SKU01,5,damaged,2025-10-27T10:00:00,2025-11-14\n" +
                "RET-02,SKU02,3,customer-remorse,2025-10-27T11:00:00,2025-11-20\n";

        Path path = createTempCsv(csv);
        try {
            List<ReturnEntry> list = ReturnsCsvLoader.loadFromFile(path.toString());
            assertEquals(2, list.size());

            ReturnEntry first = list.get(0);
            assertEquals("RET-01", first.getReturnId());
            assertEquals("SKU01", first.getSku());
            assertEquals(5, first.getQuantity());
            assertEquals(ReturnReason.DAMAGED, first.getReason());
            assertEquals(LocalDateTime.parse("2025-10-27T10:00:00"), first.getTimestamp());
            assertEquals("2025-11-14", first.getExpiryDate());
        } finally {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }

    /**
     * Verifies that expiry column can be omitted (5-column CSV) and expiry is null.
     */
    @Test
    void loadFromFile_validCsvWithoutExpiry_setsNullExpiry() throws Exception {
        String csv = "returnId,sku,quantity,reason,timestamp\n" +
                "RET-03,SKU03,2,cycle-count,2025-10-27T12:00:00\n";

        Path path = createTempCsv(csv);
        try {
            List<ReturnEntry> list = ReturnsCsvLoader.loadFromFile(path.toString());
            assertEquals(1, list.size());
            assertNull(list.get(0).getExpiryDate());
        } finally {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }

    /**
     * Verifies that a malformed row (fewer than 5 columns) triggers an IllegalArgumentException.
     */
    @Test
    void loadFromFile_malformedRow_throwsIllegalArgumentException() throws Exception {
        String csv = "returnId,sku,quantity,reason,timestamp,expiry\n" +
                "RET-BAD,SKU99,5,damaged\n"; // only 4 columns

        Path path = createTempCsv(csv);
        try {
            assertThrows(IllegalArgumentException.class, () ->
                    ReturnsCsvLoader.loadFromFile(path.toString()));
        } finally {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }

    /**
     * Verifies that a missing file triggers FileNotFoundException.
     */
    @Test
    void loadFromFile_missingFile_throwsFileNotFoundException() {
        assertThrows(java.io.FileNotFoundException.class, () ->
                ReturnsCsvLoader.loadFromFile("no-such-file.csv"));
    }

    /**
     * Verifies that the first line (header) is skipped and not treated as data.
     */
    @Test
    void loadFromFile_ignoresHeaderLine() throws Exception {
        String csv = "returnId,sku,quantity,reason,timestamp,expiry\n" +
                "RET-10,SKU10,1,expired,2025-10-27T13:00:00,2025-11-14\n";

        Path path = createTempCsv(csv);
        try {
            List<ReturnEntry> list = ReturnsCsvLoader.loadFromFile(path.toString());
            // only 1 data line -> one entry
            assertEquals(1, list.size());
            assertEquals("RET-10", list.get(0).getReturnId());
        } finally {
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
        }
    }
}
