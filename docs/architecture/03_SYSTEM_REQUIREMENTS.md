# 03 — SYSTEM REQUIREMENTS & NON-FUNCTIONAL SPECIFICATIONS (NFR)

> **Document ID:** `BS-ARCH-03-SYS-REQ`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Non-Functional Requirements (NFR) Matrix

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       NON-FUNCTIONAL REQUIREMENTS HIERARCHY                 │
├────────────────────┬──────────────────────────────────────┬─────────────────┤
│ Dimension          │ Quantitative Specification           │ Criticality     │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 1. Voice Latency   │ End-to-end voice relay <= 3000 ms    │ CRITICAL (P0)   │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 2. Edge Memory     │ Android app heap consumption < 192MB │ CRITICAL (P0)   │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 3. Offline Runtime │ 100% core classroom actions offline  │ CRITICAL (P0)   │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 4. Vector Query    │ RAG hybrid retrieval latency <= 15ms │ HIGH (P0)       │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 5. Sync Throughput │ 500 records/sec batch ingestion      │ HIGH (P1)       │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 6. Storage Sizing  │ Offline package size <= 25 MB/lang   │ HIGH (P0)       │
├────────────────────┼──────────────────────────────────────┼─────────────────┤
│ 7. Cloud Uptime    │ 99.9% availability for API gateway   │ MEDIUM (P1)     │
└────────────────────┴──────────────────────────────────────┴─────────────────┘
```

---

## 2. Quantitative Performance & Latency Budgets

### 2.1 Live Spoken Voice-to-Voice Relay Latency Budget
The classroom environment requires immediate conversational cadence. A delay exceeding $3.0\text{ seconds}$ disrupts the interaction.

$$\text{Total Elapsed Time} = T_{\text{VAD}} + T_{\text{ASR}} + T_{\text{RAG/Glossary}} + T_{\text{MT}} + T_{\text{TTS}} + T_{\text{Transport}}$$

| Pipeline Segment | Target SLA | Measured Baseline | Technical Strategy & Optimization |
|---|---|---|---|
| **Voice Activity Detection (VAD)** | $\le 100\text{ ms}$ | $95\text{ ms}$ | On-device Silero VAD (C++ ONNX runtime) running on raw PCM audio frames. |
| **Streaming Hindi ASR** | $\le 800\text{ ms}$ | $580\text{ ms}$ | Distil-Whisper / Bhashini streaming Conformer with 160ms chunk windowing. |
| **Glossary & Hybrid RAG** | $\le 150\text{ ms}$ | $120\text{ ms}$ | Pre-indexed in-memory Trie for JCERT glossaries + cached BGE-M3 vector lookup. |
| **Machine Translation (MT)** | $\le 500\text{ ms}$ | $440\text{ ms}$ | Quantized NLLB-200 (INT8) / Gemini 3.1 Pro endpoint with strict token budget ($< 64$ tokens). |
| **TTS Audio Synthesis** | $\le 700\text{ ms}$ | $620\text{ ms}$ | Kokoro-82M / Piper streaming ONNX engine generating 24kHz Opus/MP3 audio. |
| **Client Audio Buffer & Play** | $\le 150\text{ ms}$ | Trace buffer | Android ExoPlayer initial chunk pre-buffering. |
| **TOTAL END-TO-END PIPELINE** | **$\le 3000\text{ ms}$** | **$1855\text{ ms}$** | **Headroom Margin: $1145\text{ ms}$ (38.1% buffer)** |

---

## 3. Hardware Resource Constraints & Physical Edge Envelope

The target deployment environment consists of government-procured educational tablets distributed across tribal blocks:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                      TARGET PHYSICAL HARDWARE PROFILE                       │
├───────────────────────────────┬─────────────────────────────────────────────┤
│ Processor Architecture        │ Quad-core ARM Cortex-A53 / A55 @ 1.3-2.0 GHz│
│ System RAM                    │ 2048 MB (2.0 GB) Total Shared Memory         │
│ Operating System              │ Android 9.0 (API 28) through Android 14.0   │
│ Display Form Factor           │ 8.0" to 10.1" IPS LCD (1280x800 / 1920x1200)│
│ Internal Storage              │ 16 GB or 32 GB eMMC 5.1 Flash Storage       │
│ Audio Hardware                │ Dual micro-speaker, single electret mic     │
│ Battery Capacity              │ 4000 mAh – 5000 mAh Li-ion                  │
│ Thermal Throttling Threshold  │ Surface temperature > 42°C induces 50% CPU  │
└───────────────────────────────┴─────────────────────────────────────────────┘
```

