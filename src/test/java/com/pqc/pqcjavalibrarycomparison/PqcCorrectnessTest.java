package com.pqc.pqcjavalibrarycomparison;

import org.junit.jupiter.api.Test;

import javax.crypto.KEM;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Correctness sanity checks for the benchmarked operations.
 *
 * <p>These confirm that both the Bouncy Castle ("BC") and JDK built-in
 * implementations of ML-KEM and ML-DSA actually round-trip correctly, so the
 * timing benchmarks are measuring valid cryptographic operations rather than
 * silent no-ops. The {@link PqcConfiguration} static initialiser registers BC.
 */
class PqcCorrectnessTest {

    static {
        PqcConfiguration.initialize();
    }

    @Test
    void mlKemRoundTripsForBothProviders() throws Exception {
        for (String[] p : new String[][]{{"BC", "BC"}, {"SunJCE", "SunJCE"}}) {
            for (String level : new String[]{"512", "768", "1024"}) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM-" + level, p[0]);
                KeyPair kp = kpg.generateKeyPair();

                KEM kem = KEM.getInstance("ML-KEM", p[1]);
                KEM.Encapsulated enc = kem.newEncapsulator(kp.getPublic()).encapsulate();
                SecretKey decapsulated = kem.newDecapsulator(kp.getPrivate())
                        .decapsulate(enc.encapsulation());

                assertArrayEquals(enc.key().getEncoded(), decapsulated.getEncoded(),
                        "ML-KEM-" + level + " shared secret mismatch for provider " + p[0]);
            }
        }
    }

    @Test
    void mlDsaRoundTripsForBothProviders() throws Exception {
        byte[] message = "correctness".getBytes();
        for (String provider : new String[]{"BC", "SUN"}) {
            for (String level : new String[]{"44", "65", "87"}) {
                String alg = "ML-DSA-" + level;
                KeyPair kp = KeyPairGenerator.getInstance(alg, provider).generateKeyPair();

                Signature signer = Signature.getInstance(alg, provider);
                signer.initSign(kp.getPrivate());
                signer.update(message);
                byte[] sig = signer.sign();

                Signature verifier = Signature.getInstance(alg, provider);
                verifier.initVerify(kp.getPublic());
                verifier.update(message);
                assertTrue(verifier.verify(sig),
                        alg + " verification failed for provider " + provider);

                // Tampered message must NOT verify.
                byte[] tampered = Arrays.copyOf(message, message.length);
                tampered[0] ^= 0x01;
                verifier.initVerify(kp.getPublic());
                verifier.update(tampered);
                assertTrue(!verifier.verify(sig),
                        alg + " verified a tampered message for provider " + provider);
            }
        }
    }
}
