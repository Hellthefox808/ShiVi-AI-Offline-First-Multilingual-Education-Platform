# VoxBridge AI — Asynchronous Event-Driven Architecture

## 1. Event Architecture Topology (Mermaid Diagram 12)

The Event Architecture topology illustrates how immutable business and operational events flow asynchronously through VoxBridge AI via **NATS JetStream 2.10**, providing decouple-on-write persistence, guaranteed consumer group delivery, dead-lettering, and auditing.

```mermaid
graph TB
    subgraph Producers["Event Producers"]
        APIGW["API Gateway (Auth & Key Events)"]
        StreamOrch["Stream Orchestrator (Session & Utterance Events)"]
        BatchWorker["Batch Job Worker (Job Status Transitions)"]
        BillingWorker["Metering Engine (Billable Unit Ticks)"]
    end

    subgraph NATS_Cluster["NATS JetStream 2.10 Distributed Broker (Raft 3-Node)"]
        direction TB
        subgraph Stream_Sessions["Stream: VOX_SESSIONS"]
            Sub_Sess["voxbridge.events.session.>"]
        end
        subgraph Stream_Jobs["Stream: VOX_JOBS"]
            Sub_Jobs["voxbridge.events.job.>"]
        end
        subgraph Stream_Metering["Stream: VOX_METERING (Durable In-Memory)"]
            Sub_Meter["voxbridge.metering.usage.>"]
        end
        subgraph Stream_Audit["Stream: VOX_AUDIT (Encrypted File Storage)"]
            Sub_Audit["voxbridge.audit.compliance.>"]
        end
        subgraph Stream_DLQ["Stream: VOX_DEAD_LETTER"]
            Sub_DLQ["voxbridge.dlq.>"]
        end
    end

    subgraph Consumers["Durable Consumer Groups"]
        WebhookDeliverer["Webhook Delivery Service (Push/Ack)"]
        AuditArchiver["Audit & Compliance Archiver (TimescaleDB)"]
        BillingAggregator["Billing & Invoice Aggregator (Stripe Sync)"]
        RealtimeAnalytics["ClickHouse Ingestion Engine"]
    end

    subgraph ExternalSinks["External Systems & Storage"]
        CustomerWebhook["Customer HTTPS Webhook Destination"]
        TimescaleDB[("TimescaleDB (Usage Hypertable)")]
        ClickHouseDB[("ClickHouse OLAP Analytics")]
        DeadLetterStore[("S3 Quarantine Bucket")]
    end

    %% Publishing
    APIGW -->|Publish (ACK)| Sub_Audit
    StreamOrch -->|Publish (ACK)| Sub_Sess
    StreamOrch -->|Publish (ACK)| Sub_Meter
    BatchWorker -->|Publish (ACK)| Sub_Jobs
    BillingWorker -->|Publish (ACK)| Sub_Meter

    %% Consumption
    Sub_Jobs -->|Durable Pull (Max Ack: 30s)| WebhookDeliverer
    Sub_Sess -->|Durable Pull| RealtimeAnalytics
    Sub_Meter -->|Durable Pull (Batch 1000)| BillingAggregator
    Sub_Audit -->|Durable Pull| AuditArchiver

    %% Execution & DLQ
    WebhookDeliverer -->|Signed POST (HMAC)| CustomerWebhook
    WebhookDeliverer -.->|Max Retries Exceeded (10x)| Sub_DLQ
    BillingAggregator -->|SQL Batch COPY| TimescaleDB
    AuditArchiver -->|Store Encrypted Log| TimescaleDB
    RealtimeAnalytics -->|Insert| ClickHouseDB
    Sub_DLQ -->|Archive Payload| DeadLetterStore
```

---

## 2. CloudEvents 1.0 Specification Compliance

All events emitted across the VoxBridge ecosystem conform strictly to the CNCF **CloudEvents v1.0** specification in structured JSON mode.

