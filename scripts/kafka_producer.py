import sys
import time
import argparse
from kafka import KafkaProducer

parser = argparse.ArgumentParser(description="NASA Log Kafka Producer")
parser.add_argument('--input', type=str, required=True, help='Log file path')
parser.add_argument('--topic', type=str, default='nasa-logs', help='Kafka topic')
parser.add_argument('--rate', type=int, default=1000, help='Lines/sec to stream')
parser.add_argument('--broker', type=str, default='localhost:9092', help='Kafka broker address')
args = parser.parse_args()

producer = KafkaProducer(bootstrap_servers=args.broker, linger_ms=10)

with open(args.input, encoding='utf-8', errors='ignore') as f:
    count = 0
    for line in f:
        producer.send(args.topic, line.strip().encode('utf-8'))
        count += 1
        if count % args.rate == 0:
            time.sleep(1)
        if count % 50000 == 0:
            print(f"Sent {count} lines")

print("Finished streaming.")
producer.flush()