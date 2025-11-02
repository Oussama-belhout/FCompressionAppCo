package com.project.bitpacking.benchmark;

import com.project.bitpacking.util.Logger;

import java.util.Random;

/**
 * Generates skewed data: mostly small values with some large spikes.
 * Parameters: [size, smallProbability, smallMin, smallMax, largeMin, largeMax, seed]
 */
public class SkewedBenchmarkGenerator implements BenchmarkGenerator {
    @Override
    public int[] generate(String[] parameters) {
        if (parameters.length < 6) {
            throw new IllegalArgumentException("Skewed generator requires: size, smallProbability, smallMin, smallMax, largeMin, largeMax, [seed]");
        }
        int size = Integer.parseInt(parameters[0].replace("_", ""));
        double smallProbability = Double.parseDouble(parameters[1]);
        int smallMin = Integer.parseInt(parameters[2].replace("_", ""));
        int smallMax = Integer.parseInt(parameters[3].replace("_", ""));
        int largeMin = Integer.parseInt(parameters[4].replace("_", ""));
        int largeMax = Integer.parseInt(parameters[5].replace("_", ""));
        long seed = parameters.length > 6 ? Long.parseLong(parameters[6].replace("_", "")) : System.currentTimeMillis();

        Logger.debug("Generating skewed data: size=%d, smallProb=%.2f, small=[%d,%d], large=[%d,%d], seed=%d",
                size, smallProbability, smallMin, smallMax, largeMin, largeMax, seed);

        Random rnd = new Random(seed);
        int[] data = new int[size];
        int smallRange = smallMax - smallMin + 1;
        int largeRange = largeMax - largeMin + 1;
        for (int i = 0; i < size; i++) {
            if (rnd.nextDouble() < smallProbability) {
                data[i] = smallMin + rnd.nextInt(smallRange);
            } else {
                data[i] = largeMin + rnd.nextInt(largeRange);
            }
        }
        return data;
    }
}


