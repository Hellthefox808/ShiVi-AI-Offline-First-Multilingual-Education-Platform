# VoxBridge AI — Comprehensive Testing Strategy & Pyramid

## 1. The Quality Assurance Pyramid

VoxBridge AI enforces a rigorous multi-tier testing pyramid to guarantee linguistic fidelity, microsecond real-time responsiveness, and distributed fault resilience:

```
                      / \
                     /   \
                    / E2E \  (Full WebRTC / WebSocket End-to-End Voice Calls)
                   /-------\
                  / Chaos & \  (LitmusChaos: GPU Pod Kills, Network Latency, Redis Outages)
                 /   Load    \ (k6 / Locust: 100k Concurrent Audio Streams)
                /-------------\
               /  Integration  \ (Testcontainers: PostgreSQL, Redis, NATS, MinIO)
              /   & Contract    \ (Pact: Schema & gRPC Backward Compatibility)
             /-------------------\
            /     Unit Tests      \ (Deterministic Algorithms, VAD Chunking, Quota Math)
           /-----------------------\
```

---

## 2. Test Execution Tiers & Framework Catalog

| Test Tier | Scope & Focus | Frameworks & Tooling | Execution Frequency | Enforced Passing Threshold |
|---|---|---|---|---|
| **Unit Tests** | Audio frame demuxing, VAD state transitions, sliding-window rate limiters, token calculators. | Go `testing`, PyTest, Rust `cargo test` | Every commit / PR | $100\%$ pass; $\ge 85\%$ line coverage |
| **Contract Tests** | API Gateway to microservice gRPC Protobuf compatibility; REST error schemas. | Pact Foundation, Prototool | Every PR | $100\%$ pass (Zero breaking changes) |
| **Integration Tests** | PostgreSQL RLS isolation, Redis Cluster failover, NATS JetStream consumer acks. | `testcontainers-go`, Docker Compose | Every PR merge to main | $100\%$ pass |
| **Streaming E2E** | Live WebSocket audio frame exchange, partial captions, translation, and TTS audio playback. | Custom Go Headless WebRTC Client | Nightly / Pre-Release | $100\%$ pass; TTFA $\le 850\text{ms}$ |
| **Linguistic Quality**| Speech recognition WER, translation COMET/BLEU, and audio MOS quality validation. | JiWER, SacreBLEU, Unbabel COMET | Weekly / Model Update | WER $\le 8.5\%$; COMET $\ge 0.82$ |
| **Chaos Resilience** | Worker node termination, provider 500 injection, network jitter degradation. | Chaos Mesh, LitmusChaos | Bi-Weekly Staging Soak | Zero orphaned sessions; 100% failover |

---

## 3. Automated Audio & Streaming Test Harness

Testing a real-time voice translation platform cannot rely on mocked text strings alone. VoxBridge maintains a programmatic headless streaming audio test client written in Go:

```go
func TestStreamingTranslationE2E(t *testing.T) {
    // 1. Initialize Headless WebSocket Connection
    wsURL := "wss://api.sandbox.voxbridge.ai/v1/realtime?session_token=" + testToken
    conn, _, err := websocket.DefaultDialer.Dial(wsURL, nil)
    require.NoError(t, err)
    defer conn.Close()

    // 2. Stream Real 16kHz PCM Audio Chunks (80ms frames)
    audioFile, err := os.Open("testdata/sample_en_utterance.raw")
    require.NoError(t, err)
    defer audioFile.Close()

    frameBuffer := make([]byte, 2560) // 80ms @ 16kHz 16-bit
    startAudioTime := time.Now()

    for {
        n, err := audioFile.Read(frameBuffer)
        if err == io.EOF { break }
        
        // Wrap in binary frame header (Magic: 0xD8, Type: 0x01)
        wirePacket := append([]byte{0xD8, 0x01, byte(n >> 8), byte(n & 0xFF)}, frameBuffer[:n]...)
        err = conn.WriteMessage(websocket.BinaryMessage, wirePacket)
        require.NoError(t, err)
        time.Sleep(80 * time.Millisecond) // Real-time pacing
    }

    // 3. Assert Timely Arrival of Synthesized Audio Output
    _, responseBytes, err := conn.ReadMessage()
    require.NoError(t, err)
    ttfa := time.Since(startAudioTime)

    // Assert TTFA meets strict latency budget
    assert.Less(t, ttfa, 1200*time.Millisecond, "Time to First Audio exceeded acceptable budget")
    assert.Equal(t, byte(0xD8), responseBytes[0], "Valid magic byte expected")
    assert.Equal(t, byte(0x03), responseBytes[1], "Output audio frame expected")
}
```
