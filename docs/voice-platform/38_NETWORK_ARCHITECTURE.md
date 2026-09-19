# VoxBridge AI — Network Topology & Enterprise Transit Architecture

## 1. Network Topology (Mermaid Diagram 17)

The Network Architecture illustrates the multi-tier VPC zoning, perimeter routing, load balancer separation, AWS Transit Gateway connectivity, and zero-egress private subnets protecting internal speech models.

```mermaid
graph TB
    subgraph InternetZone["Public Internet & Edge Network"]
        Clients["Global Clients (REST / WebSockets / WebRTC)"]
        CF_Edge["Cloudflare Anycast Edge (DDoS Shield & SSL Offload)"]
        Clients --> CF_Edge
    end

    subgraph AWS_VPC["AWS VPC: voxbridge-prod-vpc (10.100.0.0/16)"]
        subgraph PublicSubnets["Public Subnets (10.100.0.0/20 across AZ-a,b,c)"]
            NLB["AWS Network Load Balancer (Realtime WSS/WebRTC)"]
            ALB["AWS Application Load Balancer (REST API)"]
            NAT_GW["NAT Gateways (Egress Only)"]
        end

        subgraph PrivateAppSubnets["Private Compute Subnets (10.100.16.0/20)"]
            EKS_GatewayPods["EKS Gateway & Session Pods"]
            EKS_JobPods["EKS Batch Worker Pods"]
        end

        subgraph PrivateGPUSubnets["Isolated GPU Subnets (10.100.32.0/20 - Zero Egress)"]
            EKS_GPUPods["GPU Inference Workers (STT, MT, TTS)"]
        end

        subgraph PrivateDataSubnets["Isolated Data Subnets (10.100.48.0/20)"]
            AuroraPG[("Aurora PostgreSQL 18 Multi-AZ")]
            RedisCluster[("Redis 7.4 Cluster (6 Shards)")]
            NATSCluster["NATS JetStream (3 Nodes)"]
        end

        subgraph VPCEndpoints["AWS PrivateLink Endpoints"]
            S3_Endpoint["S3 Gateway Endpoint"]
            KMS_Endpoint["KMS Interface Endpoint"]
            ECR_Endpoint["ECR Interface Endpoint"]
        end
    end

    CF_Edge -->|Forward Media Streams| NLB
    CF_Edge -->|Forward REST Calls| ALB

    NLB --> EKS_GatewayPods
    ALB --> EKS_GatewayPods

    EKS_GatewayPods --> EKS_GPUPods
    EKS_GatewayPods --> RedisCluster
    EKS_GatewayPods --> AuroraPG
    EKS_JobPods --> NATSCluster

    EKS_GPUPods -.->|Private Transit Only| S3_Endpoint
    EKS_JobPods --> NAT_GW
    EKS_GatewayPods --> KMS_Endpoint
```

---

## 2. VPC Subnet Allocation Matrix

| Subnet Group | CIDR Block | Usable IPs | Internet Gateway Routing | Egress NAT Allowed | Target Workload Types |
|---|---|---|---|---|---|
| **Public DMZ** | `10.100.0.0/20` | 4,096 | Inbound via IGW | Yes | ALB, NLB, Elastic IPs, NAT Gateways |
| **Private App** | `10.100.16.0/20` | 4,096 | None (Private) | Yes (via NAT) | API Gateway, Session Manager, Batch Workers |
| **Private GPU** | `10.100.32.0/20` | 4,096 | None (Strict Isolated)| **NO (Zero Egress)** | C++ TensorRT Whisper, vLLM NLLB Workers |
| **Private Data**| `10.100.48.0/20` | 4,096 | None (Strict Isolated)| **NO** | Aurora PostgreSQL, Redis Cluster, NATS |

---

## 3. Network Performance & Audio MTU Optimization

1. **Jumbo Frames inside VPC (MTU 9001):** Internal communication between the Streaming Gateway and GPU inference workers enables AWS Jumbo Frames (MTU 9001). This reduces CPU packet processing overhead by $70\%$ when streaming high-bandwidth uncompressed PCM audio chunks between pods.
2. **WAN Path MTU Discovery (PMTUD):** Outer edge connections to client mobile browsers strictly enforce standard Ethernet MTU (1500 bytes) with a clamping MSS of 1452 bytes, preventing packet fragmentation on cellular LTE/5G networks.
3. **AWS PrivateLink Endpoints:** S3, AWS Secrets Manager, ECR, and KMS traffic never leaves the Amazon network backbone, avoiding internet transit charges and eliminating public exposure vectors.
