# 03 — SYSTEM REQUIREMENTS & NON-FUNCTIONAL SPECIFICATIONS (NFR)

> **Document ID:** `VOX-ARCH-03-SYSTEM-REQ`  
> **Platform Name:** VoxBridge AI (Enterprise Multilingual Voice Translation API Platform)  
> **Classification:** Production-Grade Systems Architecture Document  
> **Document Version:** 4.0.0-PROD | **Status:** Active Master Reference  

---

## 1. Quantitative Performance & Latency Budgets

In conversational and real-time streaming translation, perceived responsiveness dictates enterprise viability. Latency is decomposed into measurable milestone budgets:

$$\text{E2E Latency} = T_{\text{Ingest}} + T_{\text{VAD}} + T_{\text{STT}} + T_{\text{MT}} + T_{\text{TTS}} + T_{\text{Egress}} + T_{\text{ClientBuffer}}$$

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                 REAL-TIME STREAMING VOICE LATENCY BUDGET                    │
├────────────────────┬──────────────┬───────────────┬─────────────────────────┤
│ Pipeline Segment   │ P50 Target   │ P95 Target    │ Technical Strategy      │
├────────────────────┼──────────────┼───────────────┼─────────────────────────┤
│ Network Ingest     │ 25 ms        │ 50 ms         │ Edge Anycast + TCP BBR  │
│ VAD Segmentation   │ 40 ms        │ 80 ms         │ Silero C++ ONNX (16ms)  │
│ Streaming STT      │ 220 ms       │ 350 ms        │ Conformer streaming chunk│
│ Machine Translation│ 140 ms       │ 250 ms        │ Speculative token MT    │
│ Streaming TTS First│ 200 ms       │ 350 ms        │ Flow-matching first-byte │
│ Chunk Egress       │ 25 ms        │ 50 ms         │ WebSocket Opus streaming│
│ Client Pre-Buffer  │ 50 ms        │ 70 ms         │ 2-frame jitter buffer   │
├────────────────────┼──────────────┼───────────────┼─────────────────────────┤
│ TOTAL E2E (TTFA)   │ 700 ms       │ 1200 ms       │ Sub-1500 ms SLA Met     │
└────────────────────┴──────────────┴───────────────┴─────────────────────────┘
```

* **Time to First Token (TTFT)**: $\le 450\text{ ms}$ (P95) for text subtitle streams.
* **Time to First Audio (TTFA)**: $\le 1200\text{ ms}$ (P95) for synthesized voice output streams.

---

## 2. Concurrency, Throughput & Sizing Horizons

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                      CAPACITY & THROUGHPUT TARGETS                          │
├───────────────────────────────┬───────────────────┬─────────────────────────┤
│ Architectural Dimension       │ Baseline Tier     │ Enterprise Peak Target  │
├───────────────────────────────┼───────────────────┼─────────────────────────┤
│ Concurrent Streaming Sessions │ 1,000 sessions    │ 25,000 active sessions  │
│ Ingestion Audio Bandwidth     │ 64 Mbps           │ 1.6 Gbps continuous     │
│ Daily Processed Audio Volume  │ 50,000 minutes    │ 2,500,000 minutes/day   │
│ Batch Media Processing Queue  │ 500 files/hour    │ 15,000 files/hour       │
│ Public REST API Throughput    │ 500 req/sec       │ 10,000 req/sec          │
│ Usage Metering Ingestion      │ 2,500 events/sec  │ 50,000 events/sec       │
└───────────────────────────────┴───────────────────┴─────────────────────────┘
```

---

## 3. Availability, Durability & SLA Commitments

1. **Service Availability**: **99.95% Monthly Uptime** for the Public API Gateway and Streaming WebSockets (maximum allowable unplanned downtime: $\le 21.6\text{ minutes/month}$).
2. **Data Durability**:
   - Usage metering and billing ledger: **99.999999999% (11 9s)** durability via multi-AZ PostgreSQL WAL archiving.
   - Zero tolerance for billing record loss or double-counting.
3. **Session Failover**: If a streaming worker node suffers catastrophic termination, client connections resume state on a peer worker within $\le 3.0\text{ seconds}$ without terminating the user session.

---

## 4. Regulatory & Enterprise Compliance Mandates

* **General Data Protection Regulation (GDPR / EU)**: Explicit consent management, right-to-be-forgotten webhooks, EU data residency boundary (`europe-west1`).
* **Health Insurance Portability and Accountability Act (HIPAA)**: Dedicated BAA mode enforcing zero retention of transient voice payloads, end-to-end TLS 1.3, and audit trail of transcript access.
* **Payment Card Industry Data Security Standard (PCI DSS)**: Real-time acoustic and textual redaction of numerical credit card strings from speech streams.
* **India Digital Personal Data Protection (DPDP) Act 2023**: In-country storage of national linguistic datasets and student audio streams.
