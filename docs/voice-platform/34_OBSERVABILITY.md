# VoxBridge AI — Observability Architecture & Distributed Tracing

## 1. Observability Pipeline Topology (Mermaid Diagram 20)

VoxBridge AI deploys a unified, OpenTelemetry-native observability pipeline. All services emit structured traces, Prometheus metrics, and JSON logs without exposing customer audio, credentials, or confidential transcripts.

```mermaid
graph TB
    subgraph InstrumentationTier["Instrumented Workload Pods (OpenTelemetry Go/C++/Rust)"]
        APIGW["API Gateway (HTTP Spans & Metrics)"]
        StreamGW["Streaming Gateway (WebSocket Jitter & Audio Ticks)"]
        Orchestrator["Stream Orchestrator (Pipeline Stage Spans)"]
        InferenceWorkers["GPU Workers (Inference Durations & GPU Metrics)"]
    end

    subgraph CollectionTier["Collector Tier (Kubernetes DaemonSet & Cluster Service)"]
        OTelAgent["OTel Collector Agent (DaemonSet: Node Scraping)"]
        OTelGateway["OTel Collector Gateway (Cluster HA: Batching & Redaction)"]
        OTelAgent --> OTelGateway
    end

    subgraph StorageSinks["Telemetry Storage Backends"]
        Prometheus["Prometheus / Mimir (High-Resolution Metrics - 15s)"]
        Tempo["Grafana Tempo / Jaeger (Distributed Traces - 100% Errors, 5% Sampled)"]
        Loki["Grafana Loki (Sanitized Structured JSON Logs)"]
    end

    subgraph Visualization["Visualization & Incident Response"]
        Grafana["Grafana Unified Dashboards (SLO, SRE, Business)"]
        PagerDuty["PagerDuty Incident Escalation (Sev-1 / Sev-2 Alerts)"]
    end

    APIGW -->|OTLP / gRPC (Port 4317)| OTelAgent
    StreamGW -->|OTLP / gRPC| OTelAgent
    Orchestrator -->|OTLP / gRPC| OTelAgent
    InferenceWorkers -->|OTLP / gRPC| OTelAgent

    OTelGateway -->|Remote Write| Prometheus
    OTelGateway -->|OTLP Trace Sink| Tempo
    OTelGateway -->|Loki Push API| Loki

    Prometheus --> Grafana
    Tempo --> Grafana
    Loki --> Grafana

    Prometheus -->|Alertmanager Trigger| PagerDuty
```

---

## 2. Distributed Tracing & W3C Trace Context Propagation

Every incoming request or real-time audio session initializes an OpenTelemetry trace context conforming to the **W3C Trace Context specification**:
`traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01`

### 2.1 Trace Span Hierarchy for a Streaming Utterance
```
[ROOT SPAN: RealtimeSession.ProcessUtterance] (Total: 412ms)
  ├── [SPAN: SileroVAD.DetectSilence] (Duration: 10ms)
  ├── [SPAN: ConformerSTT.DecodeAcousticFrames] (Duration: 135ms)
  │     ├── tag: provider.name = "vox_conformer_v2"
  │     ├── tag: audio.duration_ms = 2400
  │     └── tag: confidence = 0.965
  ├── [SPAN: TranslationEngine.TranslateUtterance] (Duration: 85ms)
  │     ├── tag: nmt.model = "nllb-200-3.3b"
  │     ├── tag: lang.source = "en-US"
  │     └── tag: lang.target = "es-ES"
  └── [SPAN: KokoroTTS.StreamingSynthesize] (Duration: 182ms)
        ├── tag: tts.voice_id = "vox_voice_neural_mateo_es"
        ├── tag: tts.first_audio_byte_ms = 115
        └── tag: tts.rtf = 0.12
```

---

## 3. Telemetry Dimensions & Sanitization Rules

### 3.1 Mandatory Standard Attributes
All emitted spans and log entries MUST include the following non-sensitive attributes:
- `trace_id` (W3C Hex string)
- `span_id` (W3C Hex string)
- `tenant_id` (`org_...`)
- `project_id` (`proj_...`)
- `session_id` (`sess_...` or null for batch)
- `service.name` (`api-gateway`, `stt-worker`, etc.)
- `service.version` (`v1.4.2`)
- `host.region` (`us-east-1`)
- `provider.id` (`vox_internal_gpu`, `azure_speech`, etc.)

### 3.2 Telemetry Sanitization Invariant
The OpenTelemetry Collector Gateway enforces strict redaction processors:
- **Audio Payload Banning:** Binary audio chunks are **NEVER** logged or attached as trace span attributes.
- **Transcript Exclusion:** Raw transcript and translation text strings are barred from production logs and traces. Spans record only character counts, token counts, and cryptographic SHA-256 hashes of the utterance.
- **Credential Stripping:** Any header matching `Authorization`, `Cookie`, or `Idempotency-Key` is masked with `[REDACTED]`.

---

## 4. Real-Time Streaming Core Metrics Catalog

| Metric Name | Type | Unit | Dimensions / Labels | Alert Trigger Condition |
|---|---|---|---|---|
| `vox_active_streaming_sessions` | Gauge | Count | `tenant_id`, `region`, `mode` | Spike $> 200\%$ in 5 minutes |
| `vox_audio_ingested_seconds_total` | Counter | Seconds | `tenant_id`, `codec`, `region` | Sudden drop to zero under active traffic |
| `vox_e2e_voice_translation_latency_seconds` | Histogram | Seconds | `source_lang`, `target_lang`, `provider` | p95 $> 1.200\text{s}$ for 3 minutes |
| `vox_ttft_seconds` (Time to First Token) | Histogram | Seconds | `source_lang`, `stt_model` | p95 $> 0.450\text{s}$ for 3 minutes |
| `vox_ttfa_seconds` (Time to First Audio) | Histogram | Seconds | `target_lang`, `tts_model` | p95 $> 0.850\text{s}$ for 3 minutes |
| `vox_provider_fallback_total` | Counter | Count | `from_provider`, `to_provider`, `reason` | Fallback rate $> 5\%$ of all requests |
| `vox_jitter_buffer_dropped_frames_total` | Counter | Count | `session_id`, `reason` | Dropped frames $> 1\%$ of audio packets |
| `vox_nats_consumer_lag_messages` | Gauge | Count | `stream`, `consumer_group` | Lag $> 5,000$ messages for 2 minutes |
