// src/test/java/controller/ReturnsCsvLoaderTest.java
package controller;

import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReturnsCsvLoaderTest {

    @Test
    void testLoadValidCsv(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("returns.csv");
        try (PrintWriter pw = new PrintWriter(csv.toFile())) {
            pw.println("returnId,sku,quantity,reason,timestamp,expiryDate");
            pw.println("RET-001,ABC,5,CUSTOMER-REMORSE,2025-10-20T14:30:00,2025-12-31");
            pw.println("RET-002,XYZ,1,DAMAGED,2025-10-21T09:15:00,"); // blank expiry
        }

        List<ReturnEntry> entries = ReturnsCsvLoader.loadFromFile(csv.toString());
        assertEquals(2, entries.size());

        ReturnEntry first = entries.get(0);
        assertEquals("RET-001", first.getReturnId());
        assertEquals("ABC", first.getSku());
        assertEquals(5, first.getQuantity());
        assertEquals(ReturnReason.CUSTOMER_REMORSE, first.getReason());
        // ✅ comparar como LocalDateTime (não usar toString)
        assertEquals(LocalDateTime.parse("2025-10-20T14:30:00"), first.getTimestamp());
        assertEquals("2025-12-31", first.getExpiryDate());

        ReturnEntry second = entries.get(1);
        assertEquals("RET-002", second.getReturnId());
        assertEquals("XYZ", second.getSku());
        assertEquals(1, second.getQuantity());
        assertEquals(ReturnReason.DAMAGED, second.getReason());
        // ✅ idem aqui
        assertEquals(LocalDateTime.parse("2025-10-21T09:15:00"), second.getTimestamp());
        assertNull(second.getExpiryDate());
    }

    @Test
    void testMalformedRowThrows(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("bad.csv");
        try (PrintWriter pw = new PrintWriter(csv.toFile())) {
            pw.println("returnId,sku,quantity,reason,timestamp");
            pw.println("RET-001,ABC,5"); // too short
        }
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ReturnsCsvLoader.loadFromFile(csv.toString())
        );
        assertTrue(ex.getMessage().contains("expected >=5 columns"));
    }

    @Test
    void testMissingFileThrows() {
        assertThrows(java.io.FileNotFoundException.class, () ->
                ReturnsCsvLoader.loadFromFile("nonexistent.csv")
        );
    }
}
