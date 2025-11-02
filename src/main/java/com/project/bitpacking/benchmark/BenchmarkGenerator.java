package com.project.bitpacking.benchmark;

/**
 * Interface for benchmark data generators.
 * Allows extensible benchmark creation through different generation strategies.
 */
public interface BenchmarkGenerator {
    /**
     * Generates benchmark data based on the provided parameters.
     *
     * @param parameters array of string parameters specific to the generator
     * @return generated integer array
     */
    int[] generate(String[] parameters);
}


