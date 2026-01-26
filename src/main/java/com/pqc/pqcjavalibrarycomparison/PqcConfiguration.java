package com.pqc.pqcjavalibrarycomparison;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * Configuration and setup for PQC benchmarking
 */
public class Configuration {

    static {
        // Register BouncyCastle as security provider
        Security.addProvider(new BouncyCastleProvider());
    }

    // Algorithm identifiers
    public static final String[] ML_KEM_VARIANTS = {"ML-KEM-512", "ML-KEM-768", "ML-KEM-1024"};
    public static final String[] ML_DSA_VARIANTS = {"ML-DSA-44", "ML-DSA-65", "ML-DSA-87"};
    public static final String[] CLASSICAL_VARIANTS = {"RSA/2048", "RSA/4096", "ECDSA/P-256", "X25519"};

    // Benchmark settings
    public static final int WARMUP_ITERATIONS = 5;        // 5 warmup runs
    public static final int MEASUREMENT_ITERATIONS = 10;  // 10 measurement runs
    public static final int FORK_COUNT = 3;               // 3 independent JVM forks
    public static final long TIME_PER_ITERATION = 1000;   // 1 second per iteration (ms)

    /**
     * Initialize the benchmark environment
     */
    public static void initialize() {
        System.out.println("=== PQC Benchmark Suite v1.0 ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JVM: " + System.getProperty("java.vm.name"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        System.out.println("");
    }
}
