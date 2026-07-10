#!/usr/bin/env python3
"""
Render comparison charts from the JMH JSON results.

Requires matplotlib. Run analyze_results.py first for the CSV/Markdown tables.

Reads:  results/benchmark_result.json
Writes: results/figures/*.png
"""
import json
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

HERE = os.path.dirname(os.path.abspath(__file__))
INPUT_JSON = os.path.join(HERE, "benchmark_result.json")
FIG_DIR = os.path.join(HERE, "figures")


def classify(method):
    m = method.lower()
    for key, op in [("keygen", "KeyGen"), ("encaps", "Encaps"), ("decaps", "Decaps"),
                    ("keyagreement", "KeyAgree"), ("sign", "Sign"), ("verify", "Verify")]:
        if key in m:
            return op
    return "Other"


def load():
    with open(INPUT_JSON) as f:
        data = json.load(f)
    rows = []
    for e in data:
        cls = e["benchmark"].split(".")[-2]
        method = e["benchmark"].split(".")[-1]
        p = e.get("params", {}) or {}
        rows.append({
            "cls": cls, "op": classify(method),
            "provider": p.get("provider"), "level": p.get("level"),
            "method": method, "score": e["primaryMetric"]["score"],
            "err": e["primaryMetric"].get("scoreError", 0.0),
        })
    return rows


def val(rows, cls, level, op, provider):
    for r in rows:
        if r["cls"] == cls and r["level"] == level and r["op"] == op and r["provider"] == provider:
            return r["score"], r["err"]
    return None, None


def provider_comparison(rows, cls, levels, ops, title, fname):
    """Grouped bars: for each (level, op) show BC vs JDK side by side."""
    labels = [f"{lvl}\n{op}" for lvl in levels for op in ops]
    bc = [val(rows, cls, lvl, op, "BC")[0] or 0 for lvl in levels for op in ops]
    jdk = [val(rows, cls, lvl, op, "JDK")[0] or 0 for lvl in levels for op in ops]
    bc_err = [val(rows, cls, lvl, op, "BC")[1] or 0 for lvl in levels for op in ops]
    jdk_err = [val(rows, cls, lvl, op, "JDK")[1] or 0 for lvl in levels for op in ops]

    x = range(len(labels))
    w = 0.4
    fig, ax = plt.subplots(figsize=(11, 5))
    ax.bar([i - w / 2 for i in x], bc, w, yerr=bc_err, capsize=3, label="Bouncy Castle 1.79")
    ax.bar([i + w / 2 for i in x], jdk, w, yerr=jdk_err, capsize=3, label="JDK 26 native")
    ax.set_xticks(list(x))
    ax.set_xticklabels(labels, fontsize=8)
    ax.set_ylabel("µs / operation (lower is better)")
    ax.set_title(title)
    ax.legend()
    fig.tight_layout()
    out = os.path.join(FIG_DIR, fname)
    fig.savefig(out, dpi=150)
    plt.close(fig)
    print(f"wrote {out}")


def pqc_vs_classical(rows, fname):
    """Signature sign/verify: ML-DSA (JDK) vs RSA-2048 & ECDSA-P256."""
    def cval(method_prefix, op):
        for r in rows:
            if r["cls"] == "ClassicalBenchmark" and r["op"] == op and r["method"].startswith(method_prefix):
                return r["score"]
        return 0

    groups = ["Sign", "Verify"]
    mldsa65 = [val(rows, "DsaBenchmark", "65", op, "JDK")[0] or 0 for op in groups]
    rsa = [cval("rsa", op) for op in groups]
    ecdsa = [cval("ecdsa", op) for op in groups]

    x = range(len(groups))
    w = 0.25
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.bar([i - w for i in x], mldsa65, w, label="ML-DSA-65 (JDK)")
    ax.bar([i for i in x], rsa, w, label="RSA-2048")
    ax.bar([i + w for i in x], ecdsa, w, label="ECDSA-P256")
    ax.set_xticks(list(x))
    ax.set_xticklabels(groups)
    ax.set_ylabel("µs / operation (lower is better)")
    ax.set_title("Signatures: ML-DSA-65 vs classical (sign / verify)")
    ax.legend()
    fig.tight_layout()
    out = os.path.join(FIG_DIR, fname)
    fig.savefig(out, dpi=150)
    plt.close(fig)
    print(f"wrote {out}")


def kem_vs_x25519(rows, fname):
    """Key establishment total: ML-KEM levels (JDK) vs X25519."""
    levels = ["512", "768", "1024"]
    kem_tot = []
    for lvl in levels:
        kg = val(rows, "KemBenchmark", lvl, "KeyGen", "JDK")[0] or 0
        en = val(rows, "KemBenchmark", lvl, "Encaps", "JDK")[0] or 0
        de = val(rows, "KemBenchmark", lvl, "Decaps", "JDK")[0] or 0
        kem_tot.append(kg + en + de)
    x_total = 0
    for r in rows:
        if r["cls"] == "ClassicalBenchmark" and r["method"].startswith("x25519"):
            x_total += r["score"]

    labels = [f"ML-KEM-{l}" for l in levels] + ["X25519"]
    vals = kem_tot + [x_total]
    colors = ["#4C72B0"] * len(levels) + ["#C44E52"]
    fig, ax = plt.subplots(figsize=(7, 5))
    ax.bar(labels, vals, color=colors)
    ax.set_ylabel("µs (keygen + encaps/agree + decaps)")
    ax.set_title("Key establishment cost: ML-KEM (JDK) vs X25519")
    for i, v in enumerate(vals):
        ax.text(i, v, f"{v:.0f}", ha="center", va="bottom", fontsize=9)
    fig.tight_layout()
    out = os.path.join(FIG_DIR, fname)
    fig.savefig(out, dpi=150)
    plt.close(fig)
    print(f"wrote {out}")


def main():
    os.makedirs(FIG_DIR, exist_ok=True)
    rows = load()
    provider_comparison(rows, "KemBenchmark", ["512", "768", "1024"],
                        ["KeyGen", "Encaps", "Decaps"],
                        "ML-KEM: Bouncy Castle vs JDK native", "mlkem_provider.png")
    provider_comparison(rows, "DsaBenchmark", ["44", "65", "87"],
                        ["KeyGen", "Sign", "Verify"],
                        "ML-DSA: Bouncy Castle vs JDK native", "mldsa_provider.png")
    pqc_vs_classical(rows, "signatures_pqc_vs_classical.png")
    kem_vs_x25519(rows, "kem_vs_x25519.png")


if __name__ == "__main__":
    main()
