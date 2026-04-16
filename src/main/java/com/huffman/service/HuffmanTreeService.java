// Developed by: Mohammad Sheikh Qasem
package com.huffman.service;

import com.huffman.model.HuffmanNode;
import com.huffman.model.MinHeapPriorityQueue;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class HuffmanTreeService {

    public MinHeapPriorityQueue buildPriorityQueue(Map<Character, Integer> frequencies){

        if (frequencies == null || frequencies.isEmpty()){
            throw new IllegalArgumentException("Frequency map must not be null or empty");
        }

        MinHeapPriorityQueue pq = new MinHeapPriorityQueue();
        frequencies.forEach((ch, freq) -> pq.insert(new HuffmanNode(ch, freq)));
        return pq;
    }

    public HuffmanNode buildTree(Map<Character, Integer> frequencies){
        MinHeapPriorityQueue pq = buildPriorityQueue(frequencies);

        // Edge case: only one distinct character
        if (pq.size() == 1){
            HuffmanNode only = pq.poll();
            return new HuffmanNode(only.getFrequency(), only, null);
        }

        while (pq.size() > 1){
            HuffmanNode left  = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(
                    left.getFrequency() + right.getFrequency(), left, right);
            pq.insert(parent);
        }
        return pq.poll(); // the single remaining node is the root
    }

    public HuffmanNode buildTreeExtended(Map<Character, Integer> frequencies, int k, ArrayList<String> heapSteps){

        if (k < 1) throw new IllegalArgumentException("k must be ≥ 1");

        MinHeapPriorityQueue pq = buildPriorityQueue(frequencies);

        // Record initial state
        heapSteps.add("Initial: " + pq);

        // Edge case: single character
        if (pq.size() == 1){
            HuffmanNode only = pq.poll();
            return new HuffmanNode(only.getFrequency(), only, null);
        }

        int iteration = 1;
        while (pq.size() > 1) {
            // How many nodes to pop: 2 * min(k, pq.size()/2)
            int availablePairs = pq.size() / 2;
            int pairsThisRound = Math.min(k, availablePairs);
            int nodesToPop     = pairsThisRound * 2;

            // Handle odd remainder: if one node would be stranded, pop it too
            // so it merges into the last group rather than lingering.
            // (Handled by the final single-element fallback below.)
            ArrayList<HuffmanNode> popped = pq.pollK(nodesToPop);

            // Build pairsThisRound internal nodes
            for (int p = 0; p < pairsThisRound; p++){

                HuffmanNode left  = popped.get(p * 2);
                HuffmanNode right = popped.get(p * 2 + 1);
                HuffmanNode parent = new HuffmanNode(left.getFrequency() + right.getFrequency(), left, right);
                pq.insert(parent);
            }

            // If one node was left unpaired (odd total), it stays in the queue.
            heapSteps.add("After iteration " + iteration + ": " + pq);
            iteration++;
        }
        return pq.poll();
    }

    public Map<Character, String> generateCodes(HuffmanNode root){

        Map<Character, String> codes = new LinkedHashMap<>();
        if (root == null) return codes;

        if (root.isLeaf()) {
            // Single-character input: assign code "0"
            codes.put(root.getCharacter(), "0");
            return codes;
        }
        traverseAndAssign(root, "", codes);
        return codes;
    }

    private void traverseAndAssign(HuffmanNode node, String prefix,Map<Character, String> codes){

        if (node == null) return;

        if (node.isLeaf()) {
            codes.put(node.getCharacter(), prefix);
            return;
        }
        traverseAndAssign(node.getLeft(),  prefix + "0", codes);
        traverseAndAssign(node.getRight(), prefix + "1", codes);
    }


    public String formatCodeTable(Map<Character, String> codes){

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-20s %s%n", "Character", "Huffman Code", "Code Length"));
        sb.append("─".repeat(50)).append("\n");
        codes.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().length()))
                .forEach(e -> {
                    String label = (e.getKey() == ' ') ? "SPACE"
                                 : (e.getKey() == ',') ? "COMMA"
                                 : String.valueOf(e.getKey());
                    sb.append(String.format("%-10s %-20s %d%n",
                            label, e.getValue(), e.getValue().length()));
                });
        return sb.toString();
    }

    public String printTree(HuffmanNode root){
        StringBuilder sb = new StringBuilder();
        printTreeRecursive(root, "", true, sb);
        return sb.toString();
    }

    private void printTreeRecursive(HuffmanNode node, String prefix,boolean isTail, StringBuilder sb){

        if (node == null) return;

        String connector = isTail ? "└── " : "├── ";
        String label = node.isLeaf()
                ? (node.getCharacter() == ' ' ? "SPACE" : String.valueOf(node.getCharacter()))
                  + " [" + node.getFrequency() + "]"
                : "* [" + node.getFrequency() + "]";

        sb.append(prefix).append(connector).append(label).append("\n");

        String childPrefix = prefix + (isTail ? "    " : "│   ");

        if (node.getLeft()  != null) printTreeRecursive(node.getLeft(),  childPrefix, node.getRight() == null, sb);
        if (node.getRight() != null) printTreeRecursive(node.getRight(), childPrefix, true, sb);
    }
}

