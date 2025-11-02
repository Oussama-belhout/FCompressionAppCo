package com.project.bitpacking.model;

import com.project.bitpacking.util.BitUtils;

/**
 * Bit packing codec with overflow support. Values that require more than {@code primaryBits} are stored in
 * a dedicated overflow area, while the primary stream keeps a compact representation for the bulk of the data.
 */
public final class OverflowBitPacking extends AbstractBitPacking {
    private int primaryBits;
    private int overflowIndexBits;
    private int overflowIndexMask;
    private boolean useOverflowFlag;
    private int[] overflowValues;

    @Override
    public void compress(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("Input array must not be null");
        }
        if (values.length == 0) {
            this.elementCount = 0;
            this.bitsPerValue = 0;
            this.packed = new int[0];
            this.primaryBits = 0;
            this.overflowIndexBits = 0;
            this.useOverflowFlag = false;
            this.overflowValues = new int[0];
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
        int maxBits = BitUtils.bitsRequired(max);

        Selection selection = chooseBestConfiguration(values, maxBits);
        this.primaryBits = selection.primaryBits;
        this.overflowIndexBits = selection.overflowIndexBits;
        this.overflowIndexMask = overflowIndexBits == 0
            ? 0
            : (overflowIndexBits >= 32 ? -1 : (1 << overflowIndexBits) - 1);
        this.useOverflowFlag = selection.useOverflowFlag;
        this.bitsPerValue = selection.bitsPerEntry;
        this.elementCount = values.length;
        this.overflowValues = new int[selection.overflowCount];

        long totalBits = (long) bitsPerValue * elementCount;
        int wordCount = (int) ((totalBits + 31) / 32);
        this.packed = new int[wordCount];

        int bitIndex = 0;
        int overflowCursor = 0;
        int payloadMask = primaryBits == 32 ? -1 : (1 << primaryBits) - 1;

        for (int value : values) {
            int bitsNeeded = BitUtils.bitsRequired(value);
            if (useOverflowFlag && bitsNeeded > primaryBits) {
                overflowValues[overflowCursor] = value;
                int entryPayload = overflowIndexBits == 0 ? 0 : (overflowCursor & overflowIndexMask);
                int entry = (entryPayload << 1) | 1; // LSB is overflow flag
                writeBits(bitIndex, bitsPerValue, entry);
                bitIndex += bitsPerValue;
                overflowCursor++;
            } else {
                int payload = useOverflowFlag ? (value & payloadMask) : value;
                int entry = useOverflowFlag ? (payload << 1) : payload;
                writeBits(bitIndex, useOverflowFlag ? bitsPerValue : primaryBits, entry);
                bitIndex += useOverflowFlag ? bitsPerValue : primaryBits;
            }
        }
    }

    @Override
    public void decompress(int[] destination) {
        ensureReady(destination);
        int bitIndex = 0;
        int payloadMask = primaryBits == 32 ? -1 : (1 << primaryBits) - 1;
        for (int i = 0; i < elementCount; i++) {
            if (useOverflowFlag) {
                int entry = readBits(bitIndex, bitsPerValue);
                bitIndex += bitsPerValue;
                boolean overflow = (entry & 1) == 1;
                int payload = entry >>> 1;
                if (overflow) {
                    int overflowIndex = overflowIndexBits == 0 ? 0 : (payload & overflowIndexMask);
                    if (overflowIndex >= overflowValues.length) {
                        throw new IllegalStateException("Corrupted overflow index " + overflowIndex);
                    }
                    destination[i] = overflowValues[overflowIndex];
                } else {
                    destination[i] = payload & payloadMask;
                }
            } else {
                destination[i] = readBits(bitIndex, primaryBits);
                bitIndex += primaryBits;
            }
        }
    }

    @Override
    public int get(int index) {
        requireIndex(index);
        int payloadMask = primaryBits == 32 ? -1 : (1 << primaryBits) - 1;
        if (!useOverflowFlag) {
            int bitIndex = index * primaryBits;
            return readBits(bitIndex, primaryBits);
        }
        int bitIndex = index * bitsPerValue;
        int entry = readBits(bitIndex, bitsPerValue);
        if ((entry & 1) == 1) {
            int payload = entry >>> 1;
            int overflowIndex = overflowIndexBits == 0 ? 0 : (payload & overflowIndexMask);
            if (overflowIndex >= overflowValues.length) {
                throw new IllegalStateException("Corrupted overflow index " + overflowIndex);
            }
            return overflowValues[overflowIndex];
        }
        return (entry >>> 1) & payloadMask;
    }

    @Override
    public int overflowSize() {
        return overflowValues == null ? 0 : overflowValues.length;
    }

    private void writeBits(int bitIndex, int bitCount, int value) {
        int wordIndex = bitIndex >>> 5;
        int bitOffset = bitIndex & 31;
        if (bitOffset + bitCount <= 32) {
            packed[wordIndex] |= value << bitOffset;
        } else {
            int lowerBits = 32 - bitOffset;
            int lowerMask = (1 << lowerBits) - 1;
            packed[wordIndex] |= (value & lowerMask) << bitOffset;
            packed[wordIndex + 1] |= value >>> lowerBits;
        }
    }

    private int readBits(int bitIndex, int bitCount) {
        int wordIndex = bitIndex >>> 5;
        int bitOffset = bitIndex & 31;
        if (bitOffset + bitCount <= 32) {
            int mask = bitCount == 32 ? -1 : (1 << bitCount) - 1;
            return (packed[wordIndex] >>> bitOffset) & mask;
        }
        int lowerBits = 32 - bitOffset;
        int upperBits = bitCount - lowerBits;
        int lowerMask = (1 << lowerBits) - 1;
        int lowerPart = (packed[wordIndex] >>> bitOffset) & lowerMask;
        int upperMask = upperBits == 32 ? -1 : (1 << upperBits) - 1;
        int upperPart = packed[wordIndex + 1] & upperMask;
        return (upperPart << lowerBits) | lowerPart;
    }

    private Selection chooseBestConfiguration(int[] values, int maxBits) {
        Selection best = null;
        for (int candidate = 1; candidate <= maxBits; candidate++) {
            int overflowCount = 0;
            for (int value : values) {
                int bits = BitUtils.bitsRequired(value);
                if (bits > candidate) {
                    overflowCount++;
                }
            }
            boolean useOverflowFlag = overflowCount > 0;
            int overflowBits = useOverflowFlag ? BitUtils.ceilLog2(overflowCount) : 0;
            int bitsPerEntry = useOverflowFlag ? 1 + Math.max(candidate, overflowBits) : candidate;
            long primaryCost = (long) bitsPerEntry * values.length;
            long overflowCost = (long) overflowCount * 32L;
            long totalCost = primaryCost + overflowCost;

            if (best == null || totalCost < best.totalBits ||
                    (totalCost == best.totalBits && bitsPerEntry < best.bitsPerEntry)) {
                best = new Selection(candidate, overflowBits, bitsPerEntry, overflowCount, useOverflowFlag, totalCost);
            }
        }

        if (best == null) {
            throw new IllegalStateException("Unable to derive configuration");
        }
        return best;
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

    private record Selection(int primaryBits,
                             int overflowIndexBits,
                             int bitsPerEntry,
                             int overflowCount,
                             boolean useOverflowFlag,
                             long totalBits) {
    }
}


