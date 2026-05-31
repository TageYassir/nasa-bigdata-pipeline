#!/usr/bin/env bash
set -euo pipefail

# JAR is now mounted from the host
JAR="/jars/nasa-log-mr-1.0-SNAPSHOT.jar"

# Input pattern that avoids "Path is not a file"
HDFS_INPUT="/nasa-logs/year=*/month=*/day=*/log.txt"
# For only July 1995:
# HDFS_INPUT="/nasa-logs/year=1995/month=07/day=*/log.txt"

OUT_TOPURLS="/results/top_urls"
OUT_HOURLY="/results/hourly_errors"
OUT_SUSPECT="/results/suspicious_ips"

# Mounted folder on your PC
HOST_RESULTS_DIR="/host-results"

# sanity checks
test -f "$JAR" || { echo "Jar not found at $JAR (check docker volume ./target:/jars)"; exit 1; }
mkdir -p "$HOST_RESULTS_DIR"

echo "[1/3] TopURLsJob..."
hdfs dfs -rm -r -f "$OUT_TOPURLS"
hadoop jar "$JAR" nasa.TopURLsJob "$HDFS_INPUT" "$OUT_TOPURLS"

echo "[2/3] HourlyErrorsJob..."
hdfs dfs -rm -r -f "$OUT_HOURLY"
hadoop jar "$JAR" nasa.HourlyErrorsJob "$HDFS_INPUT" "$OUT_HOURLY"

echo "[3/3] SuspiciousIPsJob..."
hdfs dfs -rm -r -f "$OUT_SUSPECT"
hadoop jar "$JAR" nasa.SuspiciousIPsJob "$HDFS_INPUT" "$OUT_SUSPECT"

echo "[download] Writing merged outputs to: ${HOST_RESULTS_DIR}"
hdfs dfs -getmerge "$OUT_TOPURLS"  "${HOST_RESULTS_DIR}/topurls.txt"
hdfs dfs -getmerge "$OUT_HOURLY"   "${HOST_RESULTS_DIR}/hourly_errors.txt"
hdfs dfs -getmerge "$OUT_SUSPECT"  "${HOST_RESULTS_DIR}/suspicious_ips.txt"

echo "[done] Host results:"
ls -lah "$HOST_RESULTS_DIR"