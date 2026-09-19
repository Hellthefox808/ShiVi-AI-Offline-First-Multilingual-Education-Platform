# 10 — C4 ARCHITECTURE MODEL: LEVEL 3 COMPONENT BREAKDOWN

> **Document ID:** `BS-ARCH-10-C4-COMPONENT`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** C4 Model for Visualizing Software Architecture (Level 3)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Web Backend Component Diagram (`services/web-backend/`)

```mermaid
C4Component
    title Component Diagram for Web Backend Gateway (NestJS 11)

    Container_Boundary(backend, "Web Backend Gateway") {
        Component(auth_ctrl, "AuthController", "NestJS Controller", "Handles /api/v1/auth/login, refresh, and session discovery.")
        Component(auth_svc, "AuthService", "NestJS Injectable", "Executes Argon2id password verification and signs JWT tokens.")
        Component(rls_guard, "RlsInterceptor & RolesGuard", "NestJS Guard", "Extracts school_id and sets PostgreSQL session variable.")

        Component(lesson_ctrl, "LessonsController", "NestJS Controller", "Endpoints for lesson CRUD, publishing, and review workflows.")
        Component(lesson_svc, "LessonsService", "NestJS Injectable", "Orchestrates lesson state machine and triggers AI requests.")

        Component(sync_ctrl, "SyncController", "NestJS Controller", "Handles /api/v1/sync/push and /api/v1/sync/pull.")
        Component(sync_svc, "SyncService", "NestJS Injectable", "Reconciles outbox operations, detects conflicts, updates state.")

        Component(ai_client, "AiOrchestrationClient", "NestJS Injectable", "Dispatches gRPC / HTTP requests to AI platform.")
        Component(bull_queue, "OfflinePackQueueService", "BullMQ Producer", "Enqueues asynchronous zip packaging tasks in Redis.")
    }

    ContainerDb(pg, "PostgreSQL 18", "Relational Database", "School tables with RLS.")
    ContainerDb(redis, "Redis 7.4", "Queue & Cache", "BullMQ background jobs.")
    Container(ai, "AI Platform", "FastAPI Service", "Inference endpoints.")

    Rel(auth_ctrl, auth_svc, "Invokes login")
    Rel(auth_svc, pg, "Queries users table")
    Rel(lesson_ctrl, rls_guard, "Guarded by RBAC & RLS")
    Rel(lesson_ctrl, lesson_svc, "Invokes business use cases")
    Rel(lesson_svc, ai_client, "Dispatches scaffolding request")
    Rel(ai_client, ai, "HTTP/2 POST /api/v1/ai/generate-lesson")
    Rel(lesson_svc, pg, "Persists lesson record")
    Rel(lesson_svc, bull_queue, "Dispatches packaging job")
    Rel(bull_queue, redis, "Enqueues task")
    Rel(sync_ctrl, sync_svc, "Passes sync batch")
    Rel(sync_svc, pg, "Applies idempotent transaction")
```

---

## 2. AI Platform Component Diagram (`services/ai-platform/`)

```mermaid
C4Component
    title Component Diagram for AI Platform Engine (FastAPI + Python 3.12)

    Container_Boundary(ai_platform, "AI Platform Engine") {
        Component(main_api, "FastAPI Endpoints", "main.py", "Exposes REST endpoints for RAG, MT, Voice, QE, and Bundles.")
        Component(rag_mod, "Hybrid RAG Engine", "rag/engine.py", "Fuses BM25 lexical search with BGE-M3 dense vector search.")
        Component(lang_prov, "Language Provider", "translation/providers/", "Translates concepts into Santhali (Ol Chiki), Ho, Mundari.")
        Component(ped_adapt, "Pedagogical Adapter", "pedagogy/adapter.py", "Injects Sarhul/Sal tree analogies and simplifies grade text.")
        Component(qe_eval, "Quality Evaluator", "quality/evaluator.py", "Executes COMETKiwi-XXL reference-free scoring.")
        Component(voice_pipe, "Voice Pipeline", "voice/service.py", "Orchestrates VAD, Whisper ASR, and Kokoro-82M TTS.")
        Component(unif_pipe, "Unified Pipeline", "pipeline.py", "Chains all 7 stages into an atomic synthesis workflow.")
    }

    ContainerDb(pg_vec, "PostgreSQL pgvector", "DiskANN Index", "Stores 1024-dim BGE-M3 embeddings.")

    Rel(main_api, unif_pipe, "Executes master pipeline")
    Rel(unif_pipe, rag_mod, "1. Retrieves textbook evidence")
    Rel(rag_mod, pg_vec, "Executes cosine similarity query")
    Rel(unif_pipe, lang_prov, "2. Translates to tribal language")
    Rel(unif_pipe, ped_adapt, "3. Injects cultural analogies")
    Rel(unif_pipe, qe_eval, "4. Assesses COMET translation quality")
    Rel(main_api, voice_pipe, "Executes live voice relay")
```

---

## 3. Android Mobile Edge App Component Diagram (`app/`)

```mermaid
C4Component
    title Component Diagram for Android Edge App (Kotlin + Compose)

    Container_Boundary(android_app, "Android Mobile Edge Application") {
        Component(viewmodels, "UI ViewModels", "Jetpack ViewModel", "Manages UI state flows: LessonViewModel, VoiceViewModel, QuizViewModel.")
        Component(tts_mgr, "TtsManager", "util/TtsManager.kt", "Routes tribal phonetics via Devanagari hi-IN acoustic engine at 0.72x speed.")
        Component(stt_mgr, "SpeechToTextManager", "util/SpeechToTextManager.kt", "Captures microphone audio, executes Silero VAD, emits RMS dB.")
        Component(repos, "Domain Repositories", "domain/repository/", "Encapsulates data access and coordinates offline outbox writes.")
        Component(room_db, "AppDatabase", "data/local/AppDatabase.kt", "Room SQLite database containing 9 entities and DAOs.")
        Component(sync_worker, "SyncWorker", "WorkManager", "Background job that monitors network and executes idempotent sync push/pull.")
    }

    Rel(viewmodels, tts_mgr, "Triggers bilingual relay audio")
    Rel(viewmodels, stt_mgr, "Starts microphone streaming")
    Rel(viewmodels, repos, "Fetches lessons and records quiz scores")
    Rel(repos, room_db, "Executes ACID SQLite operations")
    Rel(repos, room_db, "Appends operation to OutboxDao")
    Rel(sync_worker, room_db, "Pulls pending outbox rows")
```
