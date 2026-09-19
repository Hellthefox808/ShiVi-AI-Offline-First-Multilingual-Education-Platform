# VoxBridge AI — Developer Experience (DX) & Ecosystem

## 1. Developer Platform Architecture

VoxBridge AI is built from the ground up to deliver a world-class developer experience, minimizing time-to-first-voice-stream ($\text{TTFVS} \le 5\text{ minutes}$) through interactive sandboxes, idiomatic client SDKs, unified documentation, and local debugging utilities.

```
                           [VOXBRIDGE DEVELOPER ECOSYSTEM]
                                          │
       ┌──────────────────┬───────────────┴───────────────┬──────────────────┐
       ▼                  ▼                               ▼                  ▼
[DOCUMENTATION]     [OFFICIAL SDKs]             [DEVELOPER TOOLS]    [SANDBOX ENVIRONMENT]
- OpenAPI 3.1       - TypeScript (Web/Node)     - VoxBridge CLI      - Synthetic audio simulator
- Interactive Specs - Python (AI / FastAPI)     - Webhook Replayer   - Zero-billing credit pool
- Language Matrices - Kotlin (Android)          - Local Emulator     - Latency jitter simulator
- Code Recipes      - Swift (iOS)               - Postman Collection - Fast acoustic playground
```

---

## 2. Interactive Sandbox & Local Development Emulator

### 2.1 The VoxBridge Sandbox (`api.sandbox.voxbridge.ai`)
- **Zero Cost:** Developers test real-time voice translation pipelines with an unlimited, non-billable test credit quota.
- **Deterministic Testing:** Includes synthetic speech endpoints that return pre-recorded multilingual responses with deterministic latencies to facilitate reproducible CI integration tests.

### 2.2 Local Dockerized Emulator (`voxbridge-emulator`)
Developers can run a lightweight, CPU-optimized emulator locally on their laptops:
```bash
# Run local VoxBridge emulator on port 8080
docker run -p 8080:8080 -p 8443:8443 \
  -e VOX_MODE=EMULATOR \
  voxbridge/emulator:latest
```

---

## 3. Official VoxBridge CLI (`vox`)

The `vox` command-line utility provides rapid scaffolding, session inspection, and webhook debugging:

```bash
# 1. Install VoxBridge CLI
brew install voxbridge/tap/vox

# 2. Authenticate
vox auth login --key=vox_live_sec_7a8b...

# 3. Test real-time microphone translation in your terminal
vox stream --source=en-US --target=es-ES --voice=mateo

# 4. Forward live webhook events directly to your local development server
vox webhooks listen --forward-to=http://localhost:3000/api/webhooks
```

---

## 4. Five-Minute Quickstart (TypeScript SDK)

```typescript
import { VoxBridgeClient } from '@voxbridge/web-sdk';

// 1. Initialize client with ephemeral session token
const vox = new VoxBridgeClient({
  sessionToken: 'vxt_01J8V3M4K5N6P7Q8R9S0T1U2V3',
  onPartialTranscript: (t) => console.log('Live Captions:', t.text),
  onTranslation: (t) => console.log(`Translated (${t.targetLanguage}):`, t.text),
  onAudioOutput: (audioChunk) => playAudioInBrowser(audioChunk),
});

// 2. Start capturing microphone and streaming Opus frames
await vox.startAudioCapture({
  sampleRateHz: 48000,
  enableAcousticEchoCancellation: true,
});

console.log('VoxBridge streaming active. Speak into microphone...');
```