### Edge Software Guardrails:
1. **JVM Heap Cap**: Android Dalvik/ART virtual machine max heap allocation is hard-capped at **$192\text{ MB}$**. If memory exceeds $220\text{ MB}$, the OS sends `onLowMemory()` and triggers `ActivityManager` kill signals.
2. **APK Binary Sizing**: The final release binary must not exceed **$35\text{ MB}$** over-the-air to ensure downloadability over weak cellular hotspots. Current release APK is **$29.6\text{ MB}$** (`app-debug.apk`).
3. **Storage Footprint**: Total disk footprint (App + Local SQLite DB + Audio Cache) must stay under **$500\text{ MB}$** on a 16 GB device.

---

## 4. Network Tolerance & Flaky Connectivity Specification

Rural Jharkhand schools experience extreme network volatility:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                        NETWORK PROFILES & BEHAVIORS                         │
├─────────────────────┬───────────────────┬───────────────────────────────────┤
│ Network State       │ RTT Latency       │ Applied System Behavior           │
├─────────────────────┼───────────────────┼───────────────────────────────────┤
│ 1. Zero-Net         │ Infinite / Dead   │ 100% Local SQLite operation.      │
│    (Airplane Mode)  │                   │ Mutations queued in outbox table. │
├─────────────────────┼───────────────────┼───────────────────────────────────┤
│ 2. 2G Edge Network  │ 800 ms – 2500 ms  │ Background outbox sync only.      │
│    (9.6 - 64 kbps)  │ Packet loss > 20% │ Disable voice streaming; rely on  │
│                     │                   │ pre-cached offline phrasebook.    │
├─────────────────────┼───────────────────┼───────────────────────────────────┤
│ 3. Intermittent 3G  │ 250 ms – 600 ms   │ Incremental batch sync (10 items).│
│    (384 - 2000 kbps)│ Packet loss ~ 5%  │ Async audio download on idle.     │
├─────────────────────┼───────────────────┼───────────────────────────────────┤
│ 4. Broadband / 4G   │ 30 ms – 120 ms    │ Full two-way live voice streaming │
│    (> 5 Mbps)       │ Packet loss < 1%  │ & full curriculum pack updates.   │
└─────────────────────┴───────────────────┴───────────────────────────────────┘
```

---

## 5. Security & Isolation Requirements

1. **Multi-Tenant Isolation**: The central cloud backend stores data across hundreds of schools and districts. All database transactions must enforce PostgreSQL **Row-Level Security (RLS)** keyed to `current_setting('app.current_school_id')`.
2. **Cryptographic Protection**:
   - Passwords hashed using **Argon2id** ($m=65536, t=3, p=4$).
   - JWT tokens signed via HMAC-SHA256 or asymmetric Ed25519 with a strict $15\text{ minute}$ expiration.
   - Offline database on tablets encrypted via SQLCipher (256-bit AES-CBC) with keys stored in Android KeyStore.
3. **Prompt Injection & Model Security**:
   - All retrieved curriculum text chunks injected into LLM prompts are isolated inside explicit XML boundary delimiters (`<curriculum_evidence>...</curriculum_evidence>`).
   - System prompts forbid autonomous code execution or unvalidated external tool invocations.

---

## 6. Sizing & Capacity Projections

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                     5-YEAR CAPACITY PLANNING HORIZON                        │
├───────────────────────────────┬──────────────────────┬──────────────────────┤
│ Metric                        │ Year 1 (Pilot)       │ Year 3 (State Scale) │
├───────────────────────────────┼──────────────────────┼──────────────────────┤
│ Active Primary Schools        │ 500 schools          │ 12,000 schools       │
│ Registered Teachers           │ 1,500 teachers       │ 35,000 teachers      │
│ Active Student Learners       │ 30,000 students      │ 650,000 students     │
│ Daily Formative Quiz Attempts │ 45,000 attempts/day  │ 1,200,000 / day      │
│ Daily Spoken Voice Queries    │ 15,000 voice turns   │ 400,000 turns/day    │
│ Outbox Batch Sync Events      │ 3,000 sync pushes/day│ 80,000 pushes/day    │
│ Database Storage Growth       │ ~15 GB / year        │ ~350 GB / year       │
│ Object Storage (Audio Assets) │ ~50 GB               │ ~1.2 TB              │
└───────────────────────────────┴──────────────────────┴──────────────────────┘
```
