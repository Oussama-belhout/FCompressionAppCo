package com.project.bitpacking.model;

/**
 * Describes the bit packing strategy to use when encoding integer arrays.
 */
public enum CompressionType {
    /**
     * Pack bits contiguously, allowing values to straddle two consecutive 32-bit words.
     */
    CROSS_BOUNDARY,

    /**
     * Pack bits without crossing 32-bit word boundaries. Values are padded to stay within a single word.
     */
    ALIGNED,

    /**
     * Pack values using overflow-aware encoding that prioritises tight encoding for the bulk of the data
     * while delegating outliers to an auxiliary overflow area.
     */
    OVERFLOW
}


