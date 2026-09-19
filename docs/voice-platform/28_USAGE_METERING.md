# VoxBridge AI — Usage Metering & Quota Enforcement Engine

## 1. Usage Metering Dataflow Pipeline (Mermaid Diagram 22)

The Usage Metering architecture guarantees 100% financial auditability, zero data loss, and sub-second quota deduction by ingesting atomic billable unit ticks through NATS JetStream into TimescaleDB and Redis.

```mermaid
graph TB
    subgraph BillableSources["Billable Workload Engines"]
        StreamOrch["Streaming Orchestrator (Audio Seconds Ticks)"]
        NMTWorker["Translation Worker (Character / Token Ticks)"]
        TTSWorker["Speech Synthesis Worker (Synthesized Seconds Ticks)"]
        BatchWorker["Batch Job Processor (File Processing Ticks)"]
    end

    subgraph MeteringBus["NATS JetStream (Stream: VOX_METERING)"]
        SubjectMeter["voxbridge.metering.usage.> (Raft Persisted)"]
    end

    subgraph MeteringEngine["High-Throughput Metering Daemon (Go)"]
        direction TB
        BatchCollector["Micro-Batch Buffer (5,000 events OR 500ms flush)"]
        QuotaValidator["Atomic Token-Bucket Deductor (Redis Lua)"]
        PriceEvaluator["Dynamic Contract Price Evaluator (Tenant Matrix)"]
        DBInserter["High-Velocity COPY Inserter (TimescaleDB)"]

        BatchCollector --> QuotaValidator
        QuotaValidator --> PriceEvaluator
        PriceEvaluator --> DBInserter
    end

    subgraph StorageSinks["Persistence & Quota Stores"]
        RedisQuotas[("Redis Quota Clusters (Active Counter)")]
        TimescaleStore[("TimescaleDB (Hypertable: usage_records)")]
    end

    subgraph ExternalBilling["Billing & Accounting Sinks"]
        StripeMetering["Stripe Metering API (v2 Usage Records)"]
        CustomerAudit["Customer Usage Dashboard & Invoices"]
    end

    StreamOrch -->|Publish Usage Tick| SubjectMeter
    NMTWorker -->|Publish Usage Tick| SubjectMeter
    TTSWorker -->|Publish Usage Tick| SubjectMeter
    BatchWorker -->|Publish Usage Tick| SubjectMeter

    SubjectMeter -->|Durable Pull (Max Batch 5000)| BatchCollector
    QuotaValidator -->|Atomic DECRBY| RedisQuotas
    DBInserter -->|Bulk SQL COPY| TimescaleStore
    DBInserter -->|Nightly Aggregation Sync| StripeMetering
    TimescaleStore -->|Real-Time Analytics Query| CustomerAudit
```

---

## 2. Immutable Usage Record Schema

In compliance with financial auditability standards, every billable operation generates an unalterable append-only record. Invoices are never generated from mutable counters; they are deterministically synthesized by summing immutable usage events.

```sql
CREATE TABLE usage_records (
    id UUID PRIMARY KEY, -- UUIDv7
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    session_id UUID,
    operation_type VARCHAR(32) NOT NULL, -- STT_STREAMING, MT_CHARACTERS, TTS_SYNTHESIS
    provider_id VARCHAR(64) NOT NULL,
    model_id VARCHAR(64) NOT NULL,
    
    -- Metered Dimensions
    input_audio_seconds NUMERIC(10, 3) NOT NULL DEFAULT 0.000,
    output_audio_seconds NUMERIC(10, 3) NOT NULL DEFAULT 0.000,
    character_count INTEGER NOT NULL DEFAULT 0,
    token_count INTEGER NOT NULL DEFAULT 0,
    
    -- Financial Tracking
    unit_price_usd NUMERIC(12, 8) NOT NULL,
    total_cost_usd NUMERIC(10, 6) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- Metadata & Time
    idempotency_hash VARCHAR(64) NOT NULL UNIQUE,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CLOCK_TIMESTAMP()
);
```

---

## 3. Quota Management & Admission Control

Tenants are assigned configurable monthly usage and concurrency ceilings:

```
[INCOMING STREAM REQUEST]
            │
            ▼
    [Check Redis Counter: active_concurrent_streams:{tenant_id}]
            │
            ├─► Count >= Max Concurrency (e.g., 50 streams)
            │       │
            │       ▼
            │   [Reject with 429 TOO_MANY_REQUESTS / CONCURRENCY_EXCEEDED]
            │
            ▼ Count < Max Concurrency
    [Check Monthly Spend: monthly_spend_usd:{tenant_id}]
            │
            ├─► Spend >= Hard Limit ($10,000)
            │       │
            │       ▼
            │   [Reject with 402 PAYMENT_REQUIRED / QUOTA_EXHAUSTED]
            │
            ├─► Spend >= Soft Warning Limit (80% / $8,000)
            │       │
            │       ├─► Emit Webhook: quota.warning_80
            │       ├─► Send Email to Billing Admin
            │       ▼
            │   [Allow Stream to Proceed]
            │
            ▼ Spend < Soft Limit
    [ALLOW REQUEST TO INITIALIZE STREAM]
```

### 3.1 Grace Periods & Burst Allowance
- **Burst Allowance:** Enterprise tenants are permitted a **15% temporary concurrency burst** for up to 5 minutes to absorb unexpected call-center traffic spikes without dropping customer calls.
- **Credit Exhaustion Grace Period:** If a prepaid balance falls to zero during an active 30-minute real-time voice call, the call is **NEVER** terminated abruptly. The call is allowed to complete gracefully, and the resulting negative balance is automatically deducted from the next balance top-up or invoice cycle.
