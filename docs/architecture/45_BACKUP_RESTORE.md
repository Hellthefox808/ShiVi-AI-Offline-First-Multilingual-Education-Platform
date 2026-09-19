# 45 — BACKUP, RESTORE & POINT-IN-TIME RECOVERY (PITR)

> **Document ID:** `BS-ARCH-45-BACKUP-RESTORE`  
> **Classification:** Enterprise System Architecture Control Document  
> **SIH Problem ID:** SIH26042 | **Domain:** Mother-Tongue-Based Multilingual Education (MTB-MLE)  
> **Standard:** Enterprise Data Protection & Restore Verification (§35 Master Standard)  
> **Document Version:** 3.0.0-PROD | **Status:** Approved Baseline  

---

## 1. Multi-Tier Backup Schedule Matrix

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                          BACKUP LIFECYCLE MATRIX                            │
├────────────────────┬────────────────────┬───────────────┬───────────────────┤
│ Data Tier          │ Backup Mechanism   │ Frequency     │ Retention Period  │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 1. PostgreSQL 18   │ WAL Continuous     │ Real-time     │ 14 Days PITR      │
│    (Primary Store) │ Streaming (WAL-G)  │ (1-minute lag)│ Rolling Window    │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 2. PostgreSQL Full │ Logical pg_dump    │ Daily         │ 90 Days GCS Cold  │
│    Snapshots       │ (Encrypted Gzip)   │ (02:00 IST)   │ 1 Year Annual     │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 3. Redis 7.4       │ AOF (fsync everysec│ Continuous +  │ 7 Days Snapshot   │
│    (Task Queues)   │ + RDB Snapshot     │ Every 6 Hours │ Rolling Window    │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 4. Cloud Object    │ GCS Object         │ Real-time     │ 30-day Soft Delete│
│    Storage (Audio) │ Versioning         │ Versioned     │ Immutable Archive │
├────────────────────┼────────────────────┼───────────────┼───────────────────┤
│ 5. Tablet Edge DB  │ Encrypted SQLite   │ Weekly Auto   │ 4 Weekly Versions │
│    (Android Local) │ Export to Storage  │ (On Charge)   │ on Internal Flash │
└────────────────────┴────────────────────┴───────────────┴───────────────────┘
```

---

## 2. Point-in-Time Recovery (PITR) Procedure

If catastrophic database corruption occurs (e.g., unintended data deletion), PostgreSQL can be rolled back to an exact microsecond prior to the incident using Write-Ahead Logs:

```bash
# 1. Stop PostgreSQL service
systemctl stop postgresql-18

# 2. Restore base physical backup to data directory
wal-g backup-fetch /var/lib/postgresql/data LATEST

# 3. Create recovery configuration specifying target recovery timestamp
cat << EOF > /var/lib/postgresql/data/recovery.signal
restore_command = 'wal-g wal-fetch "%f" "%p"'
recovery_target_time = '2026-09-19 14:15:00.000000+05:30'
recovery_target_action = 'promote'
EOF

# 4. Restart PostgreSQL engine in recovery mode
systemctl start postgresql-18
```

---

## 3. Automated Monthly Sandbox Restore Drills

Backups that are not tested for restoration are invalid. On the 1st of every calendar month, an automated Kubernetes CronJob (`backup-drill-job`) executes a verification drill:
1. Provisions an isolated, temporary PostgreSQL pod (`restore-test-pod`).
2. Restores the latest daily `pg_dump` snapshot into the temporary instance.
3. Runs an integrity verification script asserting:
   - Total school tenant count $> 0$.
   - Integrity of pgvector DiskANN indexes via sample vector distance calculation.
   - Foreign key consistency between `lessons` and `curriculum_nodes`.
4. Emits a Slack alert with the verification report and terminates the test container.
