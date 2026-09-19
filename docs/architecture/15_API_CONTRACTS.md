# 15 — CONCRETE API CONTRACTS & OPENAPI 3.1 SPECIFICATIONS

> **Document ID:** `BS-ARCH-15-API-CONTRACTS`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Source Grounding:** [`services/ai-platform/main.py`](file:///d:/HACKTHON/bhashasetu-ai/services/ai-platform/main.py) & [`services/web-backend/`](file:///d:/HACKTHON/bhashasetu-ai/services/web-backend)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Core Educational AI Endpoints (`services/ai-platform/main.py`)

### 1.1 `POST /api/v1/ai/generate-lesson`
Executes hybrid RAG retrieval, multilingual translation, cultural analogy adaptation, and COMET quality estimation.

* **Request Payload (`LessonGenerateRequest`)**:
```json
{
  "hindi_prompt": "बच्चों, आज हम स्थानीय पेड़ों और पत्तियों के प्रकार और उनके कार्य के बारे में सीखेंगे।",
  "target_language": "SANTHALI",
  "grade_level": "GRADE_2",
  "subject": "ENVIRONMENTAL_STUDIES",
  "curriculum_node_id": "JCERT_G2_EVS_01"
}
```

* **Success Response (HTTP 200 OK)**:
```json
{
  "lesson_id": "LES-4F9A12BD",
  "hindi_prompt": "बच्चों, आज हम स्थानीय पेड़ों और पत्तियों के प्रकार और उनके कार्य के बारे में सीखेंगे।",
  "target_language": "SANTHALI",
  "grade_level": "GRADE_2",
  "subject": "ENVIRONMENTAL_STUDIES",
  "status": "REVIEW_REQUIRED",
  "adaptation": {
    "native_script": "OL_CHIKI",
    "translated_text": "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱛᱮᱦᱮᱧ ᱫᱚ ᱟᱵᱚ ᱟᱵᱚᱣᱟᱜ ᱟᱹᱛᱩ ᱨᱮᱱᱟᱜ ᱫᱟᱨᱮ ᱟᱨ ᱥᱟᱠᱟᱢ ᱠᱚ ᱤᱫᱤ ᱠᱟᱛᱮ ᱵᱚᱱ ᱪᱮᱫᱚᱜ-ᱟ᱾",
    "transliteration_hindi": "गिदरा को, तेहें दो आबो आबोवाः आतु रेनाः दारे आर साकाम को इदी काते बोन चेदोः-आ।",
    "transliteration_latin": "Gidra ko, tehenj do abo abowag aatu renag dare aar sakam ko idi kate bon chedoga.",
    "cultural_analogy": "सरहुल पर्व में पूज्य साल (सखुआ) के पेड़ और पत्तों का उदाहरण",
    "classroom_activity": "पत्तियों का मिलान और पत्तल बनाने की विधि",
    "audio_tts_url": "/audio/lessons/santhali_trees.mp3",
    "audio_metadata": {
      "sample_rate_hz": 24000,
      "bitrate_kbps": 64,
      "codec": "MP3",
      "voice_speaker_model": "Kokoro-82M-santhali-v3",
      "duration_ms": 3200
    }
  },
  "quality_report": {
    "comet_score": 0.91,
    "quality_status": "HIGH_CONFIDENCE",
    "glossary_compliance": true
  },
  "provenance": {
    "evidence_chunk_ids": ["JCERT_EVS_G2_C04_01"],
    "lo_codes": ["LO-EVS-G2-03"],
    "elapsed_ms": 142.5
  }
}
```

---

### 1.2 `POST /api/v1/voice/translate`
Sub-3-second spoken classroom dialogue translation endpoint with bilingual relay.

* **Request Payload (`VoiceTranslateRequest`)**:
```json
{
  "hindi_transcript": "बच्चों, अपनी किताब खोलो",
  "target_language": "SANTHALI",
  "fln_mode": true,
  "bilingual_relay": true
}
```

* **Success Response (HTTP 200 OK)**:
```json
{
  "turn_id": "VOICE-A7C12F",
  "source_hindi": "बच्चों, अपनी किताब खोलो",
  "target_language": "SANTHALI",
  "native_script_text": "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱟᱯᱮᱭᱟᱜ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱯᱮ",
  "transliteration_hindi": "गिदरा को, आपेयाः पुथी झीज पे",
  "speech_rate": 0.72,
  "acoustic_engine": "hi-IN",
  "bilingual_relay": {
    "enabled": true,
    "source_audio_pause_ms": 450,
    "relay_sequence": ["SOURCE_HINDI", "PAUSE_450MS", "TRIBAL_PHONETIC_HI_IN"]
  },
  "comet_score": 0.94,
  "quality_status": "HIGH_CONFIDENCE"
}
```

---

## 2. Sync & Outbox Reconciliation Endpoints (`services/web-backend/`)

### 2.1 `POST /api/v1/sync/push`
Receives a batch of local mutations from the mobile edge outbox.

* **Request Headers**:
  - `Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000`
  - `X-Device-ID: TAB-JH-DUMKA-042`
  - `Content-Encoding: gzip`
* **Request Payload**:
```json
{
  "batch_id": "BAT-9B8C12A",
  "device_id": "TAB-JH-DUMKA-042",
  "operations": [
    {
      "operation_id": "op-550e8400-e29b-41d4-a716-446655440001",
      "entity_name": "assessments",
      "action": "INSERT",
      "client_timestamp": "2026-09-19T10:15:00Z",
      "payload": {
        "attempt_id": "ATT-00124",
        "student_id": "STU-8821B",
        "quiz_id": "QZ-EVS-G2-01",
        "score": 100,
        "lo_code": "LO-EVS-G2-03"
      }
    }
  ]
}
```

* **Success Response (HTTP 200 OK)**:
```json
{
  "batch_id": "BAT-9B8C12A",
  "status": "PROCESSED",
  "processed_count": 1,
  "conflict_count": 0,
  "server_cursor": "2026-09-19T14:32:00.124Z",
  "acknowledged_operations": [
    "op-550e8400-e29b-41d4-a716-446655440001"
  ]
}
```

---

### 2.2 `GET /api/v1/sync/pull`
Pulls verified published curriculum packages and glossaries created since the last sync cursor.

* **Query Parameters**:
  - `cursor`: `2026-09-15T00:00:00Z`
  - `target_language`: `SANTHALI`
  - `grade`: `GRADE_2`
* **Success Response (HTTP 200 OK)**:
```json
{
  "server_cursor": "2026-09-19T14:32:00.124Z",
  "has_more": false,
  "updated_lessons": [
    {
      "lesson_id": "LES-4F9A12BD",
      "version": 3,
      "title": "पेड़ और पत्तियाँ",
      "status": "PUBLISHED",
      "checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      "download_url": "/packages/offline/sat_trees_v3.zip"
    }
  ],
  "glossary_deltas": []
}
```
