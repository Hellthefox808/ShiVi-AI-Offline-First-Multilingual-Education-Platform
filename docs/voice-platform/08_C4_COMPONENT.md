# VoxBridge AI — C4 Architecture: Level 3 Component Design

## 1. Component Diagrams (Mermaid Diagram 3)

The Component diagram inspects the internals of the two most critical front-line ingress services: the **API Gateway** (HTTP/2 REST plane) and the **Real-Time Streaming Engine** (WebSocket / WebRTC stateful media plane).

```mermaid
graph TB
    subgraph ClientPerimeter["Client Tier"]
        RestClient["HTTP/2 REST Client"]
        WSClient["WebSocket / WebRTC Client"]
    end

    subgraph APIGatewayService["API Gateway Service (Go)"]
        direction TB
        TLS_Term["TLS 1.3 Termination Handler"]
        Auth_Interceptor["Auth & RBAC Interceptor (HMAC/JWT)"]
        Rate_Limiter["Sliding-Window Rate Limiter (Redis Lua)"]
        Schema_Validator["JSON Schema & Payload Validator"]
        Idempotency_Mgr["Idempotency Filter (IETF Draft RFC)"]
        REST_Router["HTTP Route Multiplexer (/v1/*)"]
        Audit_Emitter["Audit Log & Trace Propagator"]

        TLS_Term --> Auth_Interceptor
        Auth_Interceptor --> Rate_Limiter
        Rate_Limiter --> Schema_Validator
        Schema_Validator --> Idempotency_Mgr
        Idempotency_Mgr --> REST_Router
        REST_Router --> Audit_Emitter
    end

    subgraph StreamingEngineService["Real-Time Streaming Gateway (Go)"]
        direction TB
        WS_Upgrade["WebSocket / WebRTC Upgrader"]
        Conn_Lifecycle["Connection & Heartbeat Keeper"]
        Frame_Demuxer["Binary Opus/PCM Frame Demuxer"]
        Ring_Buffer["Circular Lockless Ring Buffer (250ms)"]
        Backpressure_Ctrl["Dynamic Backpressure & Drop Policy Controller"]
        VAD_Engine["Silero VAD C-Go Wrapper (10ms chunks)"]
        Stream_Dispatcher["Pipeline Dispatcher (gRPC Client Pool)"]

        WS_Upgrade --> Conn_Lifecycle
        Conn_Lifecycle --> Frame_Demuxer
        Frame_Demuxer --> Ring_Buffer
        Ring_Buffer --> Backpressure_Ctrl
        Backpressure_Ctrl --> VAD_Engine
        VAD_Engine --> Stream_Dispatcher
    end

    subgraph CoreDownstream["Internal Service & Storage Plane"]
        RedisStore[("Redis Cluster")]
        PostgresDB[("PostgreSQL 18")]
        StreamOrchService["Stream Orchestration Service (gRPC)"]
        NatsJetStream["NATS JetStream (Audit/Jobs)"]
    end

    RestClient -->|HTTPS| TLS_Term
    WSClient -->|WSS / UDP| WS_Upgrade

    Auth_Interceptor -.->|Validate API Key Hash| RedisStore
    Rate_Limiter -.->|Evaluate Token Buckets| RedisStore
    Idempotency_Mgr -.->|Lookup Request Hash| RedisStore
    REST_Router -->|Fetch Relational Data| PostgresDB
    Audit_Emitter -->|Emit Event| NatsJetStream

    Conn_Lifecycle -.->|Register Connection Node| RedisStore
    Stream_Dispatcher -->|Push Audio Chunk (gRPC)| StreamOrchService
```

---

## 2. API Gateway Component Specifications

### 2.1 TLS 1.3 Termination & Handshake Enforcer
- **Requirement:** Low latency handshakes with zero legacy cipher vulnerability.
- **Architecture:** Terminated via Go's native `crypto/tls` with `tls.Config{MinVersion: tls.VersionTLS13, CipherSuites: [...]}`.
- **Implementation:** Supports ALPN negotiating `h2` and `http/1.1`. Perfect Forward Secrecy (PFS) enforced via `TLS_AES_256_GCM_SHA384` and `TLS_CHACHA20_POLY1305_SHA256`.

