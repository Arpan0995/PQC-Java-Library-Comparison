# PQC Benchmark Results (Java, JDK 26)

All times in microseconds per operation (µs/op); lower is better. `BC` = Bouncy Castle 1.79, `JDK` = built-in providers (SunJCE ML-KEM, SUN ML-DSA).


## Bouncy Castle vs. JDK native (speedup = BC / JDK)

| Primitive | Operation | BC (µs) | JDK (µs) | JDK speedup |
|---|---|--:|--:|--:|
| ML-KEM-512 | KeyGen | 19.8 | 9.8 | 2.01× |
| ML-KEM-512 | Encaps | 21.0 | 8.8 | 2.39× |
| ML-KEM-512 | Decaps | 26.9 | 11.4 | 2.36× |
| ML-KEM-768 | KeyGen | 31.9 | 16.5 | 1.93× |
| ML-KEM-768 | Encaps | 33.5 | 13.5 | 2.48× |
| ML-KEM-768 | Decaps | 41.5 | 17.5 | 2.38× |
| ML-KEM-1024 | KeyGen | 67.8 | 26.1 | 2.60× |
| ML-KEM-1024 | Encaps | 49.6 | 19.7 | 2.51× |
| ML-KEM-1024 | Decaps | 60.1 | 25.3 | 2.38× |
| ML-DSA-44 | KeyGen | 55.7 | 36.5 | 1.53× |
| ML-DSA-44 | Sign | 201.8 | 104.9 | 1.92× |
| ML-DSA-44 | Verify | 61.6 | 30.5 | 2.02× |
| ML-DSA-65 | KeyGen | 102.8 | 74.9 | 1.37× |
| ML-DSA-65 | Sign | 471.2 | 168.6 | 2.79× |
| ML-DSA-65 | Verify | 96.9 | 49.9 | 1.94× |
| ML-DSA-87 | KeyGen | 146.0 | 94.0 | 1.55× |
| ML-DSA-87 | Sign | 958.8 | 208.0 | 4.61× |
| ML-DSA-87 | Verify | 157.9 | 81.6 | 1.94× |

## Classical baselines (JDK default providers)

| Primitive | Operation | µs/op |
|---|---|--:|
| RSA-2048 | KeyGen | 91156.2 |
| RSA-2048 | Sign | 917.7 |
| RSA-2048 | Verify | 37.9 |
| ECDSA-P256 | KeyGen | 87.8 |
| ECDSA-P256 | Sign | 111.3 |
| ECDSA-P256 | Verify | 349.2 |
| X25519 | KeyGen | 105.9 |
| X25519 | KeyAgree | 104.0 |

## PQC vs. classical highlights (JDK provider)

- Key establishment (keygen+encaps/agree+decaps): ML-KEM-768 = 47.5 µs vs X25519 = 209.9 µs → **ML-KEM-768 is 4.42× faster**
- RSA-2048 sign vs ML-DSA-65 sign: **5.44×**  (917.7 vs 168.6 µs)
- ECDSA-P256 verify vs ML-DSA-65 verify: **7.00×**  (349.2 vs 49.9 µs)
- ECDSA-P256 sign vs ML-DSA-65 sign: **1.52×**  (111.3 vs 168.6 µs)
