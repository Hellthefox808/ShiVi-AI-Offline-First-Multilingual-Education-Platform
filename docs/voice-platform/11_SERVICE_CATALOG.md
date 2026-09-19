# VoxBridge AI — Enterprise Service Catalog

This catalog documents the operational specifications, business ownership, network ports, dependencies, SLOs, and incident response procedures for all 30 logical domains across VoxBridge AI's 5 physical deployable units.

---

## 1. Enterprise Service Inventory

| # | Service Name | Physical Deployable | Capability | Port & Protocol | Tier | Owner |
|---|---|---|---|---|---|---|
| 1 | `api-gateway` | Deployable Unit 1 | Perimeter auth, routing, SSL termination | 443 (HTTPS), 8080 (gRPC) | Tier 1 | Platform Edge Team |
| 2 | `identity-service` | Deployable Unit 1 | User authentication, SSO, RBAC | Internal gRPC / SQL | Tier 1 | Security Engineering |
| 3 | `project-service` | Deployable Unit 1 | Organization, Workspace, Project management | Internal gRPC / SQL | Tier 2 | Core Backend Team |
| 4 | `api-key-service` | Deployable Unit 1 | Key generation, hashing, verification | Internal gRPC / Cache | Tier 1 | Security Engineering |
| 5 | `tenant-service` | Deployable Unit 1 | Multi-tenant policy, isolation rules | Internal gRPC / SQL | Tier 1 | Core Backend Team |
| 6 | `session-service` | Deployable Unit 2 | Session state, token issuance, lease | 8082 (gRPC) | Tier 1 | Real-Time Media Team |
| 7 | `audio-ingestion-service` | Deployable Unit 2 | Frame validation, Opus decode, jitter buffer | 8443 (WSS), UDP (WebRTC) | Tier 1 | Real-Time Media Team |
| 8 | `stt-service` | Deployable Unit 4 | Conformer/Whisper streaming transcription | 50051 (gRPC) | Tier 1 | Speech AI Team |
| 9 | `language-detection-service` | Deployable Unit 4 | Acoustic and text-based LID | Internal gRPC | Tier 1 | Speech AI Team |
| 10 | `translation-service` | Deployable Unit 4 | NLLB-200 / vLLM machine translation | 50052 (gRPC) | Tier 1 | NLP / AI Team |
| 11 | `text-normalization-service` | Deployable Unit 3 | Inverse text norm, numbers, profanity | Internal gRPC | Tier 2 | Core Backend Team |
| 12 | `tts-service` | Deployable Unit 4 | Kokoro / Piper streaming neural synthesis | 50053 (gRPC) | Tier 1 | Speech AI Team |
| 13 | `conversation-service` | Deployable Unit 3 | Turn-taking, multi-party speaker state | Internal gRPC | Tier 2 | Real-Time Media Team |
| 14 | `streaming-orchestrator` | Deployable Unit 3 | Pipeline coordination (VAD->STT->MT->TTS) | 9090 (gRPC) | Tier 1 | Real-Time Media Team |
| 15 | `job-service` | Deployable Unit 5 | Asynchronous batch job management | 8085 (gRPC / REST) | Tier 2 | Data Platform Team |
| 16 | `workflow-service` | Deployable Unit 5 | Multi-step batch orchestration | Internal NATS Worker | Tier 2 | Data Platform Team |
| 17 | `notification-service` | Deployable Unit 5 | HMAC webhook delivery and dead-lettering | Outbound HTTPS | Tier 2 | Developer Platform |
| 18 | `usage-metering-service` | Deployable Unit 5 | Immutable usage ticks and aggregation | Internal NATS Consumer | Tier 1 | Billing & FinTech Team |
| 19 | `billing-service` | Deployable Unit 5 | Invoicing, credits, Stripe synchronization | 8086 (REST / Stripe) | Tier 1 | Billing & FinTech Team |
| 20 | `quota-service` | Deployable Unit 1 | Concurrency and monthly usage limits | Internal Redis Lua / gRPC | Tier 1 | Platform SRE Team |
| 21 | `media-service` | Deployable Unit 5 | Audio storage, pre-signed URLs, lifecycle | 8087 (S3 Client API) | Tier 2 | Storage SRE Team |
| 22 | `analytics-service` | Deployable Unit 5 | Aggregated metrics, latency percentiles | SQL / ClickHouse | Tier 3 | Data Platform Team |
| 23 | `audit-service` | Deployable Unit 5 | Immutable compliance and change logs | Internal NATS Consumer | Tier 2 | Security & Compliance |
| 24 | `provider-gateway` | Deployable Unit 3 | Third-party cloud vendor dispatch | Internal gRPC / HTTPS | Tier 1 | AI Systems Team |
| 25 | `model-routing-service` | Deployable Unit 3 | Dynamic fallback, cost/latency router | Internal gRPC / Cache | Tier 1 | AI Systems Team |
| 26 | `quality-evaluation-service`| Deployable Unit 5 | Offline WER, COMET, BLEU evaluation | Batch Worker | Tier 3 | ML Quality Team |
| 27 | `admin-service` | Deployable Unit 1 | Internal back-office administration | 8088 (HTTPS Admin) | Tier 2 | Platform SRE Team |
| 28 | `configuration-service` | Deployable Unit 1 | Dynamic runtime tenant configuration | Internal gRPC / Cache | Tier 1 | Platform SRE Team |
| 29 | `feature-flag-service` | Deployable Unit 1 | Progressive rollouts, A/B model tests | Internal gRPC / Cache | Tier 1 | Platform SRE Team |
| 30 | `observability-layer` | Cross-Cutting | OpenTelemetry Collector, Prometheus sink | 4317 (OTLP gRPC) | Tier 1 | Observability SRE |

