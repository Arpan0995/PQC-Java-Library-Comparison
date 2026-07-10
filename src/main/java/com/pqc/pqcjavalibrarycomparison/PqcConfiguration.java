package com.pqc.pqcjavalibrarycomparison;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provider registration and shared benchmark configuration.
 *
 * <p>Bouncy Castle is registered as an additional JCA provider (it is appended,
 * so the JDK's built-in providers keep their default precedence). Each benchmark
 * selects its provider explicitly by name ("BC" vs. "SunJCE"/"SUN"), so the two
 * libraries are compared on equal footing through the same JCA APIs.
 */
public class PqcConfiguration {

    static {
        // Register BouncyCastle as an additional security provider.
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // Benchmark settings (shared by every benchmark class).
    public static final int WARMUP_ITERATIONS = 5;        // 5 warmup iterations
    public static final int MEASUREMENT_ITERATIONS = 10;  // 10 measurement iterations
    public static final int FORK_COUNT = 3;               // 3 independent JVM forks

    /** Prints the environment banner exactly once per JVM (per fork). */
    private static final AtomicBoolean PRINTED = new AtomicBoolean(false);

    /**
     * Ensures the provider is registered and prints an environment banner the
     * first time it is called within a JVM. Safe to call from every {@code @Setup}.
     */
    public static void initialize() {
        if (PRINTED.compareAndSet(false, true)) {
            System.out.println("=== PQC Benchmark Suite ===");
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("JVM: " + System.getProperty("java.vm.name")
                    + " (" + System.getProperty("java.vm.version") + ")");
            System.out.println("OS: " + System.getProperty("os.name") + " "
                    + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
            System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
            System.out.println();
        }
    }
}
