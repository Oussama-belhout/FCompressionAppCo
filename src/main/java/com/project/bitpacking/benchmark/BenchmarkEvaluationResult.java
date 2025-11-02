package com.project.bitpacking.benchmark;

import com.project.bitpacking.model.CompressionType;

/**
 * Results from evaluating a compression method on a benchmark.
 */
public class BenchmarkEvaluationResult {
    private String benchmarkName;
    private String benchmarkDescription;
    private CompressionType strategy;
    private int originalInts;
    private int transmittedInts;
    private int bitsPerValue;
    private double compressTimeMs;
    private double decompressTimeMs;
    private double getTimeNs;
    private double compressionRatio;
    private double latencyThresholdUsPerInt;

    public BenchmarkEvaluationResult(String benchmarkName, String benchmarkDescription, CompressionType strategy,
                                    int originalInts, int transmittedInts, int bitsPerValue,
                                    double compressTimeMs, double decompressTimeMs, double getTimeNs,
                                    double compressionRatio, double latencyThresholdUsPerInt) {
        this.benchmarkName = benchmarkName;
        this.benchmarkDescription = benchmarkDescription;
        this.strategy = strategy;
        this.originalInts = originalInts;
        this.transmittedInts = transmittedInts;
        this.bitsPerValue = bitsPerValue;
        this.compressTimeMs = compressTimeMs;
        this.decompressTimeMs = decompressTimeMs;
        this.getTimeNs = getTimeNs;
        this.compressionRatio = compressionRatio;
        this.latencyThresholdUsPerInt = latencyThresholdUsPerInt;
    }

    // Getters
    public String getBenchmarkName() { return benchmarkName; }
    public String getBenchmarkDescription() { return benchmarkDescription; }
    public CompressionType getStrategy() { return strategy; }
    public int getOriginalInts() { return originalInts; }
    public int getTransmittedInts() { return transmittedInts; }
    public int getBitsPerValue() { return bitsPerValue; }
    public double getCompressTimeMs() { return compressTimeMs; }
    public double getDecompressTimeMs() { return decompressTimeMs; }
    public double getGetTimeNs() { return getTimeNs; }
    public double getCompressionRatio() { return compressionRatio; }
    public double getLatencyThresholdUsPerInt() { return latencyThresholdUsPerInt; }

    public String format() {
        String latencyText = Double.isInfinite(latencyThresholdUsPerInt) || Double.isNaN(latencyThresholdUsPerInt)
                ? "never"
                : String.format("%.3f µs/int", latencyThresholdUsPerInt);
        return String.format(
                "Results: (%s) | strategy=%s | compress=%.3f ms | decompress=%.3f ms | get=%.3f ns | " +
                "ints:%d->%d | bits/value=%d | ratio=%.2f | latency-threshold=%s",
                benchmarkDescription,
                strategy,
                compressTimeMs,
                decompressTimeMs,
                getTimeNs,
                originalInts,
                transmittedInts,
                bitsPerValue,
                compressionRatio,
                latencyText
        );
    }
}