---

## 2. Deep-Dive Service Specification Profiles

### 2.1 Service 01: `api-gateway`
- **Business Capability:** The perimeter ingress for all RESTful operations, developer authentication, rate limiting, and project configuration.
- **Service Owner:** Platform Edge Team (`#team-edge-platform`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/api-gateway`.
- **Ports & Protocol:** Public Port 443 (HTTPS/2, TLS 1.3), Internal Port 8080 (gRPC mTLS).
- **Dependencies:** Redis Cluster (Rate limits, token cache), PostgreSQL (Auth rules), NATS JetStream (Audit events).
- **Database:** Reads from PostgreSQL 18 `organizations`, `projects`, `api_keys`.
- **Cache:** Redis 7.4 Cluster (`ratelimit:{key}`, `auth:{key_hash}`).
- **Events Emitted:** `voxbridge.events.auth.login`, `voxbridge.events.apikey.revoked`.
- **Primary Public APIs:** `/v1/auth/*`, `/v1/projects/*`, `/v1/api-keys/*`, `/v1/sessions/*`.
- **SLO:** 99.99% Availability; p95 Latency $< 25\text{ms}$; 5xx Error Rate $< 0.01\%$.
- **Scaling Profile:** HPA scaling based on HTTP request rate (Target: 70% CPU, min 4, max 30 replicas).
- **Security:** Strict WAF filtering, IETF Idempotency-Key validation, mTLS with SPIRE internally.
- **Alerts:** `High5xxErrorRate` (>0.1% over 2m), `RedisRateLimitFailure` (Redis connectivity drop).
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/api-gateway-outage`.
- **Deployment & Rollback:** Helm rolling upgrade with ArgoCD; automated rollback on canary failure.

### 2.2 Service 07: `audio-ingestion-service`
- **Business Capability:** Ingests raw audio packets, decodes containers, validates formats, buffers jitter, and executes real-time Voice Activity Detection.
- **Service Owner:** Real-Time Media Team (`#team-media-infra`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/streaming-gateway`.
- **Ports & Protocol:** Public Port 8443 (WSS over TLS 1.3), UDP 10000-20000 (WebRTC DTLS/SRTP).
- **Dependencies:** `session-service` (Token validation), `streaming-orchestrator` (gRPC pipe).
- **Database:** None (Stateless compute).
- **Cache:** Redis (Active connection registration).
- **Events Emitted:** `voxbridge.events.audio.started`, `voxbridge.events.speech.detected`.
- **SLO:** 99.95% Connection Success Rate; Audio Ingestion Jitter Buffer Delay $< 20\text{ms}$.
- **Scaling Profile:** HPA scaling on Active WebSocket Connection Count (Target: 2,500 connections/pod).
- **Security:** Validates audio headers, enforces max audio duration (4 hours), rejects malformed Opus payloads.
- **Alerts:** `HighDroppedAudioFrames` (>1% over 1m), `WebSocketSuddenDrop` (>15% disconnect in 30s).
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/streaming-gateway-congestion`.
- **Deployment:** Blue/Green deployment with connection draining over 5 minutes.

### 2.3 Service 08: `stt-service` (Speech Recognition Worker)
- **Business Capability:** Converts real-time and batch audio chunks into high-fidelity text transcripts with millisecond-precision timestamps.
- **Service Owner:** Speech AI Team (`#team-speech-ai`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/ai-stt-worker`.
- **Ports & Protocol:** Internal Port 50051 (gRPC streaming over mTLS).
- **Dependencies:** NVIDIA Triton / TensorRT runtime, GPU drivers.
- **Database:** None.
- **Cache:** Shared NVMe local model weights (`/models/conformer-ctc-v2/`).
- **Events Emitted:** None directly (Streams gRPC partial and final text chunks to orchestrator).
- **SLO:** p95 Time to First Token (TTFT) $< 250\text{ms}$; Word Error Rate (WER) $< 8.5\%$ on clean audio.
- **Scaling Profile:** Custom Metrics HPA scaling on GPU Tensor Core Utilization (Target: 75%).
- **Security:** Non-routable private subnet; no outbound internet access; zero local audio persistence.
- **Alerts:** `CUDAOutOfMemoryAlert`, `STTLatencyExceeded` (p95 > 450ms over 3m).
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/stt-worker-gpu-failure`.
- **Deployment:** Rolling canary across GPU nodepools with warm-up synthetic audio inference checks.

### 2.4 Service 10: `translation-service` (Machine Translation Worker)
- **Business Capability:** Translates conversational and formal text across 100+ language pairs with custom glossary injection.
- **Service Owner:** NLP / AI Team (`#team-nlp-platform`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/ai-translation-worker`.
- **Ports & Protocol:** Internal Port 50052 (gRPC over mTLS).
- **Dependencies:** vLLM / CTranslate2 runtime, NVIDIA A10G GPUs.
- **Database:** None.
- **Cache:** In-memory glossary prefix trees (Trie) for custom customer terminology.
- **Events Emitted:** None directly (Returns translated sentence tokens).
- **SLO:** p95 Translation Latency $< 120\text{ms}$; BLEU Score preservation $\ge 38.0$.
- **Scaling Profile:** HPA scaling based on Queue Token Depth.
- **Security:** Ephemeral token memory; prompt injection sanitization; zero external API leakage.
- **Alerts:** `TranslationLatencySpike`, `GlossaryInjectionFailure`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/translation-model-crash`.

### 2.5 Service 12: `tts-service` (Speech Synthesis Worker)
- **Business Capability:** Synthesizes translated text into natural, expressive speech audio streams (Opus/PCM) with custom voice matching.
- **Service Owner:** Speech AI Team (`#team-speech-ai`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/ai-tts-worker`.
- **Ports & Protocol:** Internal Port 50053 (gRPC streaming over mTLS).
- **Dependencies:** ONNX Runtime, CUDA 12.4, Piper / Kokoro vocoder.
- **Database:** None.
- **Cache:** S3 voice embedding vector cache.
- **Events Emitted:** None directly (Streams chunked Opus audio back to orchestrator).
- **SLO:** p95 Time to First Audio (TTFA) $< 300\text{ms}$; Real-Time Factor (RTF) $< 0.15$.
- **Scaling Profile:** HPA scaling on active synthesis pipelines.
- **Security:** Voice cloning authorization validation; zero audio storage on worker nodes.
- **Alerts:** `TTSRealTimeFactorDegradation` (RTF > 0.3), `SynthesisAudioClipping`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/tts-synthesis-exhaustion`.

### 2.6 Service 18: `usage-metering-service`
- **Business Capability:** Collects immutable billable usage events (audio seconds, characters, tokens) and performs real-time quota deduction and billing reconciliation.
- **Service Owner:** Billing & FinTech Team (`#team-billing`).
- **Repository:** `github.com/voxbridge/voxbridge-core/services/usage-metering`.
- **Ports & Protocol:** NATS JetStream Consumer (Internal).
- **Dependencies:** NATS JetStream, Redis (Quota counters), PostgreSQL (TimescaleDB hypertable).
- **Database:** PostgreSQL 18 `usage_records` table (TimescaleDB partitioned by week).
- **Cache:** Redis atomic counters (`quota:{tenant_id}:current_seconds`).
- **Events Emitted:** `voxbridge.events.quota.warning_80`, `voxbridge.events.quota.exhausted`.
- **SLO:** 100% Audit Event Durability; Event Ingestion Lag $< 1,000\text{ms}$.
- **Scaling Profile:** Fixed 3-node HA consumer group with partition hashing.
- **Security:** Cryptographic sequence hash validation; read-only database user for billing audits.
- **Alerts:** `MeteringConsumerLagHigh` (>10,000 unread messages), `QuotaDeductionMismatch`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/metering-backlog-recovery`.