/**
 * ============================================================
 * HuffmanTreeService.java  (Parts 2 & 3)
 * ------------------------------------------------------------
 * Spring Service responsible for:
 *
 *   Part 2 – Priority Queue population
 *     buildPriorityQueue()  : turns a frequency map into a
 *                             MinHeapPriorityQueue.
 *
 *   Part 3a – Standard Huffman Tree Construction
 *     buildTree()           : standard 2-node merge algorithm.
 *
 *   Part 3b – Extended Huffman Tree Construction
 *     buildTreeExtended()   : k-pair merge algorithm (Extended Part).
 *
 *   Part 3c – Code Generation
 *     generateCodes()       : depth-first tree traversal to
 *                             produce variable-length binary codes.
 *
 * ============================================================
 */

// ─────────────────────────────────────────────────────────
// Part 2 – Priority Queue Construction
// ─────────────────────────────────────────────────────────

/**
 * Converts a character-frequency map into a MinHeapPriorityQueue.
 * Each (char, freq) pair becomes a leaf HuffmanNode.
 *
 * Time: O(n log n) where n = number of distinct characters.
 *
 * @param frequencies map of character → frequency
 * @return populated min-heap priority queue
 */

// ─────────────────────────────────────────────────────────
// Part 3a – Standard Huffman Tree (2-node merge)
// ─────────────────────────────────────────────────────────

/**
 * Builds a standard Huffman Tree using the classic algorithm:
 *
 *   while queue.size > 1:
 *     left  = queue.poll()   (smallest freq)
 *     right = queue.poll()   (second smallest)
 *     parent = new InternalNode(left.freq + right.freq, left, right)
 *     queue.insert(parent)
 *
 * Returns the root of the completed Huffman Tree.
 *
 * Time: O(n log n)
 *
 * @param frequencies map of character → frequency
 * @return root HuffmanNode of the constructed tree
 */

// ─────────────────────────────────────────────────────────
// Part 3b – Extended Huffman Tree (k-pair merge)
// ─────────────────────────────────────────────────────────

/**
 * Extended Huffman construction where {@code k} pairs of nodes
 * are merged per iteration instead of just one pair.
 *
 * Algorithm per iteration:
 *   1. Pop 2k nodes from the heap.
 *   2. Pair them sequentially: (node0, node1), (node2, node3), …
 *   3. Create k internal nodes, each with freq = pair sum.
 *   4. Push the k new nodes back into the heap.
 *   5. Repeat until one node remains.
 *
 * If the heap contains fewer than 2k nodes at any step, the
 * algorithm falls back to merging all remaining pairs.
 *
 * @param frequencies map of character → frequency
 * @param k           number of pairs to merge per iteration (≥ 1)
 * @param heapSteps   mutable list that receives heap-state snapshots
 *                    (one string appended per iteration)
 * @return root HuffmanNode of the extended Huffman Tree
 */

// ─────────────────────────────────────────────────────────
// Part 3c – Huffman Code Generation
// ─────────────────────────────────────────────────────────

/**
 * Performs a depth-first traversal of the Huffman Tree to assign
 * binary codes to each leaf character.
 *
 * Convention:
 *   going LEFT  → append '0'
 *   going RIGHT → append '1'
 *
 * For trees with a single character (degenerate case) the code
 * is "0" by convention.
 *
 * @param root root of the Huffman Tree (may not be null)
 * @return map of character → binary code string
 */

/**
 * Recursive DFS helper for code generation.
 *
 * @param node    current node in the traversal
 * @param prefix  accumulated bit string from root to current node
 * @param codes   output map being populated
 */

// ─────────────────────────────────────────────────────────
// Display helpers
// ─────────────────────────────────────────────────────────

/**
 * Formats the Huffman code table as a readable string.
 *
 * @param codes map from generateCodes()
 * @return formatted multi-line table
 */

/**
 * Prints the Huffman Tree structure as an indented text diagram.
 *
 * @param root  the tree root
 * @return multi-line ASCII diagram
 */
