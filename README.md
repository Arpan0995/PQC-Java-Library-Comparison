PQC Java Library Comparison
==========================

What this project does
----------------------
This repository contains a small JMH benchmark suite that measures the performance of post-quantum cryptography (PQC) primitives implemented via Bouncy Castle in Java, and compares them to classical algorithms. The benchmarks exercise ML-KEM (key-encapsulation), ML-DSA (signatures) variants and classical baselines (RSA, ECDSA).

Purpose of the experiment
-------------------------
The goal is to evaluate the runtime cost (average time) of key generation, encapsulation/decapsulation, signing, and verification for multiple PQC algorithm variants and compare them with classical algorithms. The results help understand performance trade-offs when considering PQC algorithms for real-world use.

What we plan to analyze
-----------------------
- Key generation time for ML-KEM variants (512, 768, 1024) and classical key generation (RSA-2048, ECDSA P-256).
- Encapsulation and decapsulation latency for ML-KEM variants.
- Signing and verification latency for ML-DSA variants and classical signature algorithms (RSA, ECDSA).
- Compare average latencies (in milliseconds), variability, and how they scale relative to classical algorithms.

How the experiment is performed (brief)
--------------------------------------
- The benchmarks are implemented using JMH in `src/main/java/com/pqc/pqcjavalibrarycomparison/PqcExperiments.java`.
- Bouncy Castle is registered as a security provider in `PqcConfiguration` and used for PQC algorithms.
- JMH settings (also in `PqcConfiguration`):
  - Warmup iterations: 5
  - Measurement iterations: 10
  - Forks: 3
  - Time unit: milliseconds (AverageTime mode)
- Each trial initializes key-pair generators and pre-generates keys used by the benchmarks. Certain benchmarks (e.g., decapsulation) encapsulate and then decapsulate within the measured method to measure the combined operation.
- The main class `PqcExperiments` runs a JMH `Runner` that writes results to `results/benchmark_result.json` in JSON format.

Prerequisites
-------------
- Java 17 (or compatible JDK)
- Apache Maven (3.6+ recommended)
- Enough memory and CPU available (benchmarks fork JVMs and are CPU-bound)

Quick run steps
---------------
1. Build the project (compile and package):

```bash
mvn clean package -DskipTests
```

2. Run the benchmark runner (uses the main method in `PqcExperiments`):

```bash
# Run the JMH Runner from the packaged jar
java -cp target/PQC-Java-Library-Comparison-0.0.1-SNAPSHOT.jar \
  com.pqc.pqcjavalibrarycomparison.PqcExperiments
```

Alternative (run directly with Maven exec plugin if you have it installed):

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass="com.pqc.pqcjavalibrarycomparison.PqcExperiments"
```

What to expect as output
------------------------
- Console output: startup info from `PqcConfiguration.initialize()` (JVM, OS, processors, memory) and JMH progress and summaries.
- Results file: `results/benchmark_result.json` will be produced. It contains an array of benchmark results in JMH JSON format. Each entry includes fields such as:
  - `benchmark`: the benchmark name
  - `primaryMetric.score`: the reported average time
  - `primaryMetric.scoreUnit`: typically `ms` (milliseconds)
  - `primaryMetric.scoreError`: estimated error
  - `params`: additional parameters or variant names

Inspect this JSON (or import into a spreadsheet / analysis script) to compare algorithms.

Notes & tips
------------
- If you want to limit which benchmarks run, modify the `OptionsBuilder` in `PqcExperiments.main` (it currently uses `.include(".*")`) or pass your own JMH arguments by changing the runner creation.
- To change benchmark settings (warmup/measurement/forks), edit constants in `PqcConfiguration`.
- Benchmarks may take a few minutes because they run multiple iterations and forks; choose machine resources accordingly.
- The verify methods in this suite perform a sign then verify inside the same benchmark method (i.e., they measure the combined operation as implemented).

Next steps (optional)
---------------------
- Parse `results/benchmark_result.json` and produce CSV reports or plots for easier comparison.
- Add JMH profilers or record GC/JVM metrics for deeper system-level analysis.
- Add more PQC algorithms or different parameter sets.

Contact
-------
For questions about this experiment, inspect the source under `src/main/java/com/pqc/pqcjavalibrarycomparison/` or open an issue in the project tracker.