### 2.2 Auth & RBAC Interceptor
- **Requirement:** Sub-millisecond credential validation across 15,000 requests per second without hammering PostgreSQL.
- **Architecture:** Two-tier credential lookup: Local in-memory LRU cache (10,000 keys, 60s TTL) backed by Redis Cluster with salted SHA-256 hash lookup.
- **Technology & Contract:**
  ```go
  type AuthContext struct {
      TenantID      uuid.UUID
      ProjectID     uuid.UUID
      APIKeyID      uuid.UUID
      Scopes        []string
      RateLimitTier string
      DataRegion    string
  }
  ```
- **Trade-off:** In-memory LRU introduces a 60-second revocation lag. To mitigate, an instantaneous NATS pub/sub revocation channel purges local caches upon key invalidation.

### 2.3 Idempotency Filter
- **Requirement:** Prevent duplicate billing or duplicate job dispatch on client network retry.
- **Architecture:** IETF Idempotency-Key specification compliance.
- **Implementation:**
  1. Inspects `Idempotency-Key: <UUIDv4/UUIDv7>`.
  2. Executes Redis atomic SETNX with a 24-hour TTL: `SET idempotency:{tenant_id}:{key} IN_PROGRESS NX EX 86400`.
  3. If key exists with status `COMPLETED`, returns the cached serialized response headers and body directly.
  4. If key exists with status `IN_PROGRESS`, responds with `409 Conflict` and `Retry-After: 2`.

---

## 3. Real-Time Streaming Gateway Component Specifications

### 3.1 Binary Opus / PCM Frame Demuxer
- **Requirement:** Parse streaming client messages containing control frames and raw binary audio without allocating garbage-collected heap objects.
- **Architecture:** Standardized 4-byte header framing:
  - Byte 0: `Magic Byte (0xD8)`
  - Byte 1: `Frame Type (0x01: Audio, 0x02: Metadata/JSON, 0x03: Ack, 0x04: Ping/Pong)`
  - Bytes 2-3: `Payload Length (Big Endian uint16, max 65535 bytes)`
  - Bytes 4+: Raw binary payload.
- **Zero-Copy Memory Pool:** Uses a sync.Pool of pre-allocated 4KB byte slices (`audioBufferPool`) to completely avoid heap fragmentation in high-throughput Go runtimes.

### 3.2 Lockless Circular Ring Buffer (Audio Jitter Manager)
- **Requirement:** Buffer up to 250ms of audio frames to absorb network jitter while enforcing strict memory caps.
- **Technology:** Lock-free, single-producer single-consumer (SPSC) circular queue based on atomic pointer increments (`atomic.Uint64`).
- **Buffer Geometry:**
  $$\text{Buffer Size} = 16\text{kHz} \times 16\text{-bit PCM} \times 1 \text{ channel} \times 0.25\text{s} = 8,000 \text{ bytes (or 25 Opus packets)}.$$

### 3.3 Dynamic Backpressure & Drop Policy Controller
- **Requirement:** Prevent unbounded memory growth if downstream speech inference slows down or client upstream saturates.
- **Policy Modes:**
  1. **GREEN (Queue < 60% capacity):** Normal processing.
  2. **YELLOW (Queue 60%–85% capacity):** Downstream throttling signaled; non-speech/silence frames dropped automatically.
  3. **RED (Queue > 85% capacity):** Strict Tail-Drop policy: Drops oldest uncommitted partial frames, preserving structural audio boundaries (VAD start/stop points). Emits `stream.backpressure` event to the client to reduce upstream frame rate.

### 3.4 Silero VAD C-Go Engine
- **Requirement:** Utterance boundary detection within 10ms with minimal CPU overhead.
- **Technology:** Silero VAD v4 compiled via C-Go dynamic bindings running on CPU AVX2 instructions.
- **Thresholds:** Speech threshold set to 0.55; Silence trigger window set to 350ms before emitting `speech.stopped`.
