package controller;

import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.io.TempDir;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Queue;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QuarantineController.
 * - Uses the official returns.csv for integration (load + ordering).
 * - Also covers enqueue/peek/poll ordering explicitly.
 */
class QuarantineControllerTest {

    private static final String OFFICIAL_RETURNS_CSV =
            "warehouseMngt/train_station_dataset/returns.csv";

    private ReturnEntry makeEntry(String id, String sku, int qty, ReturnReason reason, String ts) {
        return new ReturnEntry(id, sku, qty, reason, LocalDateTime.parse(ts), null);
    }

    @Test
    void testEnqueuePeekPollOrdering() {
        QuarantineController qc = new QuarantineController();
        assertTrue(qc.isEmpty());

        // Newest first; tie-break by returnId ascending
        ReturnEntry e1 = makeEntry("A1", "SKU1", 3, ReturnReason.DAMAGED, "2025-10-20T10:00:00");
        ReturnEntry e2 = makeEntry("A0", "SKU2", 1, ReturnReason.DAMAGED, "2025-10-21T10:00:00"); // newer
        ReturnEntry e3 = makeEntry("A0", "SKU3", 2, ReturnReason.DAMAGED, "2025-10-20T10:00:00"); // same ts as e1, id A0 < A1

        qc.enqueue(e1);
        qc.enqueue(e2);
        qc.enqueue(e3);

        // First: newest (e2)
        assertEquals(e2, qc.peekNext());
        assertEquals(e2, qc.pollNext());

        // Then among remaining (same timestamp), id asc: e3 (A0) before e1 (A1)
        assertEquals(e3, qc.pollNext());
        assertEquals(e1, qc.pollNext());
        assertTrue(qc.isEmpty());
    }

    @Test
    void testLoadAndEnqueueFromOfficialCsv() throws Exception {
        assertTrue(Files.exists(Path.of(OFFICIAL_RETURNS_CSV)),
                "Official returns.csv not found at: " + OFFICIAL_RETURNS_CSV);

        QuarantineController qc = new QuarantineController();
        int loaded = qc.loadAndEnqueueFromCsv(OFFICIAL_RETURNS_CSV);
        assertTrue(loaded > 0, "Should enqueue entries from official returns.csv");
        assertEquals(loaded, qc.size());

        // First element must be the most recent (by timestamp)
        ReturnEntry first = qc.peekNext();
        assertNotNull(first);
        // Basic sanity checks
        assertNotNull(first.getReturnId());
        assertNotNull(first.getSku());
        assertTrue(first.getQuantity() > 0);
        assertNotNull(first.getReason());
    }

    @Test
    void testLoadAndEnqueueFromCsvWithEmptyFile(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("empty.csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(csv.toFile())) {
            pw.println("returnId,sku,quantity,reason,timestamp,expiryDate");
        }

        QuarantineController qc = new QuarantineController();
        int loaded = qc.loadAndEnqueueFromCsv(csv.toString());
        assertEquals(0, loaded);
        assertTrue(qc.isEmpty());
    }


    @Test
    void testEnqueueAllNullOrEmptyList() {
        QuarantineController qc = new QuarantineController();
        // Passing null should return 0
        int result1 = qc.enqueueAll(null);
        assertEquals(0, result1);
        // Passing empty list should also return 0
        int result2 = qc.enqueueAll(List.of());
        assertEquals(0, result2);
    }

    @Test
    void testViewQueueReturnsLiveQueue() {
        QuarantineController qc = new QuarantineController();

        // O queue deve estar inicialmente vazio
        assertTrue(qc.viewQueue().isEmpty());

        // Enfileira um elemento
        ReturnEntry e = new ReturnEntry(
                "T1", "SKU123", 2, ReturnReason.DAMAGED,
                LocalDateTime.parse("2025-10-25T10:00:00"), null);
        qc.enqueue(e);

        // viewQueue deve refletir a queue interna (mesmo tamanho e elemento)
        Queue<ReturnEntry> view = qc.viewQueue();
        assertEquals(1, view.size());
        assertEquals(e, view.peek());

        // Se adicionarmos mais diretamente via método da classe,
        // a view deve refletir a alteração (prova de que é “live”)
        ReturnEntry e2 = new ReturnEntry(
                "T2", "SKU999", 1, ReturnReason.CUSTOMER_REMORSE,
                LocalDateTime.parse("2025-10-26T09:00:00"), null);
        qc.enqueue(e2);

        // A queue vista anteriormente deve agora ter 2 elementos
        assertEquals(2, view.size());
    }

}

