import json
import pandas as pd

# Input/output paths
INPUT_JSON = "benchmark_result.json"   # rename paste.txt to this
OUTPUT_CSV = "benchmark_results.csv"

with open(INPUT_JSON, "r") as f:
    data = json.load(f)

rows = []
for entry in data:
    name = entry["benchmark"].split(".")[-1]
    metric = entry["primaryMetric"]
    score = metric["score"]
    error = metric.get("scoreError", 0.0)
    unit = metric["scoreUnit"]

    lower = name.lower()

    # Family classification
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

    # Operation classification
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
        "Error_ms": error,
        "Unit": unit,
    })

df = pd.DataFrame(rows)

# Sort for readability
df = df.sort_values(["Family", "Operation", "Benchmark"]).reset_index(drop=True)

print(df)
df.to_csv(OUTPUT_CSV, index=False)
print(f"\nSaved CSV to {OUTPUT_CSV}")
