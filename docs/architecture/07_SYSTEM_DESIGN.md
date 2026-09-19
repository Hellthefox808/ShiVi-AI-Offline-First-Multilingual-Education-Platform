# 07 — SYSTEM DESIGN & ARCHITECTURAL ALTERNATIVES

> **Document ID:** `BS-ARCH-07-SYS-DESIGN`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Architecture Pattern:** Offline-First Edge $\leftrightarrow$ Cloud Gateway $\leftrightarrow$ AI Microservice Hybrid Mesh  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. System Topology Overview

BhashaSetu AI implements a **Decoupled Edge-Core Hybrid Architecture**. It combines an autonomous, offline-first mobile edge runtime operating in remote village classrooms with an enterprise cloud platform powering curriculum management, multi-lingual RAG indexing, human-in-the-loop review, and administrative intelligence.

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       BHASHASETU HYBRID TOPOLOGY                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  VILLAGE CLASSROOM (EDGE RUNTIME)                                           │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ Android Tablet (Kotlin Compose + Room SQLite + Silero VAD)            │  │
│  │ • 100% Offline Classroom UI       • Append-Only Student Attempt Log   │  │
│  │ • Local Phonetic Guides           • Durable UUID Outbox Queue         │  │
│  └───────────────────────────────────┬───────────────────────────────────┘  │
│                                      │ Opportunistic 2G/3G Delta Sync       │
│                                      ▼                                      │
│  ENTERPRISE CLOUD CLUSTER                                                   │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ NGINX Ingress Proxy (TLS 1.3, Rate Limiting, Static Asset CDN)        │  │
│  └───────────────────────────────────┬───────────────────────────────────┘  │
│                                      │                                      │
│         ┌────────────────────────────┴────────────────────────────┐         │
│         ▼ (HTTP / SSE / WS)                                       ▼         │
│  ┌───────────────────────────────┐           ┌───────────────────────────┐  │
│  │ Web Frontend (Next.js 16.3)   │           │ Web Backend (NestJS 11)   │  │
│  │ • Teacher Lesson Studio       │           │ • Auth & Multi-Tenant RLS │  │
│  │ • Linguist HITL Review Queue  │           │ • Sync Reconciliation     │  │
│  │ • Admin FLN Analytics Portal  │           │ • OpenAPI Contract Core   │  │
│  └───────────────────────────────┘           └─────────────┬─────────────┘  │
│                                                            │ (gRPC / HTTP/2)│
│                                                            ▼                │
│                                              ┌───────────────────────────┐  │
│                                              │ AI Platform (FastAPI)     │  │
│                                              │ • BGE-M3 + DiskANN RAG    │  │
│                                              │ • NLLB / Gemini MT Engine │  │
│                                              │ • Whisper ASR / Kokoro TTS│  │
│                                              │ • COMETKiwi Quality Gate  │  │
│                                              └─────────────┬─────────────┘  │
│                                                            │                │
│         ┌──────────────────────────────────────────────────┴─────┐          │
│         ▼                                                        ▼          │
│  ┌───────────────────────────────┐           ┌───────────────────────────┐  │
│  │ PostgreSQL 18 + pgvector      │           │ Redis 7.4 + BullMQ        │  │
│  │ • Relational School Data      │           │ • Offline Package Queue   │  │
│  │ • DiskANN Vector Graph Index  │           │ • AI Task Scheduling      │  │
│  │ • Immutable Audit Logs        │           │ • In-Memory Session Cache │  │
│  └───────────────────────────────┘           └───────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Architectural Alternatives Analysis

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ARCHITECTURAL TRADEOFF EVALUATION                        │
├───────────────────┬──────────────────────┬──────────────────────────────────┤
│ Pattern           │ Evaluated Tradeoffs  │ Selection Rationale              │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option A: Pure    │ • Zero edge infra    │ REJECTED: Rural schools have no  │
│ Cloud SaaS        │ • Simple updates     │ connectivity; completely fails   │
│ (Web-Only)        │ • Inoperable offline │ the primary problem mandate.     │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option B: Fat Edge│ • Fully autonomous   │ REJECTED: 2 GB RAM tablets cannot│
│ (All ML on Device)│ • Zero cloud cost    │ run 3B parameter multilingual MT │
│                   │ • High crash rate    │ or BGE-M3 models in memory.      │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option C: Hybrid  │ • Offline-first UI   │ SELECTED: Balances edge hardware │
│ Edge-Core Mesh    │ • Cloud heavy ML     │ constraints with cloud computing │
│ (Chosen)          │ • Resilient sync     │ power and persistent governance. │
└───────────────────┴──────────────────────┴──────────────────────────────────┘
```

---

## 3. Communication Protocols & Transport Matrix

| Channel | Source Subsystem | Destination Subsystem | Protocol | Serialization | Security & Authentication |
|---|---|---|---|---|---|
| **C1** | Web Frontend | Web Backend | HTTPS / WSS | JSON / UTF-8 | JWT Bearer (15m TTL) + SameSite Cookie |
| **C2** | Android Client | Web Backend (Sync) | HTTPS | Gzip JSON | JWT Bearer + Client Device UUID |
| **C3** | Web Backend | AI Platform | HTTP/2 (gRPC ready) | JSON / Protobuf | Internal mTLS / Network Mesh Isolation |
| **C4** | Android Client | AI Platform (Voice) | HTTPS / WSS | Opus Chunk / Base64 | Ephemeral Session Token |
| **C5** | Web Backend | PostgreSQL 18 | TCP (5432) | PostgreSQL Binary | SSL / TLS + Scoped Database Roles |
| **C6** | Web Backend / AI | Redis 7.4 | TCP (6379) | RESP3 Protocol | Redis Auth Password + Private Network |
