# VoxBridge AI — Disaster Recovery (DR) & Multi-Region Failover

## 1. Disaster Recovery Topology (Mermaid Diagram 23)

The Disaster Recovery Architecture guarantees business continuity in the catastrophic event of an entire AWS cloud region outage (e.g., loss of AWS us-east-1).

```mermaid
graph TB
    subgraph GlobalEdgeTier["Global Edge & DNS Steering (Cloudflare Enterprise)"]
        GeoDNS["Anycast GeoDNS / Cloudflare Load Balancer"]
        EdgeProbe["Health Check Probes (HTTP / WSS Handshake every 5s)"]
        GeoDNS --> EdgeProbe
    end

    subgraph Region_Primary["Primary Region: AWS us-east-1 (FAILURE SIMULATED)"]
        Primary_ALB["AWS ALB / NLB (UNREACHABLE)"]
        Primary_EKS["EKS Production Cluster (DOWN)"]
        Primary_Aurora[("Aurora PostgreSQL 18 (Master Writer - CRASHED)")]
        Primary_S3[("S3 Primary Audio Bucket")]
        
        Primary_ALB -.-> Primary_EKS
        Primary_EKS -.-> Primary_Aurora
    end

    subgraph ReplicationPlane["Continuous Background Replication"]
        AsyncWAL["Aurora Global Database WAL Replication (Lag < 500ms)"]
        S3Replication["S3 Cross-Region Replication (CRR)"]
        Primary_Aurora -.-> AsyncWAL
        Primary_S3 -.-> S3Replication
    end

    subgraph Region_Secondary["Secondary DR Region: AWS eu-central-1 (PROMOTED)"]
        Secondary_ALB["AWS ALB / NLB (Active Standby)"]
        Secondary_EKS["EKS Standby Cluster (Scaled up via Karpenter)"]
        Secondary_Aurora[("Aurora Read Replica (PROMOTED TO MASTER WRITER)")]
        Secondary_S3[("S3 Standby Audio Bucket")]

        Secondary_ALB --> Secondary_EKS
        Secondary_EKS --> Secondary_Aurora
        Secondary_EKS --> Secondary_S3
    end

    %% Failover Flow
    EdgeProbe -->|1. Detect Primary Outage (3 consecutive failed probes)| GeoDNS
    GeoDNS -->|2. Shift 100% Global DNS to Secondary ALB| Secondary_ALB
    AsyncWAL -->|3. Promote to Independent Writer (RTO < 8m)| Secondary_Aurora
    S3Replication --> Secondary_S3

    %% Client Reconnect
    ClientApp["Client Mobile/Web Apps"] -->|4. Reconnect to New IP via Session Token| Secondary_ALB
```

---

## 2. Recovery Objectives (RPO & RTO)

| Metric | Target SLA | Physical Mechanism | Measured Test Benchmark |
|---|---|---|---|
| **RPO (Recovery Point Objective)** | $\mathbf{\le 1.0 \text{ Second}}$ | Aurora Global Database asynchronous storage-level WAL streaming. | $\approx 320\text{ms}$ replication lag under peak write load. |
| **RTO (Recovery Time Objective)** | $\mathbf{\le 15 \text{ Minutes}}$ | Automated Cloudflare DNS shift + Aurora regional cluster promotion. | $\mathbf{7 \text{ minutes } 45 \text{ seconds}}$ full failover in bi-annual DR drill. |

---

## 3. Automated Failover Orchestration Runbook

In the event of a total regional blackout:

1. **Step 1: Automated Health Check Trip (T + 15 seconds):**
   Cloudflare origin monitors in 3 separate global geographies fail 3 consecutive synthetic `/v1/health` checks (status 504 or connection refused).
2. **Step 2: Edge Traffic Rerouting (T + 30 seconds):**
   Cloudflare Anycast routing updates origin pools, shifting incoming HTTPS and WSS traffic from `us-east-1` to `eu-central-1`.
3. **Step 3: Database Promotion (T + 2 minutes):**
   AWS Lambda DR controller executes `aws rds failover-global-cluster --global-cluster-identifier voxbridge-global --target-db-cluster-identifier voxbridge-aurora-eu`. The EU read replica is detached from replication and promoted to an independent read/write master.
4. **Step 4: Kubernetes Node Pool Burst (T + 4 minutes):**
   Karpenter detects incoming pod pending counts on the secondary cluster and provisions 60 GPU instances (`g5.4xlarge`) to absorb redirected global inference traffic.
5. **Step 5: Client Session Resumption (T + 5 minutes):**
   Mobile and web client SDKs reconnect to the new regional endpoints using their active session tokens and resume audio streaming from the last acknowledged sequence number.
