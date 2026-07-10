package com.pqc.pqcjavalibrarycomparison;

import org.openjdk.jmh.annotations.*;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.concurrent.TimeUnit;

/**
 * Classical baselines for comparison against the post-quantum primitives.
 *
 * <ul>
 *   <li>RSA-2048 and ECDSA P-256 are the signature baselines for ML-DSA.</li>
 *   <li>X25519 ephemeral key agreement is the key-establishment baseline for ML-KEM
 *       (both establish a shared secret; X25519 is the primitive ML-KEM most often
 *       replaces or is paired with in hybrid TLS).</li>
 * </ul>
 *
 * <p>All classical primitives use the JDK's default providers (SunRsaSign, SunEC),
 * and every operation is measured in isolation (keys/signatures are pre-computed in
 * {@link #setup()}).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = PqcConfiguration.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = PqcConfiguration.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(PqcConfiguration.FORK_COUNT)
public class ClassicalBenchmark {

    private KeyPairGenerator rsaKpg;
    private KeyPairGenerator ecKpg;
    private KeyPairGenerator xdhKpg;

    private byte[] message;

    // RSA-2048
    private PrivateKey rsaPrivate;
    private PublicKey rsaPublic;
    private byte[] rsaSignature;
    private Signature rsaSignEngine;
    private Signature rsaVerifyEngine;

    // ECDSA P-256
    private PrivateKey ecPrivate;
    private PublicKey ecPublic;
    private byte[] ecSignature;
    private Signature ecSignEngine;
    private Signature ecVerifyEngine;

    // X25519 key agreement
    private PrivateKey xdhPrivateA;
    private PublicKey xdhPublicB;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        message = "Benchmark message for classical signature timing.".getBytes();

        // --- RSA-2048 ---
        rsaKpg = KeyPairGenerator.getInstance("RSA");
        rsaKpg.initialize(2048);
        KeyPair rsa = rsaKpg.generateKeyPair();
        rsaPrivate = rsa.getPrivate();
        rsaPublic = rsa.getPublic();
        rsaSignEngine = Signature.getInstance("SHA256withRSA");
        rsaVerifyEngine = Signature.getInstance("SHA256withRSA");
        rsaSignEngine.initSign(rsaPrivate);
        rsaSignEngine.update(message);
        rsaSignature = rsaSignEngine.sign();

        // --- ECDSA P-256 ---
        ecKpg = KeyPairGenerator.getInstance("EC");
        ecKpg.initialize(256);
        KeyPair ec = ecKpg.generateKeyPair();
        ecPrivate = ec.getPrivate();
        ecPublic = ec.getPublic();
        ecSignEngine = Signature.getInstance("SHA256withECDSA");
        ecVerifyEngine = Signature.getInstance("SHA256withECDSA");
        ecSignEngine.initSign(ecPrivate);
        ecSignEngine.update(message);
        ecSignature = ecSignEngine.sign();

        // --- X25519 ephemeral key agreement ---
        xdhKpg = KeyPairGenerator.getInstance("X25519");
        xdhPrivateA = xdhKpg.generateKeyPair().getPrivate();
        xdhPublicB = xdhKpg.generateKeyPair().getPublic();
    }

    // ---- RSA-2048 ----
    @Benchmark
    public KeyPair rsa2048_keyGen() {
        return rsaKpg.generateKeyPair();
    }

    @Benchmark
    public byte[] rsa2048_sign() throws Exception {
        rsaSignEngine.initSign(rsaPrivate);
        rsaSignEngine.update(message);
        return rsaSignEngine.sign();
    }

    @Benchmark
    public boolean rsa2048_verify() throws Exception {
        rsaVerifyEngine.initVerify(rsaPublic);
        rsaVerifyEngine.update(message);
        return rsaVerifyEngine.verify(rsaSignature);
    }

    // ---- ECDSA P-256 ----
    @Benchmark
    public KeyPair ecdsaP256_keyGen() {
        return ecKpg.generateKeyPair();
    }

    @Benchmark
    public byte[] ecdsaP256_sign() throws Exception {
        ecSignEngine.initSign(ecPrivate);
        ecSignEngine.update(message);
        return ecSignEngine.sign();
    }

    @Benchmark
    public boolean ecdsaP256_verify() throws Exception {
        ecVerifyEngine.initVerify(ecPublic);
        ecVerifyEngine.update(message);
        return ecVerifyEngine.verify(ecSignature);
    }

    // ---- X25519 ephemeral key agreement (classical KEM-equivalent) ----
    @Benchmark
    public KeyPair x25519_keyGen() {
        return xdhKpg.generateKeyPair();
    }

    @Benchmark
    public byte[] x25519_keyAgreement() throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("X25519");
        ka.init(xdhPrivateA);
        ka.doPhase(xdhPublicB, true);
        return ka.generateSecret();
    }
}
