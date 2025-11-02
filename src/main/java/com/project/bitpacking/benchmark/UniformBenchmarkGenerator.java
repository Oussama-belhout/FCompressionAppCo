package com.project.bitpacking.benchmark;

import com.project.bitpacking.util.Logger;

import java.util.Random;

/**
 * Generates uniformly distributed random integers.
 * Parameters: [size, maxValue, seed]
 */
public class UniformBenchmarkGenerator implements BenchmarkGenerator {
    @Override
    public int[] generate(String[] parameters) {
        if (parameters.length < 2) {
            throw new IllegalArgumentException("Uniform generator requires at least size and maxValue");
        }
        int size = Integer.parseInt(parameters[0].replace("_", ""));
        int maxValue = Integer.parseInt(parameters[1].replace("_", ""));
        long seed = parameters.length > 2 ? Long.parseLong(parameters[2].replace("_", "")) : System.currentTimeMillis();

        Logger.debug("Generating uniform data: size=%d, maxValue=%d, seed=%d", size, maxValue, seed);
        Random rnd = new Random(seed);
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = rnd.nextInt(maxValue + 1);
        }
        return data;
    }
}


