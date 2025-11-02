package com.project.bitpacking.config;

import com.project.bitpacking.benchmark.Benchmark;
import com.project.bitpacking.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads configuration files for compression methods and benchmarks.
 * Uses JSON format for extensibility.
 */
public class ConfigLoader {
    /**
     * Loads compression method configurations from a JSON file.
     *
     * @param configPath path to the JSON configuration file
     * @return list of compression method configurations
     */
    public static List<CompressionMethodConfig> loadCompressionMethods(String configPath) {
        try {
            if (!Files.exists(Paths.get(configPath))) {
                Logger.debug("Compression methods config not found at %s, using defaults", configPath);
                return getDefaultCompressionMethods();
            }
            String json = new String(Files.readAllBytes(Paths.get(configPath)));
            List<CompressionMethodConfig> configs = SimpleJsonParser.parseCompressionMethods(json);
            Logger.debug("Loaded %d compression method configurations", configs.size());
            return configs.isEmpty() ? getDefaultCompressionMethods() : configs;
        } catch (IOException e) {
            Logger.error("Error loading compression methods config: %s", e.getMessage());
            return getDefaultCompressionMethods();
        } catch (Exception e) {
            Logger.error("Error parsing compression methods config: %s", e.getMessage());
            return getDefaultCompressionMethods();
        }
    }

    /**
     * Loads benchmark configurations from a JSON file.
     *
     * @param configPath path to the JSON configuration file
     * @return list of benchmark configurations
     */
    public static List<Benchmark> loadBenchmarks(String configPath) {
        try {
            if (!Files.exists(Paths.get(configPath))) {
                Logger.debug("Benchmarks config not found at %s, using defaults", configPath);
                return getDefaultBenchmarks();
            }
            String json = new String(Files.readAllBytes(Paths.get(configPath)));
            List<Benchmark> benchmarks = SimpleJsonParser.parseBenchmarks(json);
            Logger.debug("Loaded %d benchmark configurations", benchmarks.size());
            return benchmarks.isEmpty() ? getDefaultBenchmarks() : benchmarks;
        } catch (IOException e) {
            Logger.error("Error loading benchmarks config: %s", e.getMessage());
            return getDefaultBenchmarks();
        } catch (Exception e) {
            Logger.error("Error parsing benchmarks config: %s", e.getMessage());
            return getDefaultBenchmarks();
        }
    }

    private static List<CompressionMethodConfig> getDefaultCompressionMethods() {
        return List.of(
                new CompressionMethodConfig("CROSS_BOUNDARY", "CrossBoundary", 
                        "com.project.bitpacking.model.CrossBoundaryBitPacking",
                        "Packs bits contiguously across word boundaries"),
                new CompressionMethodConfig("ALIGNED", "Aligned",
                        "com.project.bitpacking.model.AlignedBitPacking",
                        "Packs bits without crossing word boundaries"),
                new CompressionMethodConfig("OVERFLOW", "Overflow",
                        "com.project.bitpacking.model.OverflowBitPacking",
                        "Uses overflow area for large values")
        );
    }

    private static List<Benchmark> getDefaultBenchmarks() {
        List<Benchmark> benchmarks = new ArrayList<>();
        benchmarks.add(new Benchmark("uniform", "Uniform distribution over 0..4095",
                "com.project.bitpacking.benchmark.UniformBenchmarkGenerator",
                null, List.of("100000", "4095", "1"), Map.of()));
        benchmarks.add(new Benchmark("skewed", "95% small values (0..31), 5% large spikes (0..1_048_575)",
                "com.project.bitpacking.benchmark.SkewedBenchmarkGenerator",
                null, List.of("100000", "0.95", "0", "31", "0", "1048575", "2"), Map.of()));
        benchmarks.add(new Benchmark("ascending", "Monotonic increasing sequence",
                "com.project.bitpacking.benchmark.AscendingBenchmarkGenerator",
                null, List.of("100000", "10"), Map.of()));
        return benchmarks;
    }
}

