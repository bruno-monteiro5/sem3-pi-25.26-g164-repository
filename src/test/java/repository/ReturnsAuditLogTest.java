package repository;

import domain.ReturnAction;
import domain.ReturnDecision;
import domain.ReturnEntry;
import domain.ReturnReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReturnsAuditLog aligned with the assignment and the current implementation.
 *
 * Log format produced by the class under test:
 * - Discard all:
 *   <ISO_LOCAL_DATE_TIME> | returnId=<ID> | sku=<SKU> | action=Discarded
 * - Restock all:
 *   <ISO_LOCAL_DATE_TIME> | returnId=<ID> | sku=<SKU> | action=Restocked | qty=<total>
 * - Partial:
 *   <ISO_LOCAL_DATE_TIME> | returnId=<ID> | sku=<SKU> | action=Partial | qty=<total> | qtyRestocked=<x> | qtyDiscarded=<y>
 */
class ReturnsAuditLogTest {

    // Accept:
    //  yyyy-MM-dd'T'HH:mm
    //  yyyy-MM-dd'T'HH:mm:ss
    //  yyyy-MM-dd'T'HH:mm:ss.S.. (1 to 9 fractional digits)
    private static final Pattern ISO_LOCAL_DT =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,9})?)?$");

    private static ReturnEntry entry(String id, String sku, int qty) {
        return new ReturnEntry(
                id,
                sku,
                qty,
                ReturnReason.DAMAGED, // reason is irrelevant for logging format
                LocalDateTime.parse("2025-10-24T12:34:56"),
                null
        );
    }

    @Test
    void testAppend_producesLinesPerSpec(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("audit.log");
        ReturnsAuditLog audit = new ReturnsAuditLog(logFile.toString());

        // total = 10
        ReturnEntry base = entry("R001", "SKU-A", 10);

        // 1) discard all (no qty field expected)
        ReturnDecision dDiscard = new ReturnDecision(base, ReturnAction.DISCARD_ALL, 0, 10);
        audit.append(dDiscard);

        // 2) restock all (qty=<total>)
        ReturnDecision dRestock = new ReturnDecision(base, ReturnAction.RESTOCK_ALL, 10, 0);
        audit.append(dRestock);

        // 3) partial (qty=<total> + qtyRestocked + qtyDiscarded)
        ReturnDecision dPartial = new ReturnDecision(base, ReturnAction.PARTIAL, 6, 4);
        audit.append(dPartial);

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(3, lines.size(), "Exactly 3 audit lines expected");

        // ---------- line 1: Discarded ----------
        String l1 = lines.get(0);
        String[] p1 = splitAudit(l1);
        assertTrue(ISO_LOCAL_DT.matcher(p1[0]).matches(), "L1[0] must be ISO_LOCAL_DATE_TIME, got: " + p1[0]);
        assertEquals("returnId=R001", p1[1]);
        assertEquals("sku=SKU-A", p1[2]);
        assertEquals("action=Discarded", p1[3]);
        // no qty on Discarded
        assertEquals(4, p1.length, "Discarded line must NOT contain qty fields");

        // ---------- line 2: Restocked ----------
        String l2 = lines.get(1);
        String[] p2 = splitAudit(l2);
        assertTrue(ISO_LOCAL_DT.matcher(p2[0]).matches(), "L2[0] must be ISO_LOCAL_DATE_TIME");
        assertEquals("returnId=R001", p2[1]);
        assertEquals("sku=SKU-A", p2[2]);
        assertEquals("action=Restocked", p2[3]);
        assertEquals(5, p2.length, "Restocked line must contain qty only");
        assertEquals("qty=10", p2[4]);

        // ---------- line 3: Partial ----------
        String l3 = lines.get(2);
        String[] p3 = splitAudit(l3);
        assertTrue(ISO_LOCAL_DT.matcher(p3[0]).matches(), "L3[0] must be ISO_LOCAL_DATE_TIME");
        assertEquals("returnId=R001", p3[1]);
        assertEquals("sku=SKU-A", p3[2]);
        assertEquals("action=Partial", p3[3]);
        assertEquals("qty=10", p3[4]);
        assertEquals("qtyRestocked=6", p3[5]);
        assertEquals("qtyDiscarded=4", p3[6]);
        assertEquals(7, p3.length, "Partial line must contain qty + qtyRestocked + qtyDiscarded");
    }

    @Test
    void testConstructorAndGetter(@TempDir Path tempDir) {
        Path logFile = tempDir.resolve("audit.log");
        ReturnsAuditLog audit = new ReturnsAuditLog(logFile.toString());
        assertEquals(logFile.toString(), audit.getLogPath());
    }

    @Test
    void testAppendNullThrows(@TempDir Path tempDir) {
        ReturnsAuditLog audit = new ReturnsAuditLog(tempDir.resolve("audit.log").toString());
        assertThrows(IllegalArgumentException.class, () -> audit.append(null));
    }

    // -------- helpers --------
    private static String[] splitAudit(String line) {
        // split by " | " exactly (same as the implementation uses)
        return line.split("\\s\\|\\s");
    }
}
