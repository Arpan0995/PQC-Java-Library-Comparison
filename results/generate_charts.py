import json
import pandas as pd
import matplotlib.pyplot as plt

INPUT_JSON = "benchmark_result.json"   # rename paste.txt if needed

with open(INPUT_JSON, "r") as f:
    data = json.load(f)

rows = []
for entry in data:
    name = entry["benchmark"].split(".")[-1]
    metric = entry["primaryMetric"]
    score = metric["score"]
    unit = metric["scoreUnit"]

    lower = name.lower()

    if "mlkem" in lower:
        family = "ML-KEM"
    elif "mldsa" in lower:
        family = "ML-DSA"
    elif "rsa" in lower:
        family = "RSA"
    elif "ecdsa" in lower:
        family = "ECDSA"
    else:
        family = "Other"

    if "keygen" in lower:
        op = "KeyGen"
    elif "encaps" in lower:
        op = "Encaps"
    elif "decaps" in lower:
        op = "Decaps"
    elif "sign" in lower:
        op = "Sign"
    elif "verify" in lower:
        op = "Verify"
    else:
        op = "Other"

    rows.append({
        "Benchmark": name,
        "Family": family,
        "Operation": op,
        "Score_ms_per_op": score,
        "Unit": unit,
    })

df = pd.DataFrame(rows)

families = df["Family"].unique()

for fam in families:
    sub = df[df["Family"] == fam].copy()
    if sub.empty:
        continue

    plt.figure(figsize=(10, 5))
    sub = sub.sort_values(["Operation", "Score_ms_per_op"])

    x = sub["Benchmark"]
    y = sub["Score_ms_per_op"]

    plt.bar(x, y)
    plt.xticks(rotation=45, ha="right")
    plt.ylabel("ms/op")
    plt.title(f"{fam} benchmarks (lower is better)")
    plt.tight_layout()

    out_name = f"{fam.replace('-', '').lower()}_benchmarks.png"
    plt.savefig(out_name, dpi=200)
    print(f"Saved {out_name}")

plt.show()
