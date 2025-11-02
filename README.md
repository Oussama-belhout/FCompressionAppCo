# Bit Packing Compression Application

A Java application implementing integer array compression using bit packing techniques, with support for multiple compression strategies, benchmarking, and an interactive REPL interface.

## Features

- **Multiple Compression Strategies**:
  - **CrossBoundary**: Packs bits contiguously across word boundaries for maximum efficiency
  - **Aligned**: Packs bits without crossing word boundaries (easier to access)
  - **Overflow**: Uses overflow areas for values requiring more bits than the majority

- **MVC Architecture**: Clean separation of concerns with Model-View-Controller pattern

- **Configurable & Extensible**: 
  - Compression methods and benchmarks defined via JSON configuration files
  - Dependency injection through configuration
  - Easy to add new compression methods or benchmarks

- **Interactive REPL**: Command-line interface for testing and evaluation

- **Comprehensive Benchmarking**: Built-in benchmark suite with timing measurements

- **Logging**: Centralized logging system controlled by a single DEBUG flag

## Requirements

- Java 17 or higher
- No external dependencies (uses standard Java libraries only)

## Project Structure

```
FCompressionAppCo/
├── src/main/java/com/project/bitpacking/
│   ├── model/          # Core compression algorithms
│   ├── view/           # Result formatting and display
│   ├── controller/     # REPL controller and command handling
│   ├── config/         # Configuration loading and parsing
│   ├── benchmark/      # Benchmark generators and evaluators
│   └── util/           # Utility classes (logging, bit operations)
├── config/
│   ├── compression-methods.json  # Compression method configurations
│   └── benchmarks.json            # Benchmark configurations
└── README.md
```

## Quick Start

### Option 1: Docker (recommended)

Build the container image:
```bash
docker build -t fcompressionapp .
```

Run the interactive REPL (press `Ctrl+C` or type `EXIT` to quit):
```bash
docker run -it --rm fcompressionapp
```

Pass flags (for example `--debug`) after the image name:
```bash
docker run -it --rm fcompressionapp --debug
```

To experiment with custom configuration files, mount them into the container:
```bash
docker run -it --rm \
  -v "$(pwd)/config:/app/config" \
  fcompressionapp
```

### Option 2: Local toolchain

#### Compile

```bash
javac -d out -sourcepath src/main/java src/main/java/com/project/bitpacking/**/*.java
```

Or use an IDE like IntelliJ IDEA or Eclipse.

#### Run

```bash
java -cp out com.project.bitpacking.Main
```

Enable debug logging:
```bash
java -cp out com.project.bitpacking.Main --debug
```

## REPL Commands

### Basic Operations

- **ARR \<n1,n2,...>**: Declare an array to work with
  ```
  >>> ARR 1,3,0,1,4,8,1
  Array [1, 3, 0, 1, 4, 8, 1]
  ```

- **COMPRESS \<strategy>**: Compress the current array
  ```
  >>> COMPRESS CrossBoundary
  Packed words (2 ints) [...] | Compression time : 0.123 ms
  ```

- **DECOMPRESS**: Decompress the last compressed array
  ```
  >>> DECOMPRESS
  Decompressed [1, 3, 0, 1, 4, 8, 1] | Decompression time : 0.045 ms
  ```

- **GET \<index>**: Retrieve a value at a specific index
  ```
  >>> GET 3
  Element 3 = 1 | Retrieval time : 0.001 ms
  ```

### Benchmarking

- **LOAD [benchmark] [params]**: Load a benchmark dataset
  ```
  >>> LOAD skewed 100000 0.95 0 31 0 1048575 2
  [||||||    ] - 60%
  ```

- **EVAL \<strategy>**: Evaluate a compression method on the loaded benchmark
  ```
  >>> EVAL CrossBoundary
  Results: (95% small values (0..31), 5% large spikes (0..1_048_575)) | strategy=CROSS_BOUNDARY | compress=0.265 ms | decompress=0.127 ms | get=48.200 ns | ints:100000->62500 | bits/value=20 | ratio=1.60 | latency-threshold=0.010 µs/int
  ```

### Utility Commands

- **HELP**: Show available commands
- **CLS**: Clear the screen
- **EXIT** or **QUIT**: Exit the REPL

