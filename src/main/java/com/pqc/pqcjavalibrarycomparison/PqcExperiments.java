package com.pqc.pqcjavalibrarycomparison;

import org.bouncycastle.jcajce.spec.MLDSAParameterSpec;
import org.bouncycastle.jcajce.spec.MLKEMParameterSpec;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.crypto.Cipher;
import java.security.*;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks for Post-Quantum Cryptography in Java
 * Measures performance of ML-KEM, ML-DSA, and classical algorithms
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = PqcConfiguration.WARMUP_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = PqcConfiguration.MEASUREMENT_ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(PqcConfiguration.FORK_COUNT)
public class PqcExperiments {

    private KeyPairGenerator mlkem512KpGen;
    private KeyPairGenerator mlkem768KpGen;
    private KeyPairGenerator mlkem1024KpGen;
    private KeyPairGenerator mldsa44KpGen;
    private KeyPairGenerator mldsa65KpGen;
    private KeyPairGenerator mldsa87KpGen;
    private KeyPairGenerator rsaKpGen;
    private KeyPairGenerator ecdsaKpGen;

    private KeyPair mlkem512Pair;
    private KeyPair mlkem768Pair;
    private KeyPair mlkem1024Pair;
    private KeyPair mldsa44Pair;
    private KeyPair mldsa65Pair;
    private KeyPair mldsa87Pair;
    private KeyPair rsaPair;
    private KeyPair ecdsaPair;

    private Cipher mlkem512Cipher;
    private Cipher mlkem768Cipher;
    private Cipher mlkem1024Cipher;

    private byte[] messageToSign;

    /**
     * Setup: Initialize all key generators and algorithms
     */
    @Setup(Level.Trial)
    public void setup() throws Exception {
        PqcConfiguration.initialize();

        // Initialize KeyPairGenerators for ML-KEM variants
        mlkem512KpGen = KeyPairGenerator.getInstance("ML-KEM", "BC");
        mlkem512KpGen.initialize(MLKEMParameterSpec.ml_kem_512);

        mlkem768KpGen = KeyPairGenerator.getInstance("ML-KEM", "BC");
        mlkem768KpGen.initialize(MLKEMParameterSpec.ml_kem_768);

        mlkem1024KpGen = KeyPairGenerator.getInstance("ML-KEM", "BC");
        mlkem1024KpGen.initialize(MLKEMParameterSpec.ml_kem_1024);

        // Initialize KeyPairGenerators for ML-DSA variants
        mldsa44KpGen = KeyPairGenerator.getInstance("ML-DSA", "BC");
        mldsa44KpGen.initialize(MLDSAParameterSpec.ml_dsa_44);

        mldsa65KpGen = KeyPairGenerator.getInstance("ML-DSA", "BC");
        mldsa65KpGen.initialize(MLDSAParameterSpec.ml_dsa_65);

        mldsa87KpGen = KeyPairGenerator.getInstance("ML-DSA", "BC");
        mldsa87KpGen.initialize(MLDSAParameterSpec.ml_dsa_87);

        // Initialize classical algorithms
        rsaKpGen = KeyPairGenerator.getInstance("RSA");
        rsaKpGen.initialize(2048);

        ecdsaKpGen = KeyPairGenerator.getInstance("EC");
        ecdsaKpGen.initialize(256);

        // Generate key pairs once per trial
        System.out.println("Generating key pairs...");
        mlkem512Pair = mlkem512KpGen.generateKeyPair();
        mlkem768Pair = mlkem768KpGen.generateKeyPair();
        mlkem1024Pair = mlkem1024KpGen.generateKeyPair();
        mldsa44Pair = mldsa44KpGen.generateKeyPair();
        mldsa65Pair = mldsa65KpGen.generateKeyPair();
        mldsa87Pair = mldsa87KpGen.generateKeyPair();
        rsaPair = rsaKpGen.generateKeyPair();
        ecdsaPair = ecdsaKpGen.generateKeyPair();

        // Initialize ciphers
        mlkem512Cipher = Cipher.getInstance("ML-KEM", "BC");
        mlkem768Cipher = Cipher.getInstance("ML-KEM", "BC");
        mlkem1024Cipher = Cipher.getInstance("ML-KEM", "BC");

        // Message to sign
        messageToSign = "Test message for signature benchmark".getBytes();

        System.out.println("Setup complete!\n");
    }

