package com.pqc.pqcjavalibrarycomparison;

import org.openjdk.jmh.annotations.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.concurrent.TimeUnit;

/**
 * ML-DSA (FIPS 204) digital-signature benchmarks.
 *
 * <p>Each cryptographic operation is measured in isolation:
 * <ul>
 *   <li>{@link #keyGen()} measures only key-pair generation.</li>
 *   <li>{@link #sign()} measures only signature generation over a fixed message
 *       (the key pair is pre-generated in {@link #setup()}).</li>
 *   <li>{@link #verify()} measures only signature verification of a signature that
 *       is pre-computed in {@link #setup()} (signing is NOT re-timed here).</li>
 * </ul>
 *
 * <p>The {@code provider} parameter selects Bouncy Castle vs. the JDK's built-in
 * SUN provider; both are exercised through the same {@link java.security.Signature} API.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = PqcConfiguration.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = PqcConfiguration.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(PqcConfiguration.FORK_COUNT)
public class DsaBenchmark {

    /** Cryptographic library under test. */
    @Param({"BC", "JDK"})
    public String provider;

    /** ML-DSA security level (44, 65, 87). */
    @Param({"44", "65", "87"})
    public String level;

    private KeyPairGenerator kpg;
    private Signature signEngine;
    private Signature verifyEngine;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] message;
    private byte[] signature;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        PqcConfiguration.initialize();

        // ML-DSA lives in Bouncy Castle ("BC") and in the JDK's SUN provider.
        String sigProvider = provider.equals("BC") ? "BC" : "SUN";
        String algorithm = "ML-DSA-" + level;

        kpg = KeyPairGenerator.getInstance(algorithm, sigProvider);
        KeyPair kp = kpg.generateKeyPair();
        privateKey = kp.getPrivate();
        publicKey = kp.getPublic();

        message = "Benchmark message for ML-DSA signature timing.".getBytes();

        signEngine = Signature.getInstance(algorithm, sigProvider);
        verifyEngine = Signature.getInstance(algorithm, sigProvider);

        // Pre-compute a signature so verify() measures verification only.
        signEngine.initSign(privateKey);
        signEngine.update(message);
        signature = signEngine.sign();
    }

    @Benchmark
    public KeyPair keyGen() {
        return kpg.generateKeyPair();
    }

    @Benchmark
    public byte[] sign() throws Exception {
        signEngine.initSign(privateKey);
        signEngine.update(message);
        return signEngine.sign();
    }

    @Benchmark
    public boolean verify() throws Exception {
        verifyEngine.initVerify(publicKey);
        verifyEngine.update(message);
        return verifyEngine.verify(signature);
    }
}
