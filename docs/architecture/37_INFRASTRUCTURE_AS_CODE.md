# 37 — INFRASTRUCTURE AS CODE (IAC) & ENVIRONMENT CONFIGURATION

> **Document ID:** `BS-ARCH-37-IAC-ENV`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** GitOps & Declarative Infrastructure as Code (§42 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Multi-Environment Parity Matrix

To ensure that bugs detected in production can be replicated locally with 100% fidelity, all environments adhere to an immutable runtime parity matrix:

| Architectural Component | Local Development (Docker Compose) | Staging Cluster (GKE Staging) | Production Cluster (GKE Prod) |
|---|---|---|---|
| **OS / Base Image** | Alpine Linux 3.20 / Debian Slim | Alpine Linux 3.20 / Distroless | Debian 12 Slim Distroless |
| **Node.js Runtime** | Node.js 22.8 LTS | Node.js 22.8 LTS | Node.js 22.8 LTS |
| **Python Runtime** | Python 3.12.5 | Python 3.12.5 | Python 3.12.5 |
| **PostgreSQL Database** | PostgreSQL 18.0 (`pgvector:pg16`) | Cloud SQL PostgreSQL 18.0 | Cloud SQL PostgreSQL 18.0 HA |
| **Redis Cache** | Redis 7.4-Alpine (Standalone) | Redis 7.4-Alpine (Cluster) | Memorystore for Redis 7.4 HA |
| **Vector Search Index** | StreamingDiskANN (Local SSD) | StreamingDiskANN (NVMe SSD) | StreamingDiskANN (Persistent SSD)|

---

## 2. Declarative Kustomize Overlay Hierarchy

Infrastructure manifests are structured hierarchically using Kustomize overlays:

```text
infra/k8s/
├── base/                           # Canonical manifest definitions
│   ├── deployment-backend.yaml
│   ├── deployment-ai.yaml
│   ├── service-backend.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/                        # Local minikube / kind testing
    │   ├── kustomization.yaml
    │   └── patch-replicas.yaml     # 1 replica, reduced memory requests
    ├── staging/                    # Pre-production validation
    │   ├── kustomization.yaml
    │   └── patch-envs.yaml         # Staging database URLs
    └── prod/                       # Live Jharkhand state production
        ├── kustomization.yaml
        ├── patch-resources.yaml    # Production CPU/Mem limits & GPU flags
        └── ingress-tls-patch.yaml  # Production SSL certificates
```

---

## 3. Secret Management & Cryptographic Zero-Leak Invariant

1. **Zero Cleartext Secrets in Git**: Plaintext passwords, API keys, and JWT secrets are strictly forbidden in Git history (`.gitignore` enforces `.env*` exclusion).
2. **Production Secrets Resolution**:
   - In Kubernetes, secrets are mounted from **GCP Secret Manager** or injected via **SealedSecrets** (Bitnami) where keys are encrypted using an asymmetric public key tied to the cluster controller.
   - At runtime, pods mount secrets as memory-backed files (`/secrets/jwt/secret.key`) rather than plaintext environment variables, preventing exposure in process dump logs.
