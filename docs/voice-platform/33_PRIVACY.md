# VoxBridge AI — Privacy Engineering & Compliance Framework

## 1. Data Classification Taxonomy

Spoken human voice contains physiological biometrics, emotional markers, and confidential conversation context. VoxBridge establishes a 4-tier data classification hierarchy:

| Classification | Data Elements | Storage Location | Default Retention | Encryption Standard | Access Policy |
|---|---|---|---|---|---|
| **Tier 1: Biometric Audio** | Raw user audio, speaker voice embeddings, synthetic cloned voices | Ephemeral RAM / S3 Private | 0 Days (Default) or Tenant Config (Max 30d) | AES-256-GCM (SSE-KMS) | Restricted (Zero internal human access) |
| **Tier 2: Linguistic Content**| Transcripts, translations, word alignments, user captions | PostgreSQL (TimescaleDB) | Configurable (Default 30 Days) | AES-256 (TDE + Column Encryption) | Tenant Scoped (RLS enforced) |
| **Tier 3: Operational Metadata**| Session IDs, durations, language pairs, codec, model IDs | PostgreSQL / TimescaleDB | 365 Days | AES-256 (TDE) | Engineering / SRE Telemetry |
| **Tier 4: Billing & Audit** | Usage records, invoices, API key IDs, compliance audit logs | TimescaleDB / S3 Vault | 7 Years (Legal Mandate) | WORM Compliant Immutable KMS | Finance & Compliance |

---

## 2. Zero-Retention Mode (ZDR)

For privacy-sensitive enterprise sectors (healthcare telemedicine, banking IVR, legal arbitration), VoxBridge offers **Certified Zero Data Retention (ZDR)**:

```
[CLIENT AUDIO INGESTION]
           │
           ▼
[IN-MEMORY STREAMING PIPELINE]
- Audio frames decoded directly in RAM (`tmpfs` memory buffers)
- VAD -> STT -> Translation -> TTS pipeline executes in transient memory
- Output audio chunks emitted back to client WebSocket
           │
           ▼
[INSTANT VOLATILE PURGE]
- All audio frame memory zeroed via `memzero_explicit()` in C-Go runtime
- Zero audio bytes written to S3 or local node disks
- Database records ONLY operational metadata: `audio_duration_seconds`, `timestamp`
- Transcript text is completely omitted from the database record
           │
           ▼
[CRYPTOGRAPHIC PROOF OF PURGE]
- Generates a signed ZDR Attestation JSON with SHA-256 trace verification
```

---

## 3. Automated PII & PHI Redaction

To prevent sensitive personal information from being persisted or leaked to downstream translation models, VoxBridge executes an automated **Inline PII/PHI Redactor** on final transcripts before translation:

```
Source Transcript:
"My name is John Doe, credit card 4532 8901 2345 6789 expiring 08/29."

Inline NER & Regex Redaction:
"My name is [NAME], credit card [CARD_NUM_REDACTED] expiring [DATE_REDACTED]."
```

### 3.1 Redaction Pattern Coverage
- **Financial:** Credit/Debit Cards (Luhn algorithm validated), IBAN, Bank Routing Numbers.
- **Government IDs:** Social Security Numbers (US SSN), Aadhaar Numbers (India), Passport Numbers.
- **Contact Info:** Email addresses, Phone numbers (E.164 standard regex), Physical street addresses.
- **Health Data (HIPAA Safe Harbor 18 Identifiers):** Medical record numbers, health plan IDs, patient names.

---

## 4. Regulatory Compliance Matrix

| Regulation | Scope / Region | Mandatory Platform Capabilities | VoxBridge Architecture Compliance |
|---|---|---|---|
| **GDPR** (EU Regulation 2016/679) | European Union citizens & entities | Right to Erasure (Art. 17), Data Portability (Art. 20), EU Data Residency | Dedicated `eu-central-1` data residency zone; automated DELETE API; Zero-Retention mode. |
| **HIPAA** (Health Insurance Portability) | US Healthcare PHI | Business Associate Agreement (BAA), Audit Trails, End-to-End Encryption | Envelope KMS encryption; dedicated audit logging; strict PII/PHI redaction; zero human audio access. |
| **DPDP Act 2023** (India Data Protection) | Indian Citizens & Sovereignty | Explicit Consent for Processing, Grievance Officer Audit, Local Processing | Devanagari/regional language local inference; consent challenge verification; automated data deletion. |
| **CCPA / CPRA** (California Privacy) | California Consumers | "Do Not Sell or Share My Info", Notice at Collection, Right to Delete | Zero third-party data sharing; enterprise models never train on customer audio streams. |

### 4.1 Strict Invariant: No AI Training on Customer Audio
Under no circumstances does VoxBridge utilize customer voice audio, transcripts, or translations to fine-tune or train foundation models. All inference is executed in zero-retention inference contexts under strict enterprise non-disclosure terms.
