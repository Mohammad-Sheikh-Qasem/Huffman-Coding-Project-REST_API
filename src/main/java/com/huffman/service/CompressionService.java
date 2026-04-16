// Developed by: Mohammad Sheikh Qasem
package com.huffman.service;

import com.huffman.model.HuffmanNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class CompressionService {

    public int[] compress(String text, Map<Character, String> codes){
        // Step 1-2: Build the full bit string
        String bitString = encodeToBits(text, codes);

        // Step 3: Pad to 8-bit boundary
        int padBits = (8 - (bitString.length() % 8)) % 8;
        bitString = bitString + "0".repeat(padBits);

        // Step 4: Pack bits into int array (each int holds 8 bits, value 0–255)
        
        int numGroups = bitString.length() / 8;
        int[] data = new int[numGroups + 1]; // +1 for header
        data[0] = padBits;                // Step 5: header int

        for (int i = 0; i < numGroups; i++){
            
            String group = bitString.substring(i * 8, i * 8 + 8);
            data[i + 1] = Integer.parseInt(group, 2); // value 0–255
        }
        return data;
    }

    public String decompress(int[] compressedData, HuffmanNode root){

        if (compressedData == null || compressedData.length < 2){
            return "";
        }
        if (root == null){
            throw new IllegalArgumentException("Huffman Tree root must not be null");
        }

        // Extract header (number of padding bits at the end)
        int padBits = compressedData[0];

        // Reconstruct bit string from packed int values
        StringBuilder bitString = new StringBuilder();

        for (int i = 1; i < compressedData.length; i++){
            String group = Integer.toBinaryString(compressedData[i]);
            // Pad each group back to exactly 8 bits
            bitString.append("0".repeat(8 - group.length())).append(group);
        }

        // Remove padding bits from the end
        String bits = bitString.substring(0, bitString.length() - padBits);

        // Decode using the Huffman Tree
        return decodeFromBits(bits, root);
    }


 
    public String encodeToBits(String text, Map<Character, String> codes){
        
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()){
            String code = codes.get(c);

            if (code == null){
                log.warn("No Huffman code for character '{}' - skipped.", c == ' ' ? "SPACE" : c);
                continue;
            }
            sb.append(code);
        }
        return sb.toString();
    }


    public String decodeFromBits(String bits, HuffmanNode root) {
        StringBuilder result  = new StringBuilder();
        HuffmanNode   current = root;

        // Special case: single-character tree
        if (root.isLeaf()) {
            for (int i = 0; i < bits.length(); i++){
                result.append(root.getCharacter());
            }
            return result.toString();
        }

        for (int i = 0; i < bits.length(); i++){
            char bit = bits.charAt(i);
            current  = (bit == '0') ? current.getLeft() : current.getRight();

            if (current == null){
                throw new IllegalStateException(
                        "Null node encountered during decoding at bit index " + i
                        + ". The compressed data may be corrupted.");
            }
            if (current.isLeaf()){
                result.append(current.getCharacter());
                current = root; // restart from root for next character
            }
        }
        return result.toString();
    }

    public long compressedByteCount(int[] compressedData) {
        return compressedData == null ? 0L : Math.max(0L, compressedData.length - 1);
    }
 
    public double compressionRatio(long originalBytes, long compressedBytes) {
        if (compressedBytes == 0) return 0.0;

        return Math.round((double) originalBytes / compressedBytes * 10000.0) / 10000.0;
    }
    
    public double compressionPercent(long originalBytes, long compressedBytes) {
        if (originalBytes == 0) return 0.0;

        double saved = (1.0 - (double) compressedBytes / originalBytes) * 100.0;
        return Math.round(saved * 100.0) / 100.0;
    }
}

/**
 * ============================================================
 * CompressionService.java  (Part 3 – Encode / Decode)
 * ------------------------------------------------------------
 * Spring Service that handles the actual compression and
 * decompression of text using a pre-built set of Huffman codes.
 *
 *   compress()    : text  → binary string → packed int array
 *   decompress()  : packed int array + tree root → original text
 *   encodeToBits(): text  → binary string only (useful for display)
 *
 * Bit-packing strategy
 * ─────────────────────
 * Raw Huffman bit strings are variable-length and don't align to
 * 8-bit boundaries.  This service packs them into int[] by treating
 * the bit string as groups of 8 bits stored as int values (0–255).
 * The number of padding bits is stored in int[0] (the header) so
 * that decompression can strip the trailing zeros.
 *
 * Compressed format:
 *   int[0]        → padBits  (0–7, number of zero-pad bits at end)
 *   int[1..n]     → packed data values (each holds 8 bits, range 0–255)
 * ============================================================
 */

// ─────────────────────────────────────────────────────────
// Compression
// ─────────────────────────────────────────────────────────

/**
 * Converts input text into a compact int array using Huffman codes.
 *
 * Each int element stores one 8-bit group as a value in the range
 * 0–255 (no sign-extension issues unlike byte).
 *
 * Steps:
 *   1. Encode each character to its Huffman bit string.
 *   2. Concatenate all bit strings.
 *   3. Pad the resulting string to a multiple of 8 bits.
 *   4. Pack each 8-bit group into one int element.
 *   5. Prepend a header int recording how many bits were padded.
 *
 * @param text  original text (characters must exist in {@code codes})
 * @param codes map of character → binary code string
 * @return compressed int array (int[0] = header, int[1..n] = data)
 */

/**
 * Reconstructs the original text from a compressed int array
 * and the Huffman Tree root used during compression.
 *
 * Decoding is performed by walking the Huffman Tree:
 *   '0' → go left
 *   '1' → go right
 *   reach leaf → emit character, restart from root
 *
 * @param compressedData int array produced by {@code compress()}
 *                       (int[0] = padBits header, int[1..n] = data)
 * @param root           root of the same Huffman Tree used to compress
 * @return the reconstructed original text
 */

// ─────────────────────────────────────────────────────────
// Encode / Decode helpers (public for display purposes)
// ─────────────────────────────────────────────────────────

/**
 * Converts text to a raw binary string (before packing).
 * Characters not present in {@code codes} are skipped with a warning.
 *
 * @param text  source text
 * @param codes Huffman code lookup
 * @return concatenated binary string
 */

/**
 * Walks the Huffman Tree guided by a binary string to recover text.
 * Pure tree-traversal decoding – O(n) where n = bit string length.
 *
 * @param bits bit string (no padding, no header)
 * @param root Huffman Tree root
 * @return decoded text string
 */

// ─────────────────────────────────────────────────────────
// Statistics helpers
// ─────────────────────────────────────────────────────────

/**
 * Calculates the number of data elements in the compressed int array
 * (excluding the header element at index 0).
 *
 * Each element represents 8 bits, so multiply by 1 to get the
 * equivalent byte count for compression ratio calculations.
 *
 * @param compressedData int array from {@code compress()}
 * @return data element count (equivalent to compressed byte count)
 */

/**
 * Calculates compression ratio: original / compressed.
 * A ratio > 1 means the file shrank.
 *
 * @param originalBytes  original text byte count (UTF-8)
 * @param compressedBytes byte count of compressed output
 * @return compression ratio (rounded to 4 decimal places)
 */

/**
 * Space saved as a percentage.
 *
 * @param originalBytes  original byte count
 * @param compressedBytes compressed byte count
 * @return percentage saved (0–100), rounded to 2 decimal places
 */
