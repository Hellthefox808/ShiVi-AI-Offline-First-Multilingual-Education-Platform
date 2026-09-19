# VoxBridge AI — Service Level Objectives (SLO) & Enterprise SLAs

## 1. Enterprise Service Level Agreements (SLA)

VoxBridge AI contractually commits to the following availability thresholds for Enterprise tier agreements:

| Monthly Uptime Percentage | Customer Service Credit Refund |
|---|---|
| $\ge 99.95\%$ | Standard Operational Guarantee (0% Credit) |
| $99.00\% - 99.94\%$ | $10\%$ Credit applied to monthly invoice |
| $95.00\% - 98.99\%$ | $25\%$ Credit applied to monthly invoice |
| $< 95.00\%$ | $50\%$ Credit applied to monthly invoice |

---

## 2. Service Level Objectives (SLO) Catalog

All SLOs are computed over a **30-Day Rolling Window**:

### 2.1 Public REST API Availability
- **Target:** $\mathbf{99.95\%}$
- **SLI Metric:**
  $$\text{SLI} = \frac{\sum \text{HTTP Requests with Status } < 500}{\sum \text{Total HTTP Requests Ingested}}$$
- **Measurement:** Prometheus query on `http_requests_total`.
- **Error Budget (30 Days):** $21.6 \text{ minutes}$ of unmitigated 5xx downtime.
- **Alert Trigger:** Multi-window multi-burn alert firing on $14.4\text{x}$ burn (1 hour).
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/api-gateway-outage`.

### 2.2 Streaming Connection Availability
- **Target:** $\mathbf{99.90\%}$
- **SLI Metric:**
  $$\text{SLI} = \frac{\sum \text{Successful WebSocket / WebRTC Handshakes}}{\sum \text{Valid Client Connection Attempts}}$$
- **Measurement:** `sum(rate(websocket_connect_success_total[5m])) / sum(rate(websocket_connect_attempts_total[5m]))`.
- **Error Budget (30 Days):** $43.2 \text{ minutes}$ of connection failure.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/streaming-gateway-connection-failures`.

### 2.3 Time to First Audio (TTFA) Latency
- **Target:** $\mathbf{p95 \le 850\text{ms}}$ (Interactive Streaming Mode)
- **SLI Metric:** Elapsed time from user acoustic utterance completion (VAD speech stop) to the emission of the first synthesized translated Opus audio chunk.
- **Measurement:** Histogram `histogram_quantile(0.95, sum(rate(vox_ttfa_seconds_bucket[5m])) by (le))`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/ttfa-latency-degradation`.

### 2.4 End-to-End Voice Translation Latency
- **Target:** $\mathbf{p95 \le 1200\text{ms}}$ (Full Utterance Turnaround)
- **SLI Metric:** Elapsed time from speech end to final audio chunk delivery across top 20 language pairs.
- **Measurement:** `histogram_quantile(0.95, sum(rate(vox_e2e_voice_translation_latency_seconds_bucket[5m])) by (le))`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/e2e-latency-tuning`.

### 2.5 Provider Failover Success Rate
- **Target:** $\mathbf{\ge 99.0\%}$
- **SLI Metric:** Percentage of dropped primary provider requests successfully absorbed and fulfilled by secondary/fallback providers without returning an error to the client.
- **Measurement:** `sum(rate(vox_provider_fallback_success_total[5m])) / sum(rate(vox_provider_fallback_total[5m]))`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/provider-gateway-failover-stalls`.

### 2.6 Webhook Delivery Durability
- **Target:** $\mathbf{99.9\%}$ of webhook notifications delivered within 5 minutes of job completion.
- **SLI Metric:** `sum(rate(webhook_delivered_under_5m_total[5m])) / sum(rate(webhook_jobs_completed_total[5m]))`.
- **Runbook:** `https://wiki.voxbridge.internal/runbooks/webhook-dispatcher-backlog`.
