# 18 — DATABASE DESIGN & PERSISTENCE ECOSYSTEM

> **Document ID:** `BS-ARCH-18-DB-DESIGN`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Pattern:** Multi-Model Polyglot Persistence (Relational + Vector + Key-Value + Embedded SQLite)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Multi-Model Persistence Architecture

BhashaSetu AI leverages a **polyglot persistence ecosystem**, matching each data access pattern to the optimal storage engine:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       PERSISTENCE ECOSYSTEM TOPOLOGY                        │
├────────────────────┬────────────────────┬───────────────────────────────────┤
│ Storage Engine     │ Location & Model   │ Primary Domain Entities Stored    │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 1. PostgreSQL 18   │ Cloud / Server     │ Users, Schools, Curricula, Lessons│
│    + pgvector      │ Relational + Vector│ Student Attempts, Audit Logs,     │
│                    │ ACID Store         │ 1024-dim BGE-M3 Vector Embeddings.│
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 2. Room SQLite 3   │ Mobile Edge Tablet │ Local Lessons, Flashcards, Quizzes│
│    (AppDatabase)   │ Embedded SQLite    │ Student Entities, Outbox Queue,   │
│                    │ Local-First ACID   │ Sync Logs, Offline Glossaries.    │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 3. Redis 7.4       │ Cloud In-Memory    │ BullMQ Background Task Queues,    │
│                    │ Key-Value / Streams│ Rate Limiting Counters, Active    │
│                    │                    │ User Session Tokens.              │
├────────────────────┼────────────────────┼───────────────────────────────────┤
│ 4. S3 / MinIO      │ Cloud Object Store │ Bundled Offline Package Zips,     │
│                    │ Blob Storage       │ Synthesized MP3/Opus Audio Assets,│
│                    │                    │ Printable Dual-Language PDF Cards.│
└────────────────────┴────────────────────┴───────────────────────────────────┘
```

---

## 2. Storage Engine Trade-Off & Selection Matrix

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                     STORAGE ENGINE SELECTION JUSTIFICATION                  │
├───────────────────┬──────────────────────┬──────────────────────────────────┤
│ Engine Candidate  │ Evaluated Attributes │ Selection Decision               │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ PostgreSQL 18 +   │ • Unified relational │ SELECTED: Eliminates dual-write  │
│ pgvector          │   and vector store   │ inconsistency between vectors    │
│                   │ • ACID transactions  │ and business entities; reduces   │
│                   │ • Native RLS support │ operational host overhead.       │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Room SQLite 3     │ • Zero-config native │ SELECTED: Guaranteed 100%        │
│                   │ • Compile-time type  │ offline durability on 2 GB RAM   │
│                   │   safe DAO queries   │ Android tablets without JNI lag. │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ MongoDB / NoSQL   │ • Flexible document  │ REJECTED: Lacks native multi-    │
│                   │ • High write rate    │ tenant Row-Level Security (RLS)  │
│                   │ • No native vector   │ and ACID joins across curricula. │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Pure Vector DB    │ • Ultra-high scale   │ REJECTED: Premature optimization │
│ (Pinecone/Milvus) │ • High cost/query    │ for 25k curriculum chunks; high  │
│                   │ • External network   │ financial cost for public schools│
└───────────────────┴──────────────────────┴──────────────────────────────────┘
```

---

## 3. Data Consistency & Isolation Models

1. **Cloud Core (PostgreSQL 18)**:
   - **Isolation Level**: `READ COMMITTED` default; `SERIALIZABLE` for lesson publishing and sync outbox deduplication.
   - **Multi-Tenancy**: Row-Level Security (RLS) policies filter every table by `school_id`.
2. **Edge Client (Room SQLite)**:
   - **Isolation Level**: SQLite `WAL` (Write-Ahead Logging) mode enabled.
   - **Thread Safety**: Room handles asynchronous queries via Kotlin Coroutines `Dispatchers.IO`, preventing UI thread blocks.
3. **Cross-Boundary Replication**:
   - **Model**: Eventual Consistency with deterministic conflict resolution (Append-Only for student attempts, Immutable for published curriculum).
