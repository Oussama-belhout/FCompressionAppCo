package com.project.bitpacking.util;

/**
 * Utility class for bit manipulation operations.
 */
public final class BitUtils {
    private BitUtils() {
    }

    /**
     * Calculates the minimum number of bits required to represent a non-negative integer.
     *
     * @param value the value to analyze (must be non-negative)
     * @return number of bits needed (0 for value 0, otherwise ceil(log2(value + 1)))
     */
    public static int bitsRequired(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        if (value == 0) {
            return 0;
        }
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    /**
     * Computes the ceiling of log2 for a positive integer.
     *
     * @param value positive integer
     * @return ceil(log2(value)), or 0 if value is 0 or 1
     */
    public static int ceilLog2(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        if (value == 1) {
            return 0;
        }
        return 32 - Integer.numberOfLeadingZeros(value - 1);
    }
}


