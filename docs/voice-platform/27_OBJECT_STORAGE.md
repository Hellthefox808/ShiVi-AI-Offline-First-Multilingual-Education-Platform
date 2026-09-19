# VoxBridge AI — Object Storage & Media Lifecycle Architecture

## 1. Object Storage Architecture Topology (Mermaid Diagram 15)

VoxBridge AI utilizes an enterprise S3-compatible object storage topology (AWS S3 / Cloudflare R2 / MinIO) partitioned by tenant security boundaries, automated lifecycle expiration tiers, virus scanning, and pre-signed cryptographic access control.

```mermaid
graph TB
    subgraph IngestionSources["Ingestion Sources"]
        ClientApp["Client Mobile/Web App (Direct Media Upload)"]
        BatchService["Batch Worker (Synthesized Media Generator)"]
    end

    subgraph SecurityGate["Security & Verification Gate"]
        PresignedManager["Pre-Signed URL Generator (HMAC-SHA256, 15m Expiry)"]
        ClamAVScanner["Asynchronous ClamAV Virus & Malware Scanner"]
    end

    subgraph S3BucketTopology["S3 Bucket Hierarchy (Encrypted via AWS KMS)"]
        direction TB
        subgraph Bucket_Raw["Bucket: voxbridge-media-raw"]
            RawInbound["raw/{tenant_id}/{project_id}/{date}/{job_id}.wav"]
        end
        subgraph Bucket_Temp["Bucket: voxbridge-media-temp (24h Auto-Purge)"]
            TempAudio["temp/{session_id}/{chunk_seq}.opus"]
        end
        subgraph Bucket_Synthesized["Bucket: voxbridge-media-synthesized"]
            TargetAudio["synthesized/{tenant_id}/{date}/{utterance_id}.opus"]
        end
        subgraph Bucket_Quarantine["Bucket: voxbridge-quarantine (Isolated Subnet)"]
            InfectedAudio["quarantine/{job_id}/malformed_payload.bin"]
        end
    end

    subgraph LifecycleTiers["Automated S3 Lifecycle Tiering"]
        S3_Standard["S3 Standard (Hot: 0 - 30 Days)"]
        S3_Infrequent["S3 Standard-IA (Warm: 31 - 90 Days)"]
        S3_Glacier["S3 Glacier Instant Retrieval (Cold: 91 - 365 Days)"]
        S3_Purge["Permanent Cryptographic Deletion (Day 365+)"]
        
        S3_Standard --> S3_Infrequent
        S3_Infrequent --> S3_Glacier
        S3_Glacier --> S3_Purge
    end

    ClientApp -->|1. Request Upload URL| PresignedManager
    PresignedManager -->|2. Return Signed PUT URL| ClientApp
    ClientApp -->|3. Direct PUT Upload| RawInbound
    RawInbound -->|4. S3 ObjectCreated Event| ClamAVScanner
    ClamAVScanner -->|Clean Audio| BatchService
    ClamAVScanner -.->|Infected / Malformed| InfectedAudio
    BatchService -->|Write Generated Audio| TargetAudio
```

---

## 2. Bucket Taxonomy & Key Naming Conventions

All media assets are strictly separated across dedicated physical buckets with standardized URI key prefixes:

```
s3://voxbridge-media-raw/
└── tenants/
    └── {tenant_uuid}/
        └── projects/
            └── {project_uuid}/
                └── year=2026/month=09/day=19/
                    └── {job_uuid}/
                        └── original_input.wav

s3://voxbridge-media-synthesized/
└── tenants/
    └── {tenant_uuid}/
        └── projects/
            └── {project_uuid}/
                └── year=2026/month=09/day=19/
                    └── {job_uuid}/
                        ├── translated_es_ES.opus
                        └── translated_hi_IN.opus

s3://voxbridge-media-temp/
└── sessions/
    └── {session_uuid}/
        └── chunk_000042.opus
```

---

## 3. Pre-Signed URL Cryptographic Governance

To ensure zero media streaming bottlenecks on backend API pods, clients upload large audio files directly to object storage via **AWS S3 Pre-Signed URLs**:

1. **Short Expiration Window:** Pre-signed URLs strictly expire within **15 minutes (900 seconds)**.
2. **Method & Header Binding:** Signed URLs enforce exact HTTP methods (`PUT`), exact `Content-Type` headers (`audio/wav`, `audio/ogg`), and an explicit upper boundary on `Content-Length` (`max 524,288,000 bytes` or 500 MB).
3. **No Public Bucket Access:** All S3 buckets enforce `BlockPublicAcls = true`, `BlockPublicPolicy = true`, `IgnorePublicAcls = true`, and `RestrictPublicBuckets = true`.

---

## 4. Encryption & Customer-Managed Keys (CMK)

- **Default Encryption:** All objects are encrypted at rest via **SSE-KMS** using an AWS KMS Key with automated annual rotation.
- **BYOK (Bring Your Own Key) for Enterprise:** Enterprise clients can provide an external AWS KMS Key ARN or Google Cloud KMS Key ID. Every read/write operation decrypts/encrypts using the tenant's dedicated key. If the client revokes the key in their cloud console, all access to their historical media is instantly rendered cryptographically inaccessible.

---

## 5. Privacy Safeguards & Zero-Retention Mode

When a tenant enables `zero_retention_mode: true`:
1. **No Disk Writes:** Streaming audio frames are decoded in Linux RAM buffers (`tmpfs` /dev/shm) and discarded immediately upon STT tokenization.
2. **Zero Permanent Audio Persistence:** Neither raw user speech nor synthesized target speech is written to S3 buckets. Only transient in-memory ring buffers handle the playback stream.
3. **Automated Audit Attestation:** Emits a signed audit record verifying that zero bytes were persisted to persistent disks.
