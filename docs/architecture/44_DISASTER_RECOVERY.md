# 44 — DISASTER RECOVERY (DR) & REGIONAL FAILOVER BLUEPRINT

> **Document ID:** `BS-ARCH-44-DISASTER-RECOVERY`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** ISO 22301 Business Continuity & Cloud Disaster Recovery (§35 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Recovery Objectives: RPO & RTO

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                      DISASTER RECOVERY TARGET METRICS                       │
├────────────────────┬────────────────────┬───────────────────────────────────┤
│ Recovery Metric    │ Target Limit       │ Architectural Mechanism           │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ Recovery Point     │ < 15 Minutes       │ Continuous PostgreSQL WAL shipping│
│ Objective (RPO)    │ (Maximum Data Loss)│ to cross-region Cloud Storage.    │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ Recovery Time      │ < 30 Minutes       │ Automated Terraform & Kustomize   │
│ Objective (RTO)    │ (Downtime Duration)│ spin-up in DR Region (Delhi).     │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ Edge Independence  │ ZERO DOWNTIME      │ 100% of village classroom lessons │
│ Guarantee          │ (Autonomous Edge)  │ continue offline with 0% impact.  │
└────────────────────┴────────────────────┴───────────────────────────────────┘
```

---

## 2. Cross-Region Disaster Recovery Topology

```mermaid
graph TD
    subgraph PrimaryRegion["Primary Region: asia-south1 (Mumbai)"]
        K8sPrimary["GKE Cluster Primary (Active)"]
        DBPrimary[("PostgreSQL 18 HA Primary (Active)")]
        GCSPrimary[("Cloud Storage Primary")]
    end

    subgraph DRRegion["Disaster Recovery Region: asia-south2 (Delhi)"]
        K8sDR["GKE Cluster Warm Standby (Scaled to 1 replica)"]
        DBReplica[("PostgreSQL 18 Asynchronous Read Replica")]
        GCSDR[("Cloud Storage Replicated Bucket")]
    end

    DBPrimary -->|WAL Streaming (Async Replication)| DBReplica
    GCSPrimary -->|Bucket Cross-Region Replication| GCSDR

    TrafficRouter{"Cloudflare / Global Load Balancer"}
    TrafficRouter -->|Normal Operations (100% Traffic)| K8sPrimary
    TrafficRouter -.->|On Mumbai Region Failure Failover| K8sDR
```

---

## 3. Disaster Scenarios & Automated Recovery Procedures

### Scenario 1: Total Primary Region Outage (`asia-south1` Unreachable)
1. **Detection**: Cloudflare Health Probes detect three consecutive 30-second timeouts on the Mumbai ingress load balancer.
2. **Promotion**: Automated Cloud Function promotes Delhi PostgreSQL replica:
   ```sql
   SELECT pg_promote();
   ```
3. **Traffic Shift**: Global DNS automatically points `api.bhashasetu.in` to the Delhi ingress VIP ($< 60\text{ seconds}$ DNS propagation).
4. **HPA Scaling**: Delhi GKE cluster autoscales backend pods from warm standby ($1\text{ replica}$) to active production load ($5\text{ replicas}$). Total time elapsed: **$\sim 14\text{ minutes}$ ($< 30\text{m}$ RTO target)**.

### Scenario 2: Severe State-Wide Internet Cut in Jharkhand
- **Impact on Classrooms**: **ZERO**. Tablets operate completely unaffected in offline village schools. All teacher voice relays, lesson presentations, and student quizzes execute directly from on-device SQLite and local audio drivers. Outbox operations queue locally until network links are restored days or weeks later.
