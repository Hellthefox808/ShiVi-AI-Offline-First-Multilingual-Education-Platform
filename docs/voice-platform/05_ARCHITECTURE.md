# 05 — HIGH-LEVEL SYSTEM ARCHITECTURE & PIPELINE DESIGNS

> **Document ID:** `VOX-ARCH-05-SYSTEM-ARCH`  
> **Platform Name:** VoxBridge AI (Enterprise Multilingual Voice Translation API Platform)  
> **Classification:** Production-Grade Systems Architecture Document  
> **Pattern:** Event-Driven Modular Microservices with Specialized Streaming Gateways  
> **Document Version:** 4.0.0-PROD | **Status:** Active Master Reference  

---

## 1. Global System Architecture Blueprint

```mermaid
graph TD
    Clients["Clients: Web / Mobile / SDK / Telephony / IoT"]
    
    subgraph EdgeTier["1. GLOBAL EDGE & INGRESS"]
        WAF["Edge Anycast IP + Cloudflare WAF + DDoS Scrubbing"]
        EdgeProxy["Envoy API Gateway (TLS 1.3, Rate Limiting, Geo-Routing)"]
    end

    subgraph StreamingPlane["2. REAL-TIME STREAMING ORCHESTRATION PLANE"]
        StreamGW["Streaming Ingestion Gateway (Go / Rust WebSocket & WebRTC)"]
        VADEngine["On-Device / Edge VAD Engine (Silero C++ ONNX)"]
        StreamRouter["Stream Session Orchestrator (NATS JetStream Core)"]
    end

    subgraph IntelligencePlane["3. SPEECH & TRANSLATION ENGINE (GPU / High-CPU Nodes)"]
        STTWorkers["Streaming STT Engine (Whisper / Conformer / Provider Adapter)"]
        MTWorkers["Neural Machine Translation Engine (NLLB-200 / Gemini / DeepL)"]
        TTSWorkers["Streaming TTS Synthesis Engine (Kokoro-82M / Azure / ElevenLabs)"]
    end

    subgraph BatchPlane["4. ASYNCHRONOUS BATCH PROCESSING PLANE"]
        JobService["Job & Workflow Engine (Temporal.io / BullMQ)"]
        BatchAudioWorkers["Batch Media Audio Chunking & Diarization Workers"]
    end

    subgraph DataPlane["5. DATA, PERSISTENCE & QUEUE PLANE"]
        Postgres[("PostgreSQL 18 HA Cluster<br/>(Multi-Tenant RLS System of Record)")]
        RedisCluster[("Redis 7.4 Cluster<br/>(Session State, Rate Limiting, Ephemeral Locks)")]
        ObjectStore[("S3 / Cloud Storage<br/>(Audio Blobs, Dubbed Tracks, Export Bundles)")]
        EventBus[("NATS JetStream / Kafka<br/>(Durable Event Streaming Bus)")]
    end

    Clients -->|WSS / WebRTC / REST| WAF
    WAF --> EdgeProxy
    EdgeProxy -->|REST / Admin / Jobs| JobService
    EdgeProxy -->|Stateful WSS / WebRTC| StreamGW

    StreamGW --> VADEngine
    VADEngine --> StreamRouter
    StreamRouter --> STTWorkers
    STTWorkers --> MTWorkers
    MTWorkers --> TTSWorkers
    TTSWorkers --> StreamGW
    StreamGW -->|Translated Audio Egress| Clients

    JobService --> BatchAudioWorkers
    BatchAudioWorkers --> STTWorkers
    BatchAudioWorkers --> ObjectStore

    StreamRouter -.->|Emit Metering Events| EventBus
    EventBus --> Postgres
    StreamGW -.->|Maintain Session State| RedisCluster
    Postgres -.-> ObjectStore
```

---

## 2. Pipeline Decomposition & Execution Semantics

### 2.1 Pipeline A — Batch Audio Pipeline (Asynchronous, High Throughput)
$$\text{Audio Upload} \to \text{Validation} \to \text{S3 Stash} \to \text{Temporal Workflow} \to \text{Diarized STT} \to \text{Glossary MT} \to \text{TTS Dub} \to \text{Webhook}$$
* **Throughput**: Optimized for high file volume rather than sub-second latency.
* **Failure Model**: If a single 5-minute chunk fails translation, only that chunk is retried with backoff; the workflow does not restart from zero.

### 2.2 Pipeline B — Streaming Pipeline (Ultra-Low Latency, Stateful)
$$\text{Frame Ingest} \to \text{Jitter Buffer} \to \text{Silero VAD} \to \text{Partial STT} \to \text{Speculative MT} \to \text{First-Byte TTS} \to \text{Egress}$$
* **Concurrency Model**: Stateful actor-based connection loop per active session; zero disk writes in the critical audio forwarding loop.
* **Latency Guarantee**: P95 Time-to-First-Audio $\le 1200\text{ ms}$.

### 2.3 Pipeline C — Conversational Two-Way Interpretation
* **Speaker Isolation**: Tracks multiple participants in a single virtual room.
* **Ducking & Arbitration**: When Speaker A is translating, incoming audio from Speaker B is held in an acoustic queue to prevent speech collisions.

---

## 3. Architecture Topology Evaluation

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TOPOLOGY EVALUATION & SELECTION                          │
├───────────────────┬──────────────────────┬──────────────────────────────────┤
│ Evaluated Pattern │ Pros / Cons          │ Strategic Decision               │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option 1: Monolith│ • Simple operational │ REJECTED: Long-running streaming │
│                   │   deployment         │ WebSockets starve under batch    │
│                   │ • High blast radius  │ media CPU spikes; impossible to  │
│                   │ • Hard to autoscale  │ scale GPU workers independently. │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option 2: 30-Pcs  │ • Complete decoupling│ REJECTED: Severe network hop     │
│ Microservices     │ • Massive latency    │ tax adds 150-250ms inter-service │
│                   │ • High ops overhead  │ latency; catastrophic for voice. │
├───────────────────┼──────────────────────┼──────────────────────────────────┤
│ Option 3: Hybrid  │ • Decoupled streaming│ SELECTED: Consolidates non-real- │
│ Streaming + Core  │   gateways (Go/Rust) │ time services into modular       │
│ (Chosen Model)    │ • Co-located tensor  │ domain cores; dedicates raw      │
│                   │   inference pipelines│ high-performance runtime to voice│
└───────────────────┴──────────────────────┴──────────────────────────────────┘
```
