import os
import re
import argparse
from datetime import datetime
from kafka import KafkaConsumer

KAFKA_BROKER = 'localhost:9092'
TOPIC = 'nasa-logs'

def compute_partition_path(hdfs_base, ts_str):
    dt = datetime.strptime(ts_str, '%d/%b/%Y:%H:%M:%S %z')
    return os.path.join(hdfs_base, f"year={dt.year}", f"month={dt.month:02}", f"day={dt.day:02}", 'log.txt')

def extract_timestamp(log_line):
    m = re.match(r'\S+ \S+ \S+ \[(.*?)\]', log_line)
    if not m:
        return None
    return m.group(1)

def main():
    parser = argparse.ArgumentParser(description='Kafka -> HDFS (local) loader')
    parser.add_argument('--hdfs-base', default=os.environ.get('HDFS_BASE', '/nasa-logs'),
                        help='Base path to write partitioned logs (env HDFS_BASE)')
    parser.add_argument('--broker', default=os.environ.get('KAFKA_BROKER', KAFKA_BROKER),
                        help='Kafka bootstrap broker')
    parser.add_argument('--topic', default=os.environ.get('KAFKA_TOPIC', TOPIC),
                        help='Kafka topic to consume')
    args = parser.parse_args()

    consumer = KafkaConsumer(
        args.topic,
        group_id='hdfs-loader',
        bootstrap_servers=[args.broker],
        auto_offset_reset='earliest',
        enable_auto_commit=True
    )

    buffers = {}

    for msg in consumer:
        line = msg.value.decode('utf-8')
        ts = extract_timestamp(line)
        if not ts:
            continue  # skip malformatted
        try:
            hdfs_path = compute_partition_path(args.hdfs_base, ts)
        except Exception:
            continue
        os.makedirs(os.path.dirname(hdfs_path), exist_ok=True)
        with open(hdfs_path, 'a', encoding='utf-8') as f:
            f.write(line + '\n')
            print("Wrote line to", hdfs_path)

if __name__ == '__main__':
    main()