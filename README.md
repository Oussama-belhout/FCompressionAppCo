# Bit Packing Compression Application

Lightweight Java app that demonstrates integer-array compression using several bit-packing strategies, a small REPL for interactive experimentation, and a benchmark/evaluation subsystem. This README is updated to match the current project layout and usage.

## Highlights

- Several packing strategies implemented under `src/main/java/com/project/bitpacking/model`
- Small interactive REPL controller at `src/main/java/com/project/bitpacking/controller/ReplController.java`
- Config-driven discovery of compression strategies and benchmarks using JSON files in `config/`
- No third-party dependencies — runs on Java 17+

## Requirements

- Java 17 (or later)
- Docker (optional, for containerized runs)

The project does not include a build system like Maven/Gradle; a simple `build.bat` (Windows) and `build.sh` (POSIX) are provided to compile the sources.

## Repository layout

```
FCompressionAppCo/
├── build.bat            # Windows compile helper
├── build.sh             # POSIX compile helper
├── Dockerfile
├── README.md
├── config/
│   ├── benchmarks.json
│   └── compression-methods.json
└── src/
    └── main/java/com/project/bitpacking/
        ├── Main.java
        ├── benchmark/         # benchmark generators & evaluator
        ├── config/            # config loaders & simple JSON parser
        ├── controller/        # REPL controller
        ├── model/             # compression implementations & interfaces
        └── util/              # BitUtils, Logger, etc.
```

## Build & run

Recommended (Windows / PowerShell):

1) Use the provided build script which compiles Java sources into `out/`:

```powershell
.\build.bat
```

2) Run the application:

```powershell
java -cp out com.project.bitpacking.Main
```

Enable debug logging by adding `--debug` when running the Main class:

```powershell
java -cp out com.project.bitpacking.Main --debug
```

Notes:
- If you prefer an IDE, import the `src/main/java` folder as a source root and run `com.project.bitpacking.Main`.
- On POSIX systems you can run `./build.sh` instead of `build.bat`.

### Docker

Build the image (from project root):

```bash
docker build -t fcompressionapp .
```

Run the REPL inside the container:

```bash
docker run -it --rm fcompressionapp
```

If you want to override configuration files with your local `config/` directory, mount it into the container. Example (POSIX):

```bash
docker run -it --rm -v "$(pwd)/config:/app/config" fcompressionapp
```

On Windows PowerShell you can mount with `-v ${PWD}\config:/app/config` (watch for backslashes quoting rules).

## REPL commands (summary)

The interactive controller accepts simple commands. Key commands used for experimentation and benchmarking:

- ARR <n1,n2,...> — set current integer array to work with
- COMPRESS <strategy> — compress the current array using a strategy defined in `config/compression-methods.json` (display name or factory name)
- DECOMPRESS — decompress the last-compressed data
- GET <index> — retrieve single element from the compressed representation (latency measured)
- LOAD <benchmark> [params...] — load or generate a benchmark dataset defined in `config/benchmarks.json`
- EVAL <strategy> — run full evaluation for the loaded benchmark and chosen strategy (compress/decompress/get timings + metrics)
- HELP — show available commands
- CLS — clear screen
- EXIT / QUIT — exit REPL

The program prints simple timing/size/ratio metrics for evaluations.

## Configuration files

- `config/compression-methods.json` — maps human-readable names to implementation classes and descriptions. This drives which strategies appear in the REPL and evaluator.
- `config/benchmarks.json` — defines benchmark entries (generator class, parameters, and optional data files).

Keep those files in the `config/` folder at the project root. When running inside Docker, mount a host `config/` to `/app/config` to override.

## Extending the project

Adding a new compression strategy:

1. Add a new Java class in `src/main/java/com/project/bitpacking/model` that extends `AbstractBitPacking` or implements the expected `BitPacking` interface.
2. Add an entry to `config/compression-methods.json` with the display name and fully-qualified class name.
3. If needed, update `BitPackingFactory` (factory logic is centralized under `model/`) to recognize the new type or rely on reflection if the project loader supports it.

Adding a new benchmark generator:

1. Implement `BenchmarkGenerator` in `src/main/java/com/project/bitpacking/benchmark`.
2. Add an entry into `config/benchmarks.json` listing the generator class name and default parameters.

## Logging

Centralized logging is provided via `src/main/java/com/project/bitpacking/util/Logger.java`. Toggle the debug output at runtime with `--debug` (the Main class recognizes this flag) or by changing `Logger.DEBUG` in code if you prefer a compile-time toggle.

## Example session

Interactive examples (short):

```
>>> ARR 1,3,0,1,4,8,1
Array [1, 3, 0, 1, 4, 8, 1]

>>> COMPRESS CrossBoundary
Packed words (N ints) [...] | Compression time : 0.12 ms

>>> GET 3
Element 3 = 1 | Retrieval time : 0.001 ms

>>> DECOMPRESS
Decompressed [1, 3, 0, 1, 4, 8, 1] | Decompression time : 0.04 ms

>>> EXIT
Goodbye!
```

## Notes & troubleshooting

- This project intentionally keeps no external build system to remain minimal and easy to inspect. Use `build.bat` / `build.sh` or your IDE to compile.
- If you see ClassNotFound errors, confirm the `out/` directory exists and contains compiled classes, and that you ran the `java -cp out com.project.bitpacking.Main` command from the project root.
- If you change `config/` while the REPL is running, restart the app to reload configuration.
