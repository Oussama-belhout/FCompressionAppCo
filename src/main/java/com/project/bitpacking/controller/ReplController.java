package com.project.bitpacking.controller;

import com.project.bitpacking.benchmark.*;
import com.project.bitpacking.config.CompressionMethodConfig;
import com.project.bitpacking.config.ConfigLoader;
import com.project.bitpacking.model.BitPacking;
import com.project.bitpacking.model.CompressionType;
import com.project.bitpacking.model.BitPackingFactory;
import com.project.bitpacking.util.Logger;
import com.project.bitpacking.view.ResultView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Controller for the REPL interface.
 * Implements the Controller component of MVC architecture.
 * Handles all user commands and coordinates between Model and View.
 */
public class ReplController {
    private static final String CONFIG_DIR = "config";
    private static final String BENCHMARKS_DIR = "benchmarks";

    private final ResultView view;
    private final List<CompressionMethodConfig> compressionMethods;
    private final List<Benchmark> benchmarks;
    private final BenchmarkEvaluator evaluator;

    // REPL state
    private int[] currentArray;
    private BitPacking currentCodec;
    private CompressionType currentStrategy;
    private Benchmark currentBenchmark;
    private int[] currentBenchmarkData;

    public ReplController() {
        this.view = new ResultView();
        this.compressionMethods = ConfigLoader.loadCompressionMethods(
                CONFIG_DIR + File.separator + "compression-methods.json");
        this.benchmarks = ConfigLoader.loadBenchmarks(
                CONFIG_DIR + File.separator + "benchmarks.json");
        this.evaluator = new BenchmarkEvaluator();
        Logger.debug("ReplController initialized with %d compression methods and %d benchmarks",
                compressionMethods.size(), benchmarks.size());
    }

