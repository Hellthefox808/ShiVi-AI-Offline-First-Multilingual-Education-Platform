# 01 — PROJECT OVERVIEW: BHASHASETU AI (भाषासेतु)

> **Document ID:** `BS-ARCH-01-OVERVIEW`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Team:** SHIVI@808 (Sarala Birla University) | **Lead Architect:** Ravi Ranjan Singh (`Hellthefox808`)  
> **Monorepo Root:** `d:\HACKTHON\bhashasetu-ai`  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Executive Mission & Problem Statement

In the primary schools of tribal belts across Jharkhand (Dumka, West Singhbhum, Ranchi, Gumla, Khunti), over 70% of entering Grade 1 students speak Austroasiatic or Dravidian indigenous languages at home—predominantly **Santhali (Santali / ᱥᱟᱱᱛᱟᱲᱤ)**, **Ho (ᱦᱚᱺ ᱡᱟᱜᱟᱨ)**, and **Mundari (Munda / ᱢᱩᱱᱰᱟᱨᱤ)**. Conversely, government-appointed primary school educators predominantly speak standard **Hindi** and lack pedagogical fluency in native tribal idioms or native scripts (**Ol Chiki** for Santhali, **Warang Chiti** for Ho, and regional Devanagari variants for Mundari).

This socio-linguistic disconnect induces immediate cognitive alienation, precipitating catastrophic classroom attrition:
1. **Severe Early-Grade Dropout**: A steep drop in foundational literacy and numeracy (FLN) attainment before Grade 3.
2. **Pedagogical Paralyzation**: Teachers struggle to explain core conceptual curricula (JCERT/NCERT science, environmental studies, and mathematics) without common linguistic referents.
3. **Severe Hardware & Infrastructure Deficit**: Rural schools operate in deep media deserts characterized by intermittent or nonexistent 2G/3G connectivity, frequent electrical brownouts, and low-cost government-subsidized Android hardware ($\sim 2\text{ GB}$ RAM, ARM64/ARMv7, Android 9–13).

**BhashaSetu AI (भाषासेतु)** is an enterprise-grade, offline-first, Mother-Tongue-Based Multilingual Education scaffolding, translation, and pedagogical delivery ecosystem. It acts as an ambient cognitive bridge, empowering non-native Hindi teachers to synthesize state-standardized educational scaffolding into tribal mother tongues while enforcing strict curriculum grounding, human-in-the-loop pedagogical review, deterministic offline evaluation, and zero-data-loss synchronization.

---

## 2. Core Operational Constraints & Invariants

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CORE ARCHITECTURAL INVARIANTS                         │
├────────────────────────────────┬────────────────────────────────────────────────┤
│ 1. Zero-Network Durability     │ 100% of classroom features (voice translation, │
│                                │ lesson rendering, quizzes, student attempts)   │
│                                │ must operate indefinitely with zero network.   │
├────────────────────────────────┼────────────────────────────────────────────────┤
│ 2. Hardware Resource Envelope  │ Execution target is an entry-level Android     │
│                                │ tablet (2 GB RAM, 16-32 GB eMMC, Quad-core     │
│                                │ ARM @ 1.3-2.0 GHz). Peak heap < 192 MB.        │
├────────────────────────────────┼────────────────────────────────────────────────┤
│ 3. Tribal Script Fidelity      │ Native Unicode rendering for Ol Chiki (U+1C50- │
│                                │ U+1C7F) and Warang Chiti (U+118A0-U+118FF)     │
│                                │ paired with phonetic Devanagari transliteration│
│                                │ for non-native educator comprehension.          │
├────────────────────────────────┼────────────────────────────────────────────────┤
│ 4. Sub-3-Second Voice Relay    │ End-to-end spoken bilingual classroom relay     │
│                                │ latency <= 3000 ms (VAD -> ASR -> MT -> TTS).   │
├────────────────────────────────┼────────────────────────────────────────────────┤
│ 5. Human-in-the-Loop Gate     │ No AI-generated curriculum asset may be         │
│                                │ published or synchronized without verified     │
│                                │ teacher review or certified quality evaluation.│
└────────────────────────────────┴────────────────────────────────────────────────┘
```

---

## 3. Technology Stack & Subsystem Matrix

| Subsystem Layer | Primary Stack / Engine | Repository Path | Core Responsibilities |
|---|---|---|---|
| **Mobile Edge Client** | Android Native (Kotlin 2.0, Jetpack Compose, Room SQLite, Hilt) | `app/` & `apps/mobile/` | Offline-first classroom UI, Silero VAD, local SQLite DAOs, durable sync outbox, ExoPlayer audio playback, local phonetic rendering. |
| **Web Portal & Studio** | Next.js 16.3 (App Router), React 19.2, Tailwind CSS v4, Radix UI, TanStack Query v5 | `apps/web-frontend/` | Desktop Lesson Studio, Ol Chiki visual typography canvas, District Admin dashboards, NIPUN Bharat FLN tracking, HITL review console. |
| **API Gateway & Core** | NestJS 11 LTS, Node.js 22 LTS, TypeScript 5.x, TypeORM, BullMQ, Redis 7.4 | `services/web-backend/` | Central domain orchestrator, JWT/Argon2id authentication, Multi-tenant Row-Level Security (RLS), sync push/pull reconciliation, OpenAPI 3.1 contract engine. |
| **AI / ML Platform** | FastAPI, Python 3.12, PyTorch, Hugging Face Transformers, LangChain Core | `services/ai-platform/` | Hybrid RAG (BM25 + BGE-M3 + pgvector DiskANN), NLLB-200 / Gemini 3.1 MT, Whisper ASR, Kokoro-82M / Piper TTS, COMETKiwi / XCOMET quality evaluation. |
| **Persistence & Cache** | PostgreSQL 18 + pgvector / pgvectorscale, Redis 7.4-Alpine, S3 / MinIO | `infra/` | System-of-record relational store, DiskANN vector graph index, distributed queue buffer, signed offline asset pack repository. |
| **Shared Contracts** | TypeScript 5.x, OpenAPI 3.1, JSON Schema | `packages/contracts/` | Canonical type definitions, API payload schemas, sync event envelopes, cross-boundary DTO generators. |

---

## 4. Monorepo Structural Blueprint

```text
d:\HACKTHON\bhashasetu-ai/
├── app/                               # Android Native Application (Kotlin + Compose)
│   ├── build.gradle.kts               # Target SDK 36.1, Build Tools 36.0.0
│   └── src/main/java/com/example/
│       ├── data/local/                # Room SQLite Database, 9 Entities, DAOs
│       ├── domain/                    # Clean Architecture Repositories & Use Cases
│       └── ui/                        # Jetpack Compose Screens, ViewModels, TtsManager
├── apps/
│   ├── mobile/                        # Cross-platform client modules (Flutter / Compose)
│   └── web-frontend/                  # Next.js 16.3 App Router Portal (Port 3002)
│       ├── app/                       # Routes: (auth), (dashboard), lesson-studio
│       ├── features/                  # Domain components, audio streaming visualizers
│       └── lib/api/                   # TanStack Query hooks generated from OpenAPI
├── services/
│   ├── web-backend/                   # NestJS 11 Gateway & Enterprise Core (Port 3001)
│   │   └── src/                       # Modules: auth, curriculum, lessons, sync, common
│   └── ai-platform/                   # FastAPI + Python 3.12 AI Engine (Port 8000)
│       ├── rag/                       # BGE-M3 Dense + BM25 Lexical + DiskANN engine
│       ├── translation/               # NLLB-200, Bhashini, Gemini Provider Adapters
│       ├── voice/                     # Silero VAD, Whisper ASR, Kokoro/Piper TTS
│       └── quality/                   # COMETKiwi Reference-Free Quality Evaluator
├── packages/
│   └── contracts/                     # Shared TypeScript DTOs & Validation Schemas
├── infra/
│   ├── docker-compose.yml             # Local multi-container mesh (6 services)
│   ├── k8s/                           # Production Kubernetes manifests (HPA, StatefulSet)
│   └── nginx/                         # Reverse Proxy & SSL Gateway configuration
└── docs/
    ├── PRD.md, TAD.md, SAD.md, FSD.md # Core Living Engineering Specifications
    └── architecture/                  # 66-Document Master Architecture Library
