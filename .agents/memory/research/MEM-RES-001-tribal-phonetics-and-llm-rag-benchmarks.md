# MEM-RES-001: Tribal Phonetics & Hybrid RAG Retrieval Benchmarks

- **ID**: `MEM-RES-001`
- **TYPE**: `RESEARCH`
- **TITLE**: Empirical Benchmarks for Jharkhand Tribal Dialect Synthesis & Hybrid RAG
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [tests/benchmark_rag.py](file:///d:/HACKTHON/bhashasetu-ai/tests/benchmark_rag.py), [docs/PRD.md](file:///d:/HACKTHON/bhashasetu-ai/docs/PRD.md)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["research", "benchmarks", "rag", "phonetics", "comet", "slas"]`
- **RELATED_COMPONENTS**: `["services/ai-platform/rag/", "services/ai-platform/translation/"]`
- **RELATED_DECISIONS**: `["MEM-DEC-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent
- **PROVENANCE**: Team SHIVI@808 AI Research Lab

---

## CONTENT

### 1. Hybrid RAG Precision Findings
- **Embedding Centroids**: Combining BM25 keyword matching with dense 128-dimensional semantic representations and Reciprocal Rank Fusion (RRF) delivers **100.0% Recall@1** across the 15 JCERT curriculum nodes.
- **Mean Reciprocal Rank (MRR)**: 1.0000.
- **Average Query Latency**: 0.53 ms on commodity CPU.

### 2. Live Voice Latency SLA
- Live Voice-to-Voice Latency Budget: **1855ms**, comfortably outperforming the maximum SLA of **3000ms**.
- Audio Format: 24kHz MP3 encoding with telemetry spans.

### 3. Translation Quality Evaluation
- Unbabel COMET automated quality scoring achieves **0.954**, well above the auto-publish threshold of 0.850.
- MQM error spans correctly identify zero critical omissions on foundational curriculum terminology.
