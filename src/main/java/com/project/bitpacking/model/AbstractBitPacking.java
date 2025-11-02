package com.project.bitpacking.model;

/**
 * Base class wiring shared state handling for bit packing implementations.
 */
abstract class AbstractBitPacking implements BitPacking {
    protected int[] packed;
    protected int elementCount;
    protected int bitsPerValue;

    @Override
    public int size() {
        return elementCount;
    }

    @Override
    public int bitsPerValue() {
        return bitsPerValue;
    }

    @Override
    public int[] backingArray() {
        return packed == null ? new int[0] : packed.clone();
    }
}


