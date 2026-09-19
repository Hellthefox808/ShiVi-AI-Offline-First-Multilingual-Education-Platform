# 55 — DAY-2 OPERATIONS & STANDARD OPERATING PROCEDURES (SOP)

> **Document ID:** `BS-ARCH-55-OPERATIONS`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Enterprise IT Operations & Day-2 Maintenance (§69, §71 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Routine Maintenance Schedule Matrix

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       ROUTINE OPERATIONS SCHEDULE                           │
├──────────────┬──────────────────────────────────┬───────────────────────────┤
│ Frequency    │ Operational Procedure            │ Automation Mechanism      │
├──────────────┼──────────────────────────────────┼───────────────────────────┤
│ Daily        │ • Purge sync logs older than 30d │ PostgreSQL pg_cron job at │
│ (03:00 IST)  │ • Verify daily pg_dump backup    │ 03:00 IST every night.    │
│              │ • Rebuild in-memory RAG query LRU│                           │
├──────────────┼──────────────────────────────────┼───────────────────────────┤
│ Weekly       │ • PostgreSQL VACUUM ANALYZE      │ Automated Kubernetes      │
│ (Sunday)     │ • Re-index B-Tree and GIN indexes│ Maintenance CronJob.      │
│              │ • Audit Redis memory fragmentation│                           │
├──────────────┼──────────────────────────────────┼───────────────────────────┤
│ Monthly      │ • Sandbox backup restore drill   │ Automated restore pod     │
│ (1st Day)    │ • Security patch base OS images  │ with integrity assertion. │
│              │ • Review low COMET score lessons │                           │
├──────────────┼──────────────────────────────────┼───────────────────────────┤
│ Quarterly    │ • Rotate JWT signing secrets     │ Zero-downtime dual-secret │
│              │ • Disaster recovery failover test│ key rotation script.      │
└──────────────┴──────────────────────────────────┴───────────────────────────┘
```

---

## 2. Field Tablet Fleet Provisioning SOP

When deploying new government tablets to a remote village school:

1. **Factory Flash & Staging**:
   - Install base OS (Android 9.0+ / Android 13).
   - Install signed release APK: `bhashasetu-app-v3.0.0-release.apk` ($29.6\text{ MB}$).
2. **Offline Content Pre-Seeding**:
   - Insert pre-loaded 16 GB micro-SD card containing offline package bundle:
     `/sdcard/bhashasetu/packages/santhali_bundle_v3.zip`.
   - Launch app; app detects package and extracts seed SQLite database directly into Room, pre-populating 120 lessons and 500 audio files without consuming village cellular data.
3. **School Tenant Activation**:
   - Primary teacher scans school QR code provided by the District Education Office.
   - App stores `school_id` and registers tablet hardware serial in local KeyStore.
   - Tablet is declared **Classroom Ready**.
