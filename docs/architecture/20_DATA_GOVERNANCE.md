# 20 — DATA GOVERNANCE, CHILD PRIVACY & DPDP COMPLIANCE

> **Document ID:** `BS-ARCH-20-DATA-GOVERNANCE`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Legal Framework:** India Digital Personal Data Protection (DPDP) Act 2023 & NEP 2020  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Enterprise Data Classification Matrix

| Classification Tier | Domain Assets Included | Storage Location | Encryption at Rest | Access Control |
|---|---|---|---|---|
| **Tier 1: Public** | State JCERT textbook chunks, learning outcome codes, bilingual glossaries. | PostgreSQL, SQLite | AES-256 / Plaintext | Public read access |
| **Tier 2: Internal** | School master records, lesson versions, device serials, aggregate district FLN reports. | PostgreSQL, Redis | AES-256 (TDE) | Authenticated staff (RBAC) |
| **Tier 3: Confidential** | Teacher names, government emails, Argon2id password hashes, audit trails. | PostgreSQL | AES-256 + Salted Hashes | Strict RBAC (`ADMIN`) |
| **Tier 4: Sensitive / Child** | Primary student quiz attempts, formative scores, pseudonymous IDs. | PostgreSQL, SQLite | Encrypted SQLite (SQLCipher) | Strict Multi-Tenant RLS |

---

## 2. Child Protection & Student Privacy Architecture (DPDP Act 2023)

Primary school students in rural Jharkhand represent a vulnerable population requiring absolute data protection:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       CHILD PRIVACY SAFEGUARDS                              │
├──────────────────────┬──────────────────────────────────────────────────────┤
│ 1. Pseudonymization  │ Students are identified strictly via synthetic UUIDs │
│                      │ (`STU-8821B`). Zero collection of Aadhaar, parent    │
│                      │ names, home addresses, or phone numbers.             │
├──────────────────────┼──────────────────────────────────────────────────────┤
│ 2. Zero Biometrics   │ Zero facial recognition, zero camera permissions in  │
│                      │ the classroom, and zero fingerprint scanning.        │
├──────────────────────┼──────────────────────────────────────────────────────┤
│ 3. Transient Audio   │ Audio captured by the microphone during voice relay  │
│    Processing        │ is processed entirely in volatile RAM buffers and    │
│                      │ discarded within 500ms of transcription. No student  │
│                      │ voice audio is ever written to disk or stored.       │
├──────────────────────┼──────────────────────────────────────────────────────┤
│ 4. No Ad Targeting   │ Zero advertising trackers, zero third-party analytics│
│                      │ SDKs (Google Analytics/Facebook SDK are forbidden).  │
└──────────────────────┴──────────────────────────────────────────────────────┘
```

---

## 3. Data Retention & Purge Schedules

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DATA RETENTION LIFECYCLES                            │
├────────────────────────────┬──────────────────┬─────────────────────────────┤
│ Data Category              │ Retention Period │ Disposal Mechanism          │
├────────────────────────────┼──────────────────┼─────────────────────────────┤
│ 1. Student Quiz Attempts   │ 3 Academic Years │ Anonymized and rolled into  │
│                            │                  │ state-level FLN aggregates. │
├────────────────────────────┼──────────────────┼─────────────────────────────┤
│ 2. Sync Operation Logs     │ 30 Days          │ Automatic daily cron purge  │
│                            │                  │ via PostgreSQL pg_cron.     │
├────────────────────────────┼──────────────────┼─────────────────────────────┤
│ 3. Microservice Audit Logs │ 1 Academic Year  │ Cold compressed archive in  │
│                            │                  │ immutable S3 Glacier bucket.│
├────────────────────────────┼──────────────────┼─────────────────────────────┤
│ 4. Local Tablet Audio Cache│ LRU Eviction     │ Evicted when cache exceeds  │
│                            │                  │ 500 MB on local tablet.     │
└────────────────────────────┴──────────────────┴─────────────────────────────┘
```
