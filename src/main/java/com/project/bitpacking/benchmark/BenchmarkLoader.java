package com.project.bitpacking.benchmark;

import com.project.bitpacking.util.Logger;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Loads and manages benchmarks using dependency injection from configuration.
 */
public class BenchmarkLoader {
    /**
     * Finds a benchmark by name from the list.
     *
     * @param benchmarks list of available benchmarks
     * @param name benchmark name
     * @return the benchmark or null if not found
     */
    public static Benchmark findBenchmark(List<Benchmark> benchmarks, String name) {
        return benchmarks.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates benchmark data using the configured generator class.
     *
     * @param benchmark the benchmark configuration
     * @param parameters override parameters (if null, uses benchmark's default parameters)
     * @return generated data
     */
    public static int[] generateBenchmarkData(Benchmark benchmark, String[] parameters) {
        String generatorClass = benchmark.getGeneratorClass();
        Logger.debug("Creating generator: %s", generatorClass);

        try {
            Class<?> clazz = Class.forName(generatorClass);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            BenchmarkGenerator generator = (BenchmarkGenerator) constructor.newInstance();

            String[] params = parameters != null ? parameters : 
                    benchmark.getParameters().toArray(new String[0]);
            return generator.generate(params);
        } catch (Exception e) {
            Logger.error("Failed to generate benchmark data: %s", e.getMessage());
            throw new RuntimeException("Failed to generate benchmark data: " + e.getMessage(), e);
        }
    }

    /**
     * Generates benchmark data using benchmark's default parameters.
     *
     * @param benchmark the benchmark configuration
     * @return generated data
     */
    public static int[] generateBenchmarkData(Benchmark benchmark) {
        return generateBenchmarkData(benchmark, null);
    }
}


