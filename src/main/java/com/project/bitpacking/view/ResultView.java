package com.project.bitpacking.view;

import com.project.bitpacking.model.BitPacking;
import com.project.bitpacking.benchmark.BenchmarkEvaluationResult;

import java.util.Arrays;
import java.util.Locale;

/**
 * View layer responsible for formatting and displaying results.
 * Implements the View component of MVC architecture.
 */
public class ResultView {
    private static final Locale LOCALE = Locale.US;

    /**
     * Displays array declaration result.
     */
    public void displayArray(int[] array) {
        System.out.printf("Array %s%n", Arrays.toString(array));
    }

    /**
     * Displays compression result with timing.
     */
    public void displayCompression(BitPacking codec, double timeMs) {
        int[] backing = codec.backingArray();
        System.out.printf(LOCALE, "Packed words (%d ints) %s | Compression time : %.3f ms%n",
                backing.length, Arrays.toString(backing), timeMs);
        if (codec.overflowSize() > 0) {
            System.out.printf(LOCALE, "Overflow area length: %d%n", codec.overflowSize());
        }
    }

    /**
     * Displays decompression result with timing.
     */
    public void displayDecompression(int[] array, double timeMs) {
        System.out.printf(LOCALE, "Decompressed %s | Decompression time : %.3f ms%n",
                Arrays.toString(array), timeMs);
    }

    /**
     * Displays element retrieval result with timing.
     */
    public void displayGet(int index, int value, double timeMs) {
        System.out.printf(LOCALE, "Element %d = %d | Retrieval time : %.3f ms%n",
                index, value, timeMs);
    }

    /**
     * Displays benchmark evaluation result.
     */
    public void displayEvaluation(BenchmarkEvaluationResult result) {
        System.out.println(result.format());
    }

    /**
     * Displays benchmark loading progress.
     */
    public void displayBenchmarkLoading(String benchmarkName, int percent) {
        int bars = percent / 10;
        StringBuilder progress = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                progress.append("|");
            } else {
                progress.append(" ");
            }
        }
        progress.append("] - ").append(percent).append("%");
        System.out.println(progress);
    }

    /**
     * Displays error message.
     */
    public void displayError(String message) {
        System.err.println("Error: " + message);
    }

    /**
     * Displays info message.
     */
    public void displayInfo(String message) {
        System.out.println(message);
    }

    /**
     * Displays available benchmarks.
     */
    public void displayAvailableBenchmarks(java.util.List<com.project.bitpacking.benchmark.Benchmark> benchmarks) {
        System.out.println("Available benchmarks:");
        for (com.project.bitpacking.benchmark.Benchmark bench : benchmarks) {
            System.out.printf("  %s: %s%n", bench.getName(), bench.getDescription());
        }
    }

    /**
     * Displays available compression methods.
     */
    public void displayAvailableCompressionMethods(java.util.List<com.project.bitpacking.config.CompressionMethodConfig> methods) {
        System.out.println("Available compression methods:");
        for (com.project.bitpacking.config.CompressionMethodConfig method : methods) {
            System.out.printf("  %s: %s%n", method.getDisplayName(), method.getDescription());
        }
    }
}


