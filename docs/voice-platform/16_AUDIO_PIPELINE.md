# VoxBridge AI — Audio Ingestion & Processing Pipeline

## 1. Ingestion Pipeline Overview

The audio pipeline transforms heterogeneous incoming audio streams and static files into clean, normalized, 16kHz single-channel linear PCM frames required by the underlying acoustic speech models.

```
[RAW INCOMING AUDIO] 
       │
       ▼
 1. HEADER & CONTAINER VALIDATION (Magic byte inspection, frame integrity, chunk size checks)
       │
       ▼
 2. SECURE DECODING & DEMUXING (FFmpeg / libopus in memory, zero temporary files)
       │
       ▼
 3. AUDIO NORMALIZATION (LUFS loudness leveling to -23 LUFS, DC-offset filter)
       │
       ▼
 4. RESAMPLING & CHANNEL REDUCTION (High-fidelity sinc resampler -> 16,000 Hz, 16-bit Mono PCM)
       │
       ▼
 5. VOICE ACTIVITY DETECTION (Silero VAD v4 on 10ms / 160-sample sliding windows)
       │
       ▼
 6. UTTERANCE SEGMENTATION (Silence threshold: 350ms, Min duration: 200ms, Max: 15,000ms)
       │
       ▼
[NORMALIZED AUDIO FRAMES -> STT INFERENCE WORKERS]
```

---

## 2. Audio Format Specification Matrix

| Parameter | Streaming Ingestion (WSS / WebRTC) | Batch Audio File Upload (REST / S3) | Output Synthesized Audio Stream |
|---|---|---|---|
| **Supported Codecs** | Opus (RFC 6716), Linear PCM (Signed 16-bit LE) | WAV, MP3, AAC, FLAC, Ogg/Opus, WebM | Opus (Default), Linear PCM (Raw WAV) |
| **Supported Sample Rates** | 16,000 Hz, 24,000 Hz, 48,000 Hz | 8,000 Hz to 96,000 Hz (Any standard) | 24,000 Hz (Standard), 48,000 Hz (High-Fi) |
| **Channels** | 1 (Mono) or 2 (Stereo downmixed to Mono) | 1 to 8 Channels (Downmixed or Diarized) | 1 (Mono) |
| **Frame / Chunk Size** | 20ms chunks (960 samples @ 48kHz Opus) | Dynamic buffer chunks (1 MB streaming read) | 20ms Opus frames (40ms aggregated bursts) |
| **Bit Depth** | 16-bit Signed Integer (Linear PCM) | 16-bit, 24-bit, 32-bit float | 16-bit Signed Integer |
| **Bitrate** | 16 kbps to 64 kbps (Opus variable bitrate) | Up to 320 kbps (MP3/AAC) | 32 kbps (Voice optimized Opus VBR) |
| **Max Payload Size** | 64 KB per WebSocket frame | 500 MB per batch file | 64 KB per binary audio output frame |
| **Max Stream Duration** | 4 Hours continuous per session | 3 Hours per audio file | N/A (Emitted dynamically) |

---

## 3. Secure Decoding and Sanitization

### 3.1 Memory Safety and Container Traps
Media decoding libraries (`libavcodec`, `libopus`, `libvorbis`) are historically prone to memory corruption vulnerabilities (heap overflows, out-of-bounds reads from crafted audio metadata headers). VoxBridge enforces strict sandboxing:

1. **Magic Byte Verification:** The ingestion proxy validates the initial bytes before invoking any decoding library:
   - Opus in Ogg: `OggS` (`0x4F 0x67 0x67 0x53`)
   - RIFF WAV: `RIFF....WAVE` (`0x52 0x49 0x46 0x46 .... 0x57 0x41 0x56 0x45`)
   - FLAC: `fLaC` (`0x66 0x4C 0x61 0x43`)
   - MP3: Sync word `0xFF 0xFB` or ID3 tag `ID3` (`0x49 0x44 0x33`)
2. **Memory-Bounded Demuxing:** Ingestion decoders run with strict Linux `cgroups v2` limits: maximum 128 MB RAM per decode process, terminating immediately on memory growth spikes.
3. **Strip Non-Audio Streams:** Video tracks, embedded cover art images, ID3 lyrics, and arbitrary binary metadata chunks are rejected or stripped prior to processing.

---

## 4. Voice Activity Detection (VAD) & Utterance Segmentation

### 4.1 Algorithm and Model Configuration
VoxBridge utilizes **Silero VAD v4** (a lightweight, highly optimized convolutional recurrent neural network) compiled into native C-Go shared libraries.

- **Window Size:** 160 samples (10ms @ 16kHz).
- **Speech Probability Calculation:**
  $$P(\text{speech}_t) = \text{Model}(\text{chunk}_t, \text{hidden\_state}_{t-1})$$
- **Speech Threshold ($\alpha_{\text{speech}}$):** `0.55`. Frames with $P(\text{speech}) \ge 0.55$ increment the active speech duration counter.
- **Silence Threshold ($\alpha_{\text{silence}}$):** `0.35`. Frames with $P(\text{speech}) < 0.35$ increment the consecutive silence counter.

### 4.2 State Machine & Utterance Boundary Guardrails

```
             ┌──────────────┐
             │   IDLE /     │◄─────────────────────────────┐
             │   SILENCE    │                              │
             └──────┬───────┘                              │
                    │                                      │
     P(speech) >= 0.55 for >= 200ms                        │
                    │                                      │
                    ▼                                      │
             ┌──────────────┐                              │
             │   VOICED     │                              │
             │  UTTERANCE   │                              │
             └──────┬───────┘                              │
                    │                                      │
       P(speech) < 0.35 for >= 350ms                       │
       OR Max Utterance Duration (15,000ms) reached        │
                    │                                      │
                    ▼                                      │
             ┌──────────────┐                              │
             │   UTTERANCE  │                              │
             │  COMMITTED   ├──────────────────────────────┘
             └──────────────┘
```

1. **Minimum Speech Duration:** `200ms`. Audio bursts shorter than 200ms (coughing, microphone clicks, background desk taps) are discarded as acoustic artifacts.
2. **Silence Release Window:** `350ms`. Once an active speaker pauses for 350ms, the utterance is sealed, and `speech.stopped` is dispatched to the pipeline orchestrator.
3. **Maximum Utterance Cap:** `15,000ms (15 seconds)`. If a speaker talks continuously without taking a breath for 15 seconds, the VAD engine forces a boundary at the lowest local energy minimum, preventing unbounded STT decoding latency.
4. **Pre-Roll / Post-Roll Audio Padding:**
   - Pre-roll padding: `100ms` of audio immediately preceding the detected speech start is retained to capture initial unvoiced consonants (e.g., /p/, /t/, /k/).
   - Post-roll padding: `150ms` of audio following speech cessation is retained to prevent clipping tail phonemes.
