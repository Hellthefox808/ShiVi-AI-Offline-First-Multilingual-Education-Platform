# MEM-DEPLOY-001: Deployment, Packaging & Distribution Matrix

- **ID**: `MEM-DEPLOY-001`
- **TYPE**: `DEPLOYMENT`
- **TITLE**: Production Containerization, Kubernetes Orchestration, and Mobile APK Delivery
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [infra/docker-compose.yml](file:///d:/HACKTHON/bhashasetu-ai/infra/docker-compose.yml), [infra/k8s/](file:///d:/HACKTHON/bhashasetu-ai/infra/k8s)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["deployment", "docker", "k8s", "apk", "adb", "distribution"]`
- **RELATED_COMPONENTS**: `["infra/", "app/build.gradle.kts"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 DevOps

---

## CONTENT

1. **Android APK Direct Distribution**:
   - Primary Binary: [BhashaSetu-v1.0-debug.apk](file:///d:/HACKTHON/bhashasetu-ai/BhashaSetu-v1.0-debug.apk) (27.43 MB).
   - Direct Tablet Sideload:
     ```powershell
     adb install -r D:\HACKTHON\bhashasetu-ai\BhashaSetu-v1.0-debug.apk
     ```
   - Can be shared directly via USB, Bluetooth, or SD card in rural clusters.

2. **Docker Compose Production Mesh**:
   - Located at [infra/docker-compose.yml](file:///d:/HACKTHON/bhashasetu-ai/infra/docker-compose.yml).
   - Starts 6 coordinated microservices: `postgres` (pgvector), `redis` (7.4), `ai-platform` (FastAPI), `web-backend` (NestJS), `web-frontend` (Next.js), and `nginx-gateway`.

3. **Kubernetes Cluster Deployments**:
   - 11 production manifests in [infra/k8s/](file:///d:/HACKTHON/bhashasetu-ai/infra/k8s): ConfigMaps, Secrets, PostgreSQL StatefulSet, Redis Deployment, AI Engine Deployment + HPA, Web Backend Deployment + HPA, Web Frontend Deployment, and Ingress.
