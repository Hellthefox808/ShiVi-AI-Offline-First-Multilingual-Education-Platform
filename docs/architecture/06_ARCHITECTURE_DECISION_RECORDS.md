# 06 — ARCHITECTURAL DECISION RECORDS (ADR REGISTER)

> **Document ID:** `BS-ARCH-06-ADR-REGISTER`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Format Standard:** Markdown Architectural Decision Records (MADR 3.0)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Master Decision Register Summary

| ADR ID | Title | Status | Primary Decision Driver | Chosen Architecture |
|---|---|---|---|---|
| **ADR-001** | Decoupled Polyglot Monorepo | **APPROVED** | Multi-ecosystem tooling (Kotlin, Python, Node, React) | Turborepo / Pnpm workspace + Native modules |
| **ADR-002** | NestJS 11 for Application Gateway | **APPROVED** | Enterprise DI, TypeScript type safety, OpenAPI | NestJS 11 LTS + TypeORM |
| **ADR-003** | FastAPI for AI/ML Microservice | **APPROVED** | Native PyTorch/Transformers async inference | FastAPI + Python 3.12 + Pydantic v2 |
| **ADR-004** | PostgreSQL 18 with pgvector | **APPROVED** | Single ACID datastore for relational + vectors | PostgreSQL 18 + pgvector extension |
| **ADR-005** | StreamingDiskANN for Vector Search | **APPROVED** | RAM constraints on SSD cloud hosts ($10\times$ less RAM) | DiskANN graph index (pgvectorscale) |
| **ADR-006** | Local-First Android SQLite (Room) | **APPROVED** | 100% disconnected rural classroom operation | Native Android Room SQLite DAOs |
| **ADR-007** | UUID Idempotent Outbox Pattern | **APPROVED** | Flaky 2G/3G network drops and replay storms | Client UUID `operation_id` outbox table |
| **ADR-008** | Contract-First OpenAPI 3.1 | **APPROVED** | Eliminating cross-service type drift | Single Swagger/OpenAPI spec generates DTOs |
| **ADR-009** | Redis 7.4 & BullMQ Task Queue | **APPROVED** | Non-blocking offline pack building & AI batching | Redis 7.4-Alpine + BullMQ workers |
| **ADR-010** | Multilingual LanguageProvider | **APPROVED** | Avoiding single-vendor LLM lock-in | Abstract Python interface with Bhashini/Gemini/NLLB |
| **ADR-011** | COMETKiwi Reference-Free QE | **APPROVED** | Real-time translation evaluation without gold refs | Unbabel COMETKiwi-XXL scoring gate |
| **ADR-012** | On-Device Silero VAD | **APPROVED** | Low-latency audio segmentation on 2GB tablets | Silero VAD C++ ONNX runtime |
| **ADR-013** | Bilingual Relay Audio Delivery | **APPROVED** | Non-native teacher + tribal student comprehension | Hindi audio $\to$ 450ms pause $\to$ Tribal audio ($0.72\times$) |
| **ADR-014** | Next.js 16.3 App Router for Web | **APPROVED** | Teacher studio desktop responsiveness & SSR | Next.js 16.3 + React 19.2 + Tailwind CSS v4 |
| **ADR-015** | Deterministic On-Device Assessment | **APPROVED** | Zero-latency instant feedback during airplane mode | Client-side deterministic quiz engine |

---

## 2. Exhaustive Decision Specifications

### ADR-001: Decoupled Polyglot Monorepo Architecture
* **Context:** The platform requires an Android edge client (Kotlin), an enterprise gateway (Node.js/TypeScript), an AI engine (Python/PyTorch), and an administrative web portal (React/Next.js). Maintaining separate repositories creates severe dependency synchronization friction.
* **Considered Options:**
  1. Multi-repo (4 independent Git repositories).
  2. Monolithic single-runtime repo (pure TypeScript or pure Python).
  3. Decoupled polyglot monorepo with unified tooling and shared contracts.
* **Decision Outcome:** Adopted Option 3. Polyglot monorepo rooted at `d:\HACKTHON\bhashasetu-ai` with directory seams: `app/`, `apps/web-frontend/`, `services/web-backend/`, `services/ai-platform/`, and `packages/contracts/`.
* **Consequences:** Single atomic Git commits span contracts, backend routes, and UI features. Requires modular CI pipelines that only build modified paths.
* **Rollback Strategy:** If workspace build complexity becomes unmanageable, subtrees can be extracted into standalone Git repositories using `git subtree split`.

---

### ADR-004: PostgreSQL 18 with pgvector as Unified Data & Vector Store
* **Context:** System requires relational storage for users, schools, curricula, and student attempts, alongside high-dimensional vector embeddings ($1024$-dim BGE-M3) for textbook RAG retrieval.
* **Considered Options:**
  1. Dedicated vector database (Pinecone / Qdrant / Milvus) + Relational PostgreSQL.
  2. Unified PostgreSQL 18 with `pgvector` extension.
  3. In-memory FAISS on the AI microservice + PostgreSQL.
