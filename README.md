# PQC Java Library Comparison

A JMH microbenchmark suite that compares **two Java implementations of the NIST
post-quantum standards** — Bouncy Castle and the JDK's own built-in providers —
for ML-KEM (FIPS 203, key encapsulation) and ML-DSA (FIPS 204, digital
signatures), alongside classical baselines (RSA-2048, ECDSA P-256, X25519).

Both libraries are exercised through the **same JCA APIs**
(`javax.crypto.KEM`, `java.security.Signature`), so the numbers reflect
implementation differences rather than API differences.

## What is measured

Each cryptographic operation is timed **in isolation** (this is a deliberate fix
of an earlier version that timed sign+verify and encaps+decaps together):

| Family | Variants | Operations | Providers |
|---|---|---|---|
| ML-KEM | 512, 768, 1024 | KeyGen, Encapsulate, Decapsulate | Bouncy Castle, JDK (SunJCE) |
| ML-DSA | 44, 65, 87 | KeyGen, Sign, Verify | Bouncy Castle, JDK (SUN) |
| RSA | 2048 | KeyGen, Sign, Verify | JDK (SunRsaSign) |
| ECDSA | P-256 | KeyGen, Sign, Verify | JDK (SunEC) |
| X25519 | — | KeyGen, Key agreement | JDK (SunEC) |

Signatures/ciphertexts used by the `Verify`/`Decapsulate` benchmarks are
pre-computed in `@Setup`, so only the target operation is on the measured path.

## Layout

```
src/main/java/com/pqc/pqcjavalibrarycomparison/
  PqcConfiguration.java   registers Bouncy Castle; shared JMH settings
  KemBenchmark.java        ML-KEM (parameterised by provider × level)
  DsaBenchmark.java        ML-DSA (parameterised by provider × level)
  ClassicalBenchmark.java  RSA / ECDSA / X25519 baselines
  PqcExperiments.java      runner main() → results/benchmark_result.json
src/test/java/.../PqcCorrectnessTest.java   both providers round-trip + reject tampering
results/
  analyze_results.py   JSON → summary.csv + RESULTS.md   (standard library only)
  generate_charts.py   JSON → figures/*.png              (needs matplotlib)
```

## Requirements

- **Build:** JDK 21+ (the `javax.crypto.KEM` API, JEP 452, is required to compile).
- **Run:** **JDK 24+** — the JDK's native ML-KEM/ML-DSA providers (JEP 496 / 497)
  only exist from JDK 24 onward. The published results were produced on **JDK 26**.
- Maven 3.8+.

> If you run on a JDK older than 24, the `provider=JDK` benchmarks cannot find the
> native algorithms and will fail; only the Bouncy Castle arm would work.

## JMH configuration

`AverageTime` mode, output in **microseconds/op**; 5 warmup + 10 measurement
iterations (1 s each) across **3 forks** (see `PqcConfiguration`). The pom
declares the JMH annotation processor explicitly under
`maven-compiler-plugin/annotationProcessorPaths` — this is required because since
JDK 23 `javac` no longer runs processors found only on the classpath, which would
otherwise leave the JMH `BenchmarkList` ungenerated.

## Running

```bash
export JAVA_HOME=/path/to/jdk-24-or-newer

# 1. compile (also generates the JMH benchmark list)
mvn -DskipTests clean compile

# 2. build the runtime classpath
mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.1:build-classpath \
    -Dmdep.outputFile=cp.txt

# 3. run the full suite (~35 min; writes results/benchmark_result.json)
java -cp "target/classes:$(cat cp.txt)" \
    com.pqc.pqcjavalibrarycomparison.PqcExperiments

# 4. tables + charts
python3 results/analyze_results.py          # summary.csv, RESULTS.md (no deps)
python3 results/generate_charts.py           # figures/*.png (pip install matplotlib)
```

Run the correctness tests with `mvn test`.

To run a subset directly via JMH, e.g. only ML-KEM-768 for both providers:

```bash
java -cp "target/classes:$(cat cp.txt)" org.openjdk.jmh.Main \
    ".*KemBenchmark.*" -p provider=BC,JDK -p level=768
```

## Results (JDK 26, Apple Silicon; µs/op, lower is better)

Full data in [`results/summary.csv`](results/summary.csv); digest in
[`results/RESULTS.md`](results/RESULTS.md); charts in `results/figures/`.

**Bouncy Castle vs. JDK native** — the JDK implementation is faster across the
board (speedup = BC / JDK):

| Primitive | Op | BC | JDK | JDK speedup |
|---|---|--:|--:|--:|
| ML-KEM-768 | KeyGen | 33.8 | 15.4 | 2.20× |
| ML-KEM-768 | Encaps | 31.8 | 13.7 | 2.32× |
| ML-KEM-768 | Decaps | 41.1 | 18.4 | 2.24× |
| ML-DSA-65 | Sign | 430.5 | 173.6 | 2.48× |
| ML-DSA-65 | Verify | 95.7 | 50.0 | 1.91× |
| ML-DSA-87 | Sign | 552.3 | 240.5 | 2.30× |

**PQC vs. classical** (JDK provider):

- **Key establishment: ML-KEM-768 = 47.4 µs vs X25519 = 207.8 µs → 4.4× faster.**
  Post-quantum key exchange is *cheaper* than the classical primitive here.
- Signing: ML-DSA-65 is 5.2× faster than RSA-2048, but ~1.6× slower than ECDSA P-256.
- Verifying: ML-DSA-65 is 7× faster than ECDSA P-256 and comparable to RSA-2048.
- RSA-2048 key generation (~85 ms) dwarfs everything else.

> Numbers are machine-specific (they were collected on an 8-core Apple Silicon
> laptop). Reproduce on your target hardware before drawing capacity conclusions;
> the *relative* ordering is the portable takeaway.

## Companion project

This repository measures the **primitive** cost of PQC in Java. Its companion,
[`pqc-hybrid-vs-classical`](https://github.com/Arpan0995/pqc-hybrid-vs-classical),
measures the **protocol-level** cost (TLS 1.3 handshakes with hybrid and PQC key
exchange). Together they trace PQC's cost from primitives up to TLS.
