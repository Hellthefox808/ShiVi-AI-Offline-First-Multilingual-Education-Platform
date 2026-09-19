# VoxBridge AI — Architectural Decision Records (ADR)

This document compiles the foundational Architectural Decision Records (ADRs) governing the technical design of the VoxBridge AI Voice Translation Platform.

---

## ADR-001: Decoupled Hybrid Architecture (Go + Rust + Python/GPU) vs. Pure Language Stack

- **Status:** **ACCEPTED**
- **Date:** 2026-09-19
- **Deciders:** Principal Architect, Distributed Systems Architect, ML Systems Architect

### Context
Voice translation requires three fundamentally incompatible computational profiles:
1. Low-latency, high-concurrency network I/O to handle 100k persistent WebSockets and demux audio frames.
2. High-throughput, CPU-safe batch file processing and financial ledger persistence.
3. Heavy tensor execution (matrix multiplication) over CUDA/C++ runtimes for neural speech models.
A pure Python backend suffers from GIL bottlenecks and high memory usage per socket. A pure Rust stack increases development cycle time for rapid REST/auth features. A pure Go stack lacks native first-class PyTorch/TensorRT model serving ecosystems.

### Decision
We adopt a **Decoupled Hybrid Architecture**:
- **Go 1.23:** Powers the API Gateway, Real-Time Streaming Gateway, and Session Orchestrator (superior goroutine epoll concurrency, rapid development, rich cloud ecosystem).
- **Rust 1.81:** Powers asynchronous batch audio transcoding and high-integrity billing reconciliation workers (memory safety without GC pauses, zero-cost abstractions).
- **Python 3.12 / C++ TensorRT:** Dedicated isolated GPU inference workers communicating strictly via gRPC.

### Consequences
- **Positive:** Each layer operates at maximum hardware efficiency. Sub-20ms WebSocket demuxing in Go; maximum GPU saturation in TensorRT; zero data corruption in Rust financial workers.
- **Negative:** Polyglot stack requires CI pipelines supporting Go, Rust, and Python; developers must adhere to standardized gRPC Protobuf contracts.

---

## ADR-002: WebSockets as Primary Streaming Transport vs. WebRTC-Only

- **Status:** **ACCEPTED**
- **Date:** 2026-09-19

### Context
Real-time voice streaming demands sub-100ms transport latency. WebRTC provides ultra-low latency UDP transport via SRTP/SCTP, but requires complex ICE/STUN/TURN infrastructure, frequently fails across strict corporate/banking proxy firewalls, and adds significant SDK client weight.

### Decision
We select **WebSockets over TLS (RFC 6455) as the primary ingestion transport**, while supporting **WebRTC as an optional advanced carrier bridge**:
- Standard web and mobile apps connect via WSS on standard HTTPS Port 443 with custom 4-byte binary framing.
- Telephony carriers and live interactive video meeting bridges connect via WebRTC/SIP proxies.

### Consequences
- **Positive:** 100% traversal of enterprise firewalls, zero STUN/TURN server bandwidth fees for standard users, lightweight client SDKs.
- **Negative:** TCP head-of-line blocking under extreme packet loss (>15%). Mitigated by server-side jitter buffering and fast tail-drop policies.

---

## ADR-003: NATS JetStream as Event Broker vs. Apache Kafka

- **Status:** **ACCEPTED**
- **Date:** 2026-09-19

### Context
The platform requires durable event streaming for session lifecycles, batch jobs, webhooks, and billing records. Apache Kafka provides battle-tested throughput but imposes heavy operational complexity (ZooKeeper/KRaft, JVM heap tuning, high idle resource footprint). RabbitMQ lacks native distributed event replay and stream persistence.

### Decision
We select **NATS JetStream 2.10** running a 3-node Raft consensus cluster.

### Consequences
- **Positive:** Sub-millisecond publish latency, single static Go binary with $< 100 \text{ MB}$ memory footprint, native subject wildcards (`voxbridge.*.org_id.>`), built-in message deduplication, and lightweight pull consumers.
- **Negative:** Smaller enterprise third-party ecosystem compared to Kafka; requires engineering familiarity with NATS subject hierarchies.

---

## ADR-004: PostgreSQL 18 with UUIDv7 as System of Record vs. NoSQL

- **Status:** **ACCEPTED**
- **Date:** 2026-09-19

### Context
A voice platform manages relational metadata (organizations, projects, keys, permissions) and write-heavy time-series records (usage ticks, transcripts). Pure NoSQL databases (DynamoDB, MongoDB) lack native relational joins, ACID double-entry accounting guarantees, and fine-grained Row-Level Security (RLS).

### Decision
We select **PostgreSQL 18 Multi-AZ (with TimescaleDB extension)** as the single system of record, enforcing **UUIDv7** for all primary keys.

### Consequences
- **Positive:** Full ACID guarantees for financial billing, native declarative Row-Level Security, time-series hypertable compression for usage records, zero B-Tree index fragmentation with sequential UUIDv7.
- **Negative:** Requires active connection pooling via PgBouncer to prevent connection exhaustion.

---

## ADR-005: Kokoro-82M & Piper for Neural Speech Synthesis vs. 1B+ Diffusion Models

- **Status:** **ACCEPTED**
- **Date:** 2026-09-19

### Context
Recent generative voice models (e.g., 1-billion+ parameter diffusion transformers) produce impressive audio but exhibit Real-Time Factors (RTF) of $0.8 - 1.5$, making real-time streaming speech translation economically and technically unviable.

### Decision
We standardize on **Kokoro-82M (GPU)** and **Piper ONNX (CPU)** for real-time speech synthesis.

### Consequences
- **Positive:** Blazing-fast **RTF of 0.12** on NVIDIA A10G; Time to First Audio (TTFA) $< 300\text{ms}$; minimal GPU VRAM footprint (under 2 GB VRAM per worker).
- **Negative:** Highly stylized emotional acting is slightly lower than multi-billion parameter diffusion models, but audio naturalness comfortably exceeds human conversational requirements.
