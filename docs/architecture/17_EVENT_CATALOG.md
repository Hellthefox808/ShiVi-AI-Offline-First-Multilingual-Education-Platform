# 17 — ENTERPRISE EVENT CATALOG & SCHEMA REGISTRY

> **Document ID:** `BS-ARCH-17-EVENT-CATALOG`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** CloudEvents 1.0 Catalog Specification (§16 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Master Event Catalog Matrix

| Event Type Name | Producer Subsystem | Primary Consumers | Trigger Condition | Ordering Key |
|---|---|---|---|---|
| `lesson.created.v1` | `web-backend` | `audit-logger` | Teacher submits initial scaffolding prompt | `school_id` |
| `lesson.generated.v1`| `ai-platform` | `web-backend` | AI completes translation & quality report | `lesson_id` |
| `lesson.approved.v1` | `web-backend` | `notification-svc` | Teacher or linguist accepts draft | `lesson_id` |
| `lesson.published.v1`| `web-backend` | `offline-packager` | Immutable version signed by educator | `school_id` |
| `package.compiled.v1`| `offline-packager` | `sync-engine` | Zip bundle & checksum successfully compiled | `language` |
| `assessment.attempted.v1`| `mobile-edge` (via sync) | `fln-analytics` | Student completes quiz on classroom tablet | `student_id` |
| `sync.batch_pushed.v1`| `mobile-edge` | `reconciliation-svc`| Tablet uploads pending outbox operations | `device_id` |
| `glossary.updated.v1`| `web-backend` | `ai-platform`, `edge`| Native linguist updates term definition | `language` |
| `device.heartbeat.v1`| `mobile-edge` | `device-registry` | Tablet checks in with battery & storage state | `device_id` |

---

## 2. Event Payload Schema Definitions

### 2.1 Event: `in.gov.jh.bhashasetu.lesson.published.v1`
* **Producer**: `services/web-backend/src/lessons/lessons.service.ts`
* **Trigger**: A teacher or administrator clicks "Publish Immutable Version" in the Lesson Studio.
* **Payload Schema**:
```json
{
  "specversion": "1.0",
  "id": "evt-pub-8821a-4c12",
  "type": "in.gov.jh.bhashasetu.lesson.published.v1",
  "source": "bhashasetu://web-backend/lessons",
  "time": "2026-09-19T14:35:00Z",
  "subject": "LES-4F9A12BD",
  "data": {
    "lesson_id": "LES-4F9A12BD",
    "version": 3,
    "school_id": "SCH-DUMKA-01",
    "target_language": "SANTHALI",
    "grade_level": "GRADE_2",
    "subject": "ENVIRONMENTAL_STUDIES",
    "lo_code": "LO-EVS-G2-03",
    "title_hindi": "पेड़ और पत्तियाँ",
    "native_script": "OL_CHIKI",
    "approver_id": "USR-RAMESH-01",
    "quality_score": 0.91,
    "audio_asset_count": 4,
    "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

---

### 2.2 Event: `in.gov.jh.bhashasetu.assessment.attempted.v1`
* **Producer**: `app/src/main/java/com/example/data/local/AssessmentDao.kt` (replicated via sync push)
* **Trigger**: A student completes an interactive quiz in the village classroom.
* **Payload Schema**:
```json
{
  "specversion": "1.0",
  "id": "evt-att-9912b-1a2f",
  "type": "in.gov.jh.bhashasetu.assessment.attempted.v1",
  "source": "bhashasetu://mobile-edge/TAB-JH-DUMKA-042",
  "time": "2026-09-19T10:15:00Z",
  "subject": "ATT-00124",
  "data": {
    "attempt_id": "ATT-00124",
    "student_id": "STU-8821B",
    "school_id": "SCH-DUMKA-01",
    "quiz_id": "QZ-EVS-G2-01",
    "lo_code": "LO-EVS-G2-03",
    "target_language": "SANTHALI",
    "total_questions": 5,
    "correct_answers": 5,
    "score_percentage": 100,
    "competency_achieved": true,
    "completed_offline": true,
    "device_battery_level": 78
  }
}
```

---

### 2.3 Event: `in.gov.jh.bhashasetu.glossary.updated.v1`
* **Producer**: `services/web-backend/src/curriculum/glossary.service.ts`
* **Trigger**: A certified native linguist verifies or corrects an educational terminology mapping.
* **Payload Schema**:
```json
{
  "specversion": "1.0",
  "id": "evt-glo-1100a-9d8e",
  "type": "in.gov.jh.bhashasetu.glossary.updated.v1",
  "source": "bhashasetu://web-backend/curriculum/glossary",
  "time": "2026-09-19T14:40:00Z",
  "subject": "GLO-SAT-DARE-01",
  "data": {
    "glossary_id": "GLO-SAT-DARE-01",
    "hindi_term": "पेड़",
    "target_language": "SANTHALI",
    "native_script_term": "ᱫᱟᱨᱮ",
    "script_type": "OL_CHIKI",
    "phonetic_translit_hindi": "दारे",
    "phonetic_translit_latin": "Dare",
    "cultural_context": "सरहुल पर्व में पूज्य साल वृक्ष",
    "linguist_certifier_id": "USR-LING-HEMBRAM-01",
    "version": 4
  }
}
```
