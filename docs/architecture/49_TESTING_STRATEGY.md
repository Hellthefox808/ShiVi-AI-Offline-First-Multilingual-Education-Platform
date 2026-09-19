# 49 — TESTING STRATEGY, TEST PYRAMID & COVERAGE GATES

> **Document ID:** `BS-ARCH-49-TEST-STRATEGY`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Test Automation Pyramid & Multi-Runtime Verification (§46 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Test Automation Pyramid & Sizing Distribution

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                          THE TESTING PYRAMID RATIO                          │
├────────────────────┬───────────────┬────────────────────────────────────────┤
│ Layer              │ Target Share  │ Scope & Execution Velocity             │
├────────────────────┼───────────────┼────────────────────────────────────────┤
│ 1. E2E System      │ 10% (50 Tests)│ Full browser & tablet emulator flows.  │
│    Tests           │               │ Slow execution (~10m in CI).           │
├────────────────────┼───────────────┼────────────────────────────────────────┤
│ 2. Integration &   │ 20% (100 Tests│ Database DAO tests, OpenAPI contract   │
│    Contract Tests  │               │ verification, Redis queue processing.  │
├────────────────────┼───────────────┼────────────────────────────────────────┤
│ 3. Fast Unit Tests │ 70% (350 Tests│ Business logic, state machines, VAD    │
│                    │               │ framing, COMET scoring formulas.       │
└────────────────────┴───────────────┴────────────────────────────────────────┘
```

---

## 2. Multi-Runtime Testing Frameworks Matrix

| Subsystem Layer | Primary Framework | Mocking & Test Drivers | Key Test Directory | Command to Execute |
|---|---|---|---|---|
| **Android Mobile** | JUnit 5 / Compose Test | MockK, AndroidX Runner | `app/src/test/` & `androidTest/` | `./gradlew testDebugUnitTest` |
| **Web Backend** | Jest / Supertest | ts-mockito, TypeORM in-memory | `services/web-backend/test/` | `npm run test:e2e` |
| **AI Platform** | PyTest | FastAPI TestClient, MockTorch | `services/ai-platform/test_platform.py` | `pytest test_platform.py -v` |
| **Web Frontend** | Jest / RTL / Playwright | MSW v2 (Mock Service Worker) | `apps/web-frontend/` | `npm test` & `npx playwright test` |

---

## 3. Automated Test Execution Commands

```powershell
# 1. Test Android Mobile Unit & ViewModels
./gradlew testDebugUnitTest

# 2. Test AI Platform Endpoints, RAG & Stress
cd services/ai-platform
pytest test_platform.py test_stress.py -v

# 3. Test Web Backend Gateway & Auth Guards
cd ../web-backend
npm test

# 4. Master Polyglot Test & Stress Runner
cd ../..
powershell -ExecutionPolicy Bypass -File tools/test-and-stress-all.ps1
```
