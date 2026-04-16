// Developed by: Mohammad Sheikh Qasem
package com.huffman.model;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class MinHeapPriorityQueue {

    private final ArrayList<HuffmanNode> heap;


    public MinHeapPriorityQueue() {
        this.heap = new ArrayList<>();
    }


    public MinHeapPriorityQueue(ArrayList<HuffmanNode> nodes) {
        this.heap = new ArrayList<>(nodes);
        // Start from the last internal node and sift down
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }


    public void insert(HuffmanNode node) {
        heap.add(node);
        heapifyUp(heap.size() - 1);
    }

    public HuffmanNode poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("Priority queue is empty");
        }
        HuffmanNode min = heap.get(0);

        // Move last element to root position, then remove last
        int lastIndex = heap.size() - 1;
        heap.set(0, heap.get(lastIndex));
        heap.remove(lastIndex);

        // Restore heap property
        if (!heap.isEmpty()) {
            heapifyDown(0);
        }
        return min;
    }


    public HuffmanNode peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Priority queue is empty");
        }
        return heap.get(0);
    }


    public ArrayList<HuffmanNode> pollK(int k) {
        if (k > size()) {
            throw new IllegalArgumentException(
                    "Cannot poll " + k + " nodes from a queue of size " + size());
        }
        ArrayList<HuffmanNode> result = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            result.add(poll());
        }
        return result;
    }

    public boolean isEmpty() { return heap.isEmpty(); }

    public int size() { return heap.size(); }


    public ArrayList<HuffmanNode> getHeapSnapshot() {
        return new ArrayList<>(heap);
    }


    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break; // Heap property satisfied
            }
        }
    }

    private void heapifyDown(int index) {
        int size = heap.size();
        while (true) {
            int smallest  = index;
            int leftChild  = 2 * index + 1;
            int rightChild = 2 * index + 2;

            if (leftChild < size && heap.get(leftChild).compareTo(heap.get(smallest)) < 0) {
                smallest = leftChild;
            }
            if (rightChild < size && heap.get(rightChild).compareTo(heap.get(smallest)) < 0) {
                smallest = rightChild;
            }
            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break; // Heap property satisfied
            }
        }
    }

    private void swap(int i, int j) {
        HuffmanNode temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MinHeap[");

        for (int i = 0; i < heap.size(); i++){
            HuffmanNode n = heap.get(i);

            String label  = n.getCharacter() != null ? (n.getCharacter() == ' ' ? "SPC" : String.valueOf(n.getCharacter())) : "INT";

            sb.append(label).append(":").append(n.getFrequency());

            if (i < heap.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

}

/* ==== README ==== */

/**
 * ============================================================
 * MinHeapPriorityQueue.java
 * ------------------------------------------------------------
 * A custom Min-Heap Priority Queue built from scratch using
 * a dynamic array (ArrayList) as the underlying storage.
 *
 * WHY a custom heap instead of java.util.PriorityQueue?
 * The project requires demonstrating data-structure knowledge,
 * so this class exposes the classic heap operations explicitly:
 *   - heapifyUp   (bubble-up after insert)
 *   - heapifyDown (sift-down after removal)
 *
 * Ordering: delegates to HuffmanNode.compareTo(), which orders
 * nodes by frequency ascending (min-heap property).
 *
 * Complexity:
 *   insert / poll → O(log n)
 *   peek / size   → O(1)
 *   buildHeap     → O(n)
 * ============================================================
 */

/** Internal storage – index 0 is the root (minimum element). */

/** Internal storage – index 0 is the root (minimum element). */

// ─────────────────────────────────────────────────────────
// Constructors
// ─────────────────────────────────────────────────────────

/** Creates an empty priority queue. */

/**
 * Creates a priority queue from an existing ArrayList of nodes.
 * Uses Floyd's linear-time heap construction (O(n)).
 *
 * @param nodes initial ArrayList of HuffmanNodes
 */

// ─────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────

/**
 * Inserts a node into the priority queue.
 * Appends at the end then bubbles up to restore heap property.
 * Time: O(log n)
 *
 * @param node the HuffmanNode to insert
 */

/**
 * Removes and returns the node with the minimum frequency (the root).
 * Swaps root with the last element, removes it, then sifts down.
 * Time: O(log n)
 *
 * @return HuffmanNode with the smallest frequency
 * @throws NoSuchElementException if the queue is empty
 */

/**
 * Returns (without removing) the node with the minimum frequency.
 * Time: O(1)
 *
 * @return HuffmanNode at the root
 * @throws NoSuchElementException if the queue is empty
 */

/**
 * Polls exactly {@code k} nodes in frequency-ascending order.
 * Used by the extended Huffman algorithm where k nodes are
 * removed per iteration.
 *
 * @param k number of nodes to remove
 * @return ArrayList of k nodes (smallest first)
 * @throws IllegalArgumentException if k > size()
 */
/** @return true if no elements remain in the queue. */

/** @return the number of elements currently in the queue. */

/**
 * Returns a copy of the internal heap as an ArrayList.
 * Intended for debugging / display purposes only.
 *
 * @return ArrayList copy of the internal heap
 */

// ─────────────────────────────────────────────────────────
// Private heap maintenance helpers
// ─────────────────────────────────────────────────────────

/**
 * Bubbles element at {@code index} upward until the heap property
 * is satisfied. Called after every insert.
 *
 * @param index position of the newly inserted element
 */

/**
 * Sifts element at {@code index} downward until the heap property
 * is satisfied. Called after every poll.
 *
 * @param index position of the element to sift down
 */

/**
 * Swaps two elements in the internal array.
 *
 * @param i first index
 * @param j second index
 */
// ─────────────────────────────────────────────────────────
// Debugging
// ─────────────────────────────────────────────────────────

/**
 * Returns a human-readable level-order representation of the heap.
 * Useful for visualising the heap structure in logs.
 */