    /**
     * Starts the REPL loop.
     */
    public void start() {
        System.out.println("Bit Packing Compression REPL. Type HELP for instructions.");
        System.out.println("Available commands: ARR, COMPRESS, DECOMPRESS, GET, LOAD, EVAL, HELP, CLS, EXIT");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(">>> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String rawLine = scanner.nextLine().trim();
                if (rawLine.isEmpty()) {
                    continue;
                }

                String[] parts = rawLine.split("\\s+", 2);
                String command = parts[0].toUpperCase(Locale.ROOT);
                String remainder = parts.length > 1 ? parts[1].trim() : "";

                try {
                    boolean shouldContinue = processCommand(command, remainder);
                    if (!shouldContinue) {
                        break;
                    }
                } catch (Exception ex) {
                    view.displayError(ex.getMessage());
                    Logger.debug("Error processing command: %s", ex.getMessage());
                    if (Logger.DEBUG) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean processCommand(String command, String remainder) {
        return switch (command) {
            case "ARR" -> handleArr(remainder);
            case "COMPRESS" -> handleCompress(remainder);
            case "DECOMPRESS" -> handleDecompress();
            case "GET" -> handleGet(remainder);
            case "LOAD" -> handleLoad(remainder);
            case "EVAL" -> handleEval(remainder);
            case "HELP" -> handleHelp();
            case "CLS" -> handleCls();
            case "EXIT", "QUIT" -> handleExit();
            default -> {
                view.displayError("Unknown command. Type HELP for the list of commands.");
                yield true;
            }
        };
    }

    private boolean handleArr(String remainder) {
        if (remainder.isEmpty()) {
            view.displayError("Usage: ARR <comma-separated integers>");
            return true;
        }
        currentArray = parseArray(remainder);
        currentCodec = null;
        currentStrategy = null;
        view.displayArray(currentArray);
        return true;
    }

    private boolean handleCompress(String remainder) {
        if (currentArray == null) {
            view.displayError("Declare an array first using ARR.");
            return true;
        }
        if (remainder.isEmpty()) {
            view.displayError("Usage: COMPRESS <strategy>");
            view.displayAvailableCompressionMethods(compressionMethods);
            return true;
        }

        try {
            CompressionType type = parseCompressionType(remainder);
            BitPacking delegate = BitPackingFactory.create(type);
            currentStrategy = type;
            currentCodec = delegate;

            long start = System.nanoTime();
            currentCodec.compress(currentArray);
            double timeMs = (System.nanoTime() - start) / 1_000_000.0;

            view.displayCompression(currentCodec, timeMs);
        } catch (IllegalArgumentException e) {
            view.displayError("Unknown compression strategy: " + remainder);
            view.displayAvailableCompressionMethods(compressionMethods);
        }
        return true;
    }

    private boolean handleDecompress() {
        if (currentCodec == null) {
            view.displayError("Nothing has been compressed yet.");
            return true;
        }
        int[] buffer = new int[currentCodec.size()];
        long start = System.nanoTime();
        currentCodec.decompress(buffer);
        double timeMs = (System.nanoTime() - start) / 1_000_000.0;
        view.displayDecompression(buffer, timeMs);
        return true;
    }

    private boolean handleGet(String remainder) {
        if (currentCodec == null) {
            view.displayError("Nothing has been compressed yet.");
            return true;
        }
        if (remainder.isEmpty()) {
            view.displayError("Usage: GET <index>");
            return true;
        }
        try {
            int index = Integer.parseInt(remainder);
            if (index < 0 || index >= currentCodec.size()) {
                view.displayError(String.format("Index %d out of bounds (0..%d).", index, currentCodec.size() - 1));
                return true;
            }
            long start = System.nanoTime();
            int value = currentCodec.get(index);
            double timeMs = (System.nanoTime() - start) / 1_000_000.0;
            view.displayGet(index, value, timeMs);
        } catch (NumberFormatException e) {
            view.displayError("Invalid index: " + remainder);
        }
        return true;
    }

    private boolean handleLoad(String remainder) {
        if (remainder.isEmpty()) {
            view.displayAvailableBenchmarks(benchmarks);
            return true;
        }

        String[] parts = remainder.split("\\s+");
        String benchmarkName = parts[0];
        Benchmark benchmark = BenchmarkLoader.findBenchmark(benchmarks, benchmarkName);

        if (benchmark == null) {
            view.displayError("Benchmark not found: " + benchmarkName);
            view.displayAvailableBenchmarks(benchmarks);
            return true;
        }

        try {
            String[] parameters = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : null;
            view.displayInfo("Loading benchmark: " + benchmarkName);
            view.displayBenchmarkLoading(benchmarkName, 50);
            currentBenchmarkData = BenchmarkLoader.generateBenchmarkData(benchmark, parameters);
            currentBenchmark = benchmark;
            currentArray = currentBenchmarkData;
            currentCodec = null;
            view.displayBenchmarkLoading(benchmarkName, 100);
            view.displayInfo(String.format("Loaded benchmark: %s (%d elements)", benchmarkName, currentBenchmarkData.length));
        } catch (Exception e) {
            view.displayError("Failed to load benchmark: " + e.getMessage());
        }
        return true;
    }

    private boolean handleEval(String remainder) {
        if (currentBenchmarkData == null || currentBenchmark == null) {
            view.displayError("Load a benchmark first using LOAD.");
            return true;
        }
        if (remainder.isEmpty()) {
            view.displayError("Usage: EVAL <strategy>");
            view.displayAvailableCompressionMethods(compressionMethods);
            return true;
        }

        try {
            CompressionType type = parseCompressionType(remainder);
            BitPacking codec = BitPackingFactory.create(type);
            BenchmarkEvaluationResult result = evaluator.evaluate(
                    currentBenchmark, currentBenchmarkData, codec, type);
            view.displayEvaluation(result);
        } catch (IllegalArgumentException e) {
            view.displayError("Unknown compression strategy: " + remainder);
            view.displayAvailableCompressionMethods(compressionMethods);
        }
        return true;
    }

    private boolean handleHelp() {
        System.out.println("Available commands:");
        System.out.println("  ARR <n1,n2,...>           - declare the array to work with");
        System.out.println("  COMPRESS <strategy>       - compress the current array");
        System.out.println("  DECOMPRESS                - decompress using the last compression result");
        System.out.println("  GET <index>               - read value at the given index from compressed data");
        System.out.println("  LOAD [benchmark] [params] - load a benchmark (list available if no name given)");
        System.out.println("  EVAL <strategy>           - evaluate compression method on loaded benchmark");
        System.out.println("  HELP                      - show this help text");
        System.out.println("  CLS                       - clear the screen");
        System.out.println("  EXIT | QUIT               - leave the REPL");
        System.out.println();
        System.out.println("Compression strategies: CrossBoundary, Aligned, Overflow");
        return true;
    }

    private boolean handleCls() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print many newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
        return true;
    }

    private boolean handleExit() {
        System.out.println("Goodbye!");
        return false;
    }

    private int[] parseArray(String input) {
        return Arrays.stream(input.split("[,\\s]+"))
                .filter(token -> !token.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private CompressionType parseCompressionType(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).replace("-", "_");
        return CompressionType.valueOf(normalized);
    }
}


