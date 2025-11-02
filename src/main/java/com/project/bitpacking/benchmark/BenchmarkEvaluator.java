package com.project.bitpacking.benchmark;

import com.project.bitpacking.model.BitPacking;
import com.project.bitpacking.model.CompressionType;
import com.project.bitpacking.util.Logger;

import java.util.Random;

/**
 * Evaluates compression methods on benchmark data with timing measurements.
 */
public class BenchmarkEvaluator {
    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASURE_ROUNDS = 20;
    private static final int GET_PROBES = 1000;

    /**
     * Evaluates a compression method on benchmark data.
     *
     * @param benchmark the benchmark configuration
     * @param data the benchmark data
     * @param codec the compression codec to evaluate
     * @param strategy the compression strategy type
     * @return evaluation results
     */
    public BenchmarkEvaluationResult evaluate(Benchmark benchmark, int[] data, BitPacking codec, CompressionType strategy) {
        Logger.debug("Evaluating %s with strategy %s", benchmark.getName(), strategy);

        double compressNs = timeCompress(codec, data);
        codec.compress(data); // ensure content for following operations
        double decompressNs = timeDecompress(codec, data);
        double getNs = timeRandomAccess(codec);

        int transmittedInts = codec.backingArray().length + codec.overflowSize();
        int originalInts = data.length;
        double savedRatio = transmittedInts == 0 ? 0.0 : (double) originalInts / transmittedInts;
        double latencyThresholdNs = computeLatencyThreshold(compressNs, decompressNs, originalInts, transmittedInts);

        return new BenchmarkEvaluationResult(
                benchmark.getName(),
                benchmark.getDescription(),
                strategy,
                originalInts,
                transmittedInts,
                codec.bitsPerValue(),
                compressNs / 1_000_000.0,
                decompressNs / 1_000_000.0,
                getNs,
                savedRatio,
                latencyThresholdNs / 1_000.0
        );
    }

    private double timeCompress(BitPacking codec, int[] data) {
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            codec.compress(data);
        }
        long total = 0L;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            codec.compress(data);
            total += System.nanoTime() - start;
        }
        return total / (double) MEASURE_ROUNDS;
    }

    private double timeDecompress(BitPacking codec, int[] original) {
        int[] buffer = new int[original.length];
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            codec.decompress(buffer);
        }
        long total = 0L;
        for (int i = 0; i < MEASURE_ROUNDS; i++) {
            long start = System.nanoTime();
            codec.decompress(buffer);
            total += System.nanoTime() - start;
        }
        return total / (double) MEASURE_ROUNDS;
    }

    private double timeRandomAccess(BitPacking codec) {
        if (codec.size() == 0) {
            return 0.0;
        }
        Random rnd = new Random(42L);
        long total = 0L;
        for (int i = 0; i < GET_PROBES; i++) {
            int index = rnd.nextInt(codec.size());
            long start = System.nanoTime();
            codec.get(index);
            total += System.nanoTime() - start;
        }
        return total / (double) GET_PROBES;
    }

    private double computeLatencyThreshold(double compressNs, double decompressNs,
                                          int originalLength, int transmittedInts) {
        int savedInts = originalLength - transmittedInts;
        if (savedInts <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (compressNs + decompressNs) / savedInts;
    }
}


