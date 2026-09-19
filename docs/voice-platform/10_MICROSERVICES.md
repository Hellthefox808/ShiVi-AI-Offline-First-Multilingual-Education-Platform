# VoxBridge AI — Service Decomposition & Boundary Strategy

## 1. Architectural Strategy: The Decoupled Hybrid Architecture

### 1.1 The Microservices Fallacy vs. Operational Reality
A naive implementation of the 30 logical domains (API Gateway, Identity, Projects, Sessions, STT, Translation, TTS, Metering, Billing, etc.) as 30 distinct deployable Kubernetes microservices introduces catastrophic operational pathology:
- **Network Latency Tax:** A streaming audio pipeline passing through 7 independent microservice hops via gRPC adds 35ms to 70ms of serialized IPC overhead, instantly destroying the sub-500ms TTFT budget.
- **Distributed State Fragility:** Distributed transactions across 30 services necessitate two-phase commit (2PC) or complex Saga orchestrators, creating cascading partial failure states.
- **Infrastructure Cost Inflation:** Running 30 independent services with high availability (minimum 3 replicas per service) requires a baseline fleet of 90 pods before processing a single minute of customer audio.

### 1.2 The Hybrid Decision Matrix

```
┌───────────────────────────────────────────────────────────────────────────────────────┐
│                                VOXBRIDGE PLATFORM TOPOLOGY                            │
├───────────────────────────┬───────────────────────────┬───────────────────────────────┤
│    SERVICE CLUSTER        │    DEPLOYABLE RUNTIME     │    PRIMARY RESPONSIBILITY     │
├───────────────────────────┼───────────────────────────┼───────────────────────────────┤
│ 1. Enterprise API Gateway │ Go Modular Monolith       │ Auth, Billing, Keys, Quotas,  │
│    & Control Plane        │ (Deployable Unit 1)       │ Projects, Webhooks, REST API  │
├───────────────────────────┼───────────────────────────┼───────────────────────────────┤
│ 2. Real-Time Streaming    │ Go State-Optimized Binary │ WebSocket, WebRTC Ingestion,  │
│    Edge Gateway           │ (Deployable Unit 2)       │ Audio Demuxing, Jitter Buffer │
├───────────────────────────┼───────────────────────────┼───────────────────────────────┤
│ 3. Pipeline Streaming     │ Go Internal Engine        │ Linear VAD, Session Orchestr.,│
│    Orchestrator           │ (Deployable Unit 3)       │ Provider Routing, Fallback    │
├───────────────────────────┼───────────────────────────┼───────────────────────────────┤
│ 4. AI Inference Workers   │ GPU-Accelerated Pods      │ Dedicated TensorRT / vLLM     │
│    (STT, MT, TTS)         │ (Deployable Unit 4)       │ Workers (Whisper, NLLB, Piper)│
├───────────────────────────┼───────────────────────────┼───────────────────────────────┤
│ 5. Asynchronous Job &     │ Rust Daemon Cluster       │ Batch Audio Transcoding, File │
│    Metering Workers       │ (Deployable Unit 5)       │ Slices, TimescaleDB Flushing  │
└───────────────────────────┴───────────────────────────┴───────────────────────────────┘
```

---

## 2. Comprehensive Service Boundary Analysis (Section 98 Matrix)

### 2.1 Deployable Unit 1: Enterprise API Gateway & Control Plane
- **Logical Services Consolidated:**
  `API Gateway Service (1)`, `Identity Service (2)`, `Developer/Project Service (3)`, `API Key Service (4)`, `Tenant Service (5)`, `Quota Service (20)`, `Admin Service (27)`, `Configuration Service (28)`, `Feature Flag Service (29)`.
- **Requirement:** Low-latency RESTful developer interactions, key generation, project lifecycle management, and high-accuracy quota admission control.
- **Architecture:** Go Modular Monolith with strictly decoupled internal Go packages sharing a shared PostgreSQL connection pool (via PgBouncer) and internal event dispatchers.
- **Technology:** Go 1.23, `chi` router, `pgx/v5` connection pool, Redis Cluster client (`go-redis/v9`).
- **Trade-off:** Single deployable unit means control plane updates restart all REST endpoints, but eliminates network hops for key validation and tenant resolution.
- **Failure Mode:** Database connection exhaustion under burst.
- **Mitigation/Implementation:** Bounded worker pool; PgBouncer connection multiplexing; Redis token-bucket pre-filtering before database touches.
- **Testing:** Integration tests with `testcontainers-go` spinning up PostgreSQL and Redis.
- **Observability:** Prometheus metrics (`http_requests_total`, `http_request_duration_seconds{status, path}`), OpenTelemetry HTTP server traces.
- **Operations:** Rolling zero-downtime deployment via Kubernetes Deployment with readiness probes checking DB and Redis ping.

### 2.2 Deployable Unit 2: Real-Time Streaming Edge Gateway
- **Logical Services Consolidated:**
  `Session Service (6)`, `Audio Ingestion Service (7)`.
