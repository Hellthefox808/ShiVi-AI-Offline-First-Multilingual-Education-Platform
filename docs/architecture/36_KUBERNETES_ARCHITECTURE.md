# 36 — KUBERNETES CLUSTER ARCHITECTURE & MANIFEST CONTROL

> **Document ID:** `BS-ARCH-36-K8S-ARCH`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Source Grounding:** [`infra/k8s/`](file:///d:/HACKTHON/bhashasetu-ai/infra/k8s) (11 Verified Production Manifests)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Kubernetes Manifest Suite Structure (`infra/k8s/`)

The production deployment is orchestrated via declarative Kubernetes manifests managed through Kustomize:

```text
infra/k8s/
├── namespace.yaml                  # bhashasetu-prod namespace definition
├── kustomization.yaml              # Master resource bundler and image patcher
├── configmap.yaml                  # Shared environment configurations
├── secrets.yaml                    # Base64-encoded encrypted secrets
├── ingress.yaml                    # Ingress resource with TLS 1.3 routing
├── hpa.yaml                        # Horizontal Pod Autoscalers for stateless tiers
├── web-backend-deployment.yaml     # NestJS 11 Gateway (3 Replicas default)
├── ai-platform-deployment.yaml     # FastAPI AI Engine (2 Replicas, high-memory)
├── web-frontend-deployment.yaml    # Next.js 16.3 Web Studio (2 Replicas)
├── redis-deployment.yaml           # Redis 7.4 Task Queue Deployment
└── postgres-statefulset.yaml       # PostgreSQL 18 StatefulSet with VolumeClaimTemplates
```

---

## 2. Workload Specifications & Health Probes

### 2.1 Web Backend Deployment (`web-backend-deployment.yaml`)
- **Replicas**: Min $3$, Max $10$ (autoscaled via HPA).
- **Probes**:
  - **Liveness Probe**: `GET /api/v1/health` on port $3001$; `periodSeconds: 15`, `timeoutSeconds: 5`, `failureThreshold: 3`.
  - **Readiness Probe**: Validates active PostgreSQL connection before accepting traffic.
  - **Startup Probe**: `initialDelaySeconds: 10`, `failureThreshold: 10` (allows database migration completion).

### 2.2 AI Platform Microservice (`ai-platform-deployment.yaml`)
- **Replicas**: Min $2$, Max $8$.
- **Resource Envelope**:
  - Requests: `cpu: "1000m"`, `memory: "1024Mi"`.
  - Limits: `cpu: "2000m"`, `memory: "2048Mi"` (preventing host node OOM).
- **Probes**:
  - **Liveness Probe**: `GET /health` on port $8000$; verifies BGE-M3 model index is memory-mapped.

### 2.3 PostgreSQL StatefulSet (`postgres-statefulset.yaml`)
- **Workload Type**: `StatefulSet` with headless service `postgres-headless`.
- **Storage**: `VolumeClaimTemplate` requesting `100Gi` NVMe SSD (`ReadWriteOnce`).
- **Termination Grace Period**: $60\text{ seconds}$ (ensures clean WAL flush to disk on pod termination).

---

## 3. Horizontal Pod Autoscaler (HPA) Rules (`hpa.yaml`)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: bhashasetu-backend-hpa
  namespace: bhashasetu-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: bhashasetu-web-backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```
