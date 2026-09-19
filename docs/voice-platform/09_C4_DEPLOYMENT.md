# VoxBridge AI — C4 Architecture: Level 4 Global Deployment

## 1. Global Cloud & Edge Deployment Topology (Mermaid Diagram 4)

The Deployment diagram illustrates the physical and virtual infrastructure hosting VoxBridge AI across multi-cloud edge locations and primary cloud data centers, emphasizing geographic routing, latency minimization, network security zoning, and state persistence boundaries.

```mermaid
graph TB
    subgraph GlobalEdge["Global Anycast Edge Tier (Cloudflare Enterprise PoPs)"]
        DNS["Anycast DNS & Geo-Steering (Latency-based)"]
        WAF["WAF & DDoS Mitigation (Layer 3/4/7)"]
        EdgeWorkers["Cloudflare Workers (Static Assets & Token Verify)"]
        DNS --> WAF
        WAF --> EdgeWorkers
    end

    subgraph AWS_PrimaryRegion["AWS Region: us-east-1 (Primary Control & Compute Plane)"]
        subgraph PublicSubnet_Primary["Public Subnet (DMZ / NAT Gateways)"]
            ALB_API["AWS Application Load Balancer (REST API)"]
            NLB_Stream["AWS Network Load Balancer (WSS/WebRTC)"]
        end

        subgraph PrivateCompute_Primary["EKS Cluster: voxbridge-prod-useast1"]
            subgraph IngressNodePool["NodePool: System & Gateway (c6i.4xlarge)"]
                Pod_APIGW["API Gateway Pods (x12)"]
                Pod_StreamGW["Streaming Gateway Pods (x24)"]
                Pod_SessionMgr["Session Manager Pods (x6)"]
            end

            subgraph GPUNodePool["NodePool: AI Inference (g5.4xlarge - NVIDIA A10G)"]
                Pod_STT["Conformer/Whisper STT Workers (x16)"]
                Pod_Trans["NLLB-200 / vLLM Translation (x8)"]
                Pod_TTS["Kokoro/Piper TTS Workers (x16)"]
            end

            subgraph WorkerNodePool["NodePool: Batch Processing (m6i.2xlarge)"]
                Pod_JobProc["Async Job Processors (x12)"]
                Pod_Metering["Usage & Quota Workers (x4)"]
            end
        end

        subgraph PrivateData_Primary["Database & Messaging Subnet (Multi-AZ Encrypted)"]
            RDS_PG_Primary[("Aurora PostgreSQL 18 (Writer)")]
            RDS_PG_Replica[("Aurora PostgreSQL 18 (Reader)")]
            Redis_Cluster[("Redis 7.4 Cluster (6 Shards, 12 Nodes)")]
            NATS_Cluster["NATS JetStream (3-Node Raft Cluster)"]
            S3_Bucket[("AWS S3 Multi-Tier Audio Bucket (SSE-KMS)")]
        end
    end

    subgraph AWS_SecondaryRegion["AWS Region: eu-central-1 (Data Residency & Failover Plane)"]
        subgraph PrivateCompute_Secondary["EKS Cluster: voxbridge-prod-eucentral1"]
            Pod_Secondary_StreamGW["Streaming Gateway Pods (x12)"]
            Pod_Secondary_AI["Inference Workers (NVIDIA A10G) (x8)"]
        end
        subgraph PrivateData_Secondary["Regional Data Subnet"]
            RDS_PG_EU[("Aurora PostgreSQL Read Replica (Cross-Region)")]
            S3_Bucket_EU[("S3 Bucket (EU Data Residency Zone)")]
        end
    end

    %% Edge to Regional Routing
    EdgeWorkers -->|Route NA Traffic (Low Latency)| NLB_Stream
    EdgeWorkers -->|Route NA REST| ALB_API
    EdgeWorkers -->|Route EU Traffic (Data Residency)| Pod_Secondary_StreamGW

    %% Primary VPC Routing
    ALB_API --> Pod_APIGW
    NLB_Stream --> Pod_StreamGW
    Pod_APIGW --> Pod_SessionMgr
    Pod_StreamGW --> Pod_SessionMgr
    Pod_StreamGW --> Pod_STT
    Pod_STT --> Pod_Trans
    Pod_Trans --> Pod_TTS

    %% Data Plane Connections
    Pod_APIGW --> RDS_PG_Primary
    Pod_APIGW --> Redis_Cluster
    Pod_StreamGW --> Redis_Cluster
    Pod_Metering --> NATS_Cluster
    Pod_Metering --> RDS_PG_Primary
    Pod_JobProc --> S3_Bucket
    Pod_JobProc --> NATS_Cluster

    %% Replication
    RDS_PG_Primary -.->|Asynchronous Logical Replication| RDS_PG_EU
    RDS_PG_Primary -.->|Synchronous Multi-AZ Replication| RDS_PG_Replica
    S3_Bucket -.->|Cross-Region Replication (Filtered)| S3_Bucket_EU
```

