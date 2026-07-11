# PQC Benchmark Results (Java, JDK 26)

All times in microseconds per operation (µs/op); lower is better. `BC` = Bouncy Castle 1.84, `JDK` = built-in providers (SunJCE ML-KEM, SUN ML-DSA).


## Bouncy Castle vs. JDK native (speedup = BC / JDK)

| Primitive | Operation | BC (µs) | JDK (µs) | JDK speedup |
|---|---|--:|--:|--:|
| ML-KEM-512 | KeyGen | 21.2 | 10.2 | 2.08× |
| ML-KEM-512 | Encaps | 24.8 | 9.2 | 2.70× |
| ML-KEM-512 | Decaps | 26.6 | 12.2 | 2.18× |
| ML-KEM-768 | KeyGen | 33.8 | 15.4 | 2.20× |
| ML-KEM-768 | Encaps | 31.8 | 13.7 | 2.32× |
| ML-KEM-768 | Decaps | 41.1 | 18.4 | 2.24× |
| ML-KEM-1024 | KeyGen | 55.0 | 25.2 | 2.18× |
| ML-KEM-1024 | Encaps | 48.9 | 19.5 | 2.50× |
| ML-KEM-1024 | Decaps | 59.4 | 26.1 | 2.27× |
| ML-DSA-44 | KeyGen | 55.3 | 38.8 | 1.42× |
| ML-DSA-44 | Sign | 247.1 | 117.0 | 2.11× |
| ML-DSA-44 | Verify | 61.8 | 30.6 | 2.02× |
| ML-DSA-65 | KeyGen | 114.2 | 82.7 | 1.38× |
| ML-DSA-65 | Sign | 430.5 | 173.6 | 2.48× |
| ML-DSA-65 | Verify | 95.7 | 50.0 | 1.91× |
| ML-DSA-87 | KeyGen | 147.3 | 95.9 | 1.54× |
| ML-DSA-87 | Sign | 552.3 | 240.5 | 2.30× |
| ML-DSA-87 | Verify | 173.7 | 84.4 | 2.06× |

## Classical baselines (JDK default providers)

| Primitive | Operation | µs/op |
|---|---|--:|
| RSA-2048 | KeyGen | 85298.7 |
| RSA-2048 | Sign | 905.2 |
| RSA-2048 | Verify | 38.0 |
| ECDSA-P256 | KeyGen | 88.0 |
| ECDSA-P256 | Sign | 110.4 |
| ECDSA-P256 | Verify | 350.2 |
| X25519 | KeyGen | 103.7 |
| X25519 | KeyAgree | 104.1 |

## PQC vs. classical highlights (JDK provider)

- Key establishment (keygen+encaps/agree+decaps): ML-KEM-768 = 47.4 µs vs X25519 = 207.8 µs → **ML-KEM-768 is 4.38× faster**
- RSA-2048 sign vs ML-DSA-65 sign: **5.21×**  (905.2 vs 173.6 µs)
- ECDSA-P256 verify vs ML-DSA-65 verify: **7.01×**  (350.2 vs 50.0 µs)
- ECDSA-P256 sign vs ML-DSA-65 sign: **1.57×**  (110.4 vs 173.6 µs)
