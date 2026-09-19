# VoxBridge AI — Real-Time Streaming Protocol Specification

## 1. Protocol Evaluation & Selection Rationale

| Protocol | Latency (One-Way) | Transport | Media Overhead | Browser / Mobile Support | Enterprise Firewall Traversal | Chosen Role in VoxBridge AI |
|---|---|---|---|---|---|---|
| **WebSockets (RFC 6455)** | $20\text{ms} - 45\text{ms}$ | TCP | Low (4-byte binary frame header) | Universal (100% Modern Web & Mobile) | Excellent (Port 443 WSS over TLS) | **Primary Ingestion & Client Streaming Transport** |
| **WebRTC (DataChannel + SRTP)** | $10\text{ms} - 25\text{ms}$ | UDP (SCTP/SRTP) | Lowest (RTP packetization) | High (Requires STUN/TURN infrastructure) | Medium (UDP blocked in strict corporate proxies) | **Carrier / Telephony & Ultra-Low Latency Interactive Mode** |
| **Server-Sent Events (SSE)** | $30\text{ms} - 60\text{ms}$ | HTTP/2 TCP | High (Base64 encoding required for binary) | High (Browser native) | Excellent | Rejected (Unidirectional; client cannot stream audio upstream) |
| **gRPC Streaming** | $15\text{ms} - 30\text{ms}$ | HTTP/2 TCP | Lowest (Protobuf binary framing) | Low in Browser (Requires gRPC-Web proxy) | Medium | **Internal Service-to-Service Ingestion Standard** |

---

## 2. Wire Format Framing Architecture

VoxBridge streaming uses a multiplexed binary protocol over a single WebSocket connection. Each frame consists of a **4-byte Fixed Header** followed by a variable-length payload:

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|   Magic Byte  |   Frame Type  |         Payload Length        |
|     (0xD8)    |  (0x01..0x05) |      (16-bit Big-Endian)      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               |
|                        Payload Data                           |
|                    (Opus audio OR JSON)                       |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

### 2.1 Frame Type Definitions

- `0x01` — **Client Audio Chunk:** Binary Opus/PCM frame.
- `0x02` — **Control / Event JSON:** UTF-8 JSON control messages (Client $\leftrightarrow$ Server).
- `0x03` — **Server Audio Output:** Synthesized target speech chunk (Binary Opus).
- `0x04` — **Heartbeat Ping/Pong:** 8-byte monotonic timestamp for RTT measurement.
- `0x05` — **Backpressure Flow Control:** Server rate-throttle window.

---

## 3. Message Sequence & Real-Time Event Catalog

### 3.1 Client $\to$ Server Event Messages (Frame Type `0x02`)

#### 1. `session.start`
Initiated immediately following WebSocket handshake completion:
```json
{
  "event_type": "session.start",
  "event_id": "evt_01J8W1A2B3C4",
  "timestamp_ms": 1726756800000,
  "payload": {
    "session_token": "vxt_01J8V3M4K5N6...",
    "client_info": {
      "sdk_name": "voxbridge-web-sdk",
      "sdk_version": "1.4.2",
      "user_agent": "Mozilla/5.0..."
    }
  }
}
```

#### 2. `audio.commit`
Signals the intentional end of an utterance (manual push-to-talk release) when VAD is disabled:
```json
{
  "event_type": "audio.commit",
  "event_id": "evt_01J8W1A2B3C5",
  "sequence_number": 412,
  "timestamp_ms": 1726756804500
}
```

#### 3. `session.update`
Dynamically alter target languages or voice selection mid-call:
```json
{
  "event_type": "session.update",
  "event_id": "evt_01J8W1A2B3C6",
  "payload": {
    "target_languages": ["de-DE", "ja-JP"],
    "voice_speed": 1.1
  }
}
```

---

### 3.2 Server $\to$ Client Event Messages (Frame Type `0x02`)

#### 1. `transcript.partial` & `transcript.final`
Emitted as the speech recognizer decodes phonemes:
```json
{
  "event_type": "transcript.partial",
  "session_id": "sess_01J8V3M4K5N6",
  "sequence_number": 89,
  "timestamp_ms": 1726756801200,
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "payload": {
    "text": "Hello, how are you",
    "is_final": false,
    "confidence": 0.94,
    "start_time_ms": 200,
    "end_time_ms": 1100,
    "speaker_id": "speaker_0"
  }
}
```

#### 2. `translation.partial` & `translation.final`
Emitted as machine translation tokens stream out:
```json
{
  "event_type": "translation.final",
  "session_id": "sess_01J8V3M4K5N6",
  "sequence_number": 90,
  "timestamp_ms": 1726756801450,
  "trace_id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "payload": {
    "source_language": "en-US",
    "target_language": "es-ES",
    "translated_text": "Hola, ¿cómo estás?",
    "utterance_id": "utt_01J8W2C3D4",
    "glossary_matches": 0
  }
}
```

#### 3. `audio.output.chunk`
Preceded by metadata frame; raw audio emitted over Frame Type `0x03` with chunk header:
```json
{
  "event_type": "audio.output.started",
  "session_id": "sess_01J8V3M4K5N6",
  "sequence_number": 91,
  "timestamp_ms": 1726756801600,
  "payload": {
    "utterance_id": "utt_01J8W2C3D4",
    "target_language": "es-ES",
    "codec": "OPUS",
    "sample_rate_hz": 24000,
    "total_chunks_estimated": 14
  }
}
```

---

## 4. Backpressure, Flow Control & Drop Policies

```
             CLIENT BUFFER                              SERVER INGESTION BUFFER
   ┌───────────────────────────────┐               ┌───────────────────────────────┐
   │ Audio Queue (Max 500ms)       │               │ Ring Buffer (Max 250ms)       │
   │  [Frame 1][Frame 2][Frame 3]  │               │  [Slot 1][Slot 2][Slot 3]     │
   └───────────────┬───────────────┘               └───────────────┬───────────────┘
                   │                                               │
                   ▼ (WSS Stream)                                  ▼ (gRPC Stream)
          WebSocket Socket                                  STT Engine
                   ▲                                               │
                   │ (Frame 0x05: Window Size = 0)                 ▼
   ┌───────────────┴───────────────┐                   Buffer High-Watermark (>85%)
   │ Backpressure Signal Detected: │                   Trigger Silence Drop / 
   │ Pause upstream capture or drop│                   Force VAD Boundary
   └───────────────────────────────┘
```

1. **Sliding Window Credit:** The server continuously issues window credits in ping/pong exchanges. If downstream GPU inference stalls, credit falls to zero, instructing the client SDK to temporarily mute local capture or drop non-voiced frames.
2. **Audio Drop Order:**
   - *Drop Priority 1:* Silence frames flagged by local client VAD.
   - *Drop Priority 2:* Redundant partial translation emissions.
   - *Drop Priority 3 (Extreme Overload):* Drop interim STT results; proceed directly to final utterance synthesis to preserve conversation timing.
3. **Session Hard Limits:**
   - Maximum WebSocket frame size: `64 KB`.
   - Maximum continuous session duration: `4 hours` (clients receive `session.expiring` warning at 3 hours 55 minutes).
   - Inactivity timeout: Disconnect after `30 seconds` of zero client audio frames and missing pings.
