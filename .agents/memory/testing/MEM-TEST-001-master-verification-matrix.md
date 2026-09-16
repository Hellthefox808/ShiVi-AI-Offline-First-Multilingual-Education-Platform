# MEM-TEST-001: Master Verification & Benchmarking Matrix

- **ID**: `MEM-TEST-001`
- **TYPE**: `TESTING`
- **TITLE**: Master Verification Suite, Infra Tests, and Hybrid RAG Benchmarks
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [tests/verify_all.py](file:///d:/HACKTHON/bhashasetu-ai/tests/verify_all.py), [tests/test_infra.py](file:///d:/HACKTHON/bhashasetu-ai/tests/test_infra.py), [tests/benchmark_rag.py](file:///d:/HACKTHON/bhashasetu-ai/tests/benchmark_rag.py)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["testing", "verification", "rag-benchmark", "infra-tests", "sla"]`
- **RELATED_COMPONENTS**: `["tests/verify_all.py", "tests/test_infra.py", "tests/benchmark_rag.py"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Permanent test baseline
- **PROVENANCE**: Team SHIVI@808 QA Architecture

---

## CONTENT

### Test Execution Commands
1. **Master Subsystem Verification (13 Tests)**:
   ```powershell
   python tests/verify_all.py
   ```
   - Verifies: JCERT 15-node knowledge base, fine-tuned hybrid RAG, authentic tribal scripts, language code aliases, cultural analogies, COMET quality evaluator (0.954 score), voice latency (1855ms <= 3000ms SLA), outbox idempotency, 9 live FastAPI endpoints, and 9 NestJS domain modules.
   - Result: 13/13 passing in 0.048s.

2. **Infrastructure & Kubernetes Verification (5 Tests)**:
   ```powershell
   python tests/test_infra.py
   ```
   - Verifies: Dockerfiles, Docker Compose mesh, 11 Kubernetes manifests, HPA resource limits, NGINX reverse proxy.
   - Result: 5/5 passing in 0.011s.

3. **Hybrid RAG Benchmark (15 Queries)**:
   ```powershell
   python tests/benchmark_rag.py
   ```
   - Metrics: Recall@1 = 100.0%, Recall@3 = 100.0%, MRR = 1.0000, Avg Latency = 0.53ms.
   - Result: 15/15 passing.
