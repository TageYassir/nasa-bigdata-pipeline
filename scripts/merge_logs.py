#!/usr/bin/env python3
"""Merge two Apache-style access logs into a single file.

Usage examples:
  python merge_logs.py
  python merge_logs.py access_log_Jul95 access_log_Aug95 -o access_log_merged --sort

By default the script looks for `access_log_Jul95` and `access_log_Aug95` in the same
directory as this script and writes `access_log_combined`.
"""
import argparse
import os
import re
import sys
import datetime

TS_RE = re.compile(r"\[([0-9]{1,2}/[A-Za-z]{3}/[0-9]{4}:[0-9]{2}:[0-9]{2}:[0-9]{2} [+\-][0-9]{4})\]")


def parse_ts(line):
    m = TS_RE.search(line)
    if not m:
        return None
    s = m.group(1)
    try:
        return datetime.datetime.strptime(s, "%d/%b/%Y:%H:%M:%S %z")
    except Exception:
        return None


def merge_concat(files, out_path):
    with open(out_path, "w", encoding="utf-8", errors="replace") as out:
        for fname in files:
            with open(fname, "r", encoding="utf-8", errors="replace") as fh:
                for line in fh:
                    out.write(line)


def merge_sorted(files, out_path):
    entries = []
    for fname in files:
        with open(fname, "r", encoding="utf-8", errors="replace") as fh:
            for line in fh:
                ts = parse_ts(line)
                if ts is None:
                    key = datetime.datetime.max.replace(tzinfo=datetime.timezone.utc)
                else:
                    key = ts
                entries.append((key, line))
    entries.sort(key=lambda x: x[0])
    with open(out_path, "w", encoding="utf-8") as out:
        for _, line in entries:
            out.write(line)


def main():
    p = argparse.ArgumentParser(description="Merge Apache access logs")
    p.add_argument("inputs", nargs="*", help="Input log files (default: access_log_Jul95, access_log_Aug95)")
    p.add_argument("-o", "--output", default="access_log_combined", help="Output file path")
    p.add_argument("--sort", action="store_true", help="Sort merged output by timestamp")
    args = p.parse_args()

    if not args.inputs or len(args.inputs) == 0:
        base = os.path.dirname(os.path.abspath(__file__))
        inputs = [os.path.join(base, "access_log_Jul95"), os.path.join(base, "access_log_Aug95")]
    else:
        inputs = args.inputs

    for f in inputs:
        if not os.path.exists(f):
            print(f"Input file not found: {f}", file=sys.stderr)
            sys.exit(2)

    if args.sort:
        merge_sorted(inputs, args.output)
    else:
        merge_concat(inputs, args.output)

    print(f"Merged {len(inputs)} files -> {args.output}")


if __name__ == "__main__":
    main()
