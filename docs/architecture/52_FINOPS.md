# 52 — CLOUD FINANCIAL OPERATIONS (FINOPS) & COST MODELING

> **Document ID:** `BS-ARCH-52-FINOPS`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** FinOps Foundation Cloud Cost Management & Unit Economics (§50 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Phase 1 Pilot Cost Architecture (500 Primary Schools)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                      MONTHLY INFRASTRUCTURE BUDGET (PILOT)                  │
├────────────────────┬────────────────────┬───────────────┬───────────────────┤
│ Infrastructure Tier│ Provisioned Spec   │ Monthly Cost  │ Monthly Cost (USD)│
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 1. GKE Compute     │ 3x e2-standard-4   │ ₹24,500       │ $295              │
│    Cluster Nodes   │ (4 vCPU, 16 GB)    │               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 2. PostgreSQL 18   │ Cloud SQL db-perf  │ ₹16,200       │ $195              │
│    (HA + DiskANN)  │ 2 vCPU, 8 GB, 100GB│               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 3. Redis 7.4       │ Memorystore Cache  │ ₹4,100        │ $49               │
│    Task Queues     │ 1.0 GB Standard HA │               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 4. Cloud Storage   │ GCS Multi-Regional │ ₹2,800        │ $34               │
│    (Audio & Zips)  │ 500 GB Storage     │               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 5. AI Model Tokens │ Gemini 3.1 Pro +   │ ₹14,000       │ $168              │
│    (Fallbacks)     │ Bhashini Gateway   │               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 6. Ingress & CDN   │ Cloudflare Pro +   │ ₹3,400        │ $41               │
│    WAF Protection  │ Cloud Load Balancer│               │                   │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ TOTAL PILOT BUDGET │ 500 Rural Schools  │ ₹65,000 / mo  │ ~$782 / mo        │
└────────────────────┴────────────────────┴───────────────┴───────────────────┘
```

---

## 2. Unit Economics per Student Learner

In the 500-school pilot program across Dumka and Pakur:
* **Active Enrolled Tribal Students**: $30,000\text{ children}$.
* **Total Monthly Platform Cost**: $₹65,000$.

$$\text{Unit Cost per Student} = \frac{₹65,000}{30,000\text{ students}} = \mathbf{₹2.16\text{ per student / month} \quad (\sim \$0.026\text{ USD})}$$

This extraordinary cost-efficiency demonstrates that state-wide deployment across all 12,000 primary schools in Jharkhand can be achieved for less than ₹15 Lakhs/month ($~\$18,000\text{ USD}$).

---

## 3. Core Architectural Cost Optimizations

1. **Local Edge Execution Offload**: Because 100% of student practice quizzes and audio lessons run locally on the tablet, **$85\%$ of daily compute is handled by zero-cost client hardware**, eliminating millions of daily cloud API invocations.
2. **Open-Source Self-Hosted Models**: By hosting quantized NLLB-200, BGE-M3, and Kokoro-82M on commodity CPU/GPU nodes, the system avoids recurring per-token proprietary LLM API fees for standard curriculum tasks.
3. **StreamingDiskANN Memory Reduction**: Compressing vector graph indexes onto NVMe SSD reduces database RAM sizing from $64\text{ GB}$ to $8\text{ GB}$, saving over **₹45,000 per month** in enterprise database licensing and RAM instance costs.
