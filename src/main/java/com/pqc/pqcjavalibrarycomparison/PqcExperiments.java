package com.pqc.pqcjavalibrarycomparison;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Entry point that runs every JMH benchmark in this module and writes the
 * results to {@code results/benchmark_result.json} in JMH JSON format.
 *
 * <p>The benchmarks themselves live in dedicated classes so that each
 * cryptographic operation is measured in isolation:
 * <ul>
 *   <li>{@link KemBenchmark}       – ML-KEM key generation / encapsulation / decapsulation</li>
 *   <li>{@link DsaBenchmark}       – ML-DSA key generation / signing / verification</li>
 *   <li>{@link ClassicalBenchmark} – RSA-2048, ECDSA P-256, X25519 baselines</li>
 * </ul>
 *
 * <p>The ML-KEM and ML-DSA benchmarks are parameterised by {@code provider}
 * ({@code BC} = Bouncy Castle, {@code JDK} = the platform's built-in providers),
 * yielding a like-for-like library comparison through the same public APIs.
 */
public class PqcExperiments {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KemBenchmark.class.getName())
                .include(DsaBenchmark.class.getName())
                .include(ClassicalBenchmark.class.getName())
                .result("results/benchmark_result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
