package com.project.bitpacking.model;

import com.project.bitpacking.util.BitUtils;

/**
 * Packs integers while enforcing that every value stays within a single 32-bit word.
 * Excess capacity in a word is left unused.
 */
public final class AlignedBitPacking extends AbstractBitPacking {
    private int valuesPerWord;

    @Override
    public void compress(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Input array must not be null");
        }
        if (values.length == 0) {
            this.packed = new int[0];
            this.elementCount = 0;
            this.bitsPerValue = 0;
            this.valuesPerWord = 0;
            return;
        }

        int max = 0;
        for (int value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("Negative values are not supported by this codec");
            }
            if (value > max) {
                max = value;
            }
        }

        this.bitsPerValue = BitUtils.bitsRequired(max);
        this.elementCount = values.length;
        this.valuesPerWord = Math.max(1, 32 / bitsPerValue);

        int wordCount = (elementCount + valuesPerWord - 1) / valuesPerWord;
        this.packed = new int[wordCount];

        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            int wordIndex = i / valuesPerWord;
            int slotIndex = i % valuesPerWord;
            int bitOffset = slotIndex * bitsPerValue;
            packed[wordIndex] |= value << bitOffset;
        }
    }

    @Override
    public void decompress(int[] destination) {
        ensureReady(destination);
        for (int i = 0; i < elementCount; i++) {
            destination[i] = readAligned(i);
        }
    }

    @Override
    public int get(int index) {
        requireIndex(index);
        return readAligned(index);
    }

    private int readAligned(int logicalIndex) {
        int wordIndex = logicalIndex / valuesPerWord;
        int slotIndex = logicalIndex % valuesPerWord;
        int bitOffset = slotIndex * bitsPerValue;
        int mask = bitsPerValue == 32 ? -1 : (1 << bitsPerValue) - 1;
        return (packed[wordIndex] >>> bitOffset) & mask;
    }

    private void ensureReady(int[] destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination array must not be null");
        }
        if (destination.length < elementCount) {
            throw new IllegalArgumentException("Destination array is too small");
        }
        if (packed == null) {
            throw new IllegalStateException("Nothing has been compressed yet");
        }
    }

    private void requireIndex(int index) {
        if (index < 0 || index >= elementCount) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + elementCount);
        }
        if (packed == null) {
            throw new IllegalStateException("Nothing has been compressed yet");
        }
    }
}


