# MEM-DB-001: Hybrid Edge-to-Cloud Data Architecture

- **ID**: `MEM-DB-001`
- **TYPE**: `DATABASE`
- **TITLE**: Hybrid Edge-to-Cloud Database Topology (Room SQLite + PostgreSQL 18 pgvector)
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [app/src/main/java/com/example/data/local/AppDatabase.kt](file:///d:/HACKTHON/bhashasetu-ai/app/src/main/java/com/example/data/local/AppDatabase.kt), [infra/docker-compose.yml](file:///d:/HACKTHON/bhashasetu-ai/infra/docker-compose.yml)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["database", "room", "sqlite", "postgres", "pgvector", "outbox"]`
- **RELATED_COMPONENTS**: `["app/src/main/java/com/example/data/local/", "services/web-backend/src/"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 Database Architecture

---

## CONTENT

### Edge Database: Room SQLite (`bhashasetu_local_db`)
- **Version**: 2
- **Tables / Entities**:
  1. `LessonEntity`: Cached pedagogical lesson structures and offline content.
  2. `WorksheetEntity`: Formative exercises and worksheets.
  3. `FlashcardEntity`: Multilingual vocabulary cards with transliteration.
  4. `StudentEntity`: Local learner profiles and enrolled tribal dialects.
  5. `AssessmentAttemptEntity`: Offline quiz responses, time spent, score metrics.
  6. `GlossaryEntity`: Trilingual terminology dictionaries.
  7. `OutboxEntity`: Durable transactional sync log for event publishing.
  8. `SyncLogEntity`: Network synchronization attempt audit trail.
  9. `CurriculumContentEntity`: JCERT curriculum chunks and competencies.

### Enterprise Cloud Database: PostgreSQL 18 with `pgvector`
- **Port**: 5432
- **Capabilities**: Relational ACID storage for multi-tenant schools, districts, and teacher rosters. Dense embedding indexing for semantic curriculum retrieval.
- **Queue/Cache Layer**: Redis 7.4 (`maxmemory 512mb`, `allkeys-lru`) for BullMQ background ingestion and offline pack compilation.