---

## 2. Global Network Zoning & Security Topologies

### 2.1 Zone Classifications

```
[INTERNET] 
   │
   ▼ (Anycast GeoDNS)
[ZONE 0: CLOUDFLARE EDGE] (DDoS, Bot Shield, WAF, Edge Rate-Limits)
   │
   ▼ (mTLS Authenticated Origin Pulls)
[ZONE 1: PUBLIC DMZ] (AWS ALB / NLB, Elastic IPs, NAT Gateways)
   │
   ▼ (Private Kubernetes VPC CNI / Strict Security Groups)
[ZONE 2: APPLICATION & INGESTION TIER] (API Gateway, Streaming Gateway, Session Manager)
   │
   ▼ (Internal Cluster IP / SPIRE mTLS Workload Identity)
[ZONE 3: INFERENCE WORKER TIER] (Isolated GPU NodePools, Non-Routable Subnet)
   │
   ▼ (VPC Peering / Private Endpoints / AWS KMS Envelope Encryption)
[ZONE 4: PERSISTENCE & CONTROL TIER] (PostgreSQL Aurora, Redis Cluster, NATS JetStream, S3)
```

### 2.2 Network Routing & Cross-Zone Safeguards
1. **Direct Workload Ingress Prevention:** Pods in Zone 3 (GPU Inference) have zero internet routing capabilities. They cannot initiate outbound internet traffic (egress blocked at security group level), preventing potential model weights or transcript exfiltration even in the event of an arbitrary code execution vulnerability in inference runtimes.
2. **NLB to Streaming Gateway Affinity:** Real-time WebRTC and WebSocket streams communicate through AWS Network Load Balancers (NLB) operating in TCP/UDP cross-zone mode with Target Group Sticky Sessions enabled via client IP hashes. This avoids unnecessary cross-AZ traffic and eliminates hop jitter.

---

## 3. Data Residency and Cross-Region Disaster Recovery Strategy

| Region | Primary Functionality | RPO (Recovery Point Objective) | RTO (Recovery Time Objective) | Data Classification Hosted |
|---|---|---|---|---|
| **us-east-1 (N. Virginia)** | Global Primary Control Plane, Billing Master, US Audio Processing | Zero (Active Master) | Zero (Active Master) | Global identity, US-domiciled client media, billing master |
| **eu-central-1 (Frankfurt)** | EU Data Residency Zone, Live Inference, Read Failover Replica | $< 500\text{ms}$ (PostgreSQL cross-region streaming replication) | $< 12 \text{ minutes}$ (Automated Aurora Regional Failover) | EU-domiciled client media, local GDPR-restricted audio buffers |

### 3.1 Live Session Failover Invariant
- **No Mid-Session Live WebSocket Migration:** In the event of an entire data-center failure, active real-time streaming sessions are terminated gracefully with an RFC 6455 `1012 Service Restart` code, prompting the client SDK to immediately reconnect to the nearest healthy regional edge. The client SDK resumes from the last acknowledged audio sequence number (`last_sequence_id`).
- **Idempotent Checkpointing:** The Session Manager persistently checkpoints the streaming state every 1,000ms to Redis Cluster. Reconnecting clients re-establish pipeline synchronization without losing translation context.
