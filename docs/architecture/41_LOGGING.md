# 41 — LOGGING STANDARDS, STRUCTURED JSON & PII REDACTION

> **Document ID:** `BS-ARCH-41-LOGGING-STD`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Cloud-Native Structured JSON Logging & PII Sanitization (§38 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Structured JSON Logging Schema

All backend and AI platform services emit logs strictly formatted as single-line JSON objects to standard output (`stdout`):

```json
{
  "timestamp": "2026-09-19T14:30:00.124Z",
  "level": "INFO",
  "service": "bhashasetu-ai-platform",
  "environment": "production",
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "span_id": "00f067aa0ba902b7",
  "school_id": "SCH-DUMKA-01",
  "action": "AI_LESSON_GENERATED",
  "message": "Lesson synthesized successfully with high confidence",
  "context": {
    "lesson_id": "LES-4F9A12BD",
    "target_language": "SANTHALI",
    "grade_level": "GRADE_2",
    "comet_score": 0.91,
    "elapsed_ms": 142.5
  }
}
```

---

## 2. PII Sanitization & Zero-Leak Interceptors

To prevent accidental data leakage into log aggregators, all request and response loggers execute through a **PII Scrubbing Interceptor**:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PII MASKING RULES ENGINE                            │
├────────────────────┬──────────────────────────────────┬─────────────────────┤
│ Field Key Pattern  │ Sanitization Action              │ Example Output      │
├────────────────────┼──────────────────────────────────┼─────────────────────┤
│ `password`         │ Replace with static redaction    │ "[REDACTED_SECRET]" │
│ `authorization`    │ Truncate token prefix            │ "Bearer eyJhbG...[R]"│
│ `student_name`     │ Replace with pseudonymous code   │ "STU-8821B"         │
│ `audio_base64`     │ Suppress entirely                │ "[AUDIO_BLOB_MASKED]"
│ `aadhaar_number`   │ Hard-delete key immediately      │ [KEY_PURGED]        │
└────────────────────┴──────────────────────────────────┴─────────────────────┘
```

---

## 3. Strict Logging Anti-Patterns (Enforced by Linting)

1. **NEVER Log Raw Binary Audio**: Logging raw Base64 or PCM audio buffers crashes logging daemons and wastes storage; log only metadata (`duration_ms`, `sample_rate`).
2. **NEVER Use `console.log()` in Backend Code**: All NestJS services must inject the structured `AppLoggerService` with correlated trace IDs.
3. **NEVER Log Unparameterized SQL**: Logging queries containing raw user string literals risks leaking passwords or credentials; log only sanitized parameterized query templates.
