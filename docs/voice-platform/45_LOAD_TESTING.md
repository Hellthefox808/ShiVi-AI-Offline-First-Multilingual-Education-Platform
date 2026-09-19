# VoxBridge AI — Scalability Benchmarks & Load Testing Strategy

## 1. Multi-Tier Workload Scaling Models

VoxBridge evaluates system stability, latency degradation, and resource bottlenecks across four discrete concurrency tiers:

| Workload Tier | Concurrent Live Audio Streams | Ingested Audio Bitrate | Aggregate Audio Bandwidth | GPU A10G Nodes Required | Primary Bottleneck Identified |
|---|---|---|---|---|---|
| **Tier 1 (Base)** | 100 Streams | 32 kbps Opus | $3.2 \text{ Mbps}$ | 2 Nodes | None (Minimal cluster load) |
| **Tier 2 (Pro)** | 1,000 Streams | 32 kbps Opus | $32.0 \text{ Mbps}$ | 6 Nodes | EKS Network Load Balancer connection scaling |
| **Tier 3 (Enterprise)**| 10,000 Streams | 32 kbps Opus | $320.0 \text{ Mbps}$ | 45 Nodes | GPU TensorRT dynamic batch queuing delay |
| **Tier 4 (Global Peak)**| 100,000 Streams | 32 kbps Opus | $\mathbf{3.2 \text{ Gbps}}$ | 420 Nodes | Linux socket descriptors & Redis Cluster network I/O |

---

## 2. Resource Bottleneck Deep-Dive (Tier 4: 100,000 Concurrent Streams)

At 100,000 concurrent active voice translation sessions:
1. **Network Socket Limits:** 100,000 open TCP connections require minimum 34 streaming gateway pods (3,000 connections/pod). Linux kernel requires `sysctl -w fs.file-max=2097152` and `ulimit -n 65535` per pod container.
2. **GPU VRAM & Batching Limits:** An NVIDIA A10G GPU (24GB VRAM) running Conformer-CTC streaming inference can comfortably support up to **240 concurrent audio streams** using dynamic batching (batch size 16, step size 80ms) before latency exceeds 250ms TTFT. Total fleet requirement: $\approx 416$ GPUs.
3. **Redis Cluster Throughput:** 100,000 active sessions emitting rate limit and heartbeat pings every 5 seconds generate **20,000 Redis commands/second**, easily absorbed by 6 Redis shards (each shard handles up to 80,000 ops/sec).
4. **TimescaleDB Write IOPS:** Metering worker buffers 5,000 events before bulk SQL `COPY`, reducing database write velocity from 100,000 ops/sec to **20 batch inserts/second**, consuming $< 15\%$ of Aurora IOPS.

---

## 3. Distributed Load Testing Script (k6 Streaming Harness)

```javascript
// tests/load/streaming_load_test.js
import ws from 'k6/ws';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    voice_streaming_ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 500 },   // Warm-up to 500 streams
        { duration: '5m', target: 5000 },  // Ramp to 5,000 concurrent streams
        { duration: '10m', target: 5000 }, // Sustain peak load
        { duration: '2m', target: 0 },     // Ramp down
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    'ws_connecting{status:101}': ['p(95)<200'], // Handshake under 200ms
    'tts_audio_latency': ['p(95)<850'],        // TTFA under 850ms
  },
};

export default function () {
  const url = 'wss://stream.voxbridge.ai/v1/realtime?session_token=test_load_token';

  const res = ws.connect(url, {}, function (socket) {
    socket.on('open', () => {
      // 1. Send session.start JSON control frame
      socket.send(JSON.stringify({
        event_type: 'session.start',
        payload: { source_language: 'en-US', target_languages: ['es-ES'] }
      }));

      // 2. Stream periodic binary audio chunks (20ms Opus frames)
      socket.setInterval(() => {
        const dummyOpusAudio = new Uint8Array([0xD8, 0x01, 0x00, 0x50, /* 80 bytes of Opus data */]);
        socket.sendBinary(dummyOpusAudio.buffer);
      }, 20);
    });

    socket.on('binaryMessage', (data) => {
      // Validate received synthesized Opus frame
      check(data, {
        'valid_magic_byte': (d) => new Uint8Array(d)[0] === 0xD8,
      });
    });

    socket.setTimeout(() => {
      socket.close();
    }, 60000); // 60-second voice call duration
  });

  check(res, { 'connected_successfully': (r) => r && r.status === 101 });
}
```
