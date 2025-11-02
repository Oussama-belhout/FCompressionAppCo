package com.project.bitpacking.model;

import com.project.bitpacking.util.Logger;

/**
 * Factory responsible for instantiating the requested compression strategy.
 * Uses dependency injection through configuration.
 */
public final class BitPackingFactory {
    private BitPackingFactory() {
    }

    /**
     * Creates a BitPacking instance based on the compression type.
     *
     * @param type the compression type
     * @return a new BitPacking instance
     */
    public static BitPacking create(CompressionType type) {
        Logger.debug("Creating BitPacking instance for type: %s", type);
        return switch (type) {
            case CROSS_BOUNDARY -> new CrossBoundaryBitPacking();
            case ALIGNED -> new AlignedBitPacking();
            case OVERFLOW -> new OverflowBitPacking();
        };
    }

    /**
     * Creates a BitPacking instance from a string name (case-insensitive).
     *
     * @param name the compression type name
     * @return a new BitPacking instance
     * @throws IllegalArgumentException if the name is not recognized
     */
    public static BitPacking create(String name) {
        try {
            String normalized = name.toUpperCase().replace("-", "_");
            CompressionType type = CompressionType.valueOf(normalized);
            return create(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown compression type: " + name, e);
        }
    }
}


