/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.graph.priorityqueue;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * An implementation of a priority queue using an array-based heap.
 *
 * @author Michael T. Goodrich
 * @author Roberto Tamassia
 * @author Michael H. Goldwasser
 *
 *
 */
public class HeapPriorityQueue<K,V> extends AbstractPriorityQueue<K,V> {
    /** primary collection of priority queue entries */
    protected ArrayList<Entry<K,V>> heap = new ArrayList<>();

    /** Creates an empty priority queue based on the natural ordering of its keys. */
    public HeapPriorityQueue() { super(); }

    /**
     * Creates an empty priority queue using the given comparator to order keys.
     * @param comp comparator defining the order of keys in the priority queue
     */
    public HeapPriorityQueue(Comparator<K> comp) { super(comp); }

    /**
     * Creates a priority queue initialized with the respective
     * key-value pairs.  The two arrays given will be paired
     * element-by-element. They are presumed to have the same
     * length. (If not, entries will be created only up to the length of
     * the shorter of the arrays)
     * @param keys an array of the initial keys for the priority queue
     * @param values an array of the initial values for the priority queue
     */
    public HeapPriorityQueue(K[] keys, V[] values) {
        super();
        int n = Math.min(keys.length, values.length);
        for (int i = 0; i < n; i++) {
            checkKey(keys[i]);
            heap.add(new PQEntry<>(keys[i], values[i]));
        }
        buildHeap();  }

    // protected utilities
    protected int parent(int j) { return (j-1) / 2; }     // truncating division
    protected int left(int j) { return 2*j + 1; }
    protected int right(int j) { return 2*j + 2; }
    protected boolean hasLeft(int j) { return left(j) < heap.size(); }
    protected boolean hasRight(int j) { return right(j) < heap.size(); }

    /** Exchanges the entries at indices i and j of the array list. */
    protected void swap(int i, int j) {
        Entry<K,V> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    /** Moves the entry at index j higher, if necessary, to restore the heap property. */
    protected void percolateUp(int j) {
        while (j > 0) {
            int p = parent(j);
            if (compare(heap.get(j), heap.get(p)) >= 0) break;
            swap(j, p);
            j = p;
        }
    }

    /** Moves the entry at index j lower, if necessary, to restore the heap property. */
    protected void percolateDown(int j) {
        while (hasLeft(j)) {
            int smallChild = left(j);
            if (hasRight(j) && compare(heap.get(right(j)), heap.get(smallChild)) < 0) {
                smallChild = right(j);
            }
            if (compare(heap.get(smallChild), heap.get(j)) >= 0) break;
            swap(j, smallChild);
            j = smallChild;
        }
    }

    /** Performs a batch bottom-up construction of the heap in O(n) time. */
    protected void buildHeap() {
        int start = parent(heap.size() - 1);
        for (int j = start; j >= 0; j--) {
            percolateDown(j);
        }
    }

    // public methods

    /**
     * Returns the number of items in the priority queue.
     * @return number of items
     */
    @Override
    public int size() { return heap.size(); }

    /**
     * Returns (but does not remove) an entry with minimal key.
     * @return entry having a minimal key (or null if empty)
     */
    @Override
    public Entry<K,V> min() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    /**
     * Inserts a key-value pair and return the entry created.
     * @param key     the key of the new entry
     * @param value   the associated value of the new entry
     * @return the entry storing the new key-value pair
     * @throws IllegalArgumentException if the key is unacceptable for this queue
     */
    @Override
    public Entry<K,V> insert(K key, V value) throws IllegalArgumentException {
        checkKey(key);
        Entry<K,V> newest = new PQEntry<>(key, value);
        heap.add(newest);
        percolateUp(heap.size() - 1);
        return newest;  }

    /**
     * Removes and returns an entry with minimal key.
     * @return the removed entry (or null if empty)
     */
    @Override
    public Entry<K,V> removeMin() {
        if (heap.isEmpty()) return null;
        Entry<K,V> answer = heap.get(0);
        swap(0, heap.size() - 1);
        heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) percolateDown(0);
        return answer;  }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HeapPriorityQueue: [");
        for (int i = 0; i < heap.size(); i++) {
            Entry<K, V> e = heap.get(i);
            sb.append("<").append(e.getKey()).append(", ").append(e.getValue()).append(">");
            if (i < heap.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }



    @Override
    public HeapPriorityQueue<K, V> clone() {
        HeapPriorityQueue<K, V> other = new HeapPriorityQueue<>(this.comp);
        for (Entry<K, V> e : this.heap) {
            other.insert(e.getKey(), e.getValue());
        }
        return other;
    }



}

