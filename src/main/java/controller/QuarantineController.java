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
 * {@link ReturnEntry#compareTo(ReturnEntry)}: newer timestamps first (descending),
 * then {@code returnId} ascending to break ties.
 */
public class QuarantineController {

    private final PriorityQueue<ReturnEntry> queue = new PriorityQueue<>();

    /**
     * Enqueues a single entry into quarantine.
     *
     * @param entry the entry to enqueue; must be non-null
     * @return {@code true} if the queue changed
     * @throws NullPointerException if {@code entry} is {@code null}
     */
    public boolean enqueue(ReturnEntry entry) {
        return queue.offer(entry);
    }

    /**
     * Enqueues multiple entries into quarantine.
     *
     * @param entries entries to enqueue; ignored if {@code null} or empty
     * @return number of entries actually enqueued
     */
    public int enqueueAll(Collection<ReturnEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;
        int added = 0;
        for (ReturnEntry e : entries) {
            if (queue.offer(e)) added++;
        }
        return added;
    }

    /**
     * Loads entries from a CSV file via {@link ReturnsCsvLoader} and enqueues them.
     *
     * @param csvPath path to {@code returns.csv}
     * @return number of entries enqueued
     * @throws Exception if loading or parsing fails
     */
    public int loadAndEnqueueFromCsv(String csvPath) throws Exception {
        List<ReturnEntry> entries = ReturnsCsvLoader.loadFromFile(csvPath);
        return enqueueAll(entries);
    }

    /**
     * Peeks at the next entry to inspect without removing it.
     *
     * @return the next entry, or {@code null} if the queue is empty
     */
    public ReturnEntry peekNext() {
        return queue.peek();
    }

    /**
     * Retrieves and removes the next entry to inspect.
     *
     * @return the next entry, or {@code null} if the queue is empty
     */
    public ReturnEntry pollNext() {
        return queue.poll();
    }

    /**
     * Returns the current number of entries in quarantine.
     *
     * @return the queue size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Indicates whether there are no entries waiting.
     *
     * @return {@code true} if the queue is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Provides a read-only view of the underlying queue for diagnostics.
     * <p>
     * Note: the returned {@link Queue} references the live structure and reflects
     * subsequent modifications; callers should not mutate it.
     *
     * @return the live queue reference for inspection
     */
    public Queue<ReturnEntry> viewQueue() {
        return queue;
    }
}
