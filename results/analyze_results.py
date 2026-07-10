#!/usr/bin/env python3
"""
Parse the JMH JSON output into a tidy CSV and a Markdown results digest.

Dependency-free (standard library only) so it always runs. Charts are produced
separately by generate_charts.py (which needs matplotlib).

Reads:  results/benchmark_result.json
Writes: results/summary.csv
        results/RESULTS.md
"""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
INPUT_JSON = os.path.join(HERE, "benchmark_result.json")
SUMMARY_CSV = os.path.join(HERE, "summary.csv")
RESULTS_MD = os.path.join(HERE, "RESULTS.md")


def classify(method):
    m = method.lower()
    if "keygen" in m:
        return "KeyGen"
    if "encaps" in m:
        return "Encaps"
    if "decaps" in m:
        return "Decaps"
    if "keyagreement" in m:
        return "KeyAgree"
    if "sign" in m:
        return "Sign"
    if "verify" in m:
        return "Verify"
    return "Other"


def load_rows():
    with open(INPUT_JSON) as f:
        data = json.load(f)
    rows = []
    for e in data:
        fq = e["benchmark"]
        cls = fq.split(".")[-2]
        method = fq.split(".")[-1]
        pm = e["primaryMetric"]
        params = e.get("params", {}) or {}
        provider = params.get("provider")
        level = params.get("level")

        if cls == "KemBenchmark":
            family, variant = "ML-KEM", f"ML-KEM-{level}"
        elif cls == "DsaBenchmark":
            family, variant = "ML-DSA", f"ML-DSA-{level}"
        else:  # ClassicalBenchmark: provider is implicit JDK default
            provider = provider or "JDK"
            if method.startswith("rsa"):
                family, variant = "RSA", "RSA-2048"
            elif method.startswith("ecdsa"):
                family, variant = "ECDSA", "ECDSA-P256"
            elif method.startswith("x25519"):
                family, variant = "X25519", "X25519"
            else:
                family, variant = "Other", method

        rows.append({
            "family": family,
            "variant": variant,
            "operation": classify(method),
            "provider": provider or "-",
            "score_us": pm["score"],
            "error_us": pm.get("scoreError", float("nan")),
            "unit": pm["scoreUnit"],
            "method": method,
        })
    return rows


def write_csv(rows):
    import csv
    cols = ["family", "variant", "operation", "provider", "score_us", "error_us", "unit"]
    with open(SUMMARY_CSV, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(cols)
        for r in sorted(rows, key=lambda x: (x["family"], x["variant"], x["operation"], x["provider"])):
            w.writerow([r["family"], r["variant"], r["operation"], r["provider"],
                        f"{r['score_us']:.3f}", f"{r['error_us']:.3f}", r["unit"]])
    print(f"wrote {SUMMARY_CSV}")


def get(rows, variant, op, provider):
    for r in rows:
        if r["variant"] == variant and r["operation"] == op and r["provider"] == provider:
            return r["score_us"]
    return None


def write_markdown(rows):
    L = []
    L.append("# PQC Benchmark Results (Java, JDK 26)\n")
    L.append("All times in microseconds per operation (µs/op); lower is better. "
             "`BC` = Bouncy Castle 1.79, `JDK` = built-in providers (SunJCE ML-KEM, SUN ML-DSA).\n")

    L.append("\n## Bouncy Castle vs. JDK native (speedup = BC / JDK)\n")
    L.append("| Primitive | Operation | BC (µs) | JDK (µs) | JDK speedup |")
    L.append("|---|---|--:|--:|--:|")
    pqc = [("ML-KEM-512", "KeyGen"), ("ML-KEM-512", "Encaps"), ("ML-KEM-512", "Decaps"),
           ("ML-KEM-768", "KeyGen"), ("ML-KEM-768", "Encaps"), ("ML-KEM-768", "Decaps"),
           ("ML-KEM-1024", "KeyGen"), ("ML-KEM-1024", "Encaps"), ("ML-KEM-1024", "Decaps"),
           ("ML-DSA-44", "KeyGen"), ("ML-DSA-44", "Sign"), ("ML-DSA-44", "Verify"),
           ("ML-DSA-65", "KeyGen"), ("ML-DSA-65", "Sign"), ("ML-DSA-65", "Verify"),
           ("ML-DSA-87", "KeyGen"), ("ML-DSA-87", "Sign"), ("ML-DSA-87", "Verify")]
    for variant, op in pqc:
        bc = get(rows, variant, op, "BC")
        jdk = get(rows, variant, op, "JDK")
        if bc and jdk:
            L.append(f"| {variant} | {op} | {bc:.1f} | {jdk:.1f} | {bc / jdk:.2f}× |")

    L.append("\n## Classical baselines (JDK default providers)\n")
    L.append("| Primitive | Operation | µs/op |")
    L.append("|---|---|--:|")
    for variant, op in [("RSA-2048", "KeyGen"), ("RSA-2048", "Sign"), ("RSA-2048", "Verify"),
                        ("ECDSA-P256", "KeyGen"), ("ECDSA-P256", "Sign"), ("ECDSA-P256", "Verify"),
                        ("X25519", "KeyGen"), ("X25519", "KeyAgree")]:
        v = get(rows, variant, op, "JDK")
        if v:
            L.append(f"| {variant} | {op} | {v:.1f} |")

    L.append("\n## PQC vs. classical highlights (JDK provider)\n")
    kg = get(rows, "ML-KEM-768", "KeyGen", "JDK")
    en = get(rows, "ML-KEM-768", "Encaps", "JDK")
    de = get(rows, "ML-KEM-768", "Decaps", "JDK")
    x_kg = get(rows, "X25519", "KeyGen", "JDK")
    x_ka = get(rows, "X25519", "KeyAgree", "JDK")
    if None not in (kg, en, de, x_kg, x_ka):
        kem_total, x_total = kg + en + de, x_kg + x_ka
        faster = "faster" if kem_total < x_total else "slower"
        L.append(f"- Key establishment (keygen+encaps/agree+decaps): "
                 f"ML-KEM-768 = {kem_total:.1f} µs vs X25519 = {x_total:.1f} µs "
                 f"→ **ML-KEM-768 is {max(kem_total, x_total) / min(kem_total, x_total):.2f}× {faster}**")

    def cmp(label, a, b):
        if a and b:
            L.append(f"- {label}: **{max(a, b) / min(a, b):.2f}×**  ({a:.1f} vs {b:.1f} µs)")

    cmp("RSA-2048 sign vs ML-DSA-65 sign",
        get(rows, "RSA-2048", "Sign", "JDK"), get(rows, "ML-DSA-65", "Sign", "JDK"))
    cmp("ECDSA-P256 verify vs ML-DSA-65 verify",
        get(rows, "ECDSA-P256", "Verify", "JDK"), get(rows, "ML-DSA-65", "Verify", "JDK"))
    cmp("ECDSA-P256 sign vs ML-DSA-65 sign",
        get(rows, "ECDSA-P256", "Sign", "JDK"), get(rows, "ML-DSA-65", "Sign", "JDK"))

    with open(RESULTS_MD, "w") as f:
        f.write("\n".join(L) + "\n")
    print(f"wrote {RESULTS_MD}")


def main():
    rows = load_rows()
    print(f"parsed {len(rows)} benchmark rows")
    write_csv(rows)
    write_markdown(rows)


if __name__ == "__main__":
    main()
