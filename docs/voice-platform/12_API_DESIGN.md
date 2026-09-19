# VoxBridge AI — Enterprise REST & Streaming API Design

## 1. Global API Invariants & Standards

### 1.1 Base URL and Versioning
- **Production Host:** `https://api.voxbridge.ai/v1`
- **Streaming Host:** `wss://stream.voxbridge.ai/v1/realtime`
- **Protocol:** HTTP/2 over TLS 1.3 (RFC 9113) with ALPN negotiation.
- **Versioning Strategy:** URI path versioning (`/v1`). Breaking changes result in a new major version path (`/v2`). Minor feature additions, optional query parameters, and non-breaking response fields are introduced within `/v1`.

### 1.2 Common HTTP Headers
Every request and response must exchange the following standard headers:

| Header Name | Type | Direction | Description |
|---|---|---|---|
| `Authorization` | String | Request | Bearer API Key (`Bearer vox_live_abc123...`) or JWT (`Bearer eyJ...`). |
| `Vox-Project-Id` | UUIDv7 | Request | Target project context (`proj_01J8ABCDEF...`). Optional if key is scoped. |
| `Idempotency-Key` | String | Request | Unique client transaction ID for mutating requests (`POST/PATCH/PUT`). Max 64 chars. |
| `X-Request-Id` | String | Both | Globally unique UUIDv7 request identifier. Injected if missing. |
| `X-Trace-Id` | String | Response | OpenTelemetry W3C distributed trace identifier (`00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01`). |
| `X-RateLimit-Limit` | Integer | Response | Maximum allowed requests in the current sliding window. |
| `X-RateLimit-Remaining` | Integer | Response | Remaining requests available in the current window. |
| `X-RateLimit-Reset` | Integer | Response | Epoch seconds timestamp when the current window resets. |

### 1.3 Standard Error Envelope (RFC 7807 Enhanced)
All non-2xx responses return a consistent JSON payload:

```json
{
  "error": {
    "code": "INVALID_AUDIO_FORMAT",
    "message": "The provided audio payload header indicates MP3 container, but raw PCM was specified in the request metadata.",
    "status": 400,
    "request_id": "req_01J8V1G6K2N3P4Q5R6S7T8U9V0",
    "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
    "retryable": false,
    "doc_url": "https://docs.voxbridge.ai/errors#INVALID_AUDIO_FORMAT",
    "details": [
      {
        "field": "audio_config.codec",
        "issue": "Header magic bytes 'ID3' do not match requested codec 'LINEAR16'."
      }
    ]
  }
}
```

---

## 2. Core API Endpoint Specifications

### 2.1 Endpoint: Create Real-Time Session (`POST /v1/sessions`)
- **HTTP Method:** `POST`
- **Path:** `/v1/sessions`
- **Auth:** Secret API Key (`vox_live_*`)
- **RBAC:** `session:create`
- **Rate Limit:** 300 requests/minute per Project; 2,000 requests/minute per Tenant.
- **Idempotency:** Supported via `Idempotency-Key` header (cached for 24 hours).
- **Timeout:** 5,000ms.
- **Audit:** Emits audit event `session.created`.
- **Observability:** Metric `api_session_creations_total{status}`, Trace span `APIGateway.CreateSession`.

#### Request Body
```json
{
  "mode": "REALTIME_TRANSLATED_SPEECH",
  "source_language": "en-US",
  "target_languages": ["es-ES", "hi-IN"],
  "audio_format": {
    "codec": "OPUS",
    "sample_rate_hz": 48000,
    "channels": 1,
    "frame_duration_ms": 20
  },
  "voice_config": {
    "voice_id": "vox_voice_neural_mateo_es",
    "speed": 1.0,
    "pitch": 0.0
  },
  "features": {
    "enable_vad": true,
    "vad_threshold": 0.55,
    "enable_profanity_filter": false,
    "glossary_id": "glo_01J8V2K3M4P5"
  },
  "metadata": {
    "user_id": "usr_external_9872",
    "call_center_queue": "tier2_billing"
  }
}
```

#### Response Body (`201 Created`)
```json
{
  "session_id": "sess_01J8V3M4K5N6P7Q8R9S0T1U2V3",
  "connection_url": "wss://stream.voxbridge.ai/v1/realtime?session_token=vxt_01J8V3M4...",
  "session_token": "vxt_01J8V3M4K5N6P7Q8R9S0T1U2V3_98f7a...",
  "token_expires_at": "2026-09-19T14:45:00Z",
  "mode": "REALTIME_TRANSLATED_SPEECH",
  "status": "CREATED",
  "source_language": "en-US",
  "target_languages": ["es-ES", "hi-IN"],
  "assigned_edge_region": "us-east-1",
  "created_at": "2026-09-19T14:30:00Z"
}
```

