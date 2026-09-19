# VoxBridge AI — Official SDK Architecture & Client Runtime

## 1. SDK Core Responsibilities & Common Invariants

Official VoxBridge SDKs provide idiomatic, zero-dependency client abstractions across web, mobile, and server environments. SDKs encapsulate the low-level complexities of audio hardware capture, real-time binary framing, jitter buffering, and network reconnection while exposing simple, reactive event streams.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VOXBRIDGE CLIENT SDK CORE                       │
├──────────────────────────┬──────────────────────────┬──────────────────┤
│    AUDIO HARDWARE LAYER  │    NETWORK & FRAMING     │  EVENT DISPATCH  │
├──────────────────────────┼──────────────────────────┼──────────────────┤
│ - Microphone Capture     │ - Binary Header Packing  │ - Reactive Flows │
│ - WebRTC AEC & AGC       │ - WebSocket Reconnect    │ - Transcript Bus │
│ - 16kHz Sinc Resampling  │ - Heartbeat Ping/Pong    │ - Audio Player   │
│ - Client VAD Pre-Filter  │ - Sequence Numbering     │ - Error Handler  │
└──────────────────────────┴──────────────────────────┴──────────────────┘
```

---

## 2. Multi-Platform SDK Support Matrix

| Platform / Language | Native Audio Driver | Opus Encoder Engine | Streaming Transport | State Reactive Primitive |
|---|---|---|---|---|
| **TypeScript (Web)** | Web Audio API / AudioWorklet | WebAssembly (Wasm) `libopus` | Native `WebSocket` / `RTCDataChannel` | EventTarget / RxJS Observables |
| **Kotlin (Android)** | Oboe (C++ AAudio / OpenSL ES) | Native C++ JNI `libopus.so` | OkHttp WebSocket / Pion WebRTC | Kotlin Coroutines `StateFlow` |
| **Swift (iOS/macOS)** | `AVAudioEngine` / CoreAudio | Native C Framework `libopus.a` | `URLSessionWebSocketTask` | Swift Concurrency `AsyncSequence` |
| **Python (Server)** | SoundDevice / PyAudio | Python `cffi` wrapper | `aiohttp` / `websockets` | Python `asyncio.Queue` |
| **Go (Server/CLI)** | PortAudio / PulseAudio | Go CGO `hraban/opus` | `gorilla/websocket` | Go typed `chan Event` |

---

## 3. Client-Side Audio Pipeline & Jitter Buffer

```
[MICROPHONE] ──► [Acoustic Echo Cancellation (AEC)] ──► [Client VAD]
                                                            │
                                            Is Voiced Audio?│
                               ┌────────────────────────────┴────────────────────────────┐
                               ▼ YES                                                     ▼ NO
                     [Opus Encoder (20ms)]                                        [Discard Frame]
                               │                                              (Saves 40% Mobile Data)
                               ▼
               [Binary Packet Header (0xD8 0x01)]
                               │
                               ▼
               [WSS Upstream Socket Connection]
```

### 3.1 Audio Playback Jitter Buffer
On the receiving side, synthesized speech packets arriving over the network experience variable transit latency. 
- The client SDK maintains an internal **60ms adaptive jitter buffer**.
- Playback begins only when the buffer accumulates 3 contiguous Opus frames, preventing audible audio clicks, under-runs, or robotic distortion caused by cellular packet jitter.

---

## 4. Kotlin (Android) Production Implementation Sample

```kotlin
// Android Jetpack Compose / Kotlin SDK Integration
class VoxBridgeSession(
    private val sessionToken: String,
    private val scope: CoroutineScope
) {
    private val _transcripts = MutableStateFlow<String>("")
    val transcripts: StateFlow<String> = _transcripts.asStateFlow()

    private val client = OkHttpClient.Builder()
        .pingInterval(Duration.ofSeconds(5))
        .build()

    fun startStreaming() {
        val request = Request.Builder()
            .url("wss://stream.voxbridge.ai/v1/realtime?session_token=$sessionToken")
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val event = Json.decodeFromString<ServerEvent>(text)
                if (event.eventType == "transcript.partial" || event.eventType == "transcript.final") {
                    _transcripts.value = event.payload.text
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Play binary Opus audio chunk through Android AudioTrack
                AudioTrackPlayer.enqueue(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Automated reconnection with backoff
                scheduleReconnect()
            }
        })
    }
}
```
