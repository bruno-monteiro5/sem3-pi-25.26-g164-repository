package controller;

import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link QuarantineController}.
 * <p>
 * Covers all public methods: {@code enqueue}, {@code enqueueAll}, {@code loadAndEnqueueFromCsv},
 * {@code peekNext}, {@code pollNext}, {@code size}, {@code isEmpty}, and {@code viewQueue}.
 * The tests verify ordering (newest timestamp first; id ascending as tie-breaker) and
 * CSV loading behavior, including error paths.
 */
class QuarantineControllerTest {

    private static ReturnEntry entry(String id, String sku, int qty, ReturnReason reason, LocalDateTime ts, String expiry) {
        return new ReturnEntry(id, sku, qty, reason, ts, expiry);
    }

    /**
     * Verifies that a single valid enqueue inserts into the priority queue and updates size.
     */
    @Test
    void enqueue_single_increasesSize_andIsPeekable() {
        QuarantineController qc = new QuarantineController();
        ReturnEntry e = entry("RET-001", "SKU1", 3, ReturnReason.CUSTOMER_REMORSE,
                LocalDateTime.parse("2025-10-27T10:00:00"), null);

        assertTrue(qc.enqueue(e));
        assertEquals(1, qc.size());
        assertFalse(qc.isEmpty());
        assertSame(e, qc.peekNext());
    }

    /**
     * Verifies that {@code enqueue(null)} propagates NPE from the underlying queue.
     */
    @Test
    void enqueue_null_throwsNullPointerException() {
        QuarantineController qc = new QuarantineController();
        assertThrows(NullPointerException.class, () -> qc.enqueue(null));
    }

    /**
     * Verifies that {@code enqueueAll(null)} returns 0 and does not change the queue.
     */
    @Test
    void enqueueAll_null_returnsZero_andDoesNotChange() {
        QuarantineController qc = new QuarantineController();
        assertEquals(0, qc.enqueueAll(null));
        assertEquals(0, qc.size());
        assertTrue(qc.isEmpty());
    }

    /**
     * Verifies that {@code enqueueAll(empty)} returns 0 and does not change the queue.
     */
    @Test
    void enqueueAll_empty_returnsZero_andDoesNotChange() {
        QuarantineController qc = new QuarantineController();
        assertEquals(0, qc.enqueueAll(List.of()));
        assertEquals(0, qc.size());
        assertTrue(qc.isEmpty());
    }

    /**
     * Verifies bulk enqueue adds all entries and preserves the ordering policy:
     * newest timestamp first; when equal, returnId ascending.
     */
    @Test
    void enqueueAll_ordersByTimestampDesc_thenIdAsc_onPeekAndPoll() {
        QuarantineController qc = new QuarantineController();

        // Same timestamp, different IDs -> id ascending wins as tie-breaker
        LocalDateTime sameTs = LocalDateTime.parse("2025-10-27T12:00:00");
        ReturnEntry a = entry("RET-002", "SKU", 1, ReturnReason.DAMAGED, sameTs, null);
        ReturnEntry b = entry("RET-001", "SKU", 1, ReturnReason.DAMAGED, sameTs, null);

        // Older and newer to check primary ordering by timestamp (newer first)
        ReturnEntry newest = entry("RET-100", "SKU", 1, ReturnReason.CYCLE_COUNT,
                LocalDateTime.parse("2025-10-27T13:00:00"), null);
        ReturnEntry oldest = entry("RET-000", "SKU", 1, ReturnReason.CUSTOMER_REMORSE,
                LocalDateTime.parse("2025-10-27T09:00:00"), null);

        int added = qc.enqueueAll(List.of(a, b, oldest, newest));
        assertEquals(4, added);
        assertEquals(4, qc.size());

        // Expected peek order:
        // 1) newest (13:00)
        // 2) among a & b with same TS (12:00): id ascending => RET-001 first, then RET-002
        // 3) oldest (09:00) last
        assertSame(newest, qc.peekNext());

        assertSame(newest, qc.pollNext());
        assertSame(b, qc.pollNext());         // RET-001 before RET-002
        assertSame(a, qc.pollNext());
        assertSame(oldest, qc.pollNext());
        assertTrue(qc.isEmpty());
    }

