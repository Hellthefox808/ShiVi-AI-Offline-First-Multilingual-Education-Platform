# VoxBridge AI — Global Cloud Infrastructure Architecture

## 1. Cloud Provider Strategy & Evaluation

| Dimension | AWS (Amazon Web Services) | GCP (Google Cloud Platform) | Cloudflare Edge | VoxBridge Multi-Cloud Decision |
|---|---|---|---|---|
| **GPU Inference Fleet** | G5 (A10G) & G6 (L4) instances with high spot availability | G2 (L4) & A2 (A100) instances | Cloudflare Workers AI (Serverless) | **AWS Primary for Core Compute & High-Density GPU Nodegroups** |
| **Edge Network & Anycast** | AWS CloudFront / Global Accelerator | GCP Cloud CDN | Cloudflare Enterprise (330+ PoPs) | **Cloudflare Edge for DDoS, WAF, and Global GeoDNS Routing** |
| **Relational Database** | Amazon Aurora PostgreSQL 18 Multi-AZ | Cloud SQL / AlloyDB | D1 (SQLite) | **Aurora PostgreSQL Multi-AZ (Primary Writer + Read Replicas)** |
| **Object Storage** | Amazon S3 with Multi-Tier Lifecycle | Google Cloud Storage (GCS) | Cloudflare R2 (Zero Egress Fees) | **S3 for Hot Audio; R2 for Public SDK Assets & Export Media** |

---

## 2. Multi-Region Deployment Topology

VoxBridge deploys an **Active-Active Edge with Primary Regional Core** architecture:

```
                          [GLOBAL ANYCAST DNS (CLOUDFLARE)]
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   ▼                                           ▼
      [Cloudflare PoP: North America]             [Cloudflare PoP: Europe]
                   │                                           │
                   ▼ (Latency Routing)                         ▼ (Data Residency Routing)
       [AWS us-east-1 (N. Virginia)]               [AWS eu-central-1 (Frankfurt)]
       - Primary Control Plane                     - Regional Ingestion Gateway
       - Master Database (Aurora PostgreSQL)       - Isolated GPU NodePools (EU Data)
       - Primary GPU Cluster (A10G Nodes)          - Read-Only Aurora Replica (Async)
       - NATS JetStream 3-Node Raft                - Local EU S3 Media Bucket
```

---

## 3. Hardware Acceleration & Compute Specifications

### 3.1 Kubernetes GPU NodePool (Inference Tier)
- **Instance Type:** `g5.4xlarge` (AWS EC2)
- **GPU Accelerator:** 1x NVIDIA A10G (24 GB VRAM, 2nd Gen RT Cores, 3rd Gen Tensor Cores).
- **vCPU & RAM:** 16 vCPUs (AMD EPYC 7R32), 64 GiB System Memory, 600 GB Local NVMe SSD storage for model weight caching.
- **Cost Optimization via Spot & Karpenter:**
  - 40% of standard batch workers run on AWS Spot Instances (saving ~65% on compute bills).
  - Karpenter automatically provisions and terminates GPU nodes within 90 seconds based on live pod queue backlog.

### 3.2 Kubernetes CPU NodePool (Gateway & Control Plane)
- **Instance Type:** `c6i.4xlarge` (Compute-Optimized Intel Xeon 8375C).
- **Network Bandwidth:** Up to 12.5 Gbps enhanced networking (ENA).
- **Kernel Tuning:** `net.core.somaxconn = 32768`, `net.ipv4.tcp_max_syn_backlog = 16384`, epoll file descriptor limit set to `1,048,576`.
