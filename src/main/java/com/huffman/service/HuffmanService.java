// Developed by: Mohammad Sheikh Qasem
package com.huffman.service;

import com.huffman.model.CompressionResult;
import com.huffman.model.HuffmanNode;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class HuffmanService {

    private final FrequencyAnalysisService frequencyService;
    private final HuffmanTreeService treeService;
    private final CompressionService compressionService;

    public HuffmanService(FrequencyAnalysisService frequencyService,HuffmanTreeService treeService,
                          CompressionService compressionService){

        this.frequencyService = frequencyService;
        this.treeService = treeService;
        this.compressionService = compressionService;
    }

    public CompressionResult compressAndDecompress(String text){
        validateText(text);

        // ── Step 1: Frequency analysis ────────────────────────
        Map<Character, Integer> frequencies = frequencyService.analyse(text);

        // ── Step 2-3: Build Huffman Tree ──────────────────────
        HuffmanNode root = treeService.buildTree(frequencies);

        // ── Step 4: Generate codes ────────────────────────────
        Map<Character, String> codes = treeService.generateCodes(root);

        // ── Step 5: Compress ──────────────────────────────────
        int[] compressed = compressionService.compress(text, codes);
        long origBytes = text.getBytes(StandardCharsets.UTF_8).length;
        long compBytes = compressionService.compressedByteCount(compressed);

        // ── Step 6: Decompress ────────────────────────────────
        String decompressed = compressionService.decompress(compressed, root);

        // ── Step 7: Assemble result ───────────────────────────
        return CompressionResult.builder()
                .originalText(text)
                .characterFrequencies(frequencies)
                .huffmanCodes(codes)
                .encodedBinary(compressionService.encodeToBits(text, codes))
                .originalBytes(origBytes)
                .compressedBytes(compBytes)
                .compressionRatio(compressionService.compressionRatio(origBytes, compBytes))
                .compressionPercent(compressionService.compressionPercent(origBytes, compBytes))
                .decompressedText(decompressed)
                .roundTripSuccess(text.equals(decompressed))
                .kValue(1) // standard = 1 pair per iteration
                .heapSteps(new ArrayList<>()) // no extended steps
                .build();
    }

    public CompressionResult compressAndDecompressExtended(String text, int k) {
        validateText(text);

        if (k < 1){
            throw new IllegalArgumentException("k must be at least 1");
        }

        // ── Step 1: Frequency analysis ────────────────────────
        Map<Character, Integer> frequencies = frequencyService.analyse(text);

        // ── Step 2-3: Build Extended Huffman Tree ─────────────
        ArrayList<String> heapSteps = new ArrayList<>();
        HuffmanNode root = treeService.buildTreeExtended(frequencies, k, heapSteps);

        // ── Step 4: Generate codes ────────────────────────────
        Map<Character, String> codes = treeService.generateCodes(root);

        // ── Step 5: Compress ──────────────────────────────────
        int[] compressed = compressionService.compress(text, codes);
        long origBytes = text.getBytes(StandardCharsets.UTF_8).length;
        long compBytes = compressionService.compressedByteCount(compressed);

        // ── Step 6: Decompress ────────────────────────────────
        String decompressed = compressionService.decompress(compressed, root);

        // ── Step 7: Assemble result ───────────────────────────
        return CompressionResult.builder()
                .originalText(text)
                .characterFrequencies(frequencies)
                .huffmanCodes(codes)
                .encodedBinary(compressionService.encodeToBits(text, codes))
                .originalBytes(origBytes)
                .compressedBytes(compBytes)
                .compressionRatio(compressionService.compressionRatio(origBytes, compBytes))
                .compressionPercent(compressionService.compressionPercent(origBytes, compBytes))
                .decompressedText(decompressed)
                .roundTripSuccess(text.equals(decompressed))
                .kValue(k)
                .heapSteps(heapSteps)
                .build();
    }


    public List<CompressionResult> benchmarkKValues(String text, List<Integer> kValues){
        validateText(text);
        List<CompressionResult> results = new ArrayList<>();
        for (int k : kValues) {
            results.add(compressAndDecompressExtended(text, k));
        }
        return results;
    }

    public Map<Character, Integer> getFrequencies(String text){
        validateText(text);
        return frequencyService.analyse(text);
    }


    public Map<Character, String> getHuffmanCodes(String text){
        validateText(text);
        Map<Character, Integer> frequencies = frequencyService.analyse(text);
        HuffmanNode root = treeService.buildTree(frequencies);
        return treeService.generateCodes(root);
    }


    public String getTreeDiagram(String text) {
        validateText(text);
        Map<Character, Integer> frequencies = frequencyService.analyse(text);
        HuffmanNode root = treeService.buildTree(frequencies);
        return treeService.printTree(root);
    }


    private void validateText(String text){
        if (text == null || text.isBlank()){
            throw new IllegalArgumentException("Input text must not be null or blank");
        }
    }
}



