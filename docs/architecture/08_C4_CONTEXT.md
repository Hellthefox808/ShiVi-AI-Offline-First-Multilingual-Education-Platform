# 08 — C4 ARCHITECTURE MODEL: LEVEL 1 SYSTEM CONTEXT

> **Document ID:** `BS-ARCH-08-C4-CONTEXT`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** C4 Model for Visualizing Software Architecture (Level 1)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. System Context Diagram (Level 1)

```mermaid
C4Context
    title System Context Diagram for BhashaSetu AI (Level 1)

    Person(teacher, "Primary School Teacher", "Non-native Hindi speaker instructing primary classes in tribal village schools.")
    Person(student, "Tribal Student (Grades 1-5)", "Native Santhali, Ho, or Mundari child acquiring foundational literacy & numeracy.")
    Person(linguist, "Certified Native Linguist", "Tribal language authority reviewing machine-translated curriculum and scripts.")
    Person(admin, "Block Education Officer (BEO)", "Monitors NIPUN Bharat FLN attainment and school-level sync health.")

    Enterprise_Boundary(b0, "BhashaSetu AI Ecosystem") {
        System(bhashasetu, "BhashaSetu AI Platform", "Offline-first MTB-MLE AI scaffolding, translation, voice relay, and sync platform.")
    }

    System_Ext(jcert, "JCERT / DIKSHA Portal", "Official state curriculum repository, textbook PDFs, and national LO standards.")
    System_Ext(bhashini, "National Bhashini AI Gateway", "Government speech recognition, translation, and TTS API infrastructure.")
    System_Ext(gemini, "Gemini AI & Google Cloud", "High-capacity LLM multimodal reasoning, translation fallback, and object storage.")
    System_Ext(mdm, "State Tablet MDM Registry", "Tracks physical tablet hardware serials, device allocation, and school mapping.")

    Rel(teacher, bhashasetu, "Creates lessons, speaks bilingual voice prompts, reviews student scores", "HTTPS / WSS")
    Rel(student, bhashasetu, "Plays interactive quizzes, reads dual-script worksheets, listens to tribal audio", "On-Device UI")
    Rel(linguist, bhashasetu, "Curates tribal glossaries, verifies Ol Chiki / Warang Chiti scripts", "HTTPS")
    Rel(admin, bhashasetu, "Inspects district FLN dashboards, school sync telemetry", "HTTPS")

    Rel(bhashasetu, jcert, "Ingests syllabus chunks, competencies, and learning outcomes", "HTTPS / Batch ETL")
    Rel(bhashasetu, bhashini, "Dispatches ASR and MT requests for Indian languages", "REST / TLS 1.3")
    Rel(bhashasetu, gemini, "Executes complex pedagogical adaptation & Gemini MT fallbacks", "gRPC / TLS 1.3")
    Rel(bhashasetu, mdm, "Synchronizes device telemetry, battery, and storage status", "HTTPS REST")
```

---

## 2. Actor Profile & Interface Specifications

| Actor | Access Medium | Primary Security Token | Core Operations |
|---|---|---|---|
| **Primary Teacher** | Android Tablet & Web Portal | JWT Bearer ($15\text{m}$ TTL) | `create_lesson`, `trigger_voice_relay`, `approve_draft`, `view_class_fln` |
| **Tribal Student** | Android Tablet (Shared/Single) | Pseudonymous `student_id` | `start_quiz`, `submit_attempt`, `listen_audio`, `view_flashcard` |
| **Native Linguist** | Web Frontend Desktop | RBAC Role: `LINGUIST` | `audit_translation`, `edit_glossary`, `certify_script`, `resolve_conflict` |
| **Block Admin (BEO)** | Web Frontend Desktop | RBAC Role: `DISTRICT_ADMIN` | `export_fln_report`, `view_sync_map`, `provision_school_tenant` |

---

## 3. External System Interfaces & Fallback Strategies

1. **JCERT / DIKSHA Curriculum Repository**:
   - **Protocol**: HTTPS REST / Periodic JSON & PDF data ingest.
   - **Fallback**: Pre-bundled local JCERT knowledge base in `services/ai-platform/rag/engine.py` ensures 100% autonomy even if state portals undergo maintenance.
2. **National Bhashini AI Gateway**:
   - **Protocol**: REST over TLS 1.3 with API Key Authentication.
   - **Fallback**: Seamlessly routes requests to local quantized NLLB-200 or Gemini 3.1 Pro if Bhashini latency exceeds $1500\text{ ms}$.
3. **Gemini AI & Google Cloud Platform**:
   - **Protocol**: HTTP/2 and gRPC client SDK.
   - **Fallback**: Quantized on-premises models (Distil-Whisper, Kokoro-82M, NLLB-200) run locally in Docker Compose with zero cloud egress.
4. **State Tablet MDM Registry**:
   - **Protocol**: HTTPS JSON Telemetry webhook.
   - **Fallback**: Tablet stores telemetry in local `sync_logs` table until administrative connection succeeds.