- **Requirement:** Terminate 100,000 concurrent long-lived WebSocket and WebRTC connections, handle frame validation, and manage client heartbeats.
- **Architecture:** Dedicated stateful edge gateway with epoll-driven network I/O, ring buffering, and zero heap-allocation frame handling.
- **Technology:** Go 1.23, `gorilla/websocket`, `pion/webrtc/v3`.
- **Trade-off:** High memory footprint per connection (~16KB per WebSocket socket buffer). Requires careful OS socket tuning (`sysctl net.ipv4.tcp_rmem`).
- **Failure Mode:** Network partition or pod termination dropping thousands of active audio calls.
- **Mitigation/Implementation:** Graceful drain handler (`SIGTERM`) sending `1012 Service Restart` to clients with a staggered 5-second reconnect window.
- **Testing:** Soak testing with Locust and custom WebRTC headless load generators simulating 10k concurrent streams with packet drop injection.
- **Observability:** `active_websocket_connections`, `audio_bytes_ingested_total`, `jitter_buffer_drops_total`.
- **Operations:** Horizontal Pod Autoscaler (HPA) driven by Custom Metrics: `websocket_connection_density` (target: 3,000 conns/pod).

### 2.3 Deployable Unit 3: Streaming Pipeline Orchestrator
- **Logical Services Consolidated:**
  `Text Normalization Service (11)`, `Conversation Service (13)`, `Streaming Orchestrator (14)`, `Provider Gateway (24)`, `Model Routing Service (25)`.
- **Requirement:** Coordinate the real-time linear dataflow: Audio Chunk $\to$ VAD $\to$ STT $\to$ MT $\to$ TTS $\to$ Audio Output Chunk while executing provider fallback decisions within 50ms.
- **Architecture:** Pipeline Actor pattern using Go channels and goroutines. Each active voice session is represented by a dedicated `SessionOrchestrator` goroutine.
- **Technology:** Go 1.23, Google gRPC client pools with HTTP/2 subchannel multiplexing.
- **Trade-off:** Stateful in-memory execution loop per active session requires Redis state checkpointing for crash-recovery.
- **Failure Mode:** STT provider stall halts the entire pipeline, inducing client perceived lag.
- **Mitigation/Implementation:** Circuit breaker per provider model with a strict 400ms timeout on STT partial emissions before triggering fallback inference.
- **Testing:** Chaos fault injection via Toxiproxy simulating provider latency spikes and dropped gRPC frames.
- **Observability:** `pipeline_stage_duration_seconds{stage="vad|stt|mt|tts"}`, `provider_fallback_total{from_provider, to_provider, reason}`.
- **Operations:** Scaled independently based on `active_processing_pipelines`.

### 2.4 Deployable Unit 4: Specialized AI Inference Workers
- **Logical Services Consolidated:**
  `Speech Recognition Service (8)`, `Language Detection Service (9)`, `Translation Service (10)`, `Speech Synthesis Service (12)`.
- **Requirement:** High-throughput, sub-100ms tensor execution for audio feature extraction, sequence-to-sequence translation, and neural vocoding.
- **Architecture:** Independent, GPU-isolated microservice containers sharing zero local disks and communicating strictly via gRPC streaming.
- **Technology:**
  - STT: C++ runtime with TensorRT-LLM running Conformer-CTC / Whisper v3 Turbo.
  - Translation: Python 3.12 / vLLM runtime running NLLB-200 / LLaMA-3-8B with continuous batching.
  - TTS: C++ / ONNX Runtime running Kokoro-82M and Piper neural vocoder.
- **Trade-off:** High GPU cloud cost ($1.20/hr per A10G instance). Requires strict autoscaling to prevent idle resource wastage.
- **Failure Mode:** GPU CUDA Out of Memory (OOM) under unexpected batch size burst.
- **Mitigation/Implementation:** Hard batch memory caps in TensorRT; continuous dynamic batching in vLLM; client audio duration validation at ingestion before queueing.
- **Testing:** PyTest GPU regression test suites evaluating WER and BLEU scores across 20 languages.
- **Observability:** DCGM GPU telemetry (`DCGM_FI_DEV_GPU_UTIL`, `DCGM_FI_DEV_MEM_COPY_UTIL`), `inference_latency_milliseconds{model}`.
- **Operations:** K8s Cluster Autoscaler provisioning spot and on-demand GPU instances via Karpenter on AWS.

### 2.5 Deployable Unit 5: Async Job, Metering & Webhook Workers
- **Logical Services Consolidated:**
  `Job Service (15)`, `Workflow Service (16)`, `Notification Service (17)`, `Usage Metering Service (18)`, `Billing Service (19)`, `File/Media Service (21)`, `Analytics Service (22)`, `Audit Service (23)`, `Quality Evaluation Service (26)`.
- **Requirement:** Zero-data-loss processing of large audio files, asynchronous webhook dispatch with retries, and batch insertion of billable usage events.
- **Architecture:** Event-driven pull consumers subscribed to NATS JetStream durable subject streams.
- **Technology:** Rust 1.81, `tokio` async runtime, `sqlx` connection pool, AWS SDK for S3.
- **Trade-off:** Asynchronous eventual consistency for billing totals (up to 3 seconds latency between audio completion and dashboard balance update).
- **Failure Mode:** Downstream customer webhook endpoint outage blocking worker threads.
- **Mitigation/Implementation:** Exponential backoff retry with jitter across 72 hours; dead-letter queue (DLQ) after 10 failed delivery attempts.
- **Testing:** Load testing with mocked webhook endpoints returning HTTP 500/504 errors.
- **Observability:** `nats_consumer_lag_messages`, `webhook_delivery_attempts_total{status}`, `billing_flush_duration_seconds`.
- **Operations:** KEDA-driven autoscaling based on NATS JetStream consumer backlog.
