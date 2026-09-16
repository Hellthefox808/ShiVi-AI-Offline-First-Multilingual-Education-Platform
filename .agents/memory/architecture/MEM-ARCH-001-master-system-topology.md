# MEM-ARCH-001: Master Polyglot System Topology

- **ID**: `MEM-ARCH-001`
- **TYPE**: `ARCHITECTURE`
- **TITLE**: Master Polyglot System Topology & Offline Durability
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [docs/ARCHITECTURE_OVERVIEW.md](file:///d:/HACKTHON/bhashasetu-ai/docs/ARCHITECTURE_OVERVIEW.md), [README.md](file:///d:/HACKTHON/bhashasetu-ai/README.md)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["architecture", "offline-first", "monorepo", "topology", "sih-2026"]`
- **RELATED_COMPONENTS**: `["app/", "services/web-backend/", "services/ai-platform/", "apps/web-frontend/", "packages/contracts/"]`
- **RELATED_DECISIONS**: `["MEM-DEC-001"]`
- **EXPIRY / STALENESS SIGNAL**: Stable throughout v3.0.0-PROD
- **PROVENANCE**: Team SHIVI@808 Architectural Design

---

## CONTENT

BhashaSetu AI is structured as an enterprise-grade polyglot monorepo delivering Mother-Tongue-Based Multilingual Education (MTB-MLE) with zero-network classroom durability:

1. **Android Edge Client (`app/`)**:
   - Built with Kotlin 2.1, Jetpack Compose, Material3, and Room SQLite (`bhashasetu_local_db`).
   - Runs directly on low-resource Android tablets (API 26+) in rural classrooms without Internet connectivity.
   - Houses native Speech-To-Text (STT) and Devanagari phonetic synthesis for tribal dialects.

2. **Enterprise Gateway Core (`services/web-backend/`)**:
   - Built with NestJS 11 LTS on Node.js 22, TypeScript 5.7.
   - Provides 9 enterprise domain modules: `auth`, `curriculum`, `lessons`, `devices`, `offline-packs`, `sync`, `analytics`, `audit`, and `reviews`.
   - Backed by PostgreSQL 18 with `pgvector` and Redis 7.4 BullMQ async queues.

3. **AI / ML Platform Microservice (`services/ai-platform/`)**:
   - Built with FastAPI, Python 3.12, Uvicorn.
   - Delivers a 7-stage synthesis pipeline: Hybrid RAG (BM25 + 128-dim dense centroids + RRF), pedagogical cultural analogy preservation, Unbabel COMET scoring, and voice latency telemetry under 3000ms SLA.

4. **Web Frontend Portal (`apps/web-frontend/`)**:
   - Built with Next.js 15/16 App Router, React 19, Tailwind CSS v4.
   - Lesson Studio Canvas for curriculum designers and district administrators.

5. **Shared Contracts (`packages/contracts/`)**:
   - TypeScript 5.x strict contracts and schemas shared across the frontend and backend.
