# 13 — ENTERPRISE SERVICE CATALOG & SUBSYSTEM REGISTRY

> **Document ID:** `BS-ARCH-13-SERVICE-CATALOG`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Enterprise Service Catalog Specification (§7 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Service 01: AI Platform Microservice (`bhashasetu-ai-platform`)

* **Purpose**: Dedicated machine learning inference gateway providing hybrid RAG, multilingual translation, live voice translation, and quality estimation.
* **Owner**: AI Systems Engineering (SHIVI@808)
* **Runtime & Language**: Python 3.12, FastAPI, PyTorch 2.3, Hugging Face Transformers.
* **Port & Network**: Internal Port `8000`, Host binding `8000:8000`, Network: `bhashasetu-mesh`.
* **Upstream Callers**: `web-backend` (via HTTP/2 REST), `mobile-edge` (via direct WebSocket voice stream).
* **Downstream Dependencies**: `postgres` (pgvector DiskANN vector queries).
* **Datastores Owned**: Local in-memory LRU query cache, filesystem audio cache (`/app/audio`).
* **Health Endpoints**: `GET /health` (returns JSON status, RAG index readiness, active model versions).
* **Prometheus Metrics**: `ai_request_latency_seconds_bucket`, `ai_rag_retrieval_ms`, `ai_comet_score_gauge`.
* **Scaling Strategy**: Horizontal Pod Autoscaler (HPA) targeting CPU $> 80\%$ or GPU duty $> 85\%$.
* **Failure Modes & Degradation**: If GPU fails, falls back to CPU INT8 ONNX; if RAG fails, falls back to in-memory static JCERT glossaries.

---

## 2. Service 02: Web Backend Gateway (`bhashasetu-web-backend`)

* **Purpose**: Enterprise application gateway, authentication provider, multi-tenant RLS enforcer, curriculum manager, and durable sync reconciler.
* **Owner**: Backend Core Engineering (SHIVI@808)
* **Runtime & Language**: Node.js 22 LTS, NestJS 11, TypeScript 5.x, TypeORM.
* **Port & Network**: Internal Port `3001`, Host binding `3001:3001`, Network: `bhashasetu-mesh`.
* **Upstream Callers**: `web-frontend`, `mobile-edge` (via NGINX reverse proxy).
* **Downstream Dependencies**: `postgres` (Primary ACID store), `redis` (BullMQ queues), `ai-platform` (Inference).
* **Datastores Owned**: Master relational schema in PostgreSQL 18.
* **Health Endpoints**: `GET /api/v1/health` (checks PostgreSQL and Redis connectivity).
* **Prometheus Metrics**: `http_request_duration_seconds`, `sync_push_batches_total`, `active_user_sessions`.
* **Scaling Strategy**: HPA targeting CPU $> 70\%$; stateless container replication (3–10 pods).
* **Failure Modes & Degradation**: If Redis drops, sync operations write directly to database transactions; if AI drops, queues generation tasks for later execution.

---

## 3. Service 03: Web Frontend Portal (`bhashasetu-web-frontend`)

* **Purpose**: Presentation layer providing teacher Lesson Studio, tribal script typography canvas, linguist HITL review, and district FLN analytics.
* **Owner**: Frontend & UX Engineering (SHIVI@808)
* **Runtime & Language**: Next.js 16.3 (App Router), React 19.2, Tailwind CSS v4, TypeScript.
* **Port & Network**: Internal Port `3000`, Host binding `3000:3000` (Dev: 3002 to avoid Antigravity IDE port collision).
* **Upstream Callers**: End-user web browsers (Educators, Linguists, District Officers).
* **Downstream Dependencies**: `web-backend` (via REST API).
* **Datastores Owned**: Browser LocalStorage / IndexedDB for draft caching and user preferences.
* **Health Endpoints**: `GET /` (HTTP 200 health probe).
* **Scaling Strategy**: Edge CDN caching for static bundles; HPA 2–5 pods for SSR rendering.
* **Failure Modes & Degradation**: Full client-side Mock Service Worker (MSW) fallback for offline demonstration.

---

## 4. Service 04: Android Mobile Edge Client (`bhashasetu-mobile-edge`)

* **Purpose**: Primary classroom delivery engine; delivers offline lessons, interactive quizzes, spoken voice relay, and local sync outbox queue.
* **Owner**: Mobile Engineering (SHIVI@808)
* **Runtime & Language**: Android Native, Kotlin 2.0, Jetpack Compose, Room SQLite, Android SDK 36.1.
* **Port & Network**: Native embedded application; opportunistic outbound HTTPS to cloud gateway.
* **Upstream Callers**: Primary school teachers and tribal students via physical tablet touch/audio.
* **Downstream Dependencies**: `web-backend` (sync), `ai-platform` (voice streaming when online).
* **Datastores Owned**: Local SQLite database (`bhashasetu_local_db`) with 9 tables.
* **Health Endpoints**: In-app self-diagnostic battery and database integrity check.
* **Metrics**: On-device telemetry logged to `sync_logs` table (RAM usage, battery level, sync lag).
* **Scaling Strategy**: Distributed edge topology across 50,000+ autonomous physical tablets.
* **Failure Modes & Degradation**: 100% functional in complete Airplane Mode; zero network exceptions surfaced to UI.

---

## 5. Service 05: Primary Database (`bhashasetu-postgres`)

* **Purpose**: System of record for users, schools, curricula, student attempts, and pgvector DiskANN indexes.
* **Owner**: Database Administration / SRE
* **Runtime**: PostgreSQL 18 with `pgvector` and `pgvectorscale` extensions.
* **Port & Network**: Port `5432:5432`, Network: `bhashasetu-mesh`.
* **Upstream Callers**: `web-backend`, `ai-platform`.
* **Health Endpoints**: `pg_isready -U bhashasetu_user -d bhashasetu_db`.
* **Scaling Strategy**: Primary writer with asynchronous read replicas; NVMe SSD storage.

---

## 6. Service 06: Task Queue & Cache (`bhashasetu-redis`)

* **Purpose**: Distributed task queue (BullMQ), session store, and API rate limiting token bucket.
* **Owner**: SRE / DevOps
* **Runtime**: Redis 7.4-Alpine.
* **Port & Network**: Port `6379:6379`, Network: `bhashasetu-mesh`.
* **Upstream Callers**: `web-backend`.
* **Health Endpoints**: `redis-cli ping` (returns `PONG`).
* **Scaling Strategy**: In-memory maxmemory bound at $512\text{ MB}$ with `allkeys-lru` eviction.
