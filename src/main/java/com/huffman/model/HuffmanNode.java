// Developed by: Mohammad Sheikh Qasem
package com.huffman.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class HuffmanNode implements Comparable<HuffmanNode> {

    private final Character character;

    private final int frequency;

    @Setter
    private HuffmanNode left;

    @Setter
    private HuffmanNode right;

    public HuffmanNode(Character character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left      = null;
        this.right     = null;
    }

    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
        this.character = null;
        this.frequency = frequency;
        this.left      = left;
        this.right     = right;
    }

    @Override
    public int compareTo(HuffmanNode other){

        int freqDiff = Integer.compare(this.frequency, other.frequency);
        if (freqDiff != 0) return freqDiff;

        // Tie-break: leaf < internal, then alphabetical
        if (this.character != null && other.character != null) {
            return Character.compare(this.character, other.character);
        }

        if (this.character != null) return -1;
        if (other.character != null) return 1;

        return 0;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        String charDisplay = (character != null)
                ? "'" + (character == ' ' ? "SPACE" : character) + "'"
                : "INTERNAL";
        return "HuffmanNode{char=" + charDisplay + ", freq=" + frequency + "}";
    }
}


/* ====== README ===== */

/**
 * ============================================================
 * HuffmanNode.java
 * ------------------------------------------------------------
 * Represents a single node in the Huffman Tree.
 *
 * Each node stores:
 *   - character  : the character this node represents (null for internal nodes)
 *   - frequency  : total frequency (sum for internal, actual for leaf)
 *   - left/right : child pointers for building the binary tree
 *
 * Implements Comparable so nodes can be ordered by frequency
 * inside a priority queue (min-heap) without external comparators.
 *
 * Data structure used: Binary Tree Node (linked structure).
 * ============================================================
 */

/** The character stored in this leaf node. Null for internal nodes. */

/** Frequency (weight) of this node. */

/** Left child – conventionally carries bit '0'. */

/** Right child – conventionally carries bit '1'. */

// ─────────────────────────────────────────────────────────
// Constructors
// ─────────────────────────────────────────────────────────

/**
 * Leaf node constructor.
 *
 * @param character the character this leaf represents
 * @param frequency how many times the character appears in the input
 */

/**
 * Internal node constructor.
 * Character is set to null because internal nodes don't represent
 * a specific character—they are synthetic merge nodes.
 *
 * @param frequency combined frequency of all merged children
 * @param left      left child subtree
 * @param right     right child subtree
 */

// ─────────────────────────────────────────────────────────
// Comparable – enables min-heap ordering by frequency
// ─────────────────────────────────────────────────────────

/**
 * Compares two nodes by frequency (ascending).
 * Nodes with equal frequency are further compared by character
 * value to guarantee a deterministic, reproducible tree shape.
 */

// ─────────────────────────────────────────────────────────
// Predicates
// ─────────────────────────────────────────────────────────

/** @return true when this node is a leaf (has no children). */

// ─────────────────────────────────────────────────────────
// Getters & Setters
// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// Debugging
// ─────────────────────────────────────────────────────────





