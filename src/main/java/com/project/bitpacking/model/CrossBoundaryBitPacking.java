package com.project.bitpacking.model;

import com.project.bitpacking.util.BitUtils;

import java.util.Arrays;

/**
 * Packs integers contiguously, allowing values to span across successive 32-bit words.
 */
public final class CrossBoundaryBitPacking extends AbstractBitPacking {

    @Override
    public void compress(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Input array must not be null");
        }
        if (values.length == 0) {
            this.packed = new int[0];
            this.elementCount = 0;
            this.bitsPerValue = 0;
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

        if (bitsPerValue == 0) {
            this.packed = new int[0];
            return;
        }

        long totalBits = (long) bitsPerValue * elementCount;
        int wordCount = (int) ((totalBits + 31) / 32);
        this.packed = new int[wordCount];

        int bitIndex = 0;
        for (int value : values) {
            writeValue(bitIndex, value);
            bitIndex += bitsPerValue;
        }
    }

    @Override
    public void decompress(int[] destination) {
        ensureReady(destination);
        if (bitsPerValue == 0) {
            Arrays.fill(destination, 0, elementCount, 0);
            return;
        }
        int bitIndex = 0;
        for (int i = 0; i < elementCount; i++) {
            destination[i] = readValue(bitIndex);
            bitIndex += bitsPerValue;
        }
    }

    @Override
    public int get(int index) {
        requireIndex(index);
        if (bitsPerValue == 0) {
            return 0;
        }
        int bitIndex = index * bitsPerValue;
        return readValue(bitIndex);
    }

    private void writeValue(int bitIndex, int value) {
        int wordIndex = bitIndex >>> 5;
        int bitOffset = bitIndex & 31;

        if (bitOffset + bitsPerValue <= 32) {
            packed[wordIndex] |= value << bitOffset;
        } else {
            int lowerBits = 32 - bitOffset;
            int upperBits = bitsPerValue - lowerBits;
            int lowerMask = (1 << lowerBits) - 1;
            packed[wordIndex] |= (value & lowerMask) << bitOffset;
            packed[wordIndex + 1] |= value >>> lowerBits;
        }
    }

    private int readValue(int bitIndex) {
        int wordIndex = bitIndex >>> 5;
        int bitOffset = bitIndex & 31;

        if (bitOffset + bitsPerValue <= 32) {
            int mask = bitsPerValue == 32 ? -1 : (1 << bitsPerValue) - 1;
            return (packed[wordIndex] >>> bitOffset) & mask;
        }
        int lowerBits = 32 - bitOffset;
        int upperBits = bitsPerValue - lowerBits;
        int lowerMask = (1 << lowerBits) - 1;
        int lowerPart = (packed[wordIndex] >>> bitOffset) & lowerMask;
        int upperPart = packed[wordIndex + 1] & ((1 << upperBits) - 1);
        return (upperPart << lowerBits) | lowerPart;
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


