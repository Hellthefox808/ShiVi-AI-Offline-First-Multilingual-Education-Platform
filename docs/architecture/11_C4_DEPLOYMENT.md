# 11 — C4 ARCHITECTURE MODEL: LEVEL 4 DEPLOYMENT TOPOLOGY

> **Document ID:** `BS-ARCH-11-C4-DEPLOYMENT`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** C4 Model for Visualizing Software Architecture (Level 4)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Local Development Multi-Container Mesh (`infra/docker-compose.yml`)

The complete platform runs locally in an isolated bridge network (`bhashasetu-mesh`) with strict resource constraints matching production bounds:

```mermaid
graph TD
    subgraph Host["Developer Workstation (Windows / Linux)"]
        subgraph Net["Docker Bridge Network: bhashasetu-mesh"]
            Nginx["bhashasetu-nginx-gateway<br/>(Port 80:80, CPU: 0.5, Mem: 256M)"]
            
            Frontend["bhashasetu-web-frontend<br/>(Next.js 16.3, Port 3000, CPU: 1.5, Mem: 1G)"]
            Backend["bhashasetu-web-backend<br/>(NestJS 11, Port 3001, CPU: 1.5, Mem: 1G)"]
            AI["bhashasetu-ai-platform<br/>(FastAPI, Port 8000, CPU: 2.0, Mem: 2G)"]
            
            PG[("bhashasetu-postgres<br/>(PostgreSQL 18 + pgvector, Port 5432, CPU: 2.0, Mem: 2G)")]
            Redis[("bhashasetu-redis<br/>(Redis 7.4 Alpine, Port 6379, CPU: 1.0, Mem: 512M)")]
        end
        
        VolPG[("Volume: pgdata")] --- PG
        VolRedis[("Volume: redisdata")] --- Redis
        VolAudio[("Volume: audio-assets")] --- AI
    end

    UserBrowser["Browser / Client"] -->|HTTP /80| Nginx
    Nginx -->|/| Frontend
    Nginx -->|/api/v1/*| Backend
    Nginx -->|/api/v1/voice/*| AI

    Backend -->|Database Queries| PG
    Backend -->|Task Queues| Redis
    Backend -->|Inference Dispatch| AI
    AI -->|DiskANN Vector Queries| PG
```

---

## 2. Production Kubernetes Cluster Topology (`infra/k8s/`)

For state-scale deployment across Jharkhand's 12,000 primary schools, BhashaSetu AI deploys to a managed Kubernetes cluster:

```mermaid
graph TD
    subgraph Cloud["Kubernetes Cluster (Namespace: bhashasetu-prod)"]
        Ingress["NGINX / Cloud Ingress Controller<br/>(TLS 1.3 Termination, Let's Encrypt)"]
        
        subgraph StatelessPods["Stateless Application Tier (Horizontal Pod Autoscaler)"]
            HPAFrontend["Web Frontend Deploy (2-5 Replicas)"]
            HPABackend["Web Backend Deploy (3-10 Replicas)"]
            HPAAI["AI Platform Deploy (2-8 Replicas, GPU/High-CPU)"]
        end
        
        subgraph StatefulTier["Stateful Infrastructure Tier"]
            StatefulPG[("PostgreSQL 18 StatefulSet<br/>(Primary + Read Replica with pgvector)")]
            DeployRedis[("Redis 7.4 Sentinel Deployment<br/>(AOF Persistence + In-Memory LRU)")]
        end
        
        subgraph Storage["Persistent Volume Claims (CSI NVMe SSD)"]
            PVCPG["pvc-postgres-data (100Gi)"]
            PVCRedis["pvc-redis-data (20Gi)"]
            PVCAudio["pvc-audio-assets (500Gi)"]
        end
    end

    Internet["Public Cellular / Broadband"] --> Ingress
    Ingress --> HPAFrontend
    Ingress --> HPABackend
    Ingress --> HPAAI

    HPABackend --> StatefulPG
    HPABackend --> DeployRedis
    HPABackend --> HPAAI
    HPAAI --> StatefulPG

    StatefulPG --- PVCPG
    DeployRedis --- PVCRedis
    HPAAI --- PVCAudio
```

---

## 3. Container Resource Sizing & Limits Table

| Service Identifier | Kubernetes Pod Name | CPU Request / Limit | Memory Request / Limit | Autoscaling Rule (HPA) |
|---|---|---|---|---|
| `web-frontend` | `bhashasetu-web-frontend-*` | $500\text{m} / 1500\text{m}$ | $512\text{Mi} / 1024\text{Mi}$ | Target CPU $> 75\%$ or Memory $> 80\%$ |
| `web-backend` | `bhashasetu-web-backend-*` | $500\text{m} / 1500\text{m}$ | $512\text{Mi} / 1024\text{Mi}$ | Target CPU $> 70\%$ or Throughput $> 250\text{ rps}$ |
| `ai-platform` | `bhashasetu-ai-platform-*` | $1000\text{m} / 2000\text{m}$ | $1024\text{Mi} / 2048\text{Mi}$ | Target CPU $> 80\%$ (Optional T4 GPU slice) |
| `postgres` | `postgres-0` (StatefulSet) | $1000\text{m} / 2000\text{m}$ | $1024\text{Mi} / 2048\text{Mi}$ | Vertical Pod Autoscaler (VPA) |
| `redis` | `redis-*` | $250\text{m} / 1000\text{m}$ | $256\text{Mi} / 512\text{Mi}$ | Memory max bound at $512\text{MB}$ with LRU |
| `nginx-gateway` | `ingress-controller-*` | $250\text{m} / 500\text{m}$ | $128\text{Mi} / 256\text{Mi}$ | Cluster Ingress standard daemonset |
