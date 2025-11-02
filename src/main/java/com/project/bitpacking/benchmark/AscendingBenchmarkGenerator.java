package com.project.bitpacking.benchmark;

import com.project.bitpacking.util.Logger;

/**
 * Generates an ascending sequence.
 * Parameters: [size, step]
 */
public class AscendingBenchmarkGenerator implements BenchmarkGenerator {
    @Override
    public int[] generate(String[] parameters) {
        if (parameters.length < 2) {
            throw new IllegalArgumentException("Ascending generator requires: size, step");
        }
        int size = Integer.parseInt(parameters[0].replace("_", ""));
        int step = Integer.parseInt(parameters[1].replace("_", ""));

        Logger.debug("Generating ascending data: size=%d, step=%d", size, step);
        int[] data = new int[size];
        int value = 0;
        for (int i = 0; i < size; i++) {
            data[i] = value;
            value += step;
        }
        return data;
    }
}


