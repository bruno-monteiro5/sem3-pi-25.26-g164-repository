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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class QuarantineControllerTest {

    // Helper to create a ReturnEntry
    private ReturnEntry makeEntry(String id, String sku, int qty, ReturnReason reason, String ts) {
        return new ReturnEntry(id, sku, qty, reason, LocalDateTime.parse(ts), null);
    }

    @Test
    void testEnqueueAndSizePeekPoll() {
        QuarantineController qc = new QuarantineController();
        assertTrue(qc.isEmpty());
        ReturnEntry e1 = makeEntry("A", "SKU1", 5, ReturnReason.DAMAGED, "2025-10-20T10:00:00");
        ReturnEntry e2 = makeEntry("B", "SKU2", 2, ReturnReason.CUSTOMER_REMORSE,   "2025-10-21T09:00:00");
        assertTrue(qc.enqueue(e1));
        assertEquals(1, qc.size());
        assertFalse(qc.isEmpty());
        assertTrue(qc.enqueue(e2));
        assertEquals(2, qc.size());

        // Newer timestamp first (e2 is newer), so peekNext() should be e2
        assertEquals(e2, qc.peekNext());
        // peek does not remove
        assertEquals(2, qc.size());

        // Poll should return e2 then e1
        ReturnEntry p1 = qc.pollNext();
        assertEquals(e2, p1);
        assertEquals(1, qc.size());
        ReturnEntry p2 = qc.pollNext();
        assertEquals(e1, p2);
        assertTrue(qc.isEmpty());
        assertNull(qc.peekNext());
        assertNull(qc.pollNext());
    }

    @Test
    void testEnqueueAllAndViewQueue(@TempDir Path tempDir) throws Exception {
        QuarantineController qc = new QuarantineController();
        // Prepare a temporary CSV file for loadAndEnqueueFromCsv
        Path csv = tempDir.resolve("returns.csv");
        try (PrintWriter pw = new PrintWriter(csv.toFile())) {
            pw.println("returnId,sku,quantity,reason,timestamp,expiryDate");
            pw.println("R1,SKU-A,10,DAMAGED,2025-10-22T08:30:00,");     // no expiry
            pw.println("R2,SKU-B,3,customer-remorse,2025-10-22T09:00:00,2026-12-31");
        }
        // Using loadAndEnqueueFromCsv should add 2 entries
        int count = qc.loadAndEnqueueFromCsv(csv.toString());
        assertEquals(2, count);
        assertEquals(2, qc.size());
        // viewQueue should contain the same elements
        assertEquals(qc.size(), qc.viewQueue().size());

        // Verify peekNext logic on newly loaded entries:
        // Entry R2 has later time (09:00 vs 08:30), so it should be first
        ReturnEntry next = qc.peekNext();
        assertEquals("R2", next.getReturnId());
    }
}
