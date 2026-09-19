# 63 — ENTERPRISE RISK REGISTER & MITIGATION STRATEGIES

> **Document ID:** `BS-ARCH-63-RISK-REGISTER`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Enterprise Risk Management (ERM) & Threat Mitigation (§95 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Master Risk Assessment Matrix

$$\text{Risk Severity Score} = \text{Likelihood } (1..5) \times \text{Impact } (1..5)$$

| Risk ID | Risk Title & Concrete Description | Likelihood | Impact | Score | Technical Mitigation Strategy | Contingency Plan |
|---|---|---|---|---|---|---|
| **RISK-01** | **Tribal Translation Hallucination**: AI generates incorrect terminology in Santhali or Ho. | 3 | 5 | **15 (HIGH)** | Multi-signal COMETKiwi quality gate + JCERT in-memory glossary verification. | Quarantines lesson in `REVIEW_REQUIRED` state; requires human educator approval. |
| **RISK-02** | **Extreme Cellular Dropout**: Remote forest schools experience zero network for 30+ days. | 5 | 3 | **15 (HIGH)** | 100% offline edge architecture using Room SQLite; pre-seeded SD card content packs. | Tablets continue operating indefinitely offline; outbox queues up to 10,000 attempts. |
| **RISK-03** | **Hardware Thermal Throttling**: 2 GB Android tablets overheat in summer classrooms (>42°C). | 4 | 3 | **12 (MED)** | On-device tasks capped to lightweight VAD and audio playback; heavy ML offloaded to cloud. | Android app reduces background polling rate and dims screen brightness automatically. |
| **RISK-04** | **Teacher Adoption Resistance**: Teachers find complex digital tools intimidating. | 3 | 4 | **12 (MED)** | Ambient voice interface (press-and-speak microphone button); zero technical forms required. | Provide visual audio flashcards and printable PDF worksheets as physical bridges. |
| **RISK-05** | **Cloud Cost Overrun**: High API token consumption during state-wide scale. | 2 | 4 | **8 (MED)** | Quantized on-premises models (NLLB-200, BGE-M3) eliminate per-token external API fees. | Fall back strictly to cached static JCERT glossaries if cloud budget limit is approached. |
| **RISK-06** | **Cloud-Edge Schema Drift**: Room SQLite schema diverges from central PostgreSQL schema. | 2 | 4 | **8 (MED)** | Contract-first OpenAPI 3.1 code generation and automated migration integrity test suites. | Tablet sync worker rejects delta packs with mismatched schema version; prompts update. |

---

## 2. Risk Review & Monitoring Cadence

The Architectural Risk Register is reviewed bi-weekly by the Lead Architect and Core Engineering team. Any risk with a score $\ge 12$ requires a documented, tested mitigation in the codebase prior to production deployment.
