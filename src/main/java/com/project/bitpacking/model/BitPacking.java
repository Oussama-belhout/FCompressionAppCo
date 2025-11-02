package com.project.bitpacking.model;

/**
 * Core contract for bit packing codecs.
 * This interface defines the operations for compressing and decompressing integer arrays.
 */
public interface BitPacking {

    /**
     * Compresses the provided array. Implementations may retain references to the input until the next call to
     * {@link #compress(int[])}.
     *
     * @param values integers to compress, must not be null
     */
    void compress(int[] values);

    /**
     * Decompresses the stored compressed representation into the provided destination array.
     *
     * @param destination buffer that will receive the decompressed integers; must be at least as long as the
     *                    original array passed to {@link #compress(int[])}
     */
    void decompress(int[] destination);

    /**
     * Provides random access to the decompressed value at the requested index without fully materialising the array.
     *
     * @param index zero-based index into the original integer sequence
     * @return value at the index
     */
    int get(int index);

    /**
     * @return the number of elements in the compressed sequence
     */
    int size();

    /**
     * @return the number of bits used per value in the primary compressed representation
     */
    int bitsPerValue();

    /**
     * @return raw packed data backing the compression output
     */
    int[] backingArray();

    /**
     * @return size of the overflow region that must also be transmitted (defaults to 0 for strategies that do not
     * use an overflow area)
     */
    default int overflowSize() {
        return 0;
    }
}


