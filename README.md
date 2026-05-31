# NASA Big Data Pipeline
## 🤝 Contributors
| Avatar | Contributor |
| :---: | :--- |
| <img src="https://github.com/TageYassir.png" width="40px;"/> | **Yassir Tagemouati** [@yassir](https://github.com/TageYassir) |
| <img src="https://github.com/Ilyass1234-gif.png" width="40px;"/> | **Ilyass Bennani** [@IlyassBennani](https://github.com/Ilyass1234-gif) |

A small big-data project that works with NASA web server logs for analysis and study.

This repository includes:
- A Docker-based environment for **Hadoop HDFS** and **Kafka**
- Python scripts to explore/produce/consume log data
- A Hadoop job orchestration script to run batch analytics and export results
- A project report available as `report.docx`

---

## Project structure

- `docker-compose.yml`  
  Starts the Hadoop HDFS services (NameNode + DataNode).

- `docker-compose.kafka.yml`  
  Starts Kafka (configured in **KRaft mode**, no Zookeeper).

- `scripts/`  
  Python scripts for log exploration, Kafka producer/consumer, merging logs, and visualization.

- `Orchestration/job_orchestration.sh`  
  Runs Hadoop jobs (Top URLs, Hourly Errors, Suspicious IPs) and downloads merged outputs.

- `report.docx`  
  Full project report/documentation.

---

## Prerequisites

- Docker + Docker Compose
- Python 3.x (optional, if you want to run scripts locally)

Python dependency used in this repo:
- `kafka-python` (see `scripts/requirements.txt`)

Install:
```bash
pip install -r scripts/requirements.txt
```

---

## Quick start (Docker)

### 1) Start Hadoop (HDFS)
```bash
docker compose up -d
```

### 2) Start Kafka
```bash
docker compose -f docker-compose.kafka.yml up -d
```

---

## Running the batch jobs (Hadoop orchestration)

The orchestration script runs 3 Hadoop jobs and then exports results to a host folder:

- `TopURLsJob` → `/results/top_urls`
- `HourlyErrorsJob` → `/results/hourly_errors`
- `SuspiciousIPsJob` → `/results/suspicious_ips`

Then it downloads merged outputs into:
- `/host-results/topurls.txt`
- `/host-results/hourly_errors.txt`
- `/host-results/suspicious_ips.txt`

Run:
```bash
bash Orchestration/job_orchestration.sh
```

### Notes
- The script expects a JAR to be available inside the container at:
  `/jars/nasa-log-mr-1.0-SNAPSHOT.jar`
- It reads input logs from HDFS using a pattern like:
  `/nasa-logs/year=*/month=*/day=*/log.txt`

---

## Python scripts overview (`scripts/`)

Common scripts included:
- `explore_nasa_logs.py` — basic exploration/inspection
- `merge_logs.py` — merges/prepares logs
- `kafka_producer.py` — publishes log events to Kafka
- `kafka_consumer_hdfs.py` — consumes from Kafka and writes to HDFS
- `Data_Visualisation.py` — visualizations of results
- `gen_cluster_id.py` — helper utility (Kafka KRaft cluster id)

---

## Report

The full report is available here:
- `report.docx`
