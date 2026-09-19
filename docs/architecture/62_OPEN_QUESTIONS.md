# 62 — OPEN QUESTIONS, ARCHITECTURAL UNKNOWNS & ASSUMPTIONS

> **Document ID:** `BS-ARCH-62-OPEN-QUESTIONS`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Living Architectural Risk & Assumption Register (§97 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Master Open Questions Register

| Question ID | Architectural Open Question | Category | Current Working Assumption | Impact If Wrong | Validation Strategy |
|---|---|---|---|---|---|
| **OQ-01** | Will the state education department mandate hosting on **MeitY NIC Cloud** or approve **GCP India** (`asia-south1`)? | Cloud / Infra | Deployment targets GCP India region with full MeitY sovereign boundary compliance. | Requires migrating K8s manifests from GKE to OpenShift / NIC MeghRaj. | Formal clarification with Jharkhand Education Project Council (JEPC). |
| **OQ-02** | Will the platform expand to Odisha (Odia / Kui) and Chhattisgarh (Gondi) in Phase 2? | Domain / Scale | Single-state (Jharkhand) pilot first; architecture decoupled via `LanguageProvider` for multi-state. | Database tenancy model may require state-level horizontal partitioning. | Test adding Gondi dialect to `LanguageProvider` registry. |
| **OQ-03** | What is the committed long-term engineering team size post-SIH hackathon? | Operations | 3–5 core maintainers (per SHIVI@808 team structure); high automation required. | Operational complexity must remain minimal; avoid unnecessary microservices. | SRE runbooks and CI/CD pipelines automate 90% of routine toil. |
| **OQ-04** | Are there state-specific biometric or Aadhaar linkage mandates for student FLN tracking? | Compliance | Zero biometrics; student data is strictly pseudonymous under DPDP Act 2023. | Would require hardware HSM and UIDAI compliant encryption vault. | Consult with State Data Protection Officer. |
| **OQ-05** | Will target tablets have native TTF font rendering support for Warang Chiti? | Mobile / Font | Older Android 9.0 tablets require bundled TTF fonts in APK assets; native OS font missing. | Text displays as blank squares (tofu) if fonts not bundled. | Verified: Bundling `OlChiki.ttf` and `WarangChiti.ttf` in APK assets eliminates OS dependency. |

---

## 2. Validation & Closure Protocol

Each open question is tracked to resolution:
1. **UNKNOWN**: Question raised; no consensus.
2. **ASSUMED**: Working assumption adopted in architecture; risk bounded.
3. **VALIDATED**: Empirical test or stakeholder confirmation received; transitioned to ADR or PRD requirement.
