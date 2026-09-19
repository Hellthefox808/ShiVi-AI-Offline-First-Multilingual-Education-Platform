# 04 — PRODUCT REQUIREMENTS DOCUMENT (PRD) & SCOPE BOUNDARY

> **Document ID:** `BS-ARCH-04-PRD`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Canonical Source Reference:** [`docs/PRD.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/PRD.md) & [`docs/FSD.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/FSD.md)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Executive Product Vision

BhashaSetu AI provides an end-to-end cognitive bridge that transforms state-standardized primary education into culturally resonant, linguistically grounded tribal mother-tongue scaffolding. It empowers educators with real-time translation, offline curriculum delivery, automated formative evaluation, and verified pedagogical fidelity.

---

## 2. 30 Discrete Functional Domains Catalog

The functional capabilities of BhashaSetu AI are organized into 30 discrete domains:

| Domain ID | Functional Domain | Key Actor | Core Capability | Priority | Codebase Implementation |
|---|---|---|---|---|---|
| **F01** | Identity & Authentication | All | Argon2id hashing, JWT (15m), secure refresh | **P0** | `services/web-backend/src/auth/` |
| **F02** | Role-Based Access Control | All | Multi-tier RBAC (`TEACHER`, `ADMIN`, `LINGUIST`) | **P0** | `services/web-backend/src/auth/guards/` |
| **F03** | Tenant / School Scoping | Admin / Teacher | PostgreSQL RLS scoping by `school_id` | **P0** | `services/web-backend/src/common/rls/` |
| **F04** | Curriculum Hierarchy | Admin / Teacher | State-prescribed JCERT Grades 1–5 taxonomy | **P0** | `services/web-backend/src/curriculum/` |
| **F05** | Learning Outcomes (LO) | Teacher / Admin | NIPUN Bharat FLN competency mapping | **P0** | `services/ai-platform/rag/engine.py` |
| **F06** | Lesson Studio | Teacher | Multi-step curriculum scaffolding editor | **P0** | `apps/web-frontend/app/(dashboard)/lessons/` |
| **F07** | Language Identification | System / Teacher | Acoustic/text detection of Hindi vs tribal tongues | **P0** | `services/ai-platform/translation/` |
| **F08** | Curriculum Translation | System | Grounded translation into Santhali, Ho, Mundari | **P0** | `services/ai-platform/translation/providers/` |
| **F09** | Transliteration Engine | System / Teacher | Native Ol Chiki/Warang Chiti + Devanagari guide | **P0** | `services/ai-platform/translation/` |
| **F10** | Pedagogical Adaptation | System | Injects Sarhul/Sohrai cultural metaphors | **P0** | `services/ai-platform/pedagogy/adapter.py` |
| **F11** | Hybrid Multilingual RAG | System | BM25 + BGE-M3 + DiskANN vector retrieval | **P0** | `services/ai-platform/rag/engine.py` |
| **F12** | Live Voice-to-Voice Relay | Teacher / Student | Sub-3s streaming classroom speech relay | **P0** | `services/ai-platform/voice/service.py` |
| **F13** | Speech Synthesis (TTS) | System / Teacher | Kokoro-82M / Piper tribal audio generation | **P0** | `app/src/main/java/.../TtsManager.kt` |
| **F14** | Bilingual Worksheets | Teacher / Student | Automated printable PDF worksheet generator | **P0** | `services/web-backend/src/lessons/` |
| **F15** | Visual Flashcards | Teacher / Student | Dual-script flashcards with audio trigger | **P0** | `app/src/main/java/.../FlashcardDao.kt` |
| **F16** | Teacher Review & Approval | Teacher / Reviewer | HITL quality review console with COMET scores | **P0** | `apps/web-frontend/features/lesson-studio/` |
| **F17** | Content Publishing | Teacher / Admin | Cryptographically signed, immutable releases | **P0** | `services/web-backend/src/lessons/` |
| **F18** | Student Learning Delivery | Student / Teacher | Offline classroom presentation on tablet | **P0** | `app/src/main/java/.../LessonScreen.kt` |
| **F19** | Formative Assessment | Student / Teacher | Deterministic interactive quizzes | **P0** | `app/src/main/java/.../QuizScreen.kt` |
| **F20** | Progress & FLN Tracking | Teacher / Admin | Append-only student competency tracking | **P0** | `app/src/main/java/.../AssessmentDao.kt` |
| **F21** | Offline Content Packs | Teacher / Operator | Bundled zip archives containing lessons + audio | **P0** | `services/ai-platform/main.py` |
| **F22** | Local Offline Database | Mobile Device | Room / SQLite 3 database with 9 DAOs | **P0** | `app/src/main/java/.../AppDatabase.kt` |
| **F23** | Durable Outbox Sync | System | Gzip-compressed, UUID idempotent push/pull | **P0** | `services/web-backend/src/sync/` |
| **F24** | Conflict Resolution | System | Deterministic policies (Append-only, Immutable) | **P0** | `services/web-backend/src/sync/conflict/` |
| **F25** | Device Inventory Mgmt | Admin / Operator | Tablet hardware health & sync status | **P1** | `services/web-backend/src/devices/` |
| **F26** | Administrative Analytics | District Admin | School-level FLN attainment heatmaps | **P0** | `apps/web-frontend/app/(dashboard)/admin/` |
| **F27** | Audit & Provenance | Security Admin | Cryptographic audit trail of all AI generations | **P0** | `services/web-backend/src/common/audit/` |
| **F28** | Notification Dispatcher | Teacher / Admin | In-app alerts for sync status & new content | **P1** | `apps/web-frontend/features/notifications/` |
| **F29** | AI Quality Monitoring | AI Engineer | Real-time tracking of COMET and latency metrics | **P0** | `services/ai-platform/quality/evaluator.py` |
| **F30** | Operational Health | DevOps / SRE | Microservice readiness & liveness probes | **P0** | `services/ai-platform/main.py:health_check` |

---

## 3. Scope Boundaries: In-Scope vs. Out-of-Scope

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SCOPE BOUNDARY MATRIX                            │
├──────────────────────────────────────┬──────────────────────────────────────┤
│ IN-SCOPE (Explicitly Delivered)      │ OUT-OF-SCOPE (Explicitly Excluded)   │
├──────────────────────────────────────┼──────────────────────────────────────┤
│ • Primary education: Grades 1–5      │ • Secondary / Higher Education       │
│ • Target Languages: Santhali, Ho,    │   (Grades 6–12, University)          │
│   Mundari, Hindi, English            │ • Languages outside Austroasiatic/   │
│ • Offline tablet classroom delivery  │   Jharkhand tribal sphere            │
│ • Deterministic interactive quizzes  │ • Synchronous video conferencing /   │
│ • Spoken bilingual audio relay       │   virtual classrooms (high bandwidth)│
│ • Printable dual-language worksheets │ • Autonomous unreviewed AI lesson    │
│ • School-level administrative sync   │   publishing (forbidden by safety)   │
│ • Hardware-optimized for 2 GB RAM    │ • Hardware procurement or device MDM │
│   Android tablets                    │   firmware flashing                  │
└──────────────────────────────────────┴──────────────────────────────────────┘
```

---

## 4. End-to-End Functional Traceability Summary

Per [`docs/FTL.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/FTL.md), all 16 SIH problem statement requirements map directly to functional domains, software modules, API contracts, automated unit/integration tests, and runtime telemetry benchmarks with **Tier E4 (Measured Runtime on Target Hardware)** evidence and **100.0% coverage**.
