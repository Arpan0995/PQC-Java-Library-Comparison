package com.pqc.pqcjavalibrarycomparison;

import org.openjdk.jmh.annotations.*;

import javax.crypto.KEM;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.concurrent.TimeUnit;

/**
 * ML-KEM (FIPS 203) key-encapsulation benchmarks.
 *
 * <p>Each cryptographic operation is measured in isolation:
 * <ul>
 *   <li>{@link #keyGen()} measures only key-pair generation (the generator is
 *       initialised once in {@link #setup()}).</li>
 *   <li>{@link #encapsulate()} measures only encapsulation, using an
 *       {@link javax.crypto.KEM.Encapsulator} built once in {@link #setup()}.</li>
 *   <li>{@link #decapsulate()} measures only decapsulation of a ciphertext that
 *       is pre-generated in {@link #setup()} (encapsulation is NOT re-timed here).</li>
 * </ul>
 *
 * <p>The {@code provider} parameter selects the cryptographic library under test
 * (Bouncy Castle vs. the JDK's built-in providers), and both are exercised through
 * the identical {@link javax.crypto.KEM} API so the comparison is API-neutral.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = PqcConfiguration.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = PqcConfiguration.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(PqcConfiguration.FORK_COUNT)
public class KemBenchmark {

    /** Cryptographic library under test. */
    @Param({"BC", "JDK"})
    public String provider;

    /** ML-KEM security level (512, 768, 1024). */
    @Param({"512", "768", "1024"})
    public String level;

    private KeyPairGenerator kpg;
    private KEM.Encapsulator encapsulator;
    private KEM.Decapsulator decapsulator;
    private byte[] ciphertext;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        PqcConfiguration.initialize();

        // ML-KEM lives in Bouncy Castle ("BC") and in the JDK's SunJCE provider.
        String kpgProvider = provider.equals("BC") ? "BC" : "SunJCE";
        String kemProvider = provider.equals("BC") ? "BC" : "SunJCE";

        kpg = KeyPairGenerator.getInstance("ML-KEM-" + level, kpgProvider);
        KeyPair kp = kpg.generateKeyPair();

        KEM kem = KEM.getInstance("ML-KEM", kemProvider);
        encapsulator = kem.newEncapsulator(kp.getPublic());
        decapsulator = kem.newDecapsulator(kp.getPrivate());

        // Pre-generate a ciphertext so decapsulate() measures decapsulation only.
        ciphertext = encapsulator.encapsulate().encapsulation();
    }

    @Benchmark
    public KeyPair keyGen() {
        return kpg.generateKeyPair();
    }

    @Benchmark
    public byte[] encapsulate() throws Exception {
        return encapsulator.encapsulate().encapsulation();
    }

    @Benchmark
    public SecretKey decapsulate() throws Exception {
        return decapsulator.decapsulate(ciphertext);
    }
}
