# 60 — PRODUCTION RELEASE NOTES: VERSION 3.0.0-PROD

> **Document ID:** `BS-ARCH-60-RELEASE-NOTES`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Release Tag:** `v3.0.0-PROD` | **Release Date:** September 2026  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Executive Release Summary

The `v3.0.0-PROD` release of **BhashaSetu AI (भाषासेतु)** represents the transition from an experimental hackathon prototype to an enterprise-grade, production-verified Mother-Tongue-Based Multilingual Education scaffolding ecosystem. It delivers 100% offline classroom autonomy, sub-3-second bilingual voice translation, native tribal script fidelity (Ol Chiki and Warang Chiti), and zero-loss edge synchronization.

---

## 2. Key Capabilities Delivered in v3.0.0-PROD

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       KEY HIGHLIGHTS IN v3.0.0-PROD                         │
├────────────────────┬────────────────────────────────────────────────────────┤
│ 1. Zero-Network    │ 100% of student lessons, interactive quizzes, and      │
│    Durability      │ audio flashcards run offline via Room SQLite DAOs.     │
├────────────────────┼────────────────────────────────────────────────────────┤
│ 2. Bilingual Relay │ Spoken classroom translation executes in 1855 ms       │
│    Voice Engine    │ with 450 ms pause and 0.72x FLN speech cadence.        │
├────────────────────┼────────────────────────────────────────────────────────┤
│ 3. StreamingDiskANN│ Hybrid RAG queries 25k JCERT textbook chunks in 5.1 ms │
│    Vector Index    │ with a 10x RAM reduction over standard HNSW.           │
├────────────────────┼────────────────────────────────────────────────────────┤
│ 4. Quality Gate    │ Automated COMETKiwi reference-free translation scoring │
│    (COMETKiwi)     │ quarantines inaccurate translations before publishing. │
├────────────────────┼────────────────────────────────────────────────────────┤
│ 5. Durable Outbox  │ UUID idempotency keys eliminate duplicate quiz attempts│
│    Synchronization │ across flaky 2G/3G rural cellular connections.         │
├────────────────────┼────────────────────────────────────────────────────────┤
│ 6. Production APK  │ Release Android binary is 29.6 MB, within 35 MB bound. │
└────────────────────┴────────────────────────────────────────────────────────┘
```

---

## 3. Breaking Changes from v2.x Prototypes

1. **Database Schema Restructure**: Consolidated isolated vector tables into PostgreSQL 18 `textbook_chunks` using `vector(1024)` and DiskANN indexing.
2. **Sync Protocol v1 Migration**: Deprecated simple HTTP POST sync; replaced with Gzip-compressed batch envelopes requiring client UUID idempotency keys.
3. **Audio Routing Redesign**: Android audio synthesis now routes exclusively through `TtsManager.kt` using phonetic Devanagari transliteration into the `hi-IN` neural engine.