    /**
     * Verifies that {@code peekNext()} returns null when empty and does not remove elements when non-empty.
     */
    @Test
    void peekNext_returnsNullWhenEmpty_andDoesNotRemove() {
        QuarantineController qc = new QuarantineController();
        assertNull(qc.peekNext());

        ReturnEntry e = entry("RET-9", "SKU9", 2, ReturnReason.EXPIRED,
                LocalDateTime.parse("2025-10-27T11:00:00"), "2025-11-14");
        qc.enqueue(e);

        assertSame(e, qc.peekNext());
        // size unchanged by peek
        assertEquals(1, qc.size());
    }

    /**
     * Verifies that {@code pollNext()} removes and returns the head entry; returns null when empty.
     */
    @Test
    void pollNext_removesHead_andReturnsNullWhenEmpty() {
        QuarantineController qc = new QuarantineController();

        ReturnEntry e1 = entry("RET-1", "SKU1", 1, ReturnReason.DAMAGED,
                LocalDateTime.parse("2025-10-27T10:00:00"), null);
        ReturnEntry e2 = entry("RET-2", "SKU2", 1, ReturnReason.DAMAGED,
                LocalDateTime.parse("2025-10-27T11:00:00"), null);

        qc.enqueueAll(List.of(e1, e2));
        assertEquals(2, qc.size());

        // Newer (11:00) should come out first
        assertSame(e2, qc.pollNext());
        assertEquals(1, qc.size());
        assertSame(e1, qc.pollNext());
        assertTrue(qc.isEmpty());

        // Empty returns null
        assertNull(qc.pollNext());
    }

    /**
     * Verifies that {@code viewQueue()} exposes a live reference that reflects subsequent changes.
     * The test does not mutate the returned queue (as documented).
     */
    @Test
    void viewQueue_returnsLiveReference_reflectingChanges() {
        QuarantineController qc = new QuarantineController();
        Queue<ReturnEntry> view = qc.viewQueue();

        assertNotNull(view);
        assertTrue(view.isEmpty());

        ReturnEntry e = entry("RET-77", "SKU77", 5, ReturnReason.CUSTOMER_REMORSE,
                LocalDateTime.parse("2025-10-27T10:30:00"), null);
        qc.enqueue(e);

        assertEquals(1, view.size());   // reflects live structure
        assertSame(e, view.peek());
    }

    /**
     * Verifies that {@code loadAndEnqueueFromCsv} reads a valid CSV and enqueues all rows.
     */
    @Test
    void loadAndEnqueueFromCsv_validFile_enqueuesRows() throws Exception {
        QuarantineController qc = new QuarantineController();

        // Build a temporary CSV matching ReturnsCsvLoader's expected format.
        // Header is skipped; expiry may be blank.
        Path tmp = Files.createTempFile("returns-", ".csv");
        try {
            String header = "returnId,sku,quantity,reason,timestamp,expiry\n";
            String r1 = "RET-10,SKU10,3,customer-remorse,2025-10-27T12:00:00,";
            String r2 = "RET-11,SKU11,1,expired,2025-10-27T13:00:00,2025-11-14";
            Files.writeString(tmp, header + r1 + "\n" + r2 + "\n");

            int loaded = qc.loadAndEnqueueFromCsv(tmp.toString());
            assertEquals(2, loaded);
            assertEquals(2, qc.size());

            // Newest first -> RET-11 (13:00)
            assertEquals("RET-11", qc.peekNext().getReturnId());
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) { }
        }
    }

    /**
     * Verifies that an invalid path triggers an exception from the loader.
     */
    @Test
    void loadAndEnqueueFromCsv_invalidPath_throwsException() {
        QuarantineController qc = new QuarantineController();
        assertThrows(Exception.class, () -> qc.loadAndEnqueueFromCsv("no-such-file.csv"));
    }
}
