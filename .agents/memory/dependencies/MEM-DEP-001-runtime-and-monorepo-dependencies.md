# MEM-DEP-001: Monorepo Runtime & Toolchain Dependencies

- **ID**: `MEM-DEP-001`
- **TYPE**: `DEPENDENCY`
- **TITLE**: Master Monorepo Runtime Dependencies & Toolchain Matrix
- **PROJECT**: `BhashaSetu-AI`
- **SOURCE**: [package.json](file:///d:/HACKTHON/bhashasetu-ai/package.json), [app/build.gradle.kts](file:///d:/HACKTHON/bhashasetu-ai/app/build.gradle.kts)
- **CREATED_AT**: 2026-09-16T10:00:00+05:30
- **UPDATED_AT**: 2026-09-16T10:00:00+05:30
- **CONFIDENCE**: 1.0 (VERIFIED)
- **STATUS**: ACTIVE
- **TAGS**: `["dependencies", "versions", "toolchain", "gradle", "node", "python"]`
- **RELATED_COMPONENTS**: `["all"]`
- **RELATED_DECISIONS**: `["MEM-ARCH-001"]`
- **EXPIRY / STALENESS SIGNAL**: Stable
- **PROVENANCE**: Monorepo Dependency Audit

---

## CONTENT

| Subsystem | Primary Runtime | Key Packages / Libraries | Build Command |
| :--- | :--- | :--- | :--- |
| **Android Client** | Kotlin 2.1 / JVM 17/21 | Jetpack Compose, Material3, Room 2.6, Android SpeechRecognizer, TextToSpeech | `./gradlew assembleDebug` |
| **Web Backend** | Node.js 22 LTS | NestJS 11, TypeScript 5.7, class-validator, RxJS, axios, Swagger | `npm run build` |
| **AI Platform** | Python 3.12 | FastAPI 0.115, Uvicorn 0.32, Pydantic 2.10, NumPy 1.26 | `uvicorn main:app --port 8000` |
| **Web Frontend** | Node.js 22 LTS | Next.js 15.1, React 19.0, Tailwind CSS 3.4 / v4, Lucide React | `npm run build` |
| **Infrastructure** | Docker 24+ | PostgreSQL 16+ (pgvector), Redis 7.4-alpine, NGINX alpine | `docker compose -f infra/docker-compose.yml up` |
