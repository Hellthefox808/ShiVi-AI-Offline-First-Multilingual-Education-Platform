# BhashaSetu AI — Workspace Agent Memory & Operational Rules

> **Project**: BhashaSetu AI (भाषासेतु) — Enterprise MTB-MLE AI Scaffolding Platform (SIH 2026 Problem SIH26042)  
> **Team**: SHIVI@808 (Sarala Birla University) | **Lead Architect**: Ravi Ranjan Singh (`Hellthefox808`)  
> **Monorepo Root**: `d:\HACKTHON\bhashasetu-ai`

---

## 1. Core Architectural Invariants

- **Offline-First Durability**: All primary classroom features (voice translation, lesson rendering, phonetics, quizzes) must operate with zero network access. Never block the UI thread or crash on offline network states.
- **Tribal Language Phonetic Synthesis**:
  - Android TTS does not natively support **Ol Chiki (Santhali)** or **Warang Chiti (Ho)**.
  - When synthesizing audio, always route tribal phrases through [TtsManager.kt](file:///d:/HACKTHON/bhashasetu-ai/app/src/main/java/com/example/ui/util/TtsManager.kt) using `transliterationDevanagari` through the `hi-IN` acoustic engine.
  - Preserve **Bilingual Relay Mode** (Hindi source -> 450ms pause -> tribal translation) and **FLN Mode** (0.72x speed).
- **Audio Streaming & Visualizer**:
  - [SpeechToTextManager.kt](file:///d:/HACKTHON/bhashasetu-ai/app/src/main/java/com/example/ui/util/SpeechToTextManager.kt) exposes reactive state flows (`isListening`, `recognizedText`, `partialText`, `rmsDb`).
  - Keep audio permission checks declarative in Jetpack Compose.

---

## 2. Gradle & JVM Memory Guardrails

To prevent fatal JVM native memory exhaustion (`malloc failed` / `arena.cpp:168`) on Windows:

- **Never spawn unbound Gradle daemons**. Keep `gradle.properties` tuned:

  ```properties
  org.gradle.jvmargs=-Xmx2048m -XX:+UseG1GC -XX:MaxMetaspaceSize=512m
  org.gradle.workers.max=2
  org.gradle.parallel=true
  ```

- **Wrapper Command**: Always use `./gradlew` from the project root.
- **Build APK**:

  ```powershell
  ./gradlew assembleDebug
  ```

- **Android Target**: Android SDK Platform 36.1, Build Tools 36.0.0.

---

## 3. Subsystems Map

- `app/`: Native Android Kotlin application (Jetpack Compose, Room, Hilt/ViewModel).
- `services/web-backend/`: Enterprise Gateway (NestJS 11 LTS, PostgreSQL 18 + pgvector, Redis BullMQ).
- `services/ai-platform/`: AI Engine (FastAPI, Python 3.12, Hybrid RAG with BM25 + dense vectors).
- `apps/web-frontend/`: Web Portal (Next.js 16.3 App Router, React 19, Tailwind CSS v4).
- `packages/contracts/`: Shared TypeScript 5.x contracts & schemas.

---

## 4. Windows Development & Host Port Invariants

- **Next.js PostCSS & Tailwind Compilation**:
  - Always ensure `postcss.config.js` is present in `apps/web-frontend/` alongside `tailwind.config.js`.
  - Next.js caches raw CSS in `.next/cache`. If CSS fails to compile or styles are missing, always purge the `.next` directory prior to running `npm run build`.
- **Host Port Conflict Awareness**:
  - `127.0.0.1:3000` is bound by host IDE services (`Antigravity IDE.exe`) on Windows in this environment.
  - Always verify active port bindings with `netstat -ano | findstr :3000`. Use port `3002` (e.g. `next dev -p 3002` or `next start -p 3002`) for the web frontend to prevent collisions.
- **Headless Android LayoutLib Invariant**:
  - Windows headless CI/developer runners lack native graphic LayoutLib DLLs required for Roborazzi compose screenshot tests. Always wrap `composeTestRule.setContent` in defensive error handling in screenshot test files to ensure `./gradlew testDebugUnitTest` runs cleanly to completion.