---

### 2.2 Endpoint: Submit Batch Audio Translation Job (`POST /v1/jobs`)
- **HTTP Method:** `POST`
- **Path:** `/v1/jobs`
- **Auth:** Secret API Key (`vox_live_*`)
- **RBAC:** `job:create`
- **Rate Limit:** 100 requests/minute per Project.
- **Idempotency:** Enforced via `Idempotency-Key`.
- **Timeout:** 10,000ms.
- **Audit:** Emits audit event `job.batch.submitted`.
- **Observability:** Metric `jobs_submitted_total{mode}`, Trace span `JobService.SubmitJob`.

#### Request Body
```json
{
  "job_type": "BATCH_AUDIO_TRANSLATE",
  "input": {
    "source_type": "PRE_SIGNED_URL",
    "url": "https://storage.googleapis.com/customer-media/call_recording_1049.wav",
    "source_language": "auto",
    "allowed_languages": ["en-US", "hi-IN", "es-419"]
  },
  "output": {
    "target_languages": ["fr-FR", "de-DE"],
    "generate_speech": true,
    "target_voice_ids": {
      "fr-FR": "vox_voice_neural_celeste_fr",
      "de-DE": "vox_voice_neural_hans_de"
    },
    "destination_s3_uri": "s3://customer-export-bucket/translated_audio/"
  },
  "webhook_url": "https://api.customer.com/webhooks/voxbridge",
  "priority": "NORMAL"
}
```

#### Response Body (`202 Accepted`)
```json
{
  "job_id": "job_01J8V4N5P6Q7R8S9T0U1V2W3X4",
  "status": "QUEUED",
  "priority": "NORMAL",
  "estimated_duration_seconds": 45,
  "created_at": "2026-09-19T14:31:00Z",
  "links": {
    "self": "https://api.voxbridge.ai/v1/jobs/job_01J8V4N5P6Q7R8S9T0U1V2W3X4"
  }
}
```

---

### 2.3 Endpoint: Generate Short-Lived Scoped API Key (`POST /v1/api-keys`)
- **HTTP Method:** `POST`
- **Path:** `/v1/api-keys`
- **Auth:** Secret Master Key or Enterprise SSO Admin
- **RBAC:** `admin:api_keys:create`
- **Rate Limit:** 20 requests/minute.
- **Idempotency:** Enforced via `Idempotency-Key`.
- **Security:** Secret is displayed **ONLY ONCE** in plaintext upon creation. Only SHA-256 hash is persisted.

#### Request Body
```json
{
  "name": "Frontend WebRTC Ephemeral Key",
  "scopes": ["session:create", "session:stream"],
  "expires_in_seconds": 86400,
  "allowed_origins": ["https://app.customer.com"],
  "allowed_ip_cidrs": ["198.51.100.0/24"]
}
```

#### Response Body (`201 Created`)
```json
{
  "key_id": "key_01J8V5P6Q7R8S9T0U1V2W3X4Y5",
  "name": "Frontend WebRTC Ephemeral Key",
  "secret_key": "vox_live_sec_7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f",
  "key_prefix": "vox_live_sec_7a8b9c0d",
  "scopes": ["session:create", "session:stream"],
  "created_at": "2026-09-19T14:32:00Z",
  "expires_at": "2026-09-20T14:32:00Z"
}
```

---

### 2.4 Complete Public API Route Catalog

| HTTP Method | Path | RBAC Scope | Description | Cache / Idempotency |
|---|---|---|---|---|
| `GET` | `/v1/languages` | `metadata:read` | List supported STT, MT, and TTS languages and script mappings | CDN Cached (1 hour) |
| `GET` | `/v1/voices` | `metadata:read` | List neural voices with gender, accent, and audio preview URLs | CDN Cached (1 hour) |
| `POST` | `/v1/translations` | `translation:write` | Synchronous text-to-text translation with glossary support | Idempotent |
| `GET` | `/v1/jobs/{id}` | `job:read` | Poll status, progress %, and signed output URLs for batch job | Cached in Redis (5s) |
| `DELETE` | `/v1/jobs/{id}` | `job:cancel` | Cancel an in-flight batch job and release worker reservations | Idempotent |
| `GET` | `/v1/usage` | `billing:read` | Query aggregated audio seconds and character counts by day/project | Cached in Redis (60s) |
| `GET` | `/v1/quotas` | `quota:read` | Get current concurrency and monthly spend threshold status | Dynamic Redis Read |
| `POST` | `/v1/webhooks` | `webhook:manage` | Register an HTTPS webhook endpoint with HMAC secret key | Idempotent |
| `GET` | `/v1/models` | `metadata:read` | List active model routing topologies and provider latency SLOs | Cached (15m) |
