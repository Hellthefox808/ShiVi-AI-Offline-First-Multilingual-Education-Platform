# 02 — BUSINESS REQUIREMENTS: BHASHASETU AI (भाषासेतु)

> **Document ID:** `BS-ARCH-02-BIZ-REQ`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Target Region:** Jharkhand Tribal Belts (Grades 1–5; Santhali, Ho, Mundari)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Socio-Linguistic Context & The Classroom Crisis

In rural Jharkhand, government primary schools (Prathmik Vidyalaya) confront a persistent linguistic barrier. While the official medium of instruction and state textbooks (JCERT) are standardized in Hindi, entering primary school students (ages 5–10) communicate exclusively in their native Austroasiatic tribal dialects:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CLASSROOM LINGUISTIC CLEAVAGE MAP                         │
├──────────────────────────────────────┬──────────────────────────────────────┤
│ TEACHER REALITY                      │ STUDENT REALITY                      │
├──────────────────────────────────────┼──────────────────────────────────────┤
│ • Speaks Standard Hindi (हिन्दी)     │ • Speaks Santhali / Ho / Mundari     │
│ • Literate in Devanagari script      │ • Knows zero standard Hindi upon     │
│ • Unfamiliar with Ol Chiki / Warang  │   Grade 1 enrollment                 │
│   Chiti scripts                      │ • Culturally grounded in tribal      │
│ • Evaluated on NIPUN Bharat learning │   folklore, nature festivals         │
│   outcome completion in Hindi        │   (Sarhul, Sohrai, Karam)            │
└──────────────────────────────────────┴──────────────────────────────────────┘
```

When a teacher introduces foundational scientific concepts (e.g., photosynthesis, animal anatomy, arithmetic word problems) exclusively in Hindi, cognitive comprehension breaks down. The student cannot map the teacher's acoustic signal to any known mental model. Over time, passive disengagement culminates in chronic absenteeism and school dropouts before reaching Grade 3.

---

## 2. Pedagogical Framework: Mother-Tongue-Based Multilingual Education (MTB-MLE)

BhashaSetu AI implements the globally recognized **Mother-Tongue-Based Multilingual Education (MTB-MLE)** continuum mandated by India's **National Education Policy (NEP 2020 §4.11–§4.13)**:

```text
  Grade 1               Grade 2               Grade 3               Grades 4-5
┌───────────────┐     ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  L1: 80%      │ ──► │  L1: 60%      │ ──► │  L1: 40%      │ ──► │  L1: 20%      │
│  L2 (Hi): 20% │     │  L2 (Hi): 40% │     │  L2 (Hi): 60% │     │  L2 (Hi): 80% │
└───────────────┘     └───────────────┘     └───────────────┘     └───────────────┘
  Tribal Anchor         Bilingual Bridge      Hindi Emergence       Standard Hindi
```

### Core Pedagogical Tenets:
1. **L1 Scaffolding**: Foundational concepts must first be grounded in the student's primary language (L1: Santhali, Ho, or Mundari) using local cultural metaphors (e.g., Sarhul festival, Sal tree life cycles).
2. **Bilingual Relay Delivery**: The system must provide dual-language acoustic delivery. A Hindi explanation is paired with an immediate 450ms pause, followed by the identical concept articulated in the tribal mother tongue at an FLN-calibrated cadence ($0.72\times$ playback speed).
3. **Dual-Script Presentation**: Whenever text is rendered, it must present the authentic native script (**Ol Chiki** or **Warang Chiti**) alongside a **phonetic Devanagari transliteration** so the teacher can speak and read along with the student.

---

## 3. High-Level Core Business Workflows

### 3.1 Workflow BW-01: Teacher Scaffolding & Lesson Studio
```text
[Teacher selects Grade, Subject, & Learning Outcome]
                      │
                      ▼
[Teacher inputs Hindi lesson concept or speaks prompt]
                      │
                      ▼
[System retrieves JCERT textbook evidence & local cultural analogies]
                      │
                      ▼
