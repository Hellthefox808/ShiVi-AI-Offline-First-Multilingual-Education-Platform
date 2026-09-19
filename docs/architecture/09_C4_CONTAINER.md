# 09 — C4 ARCHITECTURE MODEL: LEVEL 2 CONTAINER DIAGRAM

> **Document ID:** `BS-ARCH-09-C4-CONTAINER`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** C4 Model for Visualizing Software Architecture (Level 2)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Container Diagram (Level 2)

```mermaid
C4Container
    title Container Diagram for BhashaSetu AI Platform (Level 2)

    Person(teacher, "Teacher / Admin", "Primary educator or school administrator using tablet or laptop.")
    Person(student, "Tribal Student", "Primary student in village classroom.")

    System_Boundary(c0, "BhashaSetu AI Ecosystem") {
        Container(mobile, "Mobile Edge App", "Android Kotlin / Compose, Room SQLite", "Delivers offline lessons, plays audio relay, captures student quizzes, records outbox.")
        Container(web_app, "Web Portal & Studio", "Next.js 16.3, React 19.2, Tailwind v4", "Teacher scaffolding studio, native script typography canvas, admin FLN analytics.")
        Container(nginx, "Reverse Proxy & Ingress", "NGINX 1.25 Alpine", "Terminates TLS 1.3, provides path routing, rate limiting, and static asset caching.")
        Container(backend, "API Gateway & Core", "NestJS 11, Node.js 22 LTS, TypeScript", "Authenticates users, enforces RLS, manages curriculum state machines, reconciles sync.")
        Container(ai_service, "AI Platform Engine", "FastAPI, Python 3.12, PyTorch", "Executes Hybrid RAG, NLLB/Gemini MT, Whisper ASR, Kokoro TTS, and COMET quality gate.")
        ContainerDb(postgres, "Primary Database", "PostgreSQL 18 + pgvector / DiskANN", "Stores relational school entities, immutable audit logs, and 1024-dim dense vector embeddings.")
        ContainerDb(redis, "Task Queue & Cache", "Redis 7.4 Alpine + BullMQ", "Buffers offline pack packaging tasks, throttles API requests, and caches session tokens.")
        ContainerDb(sqlite, "Local Edge Database", "Room SQLite 3 (Android Local)", "Local-first storage for offline lessons, student attempts, and pending sync outbox.")
    }

    Rel(teacher, mobile, "Interacts with lessons & voice relay", "Touch / Mic / Speaker")
    Rel(student, mobile, "Solves quizzes & listens to audio", "Touch / Speaker")
    Rel(teacher, web_app, "Authors lessons & reviews translations", "HTTPS (Port 3000/3002)")

    Rel(mobile, sqlite, "Reads/writes local data 100% offline", "SQLite IPC / ACID")
    Rel(web_app, nginx, "Dispatches API requests", "HTTPS (Port 80/443)")
    Rel(mobile, nginx, "Pushes outbox & pulls delta packs", "HTTPS (Port 80/443)")

    Rel(nginx, web_app, "Routes /", "HTTP (Port 3000)")
    Rel(nginx, backend, "Routes /api/v1/*", "HTTP (Port 3001)")
    Rel(nginx, ai_service, "Routes /api/v1/voice/* & /api/v1/ai/*", "HTTP (Port 8000)")

    Rel(backend, ai_service, "Dispatches AI lesson generation", "HTTP/2 (Port 8000)")
    Rel(backend, postgres, "Executes transactional queries with RLS", "TCP (Port 5432)")
    Rel(backend, redis, "Enqueues BullMQ background tasks", "TCP (Port 6379)")
    Rel(ai_service, postgres, "Queries pgvector DiskANN vector index", "TCP (Port 5432)")
```

---

## 2. Container Responsibility & Specification Matrix

| Container Name | Runtime / Tech Stack | Port Binding | Docker / Path Mapping | Primary Responsibility |
|---|---|---|---|---|
| **Mobile Edge App** | Android Native (Kotlin 2.0, Compose, Room SQLite) | N/A (Embedded) | `app/` | Offline classroom UI, ExoPlayer audio playback, Silero VAD, local sync outbox engine. |
| **Local SQLite DB** | Room SQLite 3 (Android Filesystem) | In-process | Embedded in APK | Local system of record with 9 tables; zero network requirement. |
| **Web Frontend** | Next.js 16.3, React 19.2, TanStack Query | `3000:3000` (Dev: 3002) | `apps/web-frontend/` | Desktop Lesson Studio, Ol Chiki typography canvas, district analytics dashboards. |
| **Reverse Proxy** | NGINX Alpine | `80:80`, `443:443` | `infra/nginx/` | TLS termination, gzip compression, path routing, rate limiting. |
| **Web Backend** | NestJS 11, Node.js 22 LTS, TypeScript 5 | `3001:3001` | `services/web-backend/` | Central domain gateway, JWT/Argon2id auth, RLS tenant scoping, sync reconciliation. |
| **AI Platform** | FastAPI, Python 3.12, PyTorch | `8000:8000` | `services/ai-platform/` | Hybrid RAG (BGE-M3 + BM25), NLLB/Gemini translation, Kokoro TTS, COMET quality gate. |
| **PostgreSQL 18** | PostgreSQL 18 + `pgvector` / `pgvectorscale` | `5432:5432` | `infra/docker-compose.yml` | Unified relational store & DiskANN vector index for JCERT curriculum chunks. |
| **Redis 7.4** | Redis 7.4-Alpine | `6379:6379` | `infra/docker-compose.yml` | BullMQ task queue for offline pack generation, session caching, rate limiting. |
