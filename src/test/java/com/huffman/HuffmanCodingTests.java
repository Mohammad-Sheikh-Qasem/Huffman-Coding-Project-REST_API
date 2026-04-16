// Developed by: Mohammad Sheikh Qasem
package com.huffman;

import com.huffman.model.*;
import com.huffman.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * HuffmanCodingTests.java
 * ------------------------------------------------------------
 * Integration + Unit tests for every layer of the system.
 *
 * Test groups:
 *   1. FrequencyAnalysisService
 *   2. MinHeapPriorityQueue
 *   3. HuffmanTreeService (tree build + code generation)
 *   4. CompressionService (encode / decode)
 *   5. HuffmanService (full pipeline + extended + benchmark)
 * ============================================================
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HuffmanCodingTests {

    @Autowired private FrequencyAnalysisService frequencyService;
    @Autowired private HuffmanTreeService       treeService;
    @Autowired private CompressionService       compressionService;
    @Autowired private HuffmanService           huffmanService;

    private static final String SAMPLE_TEXT = "abracadabra";
    private static final String LONG_TEXT   =
        "the quick brown fox jumps over the lazy dog, " +
        "the lazy dog jumps over the quick brown fox";

    // ══════════════════════════════════════════════════════════
    // 1. FrequencyAnalysisService
    // ══════════════════════════════════════════════════════════

    @Test @Order(1)
    @DisplayName("1.1 Array-based frequency analysis produces correct counts")
    void testFrequencyAnalysisArray() {
        Map<Character, Integer> freqs = frequencyService.analyseWithArray(SAMPLE_TEXT);
        assertEquals(5, freqs.get('a'));
        assertEquals(2, freqs.get('b'));
        assertEquals(2, freqs.get('r'));
        assertEquals(1, freqs.get('c'));
        assertEquals(1, freqs.get('d'));
        assertEquals(5, freqs.size()); // 5 distinct chars
    }

    @Test @Order(2)
    @DisplayName("1.2 LinkedList-based frequency analysis matches array-based")
    void testFrequencyAnalysisLinkedList() {
        Map<Character, Integer> arrayFreqs  = frequencyService.analyseWithArray(SAMPLE_TEXT);
        Map<Character, Integer> linkedFreqs = frequencyService.analyseWithLinkedList(SAMPLE_TEXT);
        assertEquals(arrayFreqs, linkedFreqs);
    }

    @Test @Order(3)
    @DisplayName("1.3 Frequency analysis ignores non-accepted characters")
    void testFrequencyIgnoresSpecialChars() {
        Map<Character, Integer> freqs = frequencyService.analyse("Hello! World123");
        // '!', '1', '2', '3' should be ignored
        assertFalse(freqs.containsKey('!'));
        assertFalse(freqs.containsKey('1'));
        assertTrue(freqs.containsKey('l')); // accepted
    }

    @Test @Order(4)
    @DisplayName("1.4 Frequency analysis on blank text throws exception")
    void testFrequencyBlankTextThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> frequencyService.analyse(""));
    }

    @Test @Order(5)
    @DisplayName("1.5 Frequency table includes spaces and commas")
    void testFrequencySpaceAndComma() {
        Map<Character, Integer> freqs = frequencyService.analyse("a, b, c");
        assertTrue(freqs.containsKey(' '));
        assertTrue(freqs.containsKey(','));
    }

    // ══════════════════════════════════════════════════════════
    // 2. MinHeapPriorityQueue
    // ══════════════════════════════════════════════════════════

    @Test @Order(6)
    @DisplayName("2.1 Heap always returns minimum-frequency node")
    void testHeapMinProperty() {
        MinHeapPriorityQueue pq = new MinHeapPriorityQueue();
        pq.insert(new HuffmanNode('c', 10));
        pq.insert(new HuffmanNode('a', 2));
        pq.insert(new HuffmanNode('b', 7));

        assertEquals(2,  pq.poll().getFrequency()); // 'a' first
        assertEquals(7,  pq.poll().getFrequency()); // 'b' second
        assertEquals(10, pq.poll().getFrequency()); // 'c' third
    }

    @Test @Order(7)
    @DisplayName("2.2 Heap is empty after all elements polled")
    void testHeapEmptyAfterPolls() {
        MinHeapPriorityQueue pq = new MinHeapPriorityQueue();
        pq.insert(new HuffmanNode('a', 5));
        pq.poll();
        assertTrue(pq.isEmpty());
    }

    @Test @Order(8)
    @DisplayName("2.3 Heap pollK returns k smallest elements")
    void testHeapPollK() {
        MinHeapPriorityQueue pq = new MinHeapPriorityQueue();
        pq.insert(new HuffmanNode('e', 15));
        pq.insert(new HuffmanNode('b', 7));
        pq.insert(new HuffmanNode('a', 3));
        pq.insert(new HuffmanNode('d', 12));
        pq.insert(new HuffmanNode('c', 9));

        List<HuffmanNode> smallest2 = pq.pollK(2);
        assertEquals(2, smallest2.size());
        assertEquals(3,  smallest2.get(0).getFrequency()); // 'a'
        assertEquals(7,  smallest2.get(1).getFrequency()); // 'b'
    }

    @Test @Order(9)
    @DisplayName("2.4 Heap poll on empty queue throws exception")
    void testHeapPollEmptyThrows() {
        MinHeapPriorityQueue pq = new MinHeapPriorityQueue();
        assertThrows(NoSuchElementException.class, pq::poll);
    }

    // ══════════════════════════════════════════════════════════
    // 3. HuffmanTreeService
    // ══════════════════════════════════════════════════════════

    @Test @Order(10)
    @DisplayName("3.1 Huffman tree root has sum of all frequencies")
    void testTreeRootFrequency() {
        Map<Character, Integer> freqs = frequencyService.analyse(SAMPLE_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        int totalFreq = freqs.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(totalFreq, root.getFrequency());
    }

    @Test @Order(11)
    @DisplayName("3.2 Every character has a Huffman code")
    void testAllCharsHaveCodes() {
        Map<Character, Integer> freqs = frequencyService.analyse(SAMPLE_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);
        freqs.keySet().forEach(ch ->
                assertTrue(codes.containsKey(ch), "Missing code for '" + ch + "'"));
    }

    @Test @Order(12)
    @DisplayName("3.3 Huffman codes are prefix-free")
    void testCodesPrefixFree() {
        Map<Character, Integer> freqs = frequencyService.analyse(LONG_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);

        List<String> codeList = new ArrayList<>(codes.values());
        for (int i = 0; i < codeList.size(); i++) {
            for (int j = 0; j < codeList.size(); j++) {
                if (i != j) {
                    assertFalse(codeList.get(j).startsWith(codeList.get(i)),
                            "Code '" + codeList.get(i) + "' is a prefix of '" + codeList.get(j) + "'");
                }
            }
        }
    }

    @Test @Order(13)
    @DisplayName("3.4 More frequent characters get shorter codes")
    void testFrequentCharsGetShorterCodes() {
        Map<Character, Integer> freqs = Map.of('a', 100, 'b', 2, 'c', 3);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);
        // 'a' must have the shortest code
        assertTrue(codes.get('a').length() <= codes.get('b').length());
        assertTrue(codes.get('a').length() <= codes.get('c').length());
    }

    // ══════════════════════════════════════════════════════════
    // 4. CompressionService
    // ══════════════════════════════════════════════════════════

    @Test @Order(14)
    @DisplayName("4.1 Encode-decode round trip is lossless")
    void testEncodeDecodeLossless() {
        Map<Character, Integer> freqs = frequencyService.analyse(SAMPLE_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);
        byte[] compressed = compressionService.compress(SAMPLE_TEXT, codes);
        String decompressed = compressionService.decompress(compressed, root);
        assertEquals(SAMPLE_TEXT, decompressed);
    }

    @Test @Order(15)
    @DisplayName("4.2 Compressed size is smaller than original for typical text")
    void testCompressedSizeSmaller() {
        Map<Character, Integer> freqs = frequencyService.analyse(LONG_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);
        byte[] compressed = compressionService.compress(LONG_TEXT, codes);
        long origBytes = LONG_TEXT.getBytes().length;
        long compBytes = compressionService.compressedByteCount(compressed);
        assertTrue(compBytes < origBytes,
                "Expected compressed (" + compBytes + ") < original (" + origBytes + ")");
    }

    @Test @Order(16)
    @DisplayName("4.3 Compression ratio > 1 for typical text")
    void testCompressionRatioGreaterThanOne() {
        Map<Character, Integer> freqs = frequencyService.analyse(LONG_TEXT);
        HuffmanNode root = treeService.buildTree(freqs);
        Map<Character, String> codes = treeService.generateCodes(root);
        byte[] compressed = compressionService.compress(LONG_TEXT, codes);
        long origBytes = LONG_TEXT.getBytes().length;
        long compBytes = compressionService.compressedByteCount(compressed);
        double ratio = compressionService.compressionRatio(origBytes, compBytes);
        assertTrue(ratio > 1.0, "Expected ratio > 1, got " + ratio);
    }

    // ══════════════════════════════════════════════════════════
    // 5. HuffmanService (full pipeline)
    // ══════════════════════════════════════════════════════════

    @Test @Order(17)
    @DisplayName("5.1 Standard full pipeline: round trip success")
    void testFullPipelineRoundTrip() {
        CompressionResult result = huffmanService.compressAndDecompress(SAMPLE_TEXT);
        assertTrue(result.isRoundTripSuccess());
        assertEquals(SAMPLE_TEXT, result.getDecompressedText());
    }

    @Test @Order(18)
    @DisplayName("5.2 Extended pipeline with k=1 matches standard pipeline")
    void testExtendedK1MatchesStandard() {
        CompressionResult standard = huffmanService.compressAndDecompress(SAMPLE_TEXT);
        CompressionResult extended = huffmanService.compressAndDecompressExtended(SAMPLE_TEXT, 1);
        assertEquals(standard.getCompressedBytes(), extended.getCompressedBytes());
        assertTrue(extended.isRoundTripSuccess());
    }

    @Test @Order(19)
    @DisplayName("5.3 Extended pipeline k=2 produces valid output")
    void testExtendedK2Valid() {
        CompressionResult result = huffmanService.compressAndDecompressExtended(LONG_TEXT, 2);
        assertTrue(result.isRoundTripSuccess());
        assertFalse(result.getHuffmanCodes().isEmpty());
    }

    @Test @Order(20)
    @DisplayName("5.4 Benchmark returns one result per k value")
    void testBenchmarkResultCount() {
        List<Integer> kVals = List.of(1, 2, 3);
        List<CompressionResult> results = huffmanService.benchmarkKValues(LONG_TEXT, kVals);
        assertEquals(3, results.size());
        assertEquals(1, results.get(0).getKValue());
        assertEquals(2, results.get(1).getKValue());
        assertEquals(3, results.get(2).getKValue());
    }

    @Test @Order(21)
    @DisplayName("5.5 Blank text throws IllegalArgumentException")
    void testBlankInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> huffmanService.compressAndDecompress("   "));
    }

    @Test @Order(22)
    @DisplayName("5.6 Single-character text handled correctly")
    void testSingleCharacterText() {
        CompressionResult result = huffmanService.compressAndDecompress("aaaa");
        assertTrue(result.isRoundTripSuccess());
        assertEquals("aaaa", result.getDecompressedText());
    }
}
