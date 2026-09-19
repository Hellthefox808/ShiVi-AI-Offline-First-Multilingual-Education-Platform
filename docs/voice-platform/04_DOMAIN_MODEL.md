# 04 — DOMAIN-DRIVEN DESIGN (DDD) MODEL & STATE MACHINES

> **Document ID:** `VOX-ARCH-04-DOMAIN-MODEL`  
> **Platform Name:** VoxBridge AI (Enterprise Multilingual Voice Translation API Platform)  
> **Classification:** Production-Grade Systems Architecture Document  
> **Standard:** Strategic & Tactical Domain-Driven Design (DDD)  
> **Document Version:** 4.0.0-PROD | **Status:** Active Master Reference  

---

## 1. Strategic Bounded Context Map

VoxBridge AI partitions its business domain into seven bounded contexts with formal relationship mappings:

```mermaid
graph TD
    subgraph CoreDomain["CORE INTELLIGENCE & STREAMING DOMAIN"]
        SessionCtx["Session & Stream Context<br/>(VoiceSession, StreamConnection, AudioBuffer)"]
        SpeechCtx["Speech Intelligence Context<br/>(Utterance, TranscriptChunk, LanguageScore)"]
        TransCtx["Translation & Terminology Context<br/>(TranslationUnit, GlossaryMapping)"]
        SynthCtx["Voice Synthesis Context<br/>(VoiceProfile, AcousticSegment, EgressStream)"]
    end

    subgraph GenericSubdomains["SUPPORTING & GENERIC SUBDOMAINS"]
        TenantCtx["Identity & Tenancy Context<br/>(Organization, Workspace, Project, ApiKey)"]
        UsageCtx["Usage Metering & Billing Context<br/>(MeteredEvent, QuotaAllowance, InvoiceItem)"]
        MediaCtx["Media Asset Context<br/>(AudioAsset, ChunkStorage, LifecycleRule)"]
    end

    TenantCtx -->|Upstream Supplier| SessionCtx
    SessionCtx -->|Coordinates Pipeline| SpeechCtx
    SpeechCtx -->|Emits Transcripts| TransCtx
    TransCtx -->|Emits Translated Text| SynthCtx
    SynthCtx -->|Streams Audio Back| SessionCtx

    SessionCtx -.->|Emits Billable Ops| UsageCtx
    SessionCtx -.->|Stores Raw/Proc Blobs| MediaCtx
```

---

## 2. Core Aggregates, Entities & Value Objects

### 2.1 VoiceSession (Aggregate Root in Session Context)
* **Aggregate Root**: `VoiceSession`
  * **Entities**: `StreamConnection`, `SessionParticipant`, `ActiveRouter`
  * **Value Objects**:
    - `SessionId` (ULID prefixed `ses_`)
    - `TenantId` (`org_... / prj_...`)
    - `AudioFormat` (Codec, SampleRate, Channels, BitDepth)
    - `LanguagePair` (SourceLanguage, TargetLanguageList)
    - `LatencyClass` (`ULTRA_LOW_LATENCY`, `BALANCED`, `HIGH_QUALITY`)
  * **Invariants**:
    1. A `VoiceSession` cannot transition to `ACTIVE` without a verified `ApiKey` possessing the `session.create` permission.
    2. A `VoiceSession` enforces a hard ceiling on duration ($120\text{ minutes}$ standard, $480\text{ minutes}$ enterprise).
    3. All events within a session possess a monotonically increasing 64-bit integer `sequence_number`.

### 2.2 Utterance (Aggregate Root in Speech Context)
* **Aggregate Root**: `Utterance`
  * **Entities**: `AudioSegment`, `TranscriptChunk`
  * **Value Objects**:
    - `UtteranceId` (`utt_...`)
    - `TimeSpan` (StartMs, EndMs, DurationMs)
    - `TranscriptStatus` (`PARTIAL`, `FINAL`, `CORRECTED`)
    - `ConfidenceScore` (Float $0.0 - 1.0$)
    - `DetectedLanguage` (ISO-639 code + probability)
  * **Invariants**:
    1. `PARTIAL` transcripts are ephemeral and may be overwritten by subsequent speech frames.
    2. A `FINAL` transcript is immutable once emitted; subsequent modifications must spawn a `CORRECTED` revision with a reference to the parent segment ID.

---

## 3. Deterministic Session Lifecycle State Machine

Streaming sessions must transition deterministically through validated states:

```mermaid
stateDiagram-v2
    [*] --> CREATED: Client Requests Session Token
    CREATED --> CONNECTING: WebSocket / WebRTC Handshake
    CONNECTING --> ACTIVE: Stream Initialized & Ready
    CONNECTING --> FAILED: Handshake Timeout / Auth Reject
    
    state ACTIVE {
        [*] --> StreamingIdle
        StreamingIdle --> IngestingAudio: Audio Frames Received
        IngestingAudio --> TranslatingTurn: VAD Boundary Detected
        TranslatingTurn --> StreamingAudioEgress: TTS Chunk Emitted
        StreamingAudioEgress --> StreamingIdle: Turn Completed
    }
    
    ACTIVE --> PAUSED: Client Sends session.pause
    PAUSED --> ACTIVE: Client Sends session.resume
    
    ACTIVE --> RECONNECTING: Socket Disconnect / Keep-Alive Timeout
    RECONNECTING --> ACTIVE: Client Reconnects with Last Sequence ID
    RECONNECTING --> EXPIRED: Reconnect Window Closes (>30s)
    
    ACTIVE --> COMPLETING: Client Sends session.stop
    COMPLETING --> COMPLETED: Final Audio Flushed & Metered
    
    ACTIVE --> FAILED: Unrecoverable Provider / Worker Panic
    ACTIVE --> CANCELLED: Admin / Quota Revocation
    
    COMPLETED --> [*]
    FAILED --> [*]
    CANCELLED --> [*]
    EXPIRED --> [*]
```
