// Developed by: Mohammad Sheikh Qasem
package com.huffman.model;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class CompressionResult {

    // attributes

    private String originalText;
    private Map<Character, Integer> characterFrequencies;
    private Map<Character, String> huffmanCodes;
    private String encodedBinary;
    private long originalBytes;
    private long compressedBytes;
    private double compressionRatio;
    private double compressionPercent;
    private String decompressedText;
    private boolean roundTripSuccess;

    // Extended-mode metadata
    private int kValue;           // pairs removed per heap step
    private List<String> heapSteps;        // heap snapshot strings per iteration

    private CompressionResult() {}

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private final CompressionResult result = new CompressionResult();


        public Builder originalText(String v){
            result.originalText = v;
            return this;
        }

        public Builder characterFrequencies(Map<Character,Integer> v){
            result.characterFrequencies = v;
            return this;
        }

        public Builder huffmanCodes(Map<Character, String> v){
            result.huffmanCodes = v;
            return this;
        }

        public Builder encodedBinary(String v){
            result.encodedBinary = v;
            return this;
        }

        public Builder originalBytes(long v){
            result.originalBytes = v;
            return this;
        }

        public Builder compressedBytes(long v){
            result.compressedBytes = v;
            return this;
        }

        public Builder compressionRatio(double v){
            result.compressionRatio = v;
            return this;
        }

        public Builder compressionPercent(double v){
            result.compressionPercent = v;
            return this;
        }

        public Builder decompressedText(String v){
            result.decompressedText = v;
            return this;
        }

        public Builder roundTripSuccess(boolean v){
            result.roundTripSuccess = v;
            return this;
        }

        public Builder kValue(int v){
            result.kValue = v;
            return this;
        }

        public Builder heapSteps(List<String> v){
            result.heapSteps = v;
            return this;
        }

        public CompressionResult build() { return result; }
    }


    @Override
    public String toString() {
        return String.format("CompressionResult{originalBytes=%d, compressedBytes=%d, ratio=%.2f, success=%b, k=%d}",
                originalBytes, compressedBytes, compressionRatio, roundTripSuccess, kValue);
    }
}

/* ==== README ==== */

/**
 * ============================================================
 * CompressionResult.java
 * ------------------------------------------------------------
 * Immutable value object returned by HuffmanService after a full
 * compress-decompress cycle.  The REST controller serialises this
 * as JSON so the caller can inspect every stage of the pipeline.
 *
 * Fields:
 *   originalText        – the raw input string
 *   characterFrequencies– (char → count) sorted by frequency
 *   huffmanCodes        – (char → binary code string) e.g. 'a' → "101"
 *   encodedBinary       – the full bit-string for the input text
 *   compressedBytes     – byte count of the packed binary output
 *   originalBytes       – byte count of the original UTF-8 text
 *   compressionRatio    – originalBytes / compressedBytes
 *   compressionPercent  – space saved as a percentage
 *   decompressedText    – round-trip result (must equal originalText)
 *   roundTripSuccess    – true when decompressedText == originalText
 *   kValue              – pairs-per-iteration used (extended mode)
 *   heapSteps           – snapshot of heap state at each iteration
 * ============================================================
 */

// ─────────────────────────────────────────────────────────
// Core output fields
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Builder pattern for clean construction
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Getters  (no setters – treat as immutable after build)
// ─────────────────────────────────────────────────────────



