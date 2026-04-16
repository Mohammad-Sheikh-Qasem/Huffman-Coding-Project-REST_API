// Developed by: Mohammad Sheikh Qasem
package com.huffman.model;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class CharacterFrequency{

    /** The character being counted. */
    private final char character;

    /** Number of times {@code character} appeared in the analysed text. */
    private int frequency;


    public CharacterFrequency(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }


    public static List<CharacterFrequency> buildUsingArray(String text){
        // ── 1. Count frequencies into a fixed-size array ──────
        int[] counts = new int[54];

        for (char c : text.toCharArray()){
            int idx = charToIndex(c);
            if (idx >= 0) counts[idx]++;
        }

        // ── 2. Convert non-zero entries to CharacterFrequency ──
        List<CharacterFrequency> result = new LinkedList<>();
        for (int i = 0; i < counts.length; i++){

            if (counts[i] > 0){
                result.add(new CharacterFrequency(indexToChar(i), counts[i]));
            }
        }
        return result;
    }


    public static List<CharacterFrequency> buildUsingLinkedList(String text){
        LinkedList<CharacterFrequency> freqList = new LinkedList<>();

        for (char c : text.toCharArray()){

            if (!isAccepted(c)) continue;

            // Linear scan – O(n·m) where m = distinct character count (≤54)
            boolean found = false;

            for (CharacterFrequency entry : freqList){

                if (entry.character == c){
                    entry.frequency++;
                    found = true;
                    break;
                }
            }

            if (!found){
                freqList.add(new CharacterFrequency(c, 1));
            }
        }
        return freqList;
    }


    private static int charToIndex(char c){

        if (c >= 'A' && c <= 'Z')
            return c - 'A'; // 0–25

        if (c >= 'a' && c <= 'z')
            return c - 'a' + 26;     // 26–51

        if (c == ' ')
            return 52;

        if (c == ',')
            return 53;

        return -1; // ignored
    }

    /** Reverse mapping from array index to character. */
    private static char indexToChar(int idx){

        if (idx < 26)
            return (char)('A' + idx);

        if (idx < 52)
            return (char)('a' + idx - 26);

        if (idx == 52)
            return ' ';

        return ',';
    }

    /** Returns true if the character belongs to the accepted set. */
    private static boolean isAccepted(char c){
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == ',';
    }

    @Override
    public String toString(){
        String display = (character == ' ') ? "SPACE" : String.valueOf(character);
        return String.format("CharFreq{'%s' → %d}", display, frequency);
    }
}


/* ==== README ==== */

/**
 * ============================================================
 * CharacterFrequency.java
 * ------------------------------------------------------------
 * Stores a (character, frequency) pair and provides a static
 * factory that builds the full frequency table from raw text
 * using two alternative underlying data structures as required
 * by Part 1 of the project specification:
 *
 *   Option A – int[] array  (constant-size, O(1) access)
 *   Option B – LinkedList   (dynamic, demonstrates linked nodes)
 *
 * Accepted characters: A–Z, a–z, space, comma  (per project spec).
 * Any other character in the input is silently ignored.
 * ============================================================
 */
// ─────────────────────────────────────────────────────────
// Fields
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Constructor
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Static Factory – Array-based (Part 1, Option A)
// ─────────────────────────────────────────────────────────

/**
 * Builds a frequency list using a plain int[] array as the
 * internal counting structure.
 *
 * Mapping strategy:
 *   indices  0–25  → 'A'–'Z'
 *   indices 26–51  → 'a'–'z'
 *   index   52     → ' ' (space)
 *   index   53     → ',' (comma)
 *
 * Total array size: 54 slots.
 *
 * @param text source text to analyse
 * @return list of CharacterFrequency objects (frequency > 0 only)
 */

// ─────────────────────────────────────────────────────────
// Static Factory – LinkedList-based (Part 1, Option B)
// ─────────────────────────────────────────────────────────

/**
 * Builds a frequency list using a LinkedList as the counting
 * structure. Each unique character gets its own node; when a
 * character is encountered again the existing node is updated.
 *
 * This demonstrates dynamic, pointer-based storage vs. the
 * array approach above.
 *
 * @param text source text to analyse
 * @return list of CharacterFrequency objects (frequency > 0 only)
 */

// ─────────────────────────────────────────────────────────
// Helper methods
// ─────────────────────────────────────────────────────────

/**
 * Maps a character to its array index.
 * Returns -1 for characters outside the accepted set.
 */

// ─────────────────────────────────────────────────────────
// Getters
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Debugging
// ─────────────────────────────────────────────────────────