```

---

## 5. Primary Actor Personas

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                SYSTEM ACTORS                                    │
├───────────────────┬──────────────────────────────┬──────────────────────────────┤
│ Actor             │ Profile                      │ Key System Touchpoint        │
├───────────────────┼──────────────────────────────┼──────────────────────────────┤
│ 1. Primary School │ Non-native Hindi speaker,    │ Android Edge App: Voice Relay│
│    Teacher        │ operates in offline village. │ & Lesson Delivery. Web:      │
│    (e.g., Ramesh) │ Basic smartphone literacy.   │ Lesson Studio creation.      │
├───────────────────┼──────────────────────────────┼──────────────────────────────┤
│ 2. Tribal Student │ Grade 1-5 child, native      │ Android Tablet: Interactive  │
│    (e.g., Birsa)  │ Santhali/Ho speaker.         │ bilingual quizzes, audio     │
│                   │ Emerging Hindi vocabulary.   │ flashcards, worksheets.      │
├───────────────────┼──────────────────────────────┼──────────────────────────────┤
│ 3. Native Tribal  │ Certified linguist, fluent   │ Web Portal: HITL Review      │
│    Linguist       │ in Ol Chiki / Warang Chiti   │ Queue, glossary curation,    │
│    Reviewer       │ & regional tribal idioms.    │ COMET error span auditing.   │
├───────────────────┼──────────────────────────────┼──────────────────────────────┤
│ 4. Block / State  │ Government education officer │ Web Portal: District FLN     │
│    Education Admin│ monitoring NIPUN Bharat      │ analytics, sync telemetry,   │
│    (BPO / DPO)    │ compliance across schools.   │ tablet inventory deployment. │
└───────────────────┴──────────────────────────────┴──────────────────────────────┘
```

---

## 6. Target Geographical & Linguistic Boundary

- **Pilot Region**: State of Jharkhand, India (districts of Dumka, Pakur, West Singhbhum, East Singhbhum, Khunti, Ranchi, Saraikela-Kharsawan).
- **Primary Indigenous Languages**:
  1. **Santhali (Santali / ᱥᱟᱱᱛᱟᱲᱤ)**: ISO 639-3 `sat`, written natively in **Ol Chiki** (`sat_Olck`), transliterated to Devanagari (`sat_Deva`) and Latin (`sat_Latn`).
  2. **Ho (ᱦᱚᱺ ᱡᱟᱜᱟᱨ)**: ISO 639-3 `hoc`, written natively in **Warang Chiti** (`hoc_Wara`), transliterated to Devanagari (`hoc_Deva`).
  3. **Mundari (ᱢᱩᱱᱰᱟᱨᱤ)**: ISO 639-3 `unr`, written natively in regional Devanagari (`unr_Deva`) and historic scripts.
- **Reference Standard Source Language**: Hindi (`hin_Deva`), English (`eng_Latn`) for administrative metadata.
- **Curriculum Alignment**: Jharkhand Council of Educational Research and Training (JCERT) primary curriculum standards for Grades 1 through 5, cross-mapped to National Education Policy (NEP 2020) and NIPUN Bharat Foundational Literacy and Numeracy benchmarks.