/**
 * ============================================================
 * HuffmanService.java
 * ------------------------------------------------------------
 * Top-level Spring Service that orchestrates the complete
 * Huffman Coding pipeline by coordinating the three sub-services:
 *
 *   FrequencyAnalysisService → character counting     (Part 1)
 *   HuffmanTreeService       → tree & code generation (Parts 2-3)
 *   CompressionService       → encode / decode        (Part 3)
 *
 * Exposes two primary operations:
 *
 *   compressAndDecompress()         – standard 2-node merge
 *   compressAndDecompressExtended() – extended k-pair merge
 *
 * Both return a CompressionResult value object that the REST
 * controller can directly serialise to JSON.
 * ============================================================
 */

// ─────────────────────────────────────────────────────────
// Dependencies (constructor-injected)
// ─────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────
// Standard Huffman Pipeline
// ─────────────────────────────────────────────────────────

/**
 * Runs the complete standard Huffman pipeline on the given text:
 *
 *   1. Count character frequencies.
 *   2. Build a min-heap priority queue from the frequencies.
 *   3. Construct the Huffman Tree (2-node merge per iteration).
 *   4. Generate Huffman codes from the tree.
 *   5. Encode the text to a compact byte array.
 *   6. Decode the byte array back to text.
 *   7. Verify round-trip correctness.
 *
 * @param text raw input text (A–Z, a–z, space, comma accepted)
 * @return fully populated CompressionResult
 */

// ─────────────────────────────────────────────────────────
// Extended Huffman Pipeline (k-pair merge)
// ─────────────────────────────────────────────────────────

/**
 * Runs the extended Huffman pipeline where {@code k} pairs of nodes
 * are merged per heap iteration.
 *
 * Identical to the standard pipeline except step 3 uses
 * {@code HuffmanTreeService.buildTreeExtended()} and the result
 * includes heap step snapshots for analysis.
 *
 * @param text raw input text
 * @param k    number of pairs to pop per heap iteration (≥ 1)
 * @return fully populated CompressionResult with heap steps
 */
// ─────────────────────────────────────────────────────────
// Benchmark: effect of k on compression size
// ─────────────────────────────────────────────────────────

/**
 * Runs the extended pipeline for each value of k in
 * {@code kValues} and returns one CompressionResult per run.
 * Useful for evaluating how k affects compressed output size.
 *
 * @param text    input text to compress
 * @param kValues list of k values to test
 * @return list of results in the same order as kValues
 */

// ─────────────────────────────────────────────────────────
// Frequency-only query
// ─────────────────────────────────────────────────────────

/**
 * Returns only the character frequency map for the given text.
 * Lightweight alternative to the full pipeline.
 *
 * @param text input text
 * @return frequency map (char → count), sorted by frequency ascending
 */

/**
 * Returns the Huffman code table for the given text.
 *
 * @param text input text
 * @return code map (char → binary string)
 */

/**
 * Returns a formatted ASCII diagram of the Huffman Tree.
 *
 * @param text input text
 * @return tree diagram string
 */

// ─────────────────────────────────────────────────────────
// Input validation
// ─────────────────────────────────────────────────────────

