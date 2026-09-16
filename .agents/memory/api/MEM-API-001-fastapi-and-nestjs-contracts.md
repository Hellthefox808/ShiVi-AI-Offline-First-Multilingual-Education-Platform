# MEM-API-001: API Catalogs & Contract Standards

- **ID**: `MEM-API-001`
- **TYPE**: `API`
- **TITLE**: FastAPI AI Microservice & NestJS Enterprise Gateway API Catalog
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [services/ai-platform/main.py](file:///d:/HACKTHON/bhashasetu-ai/services/ai-platform/main.py), [services/web-backend/src/](file:///d:/HACKTHON/bhashasetu-ai/services/web-backend/src)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["api", "fastapi", "nestjs", "rest", "contracts", "openapi"]`
- **RELATED_COMPONENTS**: `["services/ai-platform/main.py", "services/web-backend/", "packages/contracts/"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 API Engineering

---

## CONTENT

### AI Platform Microservice (FastAPI, Port 8000)
- `GET /health`: Liveness & readiness probe with service telemetry.
- `POST /api/v1/curriculum/retrieve`: Hybrid RAG retrieval (BM25 + Semantic Centroids).
- `POST /api/v1/translate`: Trilingual MT translation with native script rendering.
- `POST /api/v1/pedagogy/adapt`: Injects Jharkhand cultural analogies (e.g. Sarhul Sal tree, Sohrai art).
- `POST /api/v1/quality/evaluate`: Automated COMET quality scoring and MQM error span detection.
- `POST /api/v1/voice/synthesize`: Voice-to-voice synthesis with latency budget profiling.
- `POST /api/v1/pipeline/synthesize`: Master 7-stage end-to-end synthesis pipeline.

### Enterprise Gateway (NestJS 11, Port 3001)
- `/api/v1/auth`: JWT & Argon2id authentication and role-based access.
- `/api/v1/curriculum`: JCERT curriculum catalog management and filtering.
- `/api/v1/lessons`: Teacher lesson authoring and student lesson delivery.
- `/api/v1/devices`: Tablet device registration and trust authorization.
- `/api/v1/offline-packs`: Bundled SQLite & media asset compression for rural download.
- `/api/v1/sync`: Outbox synchronization endpoint with idempotent replay rejection.
- `/api/v1/analytics`: Classroom performance, phonetics usage, and learning outcome metrics.
- `/api/v1/audit`: Immutable administrative action log.
- `/api/v1/reviews`: Peer review and quality gate adjudication for published lessons.
