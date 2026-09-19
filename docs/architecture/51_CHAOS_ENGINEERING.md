# 51 — CHAOS ENGINEERING & RESILIENCE FAULT INJECTION

> **Document ID:** `BS-ARCH-51-CHAOS-ENG`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Principles of Chaos Engineering & LitmusChaos Fault Injection (§46 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Principles of Chaos & The Steady-State Invariant

To ensure that rural connectivity failures and hardware crashes do not cause silent data corruption, BhashaSetu AI subjects its architecture to automated chaos experiments:

$$\text{Steady State Hypothesis: } \text{Data Duplication} = 0 \land \text{Classroom Availability} = 100\%$$

---

## 2. Master Chaos Experiments Matrix

| Experiment ID | Injected Fault Scenario | Target Component | Blast Radius | Steady State Assertion | Recovery Time |
|---|---|---|---|---|---|
| **CHAOS-01** | **Network Cut Mid-Sync**: Cellular link severed after uploading 50% of an outbox batch. | Android SyncWorker & Backend Gateway | 1 Mobile Tablet | Batch rolls back; tablet retries when reconnected; **zero duplicate rows** in DB. | $< 10\text{ seconds}$ |
| **CHAOS-02** | **AI Engine Pod Kill**: `SIGKILL` issued to FastAPI AI platform during active lesson generation. | `ai-platform` Kubernetes Pod | AI Scaffolding | NestJS circuit breaker trips to static JCERT glossary; pod restarts via K8s in $< 8\text{s}$. | $< 8\text{ seconds}$ |
| **CHAOS-03** | **Redis Flush & Crash**: Redis task queue terminated during offline package zip generation. | `bhashasetu-redis` | Background Jobs | BullMQ restores unacknowledged jobs from AOF log; task re-executes cleanly. | $< 15\text{ seconds}$ |
| **CHAOS-04** | **Postgres Primary Failover**: Primary database node hard terminated. | PostgreSQL 18 HA StatefulSet | Entire Cloud Core | Standby replica promoted in $< 30\text{s}$; tablets queue mutations in local SQLite. | $< 30\text{ seconds}$ |
| **CHAOS-05** | **Android Low Memory Injection**: Trigger `am send-trim-memory` on 2 GB tablet. | Android JVM Runtime | Single Tablet UI | Compose viewmodels release cached bitmaps; background outbox preserves state. | Immediate |

---

## 3. Chaos Execution Automation Script (`chaos_network_cut.sh`)

```bash
#!/bin/bash
# Simulates flaky 2G connection with 40% packet loss and 1200ms latency on tablet interface
echo "Injecting 40% packet loss and 1200ms jitter onto cellular link..."
tc qdisc add dev eth0 root netem delay 1200ms 400ms loss 40%

# Trigger sync push
curl -X POST http://localhost:3001/api/v1/sync/push -d @test_batch.json

# Assert zero duplicate rows in database
DUPLICATES=$(docker exec bhashasetu-postgres psql -U bhashasetu_user -d bhashasetu_db -t -c \
  "SELECT count(*) FROM (SELECT operation_id, count(*) FROM outbox_operations GROUP BY operation_id HAVING count(*) > 1) s;")

if [ "$DUPLICATES" -eq 0 ]; then
  echo "CHAOS TEST PASSED: Zero data duplication under severe packet loss."
else
  echo "CHAOS TEST FAILED: Duplicate rows detected!"
  exit 1
fi
```