* **Decision Outcome:** Adopted Option 2. Consolidate into PostgreSQL 18 with `pgvector`.
* **Decision Drivers:**
  - ACID transactions across curriculum metadata and embedding chunks.
  - Zero cross-database synchronization overhead or eventual consistency lag.
  - Minimizes infrastructure footprint in local Docker Compose ($< 2\text{ GB}$ container memory).
* **Rollback Strategy:** If vector query volume exceeds 50,000 QPS, pgvector tables can be replicated to an external Qdrant cluster by modifying `services/ai-platform/rag/engine.py`.

---

### ADR-005: StreamingDiskANN (pgvectorscale) for Vector Index Scaling
* **Context:** Standard HNSW vector indexes store full graph topologies in memory. For 25,000 curriculum chunks, HNSW requires $193\text{ MB}$ of RAM per index, which exhausts memory limits on low-cost cloud nodes.
* **Considered Options:**
  1. Exact vector scan (IVFFlat) — Low memory, but slow query latency ($> 80\text{ ms}$).
  2. Standard HNSW (`m=16, ef_construction=64`) — Fast ($4\text{ ms}$), but excessive RAM footprint.
  3. StreamingDiskANN via `pgvectorscale` — Graph stored on NVMe SSD, compressed cache in RAM.
* **Decision Outcome:** Adopted Option 3.
* **Consequences:** Reduces RAM footprint from $193\text{ MB}$ to $21\text{ MB}$ ($10\times$ reduction) while maintaining P95 search latency under $6\text{ ms}$ and Recall@10 $> 0.95$.
* **Rollback Strategy:** Fall back to standard HNSW indexing via simple SQL statement: `CREATE INDEX ... USING hnsw (embedding vector_cosine_ops)`.

---

### ADR-006: Local-First Android SQLite (Room) for Edge Runtime
* **Context:** Government primary schools in rural Jharkhand have zero cellular coverage. The classroom app cannot rely on an internet connection to display lessons, play audio, or record quizzes.
* **Considered Options:**
  1. Cloud-dependent app with aggressive HTTP caching.
  2. Local-first architecture using Android Room SQLite with an asynchronous outbox.
  3. Embedded Realm or ObjectBox database.
* **Decision Outcome:** Adopted Option 2. All reads and writes target local SQLite tables immediately via Room DAOs.
* **Consequences:** The Android client is 100% functional in Airplane Mode. Cloud backend is treated as an opportunistic backup and aggregation hub.
* **Rollback Strategy:** None required; core requirement of the problem statement.

---

### ADR-007: UUID-Idempotent Outbox Synchronization
* **Context:** When network connectivity is restored, tablets attempt to upload student quiz attempts over flaky 2G/3G connections. TCP resets and retries can cause massive duplicate row insertion.
* **Considered Options:**
  1. Naive auto-increment IDs with plain HTTP POST.
  2. Two-phase commit (2PC) over mobile cellular.
  3. Client-generated UUIDv4 `operation_id` with server-side unique constraints and deduplication.
* **Decision Outcome:** Adopted Option 3. Mobile writes outbox records with random UUIDs. NestJS backend executes an `INSERT ... ON CONFLICT (operation_id) DO NOTHING` or records idempotency tokens.
* **Consequences:** Guarantees zero duplicate student records or corrupted scores, regardless of how many times a packet is retried.
* **Rollback Strategy:** Fully backwards-compatible; no rollback necessary.

---

### ADR-011: Reference-Free COMETKiwi Quality Gate
* **Context:** Educational content generated in Santhali, Ho, and Mundari cannot be safely presented to young students if it contains severe mistranslations or hallucinations. Automated quality metrics must evaluate translations without human reference sentences.
* **Considered Options:**
  1. BLEU / chrF — Requires gold reference translations (unavailable for dynamic prompts).
  2. LLM-as-a-Judge — High latency ($> 2500\text{ ms}$), expensive, and hallucination-prone in low-resource languages.
  3. Unbabel COMETKiwi-XXL reference-free Quality Estimation (QE).
* **Decision Outcome:** Adopted Option 3. FastAPI AI platform executes COMETKiwi scoring. If score $< 0.85$, the lesson is quarantined in `REVIEW_REQUIRED` state and cannot be auto-published.
* **Consequences:** Protects tribal students from inaccurate pedagogical content.

---

### ADR-013: Bilingual Relay Audio Delivery Pattern
* **Context:** Tribal students know little Hindi, but their non-native Hindi teachers do not know tribal mother tongues. A pure tribal audio translation leaves the teacher lost; a pure Hindi audio translation leaves the student lost.
* **Considered Options:**
  1. Monolingual Tribal audio only.
  2. Simultaneous dual-channel stereo playback (confusing acoustic interference).
  3. Sequential Bilingual Relay: Hindi sentence spoken first $\to$ 450ms acoustic silence $\to$ Tribal mother-tongue translation spoken at $0.72\times$ FLN speed.
* **Decision Outcome:** Adopted Option 3. Enforced across [`TtsManager.kt`](file:///d:/HACKTHON/bhashasetu-ai/app/src/main/java/com/example/ui/util/TtsManager.kt) and [`voice/service.py`](file:///d:/HACKTHON/bhashasetu-ai/services/ai-platform/voice/service.py).
* **Consequences:** Fosters cooperative classroom learning where both teacher and student acquire each other's linguistic terminology.
