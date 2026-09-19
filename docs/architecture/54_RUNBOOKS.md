# 54 — PRODUCTION OPERATIONAL RUNBOOKS REGISTER (RUNBOOK 01–20)

> **Document ID:** `BS-ARCH-54-RUNBOOKS`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Production Operations Runbook Register (§70 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Top 10 Critical Production Runbooks

### RUNBOOK-01: AI Platform Microservice High Latency / Pod Crash
* **Symptoms**: Prometheus alerts `bhashasetu_voice_relay_latency_ms > 3000ms` or pod status `CrashLoopBackOff`.
* **Diagnostic Steps**:
  ```bash
  kubectl get pods -n bhashasetu-prod -l app=ai-platform
  kubectl logs -n bhashasetu-prod -l app=ai-platform --tail=100
  ```
* **Remediation Steps**:
  1. If memory limit breached: Increase pod limit to `2560Mi` via `patch-resources.yaml`.
  2. Clear in-memory query cache via management endpoint:
     ```bash
     curl -X POST http://localhost:8000/api/v1/rag/cache-clear
     ```
  3. If persistent, trip circuit breaker to route requests to static JCERT glossaries.

---

### RUNBOOK-04: Android Tablet Outbox Sync Deadlock
* **Symptoms**: Tablet sync icon spins indefinitely; zero rows acknowledged in `sync_logs`.
* **Diagnostic Steps**: Check if local sync lock is stuck in `AppDatabase.kt`.
* **Remediation Steps**:
  1. On tablet: Navigate to **Settings -> Advanced Diagnostics -> Release Sync Lock**.
  2. The app issues an SQLite query clearing stale lock timestamps:
     ```sql
     UPDATE sync_logs SET status='IDLE' WHERE status='IN_FLIGHT';
     ```
  3. Force immediate background worker retry.

---

### RUNBOOK-09: Web Studio Host Port 3000 Collision on Windows
* **Symptoms**: `npm run dev` fails in `apps/web-frontend/` with error `EADDRINUSE: address already in use :::3000`.
* **Root Cause**: `127.0.0.1:3000` is bound by the Antigravity IDE Windows process.
* **Remediation Steps**:
  ```powershell
  # Verify port binding
  netstat -ano | findstr :3000
  # Launch frontend strictly on Port 3002
  npm run dev -- -p 3002
  ```

---

### RUNBOOK-10: Android Build Native Memory Crash (`malloc failed / arena.cpp:168`)
* **Symptoms**: `./gradlew assembleDebug` terminates abruptly on Windows developer workstation.
* **Root Cause**: Gradle daemon spawning unconstrained worker threads, exhausting Windows virtual memory.
* **Remediation Steps**:
  1. Kill all running Gradle daemons:
     ```powershell
     ./gradlew --stop
     ```
  2. Verify `gradle.properties` enforces memory boundaries:
     ```properties
     org.gradle.jvmargs=-Xmx2048m -XX:+UseG1GC -XX:MaxMetaspaceSize=512m
     org.gradle.workers.max=2
     ```
  3. Rebuild cleanly: `./gradlew assembleDebug`.

---

### RUNBOOK-12: Primary PostgreSQL Failover to Standby Replica
* **Symptoms**: PostgreSQL primary connection timeout; HTTP 500 on all write endpoints.
* **Remediation Steps**:
  1. Verify standby replica health in DR zone (`asia-south2`).
  2. Execute failover promotion command:
     ```bash
     kubectl exec -it postgres-standby-0 -n bhashasetu-prod -- psql -c "SELECT pg_promote();"
     ```
  3. Update Kubernetes Service `postgres-service` selector to target `postgres-standby-0`.
  4. Verify application pods reconnect automatically via connection pool retry.

---

### RUNBOOK-16: Dead Letter Queue (DLQ) Drain & Replay
* **Symptoms**: Redis key `bhashasetu:dlq` length $> 0$.
* **Diagnostic Steps**: Inspect failed event payloads:
  ```bash
  docker exec -it bhashasetu-redis redis-cli lrange bhashasetu:dlq 0 5
  ```
* **Remediation Steps**:
  1. Fix downstream consumer bug or database schema error.
  2. Trigger DLQ replay script:
     ```bash
     node services/web-backend/dist/scripts/drain-dlq.js
     ```

---

## 2. Master Runbook Index (01–20)

| Runbook ID | Operational Scenario | Primary Subsystem | Action Type |
|---|---|---|---|
| **RUNBOOK-01** | AI Inference Pod Crash & Latency Spike | AI Platform | Automated Failover / Restart |
| **RUNBOOK-02** | PostgreSQL Connection Pool Starvation | Database Core | PgBouncer Pool Recycle |
| **RUNBOOK-03** | Redis Task Queue Backlog & Memory OOM | Task Broker | Scale Workers / LRU Evict |
| **RUNBOOK-04** | Android Tablet Outbox Sync Deadlock | Mobile Edge | In-App Lock Release |
| **RUNBOOK-05** | Corrupted Local SQLite DB on Tablet | Mobile Edge | Vacuum & Clean Seed Restore |
| **RUNBOOK-06** | SSL / TLS Certificate Emergency Expiration | Ingress NGINX | Cert-Manager Force Renew |
| **RUNBOOK-07** | Influx of 429 Too Many Requests | API Gateway | Adjust Token Bucket Quota |
| **RUNBOOK-08** | RAG Vector Index Invalidation & Rebuild | Search Engine | Rebuild DiskANN Graph |
| **RUNBOOK-09** | Web Studio Port 3000 Conflict (Windows) | Web Frontend | Rebind to Port 3002 |
| **RUNBOOK-10** | Android Build JVM Native Memory Exhaustion| Mobile Build | Cap Gradle Workers to 2 |
| **RUNBOOK-11** | High COMET Translation Quality Failure | Translation | Force Native Linguist Queue |
| **RUNBOOK-12** | Primary Database Failover to Replica | High Availability | Execute `pg_promote()` |
| **RUNBOOK-13** | Disk Storage Full on Cloud Volume | Kubernetes PVC | Dynamic Volume Expansion |
| **RUNBOOK-14** | Severe 2G Cellular Packet Loss | Edge Sync | Enable Heavy Gzip Chunking |
| **RUNBOOK-15** | Rollback of Corrupted Curriculum Bundle | Content Delivery | Revoke SHA-256 Signature |
| **RUNBOOK-16** | Dead Letter Queue (DLQ) Drain & Replay | BullMQ Queue | Fix Consumer & Re-queue |
| **RUNBOOK-17** | Teacher Password Reset & Session Revoke | Auth Security | Terminate Redis Session Tokens |
| **RUNBOOK-18** | New School Tenant Onboarding | Administration | Provision School UUID & RLS |
| **RUNBOOK-19** | Blacklist Stolen / Compromised Tablet | Edge Security | Revoke Device Certificate |
| **RUNBOOK-20** | Offline Package Zip Bundle Recompilation | Content Engine | Trigger BullMQ Re-pack Job |
