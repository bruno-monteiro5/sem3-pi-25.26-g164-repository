package controller;

import domain.ReturnEntry;

import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Manages the quarantine queue for returned goods (USEI05).
 * <p>
 * Internally uses a {@link PriorityQueue} with the ordering defined by
 * {@link ReturnEntry#compareTo(ReturnEntry)}:
 * newer timestamps first (descending), then returnId ascending to break ties.
 */
public class QuarantineController {

    private final PriorityQueue<ReturnEntry> queue = new PriorityQueue<>();

    /**
     * Enqueues a single entry into quarantine.
     * @param entry the entry to enqueue
     * @return true if the queue changed
     */
    public boolean enqueue(ReturnEntry entry) {
        return queue.offer(entry);
    }

    /**
     * Enqueues multiple entries into quarantine.
     * @param entries entries to enqueue
     * @return number of entries actually enqueued
     */
    public int enqueueAll(Collection<ReturnEntry> entries) {
        int added = 0;
        for (ReturnEntry e : entries) {
            if (queue.offer(e)) added++;
        }
        return added;
    }

    /**
     * Convenience helper: loads from CSV using {@link ReturnsCsvLoader}
     * and enqueues all parsed entries.
     *
     * @param csvPath path to {@code returns.csv}
     * @return number of entries enqueued
     * @throws Exception if loading or parsing fails
     */
    public int loadAndEnqueueFromCsv(String csvPath) throws Exception {
        List<ReturnEntry> entries = ReturnsCsvLoader.loadFromFile(csvPath);
        return enqueueAll(entries);
    }

    /** @return the next entry to inspect (peek, no removal), or {@code null} if empty. */
    public ReturnEntry peekNext() {
        return queue.peek();
    }

    /** @return and remove the next entry to inspect, or {@code null} if empty. */
    public ReturnEntry pollNext() {
        return queue.poll();
    }

    /** @return current number of entries in quarantine. */
    public int size() {
        return queue.size();
    }

    /** @return {@code true} if no entries are waiting. */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Exposes a read-only view of the underlying queue for diagnostic purposes.
     * Note: the returned {@link Queue} is still the live structure, use with care.
     */
    public Queue<ReturnEntry> viewQueue() {
        return queue;
    }
}