### 2.1 Standard CloudEvents Envelope Schema
```json
{
  "specversion": "1.0",
  "id": "evt_01J8W3D4E5F6G7H8J9K0L1M2N3",
  "source": "https://api.voxbridge.ai/services/stream-orchestrator",
  "type": "ai.voxbridge.session.utterance.completed",
  "datacontenttype": "application/json",
  "dataschema": "https://schemas.voxbridge.ai/v1/session.utterance.completed.json",
  "time": "2026-09-19T14:40:02.150Z",
  "subject": "sess_01J8V3M4K5N6P7Q8R9S0T1U2V3",
  "traceparent": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "voxorganizationid": "org_01J8ABCDEF1234567890",
  "voxprojectid": "proj_01J8ABCDEF1234567891",
  "data": {
    "utterance_id": "utt_01J8W3D4E5",
    "sequence_number": 42,
    "source_language": "en-US",
    "target_language": "hi-IN",
    "audio_duration_ms": 3200,
    "source_transcript": "Good afternoon, please state your inquiry.",
    "translated_text": "शुभ दोपहर, कृपया अपना प्रश्न बताएं।",
    "confidence": 0.962,
    "stt_provider": "vox_conformer_v2",
    "mt_provider": "vox_nllb_200",
    "tts_provider": "vox_kokoro_v1",
    "billable_audio_seconds": 3.2,
    "billable_translation_characters": 43,
    "billable_tts_characters": 35
  }
}
```

---

## 3. Stream Configuration & Ordering Guarantees

| Stream Name | Storage Backend | Retention Policy | Deduplication Window | Replication Factor | Maximum Age |
|---|---|---|---|---|---|
| `VOX_SESSIONS` | `FileStorage` | `LimitsPolicy` (Discard Old) | 60 seconds (by `Nats-Msg-Id`) | 3 Replicas (Raft) | 7 Days |
| `VOX_JOBS` | `FileStorage` | `WorkQueue` (Removed on Ack) | 120 seconds | 3 Replicas (Raft) | 30 Days |
| `VOX_METERING` | `FileStorage` | `LimitsPolicy` | 300 seconds | 3 Replicas (Raft) | 90 Days |
| `VOX_AUDIT` | `FileStorage` (Encrypted) | `InterestPolicy` | 600 seconds | 3 Replicas (Raft) | 365 Days |
| `VOX_DEAD_LETTER` | `FileStorage` | `LimitsPolicy` | None | 3 Replicas (Raft) | 180 Days |

### 3.1 Strict Partition Ordering
- **Partition Key:** The CloudEvents `subject` field (containing `session_id` or `job_id`) is used as the NATS message subject routing token:
  `voxbridge.events.session.{tenant_id}.{session_id}`.
- This ensures that all partial, final, and output audio frames for a specific voice session are strictly sequenced through the exact same JetStream stream partition, preventing race conditions or out-of-order delivery to downstream listeners.

---

## 4. Webhook Retry Policy & Dead-Letter Queue (DLQ) Lifecycle

```
[Event Generated] 
       │
       ▼
[NATS Consumer: Webhook Dispatcher]
       │
       ├─► (Attempt 1: Immediate POST) ──► HTTP 200/204 ──► [ACK NATS Message]
       │
       └─► HTTP 5xx / Timeout (5s)
               │
               ▼ (Exponential Backoff: base = 2s, factor = 2.0, jitter = ±20%)
           [Retry Schedule: 2s, 4s, 8s, 16s, 32s, 64s, 128s, 256s, 512s, 1024s]
               │
               ├─► Successful Delivery ──► [ACK NATS Message]
               │
               └─► Max Retries Exceeded (10 Attempts / ~34 Minutes)
                       │
                       ▼
               [Publish to VOX_DEAD_LETTER]
                       │
                       ├─► Emit Metric: webhook_dlq_insertions_total
                       ├─► Alert PagerDuty (if Enterprise Tenant)
                       └─► Archive JSON Payload to Encrypted S3 Quarantine
```