## Configuration

### Compression Methods (`config/compression-methods.json`)

Define available compression strategies:

```json
[
  {
    "name": "CROSS_BOUNDARY",
    "displayName": "CrossBoundary",
    "className": "com.project.bitpacking.model.CrossBoundaryBitPacking",
    "description": "Packs bits contiguously across word boundaries"
  }
]
```

### Benchmarks (`config/benchmarks.json`)

Define benchmark datasets:

```json
[
  {
    "name": "uniform",
    "description": "Uniform distribution over 0..4095",
    "generatorClass": "com.project.bitpacking.benchmark.UniformBenchmarkGenerator",
    "dataFile": null,
    "parameters": ["100000", "4095", "1"],
    "metadata": {}
  }
]
```

## Extending the Application

### Adding a New Compression Method

1. Create a class implementing `BitPacking` interface (extend `AbstractBitPacking`)
2. Add entry to `config/compression-methods.json`
3. Update `BitPackingFactory` to handle the new type (or rely on reflection)

### Adding a New Benchmark Generator

1. Create a class implementing `BenchmarkGenerator`
2. Add entry to `config/benchmarks.json` with the generator class name
3. The system will automatically discover and use it via dependency injection

### Design Patterns Used

- **Factory Pattern**: `BitPackingFactory` for creating compression instances
- **Strategy Pattern**: Different compression strategies implement the same interface
- **Template Method**: `AbstractBitPacking` provides common functionality
- **Dependency Injection**: Configuration-driven instantiation of classes
- **MVC Pattern**: Separation of Model, View, and Controller

## UML Diagrams

Comprehensive UML diagrams are available in the `diagrams/` directory, demonstrating modeling and design pattern skills:

### Available Diagrams
- **Use Case Diagram**: System use cases and user interactions
- **Model Layer Class Diagram**: Core compression algorithms and structure
- **View Layer Class Diagram**: Presentation layer architecture
- **Controller Layer Class Diagram**: Command processing and orchestration
- **Benchmark & Config Layer**: Extensibility and configuration management
- **System Architecture**: Complete integration and component interactions

### Documentation
See `diagrams/README.md` for detailed documentation of all diagrams, design patterns, and architectural principles.

**Key Highlights**:
- 6 comprehensive UML diagrams
- 12+ design pattern implementations
- Complete layer-by-layer architecture documentation
- Integration diagrams showing system-wide interactions

## Benchmark Results Format

The `EVAL` command provides comprehensive performance metrics:

- **compress**: Time to compress the dataset (ms)
- **decompress**: Time to decompress the dataset (ms)
- **get**: Average time for random access (ns)
- **ints**: Original -> Compressed integer count
- **bits/value**: Average bits per value in compressed form
- **ratio**: Compression ratio
- **latency-threshold**: Minimum network latency per integer for compression to be beneficial

## Logging

Enable debug logging by:
1. Setting `Logger.DEBUG = true` in code, or
2. Running with `--debug` flag: `java -cp out com.project.bitpacking.Main --debug`

Debug logs help track execution flow and monitor operations.

## Example Session

```
>>> ARR 1,3,0,1,4,8,1
Array [1, 3, 0, 1, 4, 8, 1]

>>> COMPRESS CrossBoundary
Packed words (2 ints) [12345, 67890] | Compression time : 0.123 ms

>>> GET 3
Element 3 = 1 | Retrieval time : 0.001 ms

>>> DECOMPRESS
Decompressed [1, 3, 0, 1, 4, 8, 1] | Decompression time : 0.045 ms

>>> LOAD skewed 100000 0.95 0 31 0 1048575 2
[||||||||||] - 100%
Loaded benchmark: skewed (100000 elements)

>>> EVAL CrossBoundary
Results: (95% small values (0..31), 5% large spikes (0..1_048_575)) | strategy=CROSS_BOUNDARY | compress=0.265 ms | decompress=0.127 ms | get=48.200 ns | ints:100000->62500 | bits/value=20 | ratio=1.60 | latency-threshold=0.010 µs/int

>>> EXIT
Goodbye!
```

## License

This project is developed as part of a Software Engineering course.

## Authors

Software Engineering Project - Bit Packing Compression Implementation

