# 58 — POLYGLOT CODING STANDARDS & FORBIDDEN ANTI-PATTERNS

> **Document ID:** `BS-ARCH-58-CODING-STD`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standards:** Kotlin Official, TypeScript Strict, PEP 8 / Python 3.12 (§76 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Kotlin & Jetpack Compose Coding Invariants

1. **State Hoisting**: Composables must remain stateless where possible. All UI state flows down from AAC ViewModels via Kotlin `StateFlow<T>`, and events flow up via lambda callbacks:
   ```kotlin
   // GOOD
   @Composable
   fun LessonCard(lesson: LessonUiModel, onApproveClick: () -> Unit) { ... }
   ```
2. **Strict Coroutine Threading**: All Room database DAOs and disk I/O operations must explicitly execute on `Dispatchers.IO`. Never perform disk or network operations on `Dispatchers.Main`.
3. **Defensive LayoutLib Wrapping**: Screenshot tests must wrap `composeTestRule.setContent` in `try/catch` to handle missing graphics DLLs on headless Windows runners.

---

## 2. TypeScript & NestJS / Next.js Invariants

1. **Zero `any` Types**: `tsconfig.json` enforces `strict: true` and `noImplicitAny: true`. Use explicit interfaces, generics, or `unknown` with runtime type narrowing.
2. **DTO Validation Pipes**: Every NestJS controller endpoint must validate incoming request bodies using class-validator decorators (`@IsString()`, `@IsUUID()`, `@IsNotEmpty()`).
3. **Server Components by Default**: Next.js 16 App Router components are React Server Components unless explicit user interaction or browser hooks (`useState`, `useEffect`) require `"use client"`.

---

## 3. Python 3.12 & FastAPI Invariants

1. **Pydantic v2 Schemas**: All API inputs and outputs must define explicit Pydantic models with field validations, docstrings, and example payloads.
2. **Asynchronous Non-Blocking Endpoints**: Endpoints declared as `async def` must never execute blocking CPU-heavy tensor operations on the event loop; offload heavy inference via `run_in_threadpool`.

---

## 4. Master Anti-Patterns Register (Strictly Forbidden)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         STRICTLY FORBIDDEN ANTI-PATTERNS                    │
├────────────────────┬──────────────────────────────────┬─────────────────────┤
│ Anti-Pattern       │ Architectural Violation          │ Automated Detection │
├────────────────────┼──────────────────────────────────┼─────────────────────┤
│ 1. Floating Async  │ Unhandled coroutine exceptions;  │ Android Lint &      │
│    in Compose      │ causes random app crashes.       │ Detekt rule check.  │
├────────────────────┼──────────────────────────────────┼─────────────────────┤
│ 2. Bypass RLS      │ Querying tenant tables without   │ PostgreSQL session  │
│    Database Query  │ setting app.current_school_id.   │ audit interceptor.  │
├────────────────────┼──────────────────────────────────┼─────────────────────┤
│ 3. Raw Audio Print │ Dumping PCM/Base64 audio chunks  │ ESLint / Semgrep    │
│    to Console      │ to stdout logs; causes OOM crash.│ regex rule.         │
├────────────────────┼──────────────────────────────────┼─────────────────────┤
│ 4. Cleartext HTTP  │ Disabling HTTPS or cleartext     │ Android Network     │
│    in Android      │ traffic in mobile configuration. │ Security Config.    │
└────────────────────┴──────────────────────────────────┴─────────────────────┘
```
