# 43 — SLI, SLO, SLA FRAMEWORK & ERROR BUDGET CALCULATIONS

> **Document ID:** `BS-ARCH-43-SLO-SLA`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Google SRE Service Level Objectives & Multi-Window Burn Rate Alerting  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Concrete Service Level Indicator (SLI) Formulations

BhashaSetu AI defines mathematical SLIs across four core user journeys:

$$\text{Availability SLI} = \frac{\sum \text{HTTP Requests with Status } < 500}{\sum \text{Total Valid HTTP Requests}} \times 100\%$$

$$\text{Voice Latency SLI} = \frac{\sum \text{Voice Turns with Elapsed Time } \le 3000\text{ ms}}{\sum \text{Total Voice Translation Turns}} \times 100\%$$

$$\text{RAG Accuracy SLI} = \frac{\sum \text{RAG Queries with Precision@2 } \ge 0.90}{\sum \text{Total Curriculum Retrieval Queries}} \times 100\%$$

$$\text{Quality SLI} = \frac{\sum \text{Lessons with COMETKiwi Score } \ge 0.85}{\sum \text{Total Synthesized Lessons}} \times 100\%$$

---

## 2. Master Service Level Objective (SLO) Matrix

| Service Capability | SLI Metric | P50 Target | P95 Target | P99 Target | Monthly SLO | External SLA Commitment |
|---|---|---|---|---|---|---|
| **Live Voice Relay** | End-to-End Latency | $1200\text{ ms}$ | **$2500\text{ ms}$** | $3000\text{ ms}$ | $\ge 98.5\%$ | $\le 3500\text{ ms}$ (95% of time) |
| **RAG Retrieval** | DiskANN Search Latency | $3.5\text{ ms}$ | **$6.0\text{ ms}$** | $12.0\text{ ms}$ | $\ge 99.9\%$ | $\le 25\text{ ms}$ (99% of time) |
| **Sync Batch Push** | Ingestion & Commit | $150\text{ ms}$ | **$450\text{ ms}$** | $950\text{ ms}$ | $\ge 99.5\%$ | Zero data duplication |
| **Gateway Uptime** | Successful HTTP 2xx/3xx| N/A | N/A | N/A | **$99.90\%$** | $99.50\%$ Monthly Uptime |
| **Translation QE** | COMETKiwi Score | $0.94$ | **$0.91$** | $0.86$ | $\ge 95.0\%$ | Score $\ge 0.80$ guaranteed |

---

## 3. Error Budget Math & Multi-Window Multi-Burn-Rate Alerting

For a $99.90\%$ Monthly Availability SLO over a 30-day billing cycle:

$$\text{Total Available Time} = 30 \times 24 \times 60 = 43,200\text{ minutes}$$
$$\text{Allowable Downtime (Error Budget)} = 43,200 \times (1 - 0.999) = \mathbf{43.2\text{ minutes per month}}$$

### Multi-Window Burn Rate Alert Thresholds:
To detect sudden outages without inducing alert fatigue from transient spikes, alerting enforces Google SRE multi-window rules:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MULTI-WINDOW BURN RATE ALERT MATRIX                      │
├───────────┬───────────┬────────────────┬─────────────────┬──────────────────┤
│ Burn Rate │ % Budget  │ Short Window   │ Long Window     │ Paging Action    │
│ Factor    │ Consumed  │ (Lookback)     │ (Lookback)      │                  │
├───────────┼───────────┼────────────────┼─────────────────┼──────────────────┤
│ 14.4x     │ 2% in 1h  │ 2 Minutes      │ 1 Hour          │ Page On-Call SRE │
│ 6.0x      │ 5% in 6h  │ 15 Minutes     │ 6 Hours         │ Page On-Call SRE │
│ 1.0x      │ 10% in 3d │ 1 Hour         │ 3 Days          │ Slack Alert      │
└───────────┴───────────┴────────────────┴─────────────────┴──────────────────┘
```