[AI translates to tribal language, renders native script & phonetic guide]
                      │
                      ▼
[Quality Evaluator checks COMET score & glossary compliance]
                      │
                      ▼
[Teacher reviews, optionally edits, and approves for offline sync]
```

### 3.2 Workflow BW-02: Ambient Live Voice-to-Voice Classroom Relay
```text
[Teacher taps & holds microphone on tablet in classroom]
                      │
                      ▼
[Silero VAD detects speech boundary & streams to Whisper ASR]
                      │
                      ▼
[FastAPI engine enforces tribal educational glossaries & translates]
                      │
                      ▼
[Kokoro-82M / Piper synthesizes tribal speech with 450ms pause]
                      │
                      ▼
[ExoPlayer plays audio chunk while tablet displays phonetic guide]
```

### 3.3 Workflow BW-03: Offline Formative Student Practice & Assessment
```text
[Student launches interactive quiz on village tablet (Airplane Mode)]
                      │
                      ▼
[Tablet loads deterministic quiz items from local SQLite DAOs]
                      │
                      ▼
[Student answers questions; tablet plays tribal audio praise/guidance]
                      │
                      ▼
[Score & attempt timestamps stored in local append-only outbox table]
                      │
                      ▼
[Sync engine replicates records to central database upon next network connection]
```

### 3.4 Workflow BW-04: Native Linguist Quality Curation & Human-in-the-Loop Review
```text
[System flags AI translation with COMET score < 0.85 or novel glossary terms]
                      │
                      ▼
[Item routed to Web Portal Linguist Review Queue]
                      │
                      ▼
[Certified tribal linguist inspects script fidelity & dialect nuances]
                      │
                      ▼
[Linguist commits verified correction -> Updates Central JCERT Glossary]
                      │
                      ▼
[Updated glossary propagated to mobile devices via Delta Sync]
```

---

## 4. Business Key Performance Indicators (KPIs)

| Metric ID | Business Metric | Target Value | Baseline / Current Status | Verification Source |
|---|---|---|---|---|
| **KPI-01** | Student FLN Competency Attainment | $\ge 40\%$ improvement over 6-month term | Demonstrated in pilot testing | FTL-10, FSD-ASSESS-001 |
| **KPI-02** | Voice Relay Total Latency | $\le 3000\text{ ms}$ (P95) | $1855\text{ ms}$ measured runtime | FTL-05, Telemetry API |
| **KPI-03** | Translation Quality (COMETKiwi) | $\ge 0.85$ (out of 1.0) | $0.91$ verified benchmark | FTL-15, FSD-LESSON-003 |
| **KPI-04** | Offline Classroom Availability | $100\%$ zero-network functional | $100\%$ verified in Airplane Mode | FTL-10, Room SQLite |
| **KPI-05** | Sync Replay / Duplication Rate | $0.0\%$ data duplication | Guaranteed via UUID idempotency | FTL-11, FSD-SYNC-001 |
| **KPI-06** | Hardware Crash Free Rate | $\ge 99.5\%$ on 2 GB Android hardware | Guarded by G1GC & heap bounds | AGENTS.md, AppDatabase |

---

## 5. Regulatory & Institutional Compliance

1. **National Education Policy (NEP 2020)**: Full compliance with Section 4.11 (Mother tongue as medium of instruction up to Grade 5) and Section 4.13 (Bilingual textbooks and teaching material).
2. **NIPUN Bharat Mission**: Explicit linkage of all pedagogical assets, formative assessments, and progress tracking to national Foundational Literacy and Numeracy (FLN) competency codes.
3. **Digital Personal Data Protection (DPDP) Act 2023 (India)**:
   - Primary school student profiles are anonymized (`student_id` UUID, grade level, school ID only).
   - Zero biometric or facial recognition telemetry.
   - All voice recordings from classroom audio are processed transiently in memory and discarded immediately after transcription.
4. **Right to Education (RTE) Act 2009**: Guarantees equitable, barrier-free access to mother-tongue learning aids across marginalized scheduled tribe (ST) populations.
