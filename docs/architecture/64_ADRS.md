# 64 — CONSOLIDATED MASTER ARCHITECTURAL DECISION RECORD (ADR) INDEX

> **Document ID:** `BS-ARCH-64-ADR-INDEX`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Source Reference:** [`docs/architecture/06_ARCHITECTURE_DECISION_RECORDS.md`](file:///d:/HACKTHON/bhashasetu-ai/docs/architecture/06_ARCHITECTURE_DECISION_RECORDS.md)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Master Architectural Decision Register (ADR-001 to ADR-015)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CONSOLIDATED ADR INDEX                            │
├─────────┬──────────────────────────────────────────┬───────────┬────────────┤
│ ADR ID  │ Title & Architectural Decision           │ Status    │ Validated  │
├─────────┼──────────────────────────────────────────┼───────────┼────────────┤
│ ADR-001 │ Decoupled Polyglot Monorepo Architecture │ APPROVED  │ Verified   │
│ ADR-002 │ NestJS 11 for Enterprise Gateway & Core  │ APPROVED  │ Verified   │
│ ADR-003 │ FastAPI + Python 3.12 for AI Engine      │ APPROVED  │ Verified   │
│ ADR-004 │ PostgreSQL 18 with pgvector Unified Store│ APPROVED  │ Verified   │
│ ADR-005 │ StreamingDiskANN (pgvectorscale) Indexing│ APPROVED  │ Verified   │
│ ADR-006 │ Local-First Android SQLite (Room) DB     │ APPROVED  │ Verified   │
│ ADR-007 │ UUID-Idempotent Outbox Synchronization   │ APPROVED  │ Verified   │
│ ADR-008 │ Contract-First OpenAPI 3.1 Schemas       │ APPROVED  │ Verified   │
│ ADR-009 │ Redis 7.4 & BullMQ Asynchronous Queues   │ APPROVED  │ Verified   │
│ ADR-010 │ Provider Abstraction for Tribal MT Models│ APPROVED  │ Verified   │
│ ADR-011 │ COMETKiwi Reference-Free Quality Gate    │ APPROVED  │ Verified   │
│ ADR-012 │ On-Device Silero VAD for Low Latency     │ APPROVED  │ Verified   │
│ ADR-013 │ Bilingual Relay Audio Delivery Pattern   │ APPROVED  │ Verified   │
│ ADR-014 │ Next.js 16.3 App Router for Web Studio   │ APPROVED  │ Verified   │
│ ADR-015 │ Deterministic On-Device Assessment Engine│ APPROVED  │ Verified   │
└─────────┴──────────────────────────────────────────┴───────────┴────────────┘
```

---

## 2. ADR Lifecycle Governance

1. **Proposed**: Drafted by an engineer; discussed in pull request review.
2. **Approved**: Signed off by Lead Architect (`@Hellthefox808`) and tested in code.
3. **Superseded**: Replaced by a subsequent ADR if requirements or technology evolves.
4. **Rejected**: Evaluated and discarded with documented negative rationale.