    // ============================================
    // ML-KEM KEY GENERATION BENCHMARKS
    // ============================================

    @Benchmark
    public KeyPair mlkem512_keyGen() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM", "BC");
        kpg.initialize(MLKEMParameterSpec.ml_kem_512);
        return kpg.generateKeyPair();
    }

    @Benchmark
    public KeyPair mlkem768_keyGen() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM", "BC");
        kpg.initialize(MLKEMParameterSpec.ml_kem_768);
        return kpg.generateKeyPair();
    }

    @Benchmark
    public KeyPair mlkem1024_keyGen() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM", "BC");
        kpg.initialize(MLKEMParameterSpec.ml_kem_1024);
        return kpg.generateKeyPair();
    }

    // ============================================
    // ML-KEM ENCAPSULATION BENCHMARKS
    // ============================================

    @Benchmark
    public byte[] mlkem512_encaps() throws Exception {
        Cipher cipher = Cipher.getInstance("ML-KEM", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, mlkem512Pair.getPublic());
        return cipher.doFinal();
    }

    @Benchmark
    public byte[] mlkem768_encaps() throws Exception {
        Cipher cipher = Cipher.getInstance("ML-KEM", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, mlkem768Pair.getPublic());
        return cipher.doFinal();
    }

    @Benchmark
    public byte[] mlkem1024_encaps() throws Exception {
        Cipher cipher = Cipher.getInstance("ML-KEM", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, mlkem1024Pair.getPublic());
        return cipher.doFinal();
    }

    // ============================================
    // ML-KEM DECAPSULATION BENCHMARKS
    // ============================================

    @Benchmark
    public byte[] mlkem512_decaps() throws Exception {
        // First encapsulate
        Cipher encCipher = Cipher.getInstance("ML-KEM", "BC");
        encCipher.init(Cipher.ENCRYPT_MODE, mlkem512Pair.getPublic());
        byte[] ciphertext = encCipher.doFinal();

        // Then decapsulate
        Cipher decCipher = Cipher.getInstance("ML-KEM", "BC");
        decCipher.init(Cipher.DECRYPT_MODE, mlkem512Pair.getPrivate());
        return decCipher.doFinal(ciphertext);
    }

    @Benchmark
    public byte[] mlkem768_decaps() throws Exception {
        Cipher encCipher = Cipher.getInstance("ML-KEM", "BC");
        encCipher.init(Cipher.ENCRYPT_MODE, mlkem768Pair.getPublic());
        byte[] ciphertext = encCipher.doFinal();

        Cipher decCipher = Cipher.getInstance("ML-KEM", "BC");
        decCipher.init(Cipher.DECRYPT_MODE, mlkem768Pair.getPrivate());
        return decCipher.doFinal(ciphertext);
    }

    @Benchmark
    public byte[] mlkem1024_decaps() throws Exception {
        Cipher encCipher = Cipher.getInstance("ML-KEM", "BC");
        encCipher.init(Cipher.ENCRYPT_MODE, mlkem1024Pair.getPublic());
        byte[] ciphertext = encCipher.doFinal();

        Cipher decCipher = Cipher.getInstance("ML-KEM", "BC");
        decCipher.init(Cipher.DECRYPT_MODE, mlkem1024Pair.getPrivate());
        return decCipher.doFinal(ciphertext);
    }

    // ============================================
    // ML-DSA SIGNING BENCHMARKS
    // ============================================

    @Benchmark
    public byte[] mldsa44_sign() throws Exception {
        Signature sig = Signature.getInstance("ML-DSA", "BC");
        sig.initSign(mldsa44Pair.getPrivate());
        sig.update(messageToSign);
        return sig.sign();
    }

    @Benchmark
    public byte[] mldsa65_sign() throws Exception {
        Signature sig = Signature.getInstance("ML-DSA", "BC");
        sig.initSign(mldsa65Pair.getPrivate());
        sig.update(messageToSign);
        return sig.sign();
    }

    @Benchmark
    public byte[] mldsa87_sign() throws Exception {
        Signature sig = Signature.getInstance("ML-DSA", "BC");
        sig.initSign(mldsa87Pair.getPrivate());
        sig.update(messageToSign);
        return sig.sign();
    }

    // ============================================
    // ML-DSA VERIFICATION BENCHMARKS
    // ============================================

    @Benchmark
    public boolean mldsa44_verify() throws Exception {
        Signature sigSign = Signature.getInstance("ML-DSA", "BC");
        sigSign.initSign(mldsa44Pair.getPrivate());
        sigSign.update(messageToSign);
        byte[] signature = sigSign.sign();

        Signature sigVerify = Signature.getInstance("ML-DSA", "BC");
        sigVerify.initVerify(mldsa44Pair.getPublic());
        sigVerify.update(messageToSign);
        return sigVerify.verify(signature);
    }

    @Benchmark
    public boolean mldsa65_verify() throws Exception {
        Signature sigSign = Signature.getInstance("ML-DSA", "BC");
        sigSign.initSign(mldsa65Pair.getPrivate());
        sigSign.update(messageToSign);
        byte[] signature = sigSign.sign();

        Signature sigVerify = Signature.getInstance("ML-DSA", "BC");
        sigVerify.initVerify(mldsa65Pair.getPublic());
        sigVerify.update(messageToSign);
        return sigVerify.verify(signature);
    }

    @Benchmark
    public boolean mldsa87_verify() throws Exception {
        Signature sigSign = Signature.getInstance("ML-DSA", "BC");
        sigSign.initSign(mldsa87Pair.getPrivate());
        sigSign.update(messageToSign);
        byte[] signature = sigSign.sign();

        Signature sigVerify = Signature.getInstance("ML-DSA", "BC");
        sigVerify.initVerify(mldsa87Pair.getPublic());
        sigVerify.update(messageToSign);
        return sigVerify.verify(signature);
    }

    // ============================================
    // CLASSICAL ALGORITHM BENCHMARKS
    // ============================================

    @Benchmark
    public KeyPair rsa2048_keyGen() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    @Benchmark
    public byte[] rsa2048_sign() throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(rsaPair.getPrivate());
        sig.update(messageToSign);
        return sig.sign();
    }

    @Benchmark
    public boolean rsa2048_verify() throws Exception {
        Signature sigSign = Signature.getInstance("SHA256withRSA");
        sigSign.initSign(rsaPair.getPrivate());
        sigSign.update(messageToSign);
        byte[] signature = sigSign.sign();

        Signature sigVerify = Signature.getInstance("SHA256withRSA");
        sigVerify.initVerify(rsaPair.getPublic());
        sigVerify.update(messageToSign);
        return sigVerify.verify(signature);
    }

    @Benchmark
    public KeyPair ecdsa_p256_keyGen() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        return kpg.generateKeyPair();
    }

    @Benchmark
    public byte[] ecdsa_p256_sign() throws Exception {
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(ecdsaPair.getPrivate());
        sig.update(messageToSign);
        return sig.sign();
    }

    @Benchmark
    public boolean ecdsa_p256_verify() throws Exception {
        Signature sigSign = Signature.getInstance("SHA256withECDSA");
        sigSign.initSign(ecdsaPair.getPrivate());
        sigSign.update(messageToSign);
        byte[] signature = sigSign.sign();

        Signature sigVerify = Signature.getInstance("SHA256withECDSA");
        sigVerify.initVerify(ecdsaPair.getPublic());
        sigVerify.update(messageToSign);
        return sigVerify.verify(signature);
    }

    // ============================================
    // MAIN ENTRY POINT
    // ============================================

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*")
                .result("results/benchmark_result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
