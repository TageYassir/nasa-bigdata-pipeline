import re
from collections import Counter
import pandas as pd

log_path = 'D:\\nasa-bigdata-pipeline\\access_log_combined'

# Regex for combined NASA log format
log_pattern = re.compile(
    r'(?P<host>\S+) \S+ \S+ \[(?P<ts>[^\]]+)\] "(?P<method>\S+)\s*(?P<url>[^\s]+).*" (?P<status>\d{3}) (?P<size>\S+)'
)

hosts, urls, statuses, parse_errors = [], [], [], 0

with open(log_path, encoding='utf-8', errors='ignore') as f:
    for line in f:
        m = log_pattern.match(line)
        if not m:
            parse_errors += 1
            continue
        hosts.append(m['host'])
        urls.append(m['url'])
        statuses.append(m['status'])

print(f"Unique hosts: {len(set(hosts))}")
print(f"Top 10 URLs: {Counter(urls).most_common(10)}")
print(f"Top status codes: {Counter(statuses).most_common(5)}")
print(f"Parse errors: {parse_errors}")