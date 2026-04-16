// Developed by: Mohammad Sheikh Qasem
package com.huffman.service;

import com.huffman.model.CharacterFrequency;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class FrequencyAnalysisService {

    public Map<Character, Integer> analyseWithArray(String text){
        validateInput(text);
        List<CharacterFrequency> rawList = CharacterFrequency.buildUsingArray(text);
        return toSortedMap(rawList);
    }


    public Map<Character, Integer> analyseWithLinkedList(String text){
        validateInput(text);
        List<CharacterFrequency> rawList = CharacterFrequency.buildUsingLinkedList(text);
        return toSortedMap(rawList);
    }


    public Map<Character, Integer> analyse(String text) {
        return analyseWithArray(text);
    }


    public String formatFrequencyTable(Map<Character, Integer> frequencies){

        if (frequencies == null || frequencies.isEmpty()) {
            return "(no frequency data)";
        }

        int maxFreq = Collections.max(frequencies.values());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s %-12s %s%n", "Character", "Frequency", "Distribution"));
        sb.append("─".repeat(60)).append("\n");

        // Display sorted by frequency descending for readability
        frequencies.entrySet().stream()
                .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    char c    = entry.getKey();
                    int  freq = entry.getValue();
                    String label = (c == ' ') ? "SPACE" : (c == ',') ? "COMMA" : String.valueOf(c);
                    int   barLen = (int) Math.round((double) freq / maxFreq * 30);
                    String bar  = "█".repeat(barLen);
                    sb.append(String.format("%-12s %-12d %s%n", label, freq, bar));
                });

        return sb.toString();
    }

    public int countAcceptedChars(String text) {
        if (text == null) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (isAccepted(c)) count++;
        }
        return count;
    }

    private Map<Character, Integer> toSortedMap(List<CharacterFrequency> list){
        return list.stream()
                .sorted(Comparator.comparingInt(CharacterFrequency::getFrequency)
                        .thenComparingInt(cf -> cf.getCharacter()))
                .collect(Collectors.toMap(
                        CharacterFrequency::getCharacter,
                        CharacterFrequency::getFrequency,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /** Throws IllegalArgumentException when the text is unusable. */
    private void validateInput(String text){

        if (text == null || text.isEmpty()){
            throw new IllegalArgumentException("Input text must not be null or empty");
        }
    }

    /** Returns true for the four accepted character classes. */
    private boolean isAccepted(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == ',';
    }
}



/**
 * ============================================================
 * FrequencyAnalysisService.java  (Part 1)
 * ------------------------------------------------------------
 * Spring Service responsible for character-frequency analysis.
 *
 * Reads a text string and counts occurrences of every accepted
 * character (A–Z, a–z, space, comma).  Exposes two analysis
 * strategies matching the project spec:
 *
 *   analyseWithArray()      → delegates to CharacterFrequency.buildUsingArray()
 *   analyseWithLinkedList() → delegates to CharacterFrequency.buildUsingLinkedList()
 *
 * Both return a frequency map sorted by frequency ascending so
 * it is directly usable by the priority queue in Part 2.
 * ============================================================
 */
// ─────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────

/**
 * Analyse character frequencies using an int[] array internally.
 * Fastest approach: O(n) time, O(1) extra space (fixed 54 slots).
 *
 * @param text input text (only A–Z, a–z, space, comma are counted)
 * @return LinkedHashMap of char → frequency, sorted ascending by freq
 */

/**
 * Analyse character frequencies using a LinkedList internally.
 * Demonstrates dynamic/pointer-based storage as per project spec.
 * O(n·m) time where m ≤ 54 distinct accepted characters.
 *
 * @param text input text
 * @return LinkedHashMap of char → frequency, sorted ascending by freq
 */
/**
 * Default analysis – uses the array strategy (faster, preferred).
 *
 * @param text input text
 * @return frequency map sorted ascending by frequency
 */
// ─────────────────────────────────────────────────────────
// Display helpers
// ─────────────────────────────────────────────────────────

/**
 * Builds a formatted, human-readable frequency table for display
 * in the API response body or console output.
 *
 * Example output line:
 *   Char: 'e'  |  Frequency:  14  |  Bar: ██████████████
 *
 * @param frequencies map produced by analyse*() methods
 * @return multi-line string suitable for display
 */

/**
 * Counts total accepted characters in the text.
 *
 * @param text raw input
 * @return number of accepted characters
 */

// ─────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────

/**
 * Converts a list of CharacterFrequency objects into a
 * LinkedHashMap sorted by frequency ascending (then char ascending).
 */
