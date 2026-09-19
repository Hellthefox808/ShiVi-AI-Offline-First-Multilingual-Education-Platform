# VoxBridge AI — Audio Testing & Acoustic Stress Matrix

## 1. Acoustic Test Matrix & Edge Case Scenarios

Speech models that perform well on pristine studio audio frequently collapse in noisy real-world call centers or mobile environments. VoxBridge validates every model release against an exhaustive **12-Point Acoustic Stress Matrix**:

| Scenario ID | Test Environment / Condition | Target Signal-to-Noise (SNR) | Audio Artifact Injected | Minimum Acceptable STT WER | Audio Output POLQA (MOS) |
|---|---|---|---|---|---|
| **ATM-01** | Quiet Studio Room (Baseline) | $> 35 \text{ dB}$ | None (Pristine 16kHz PCM) | $\le 6.5\%$ | $\ge 4.4$ (Excellent) |
| **ATM-02** | Busy Call Center Chatter | $10 \text{ dB} - 15 \text{ dB}$ | Overlapping background speech | $\le 10.5\%$ | $\ge 4.0$ (Good) |
| **ATM-03** | Street / Traffic Noise | $5 \text{ dB} - 10 \text{ dB}$ | Engine rumble, car horns, siren | $\le 12.0\%$ | $\ge 3.8$ (Fair) |
| **ATM-04** | Coffee Shop / Cafeteria | $8 \text{ dB} - 12 \text{ dB}$ | Clattering dishes, ambient babble | $\le 11.0\%$ | $\ge 3.9$ (Good) |
| **ATM-05** | Background Radio / Music | $10 \text{ dB}$ | Classical and pop music audio | $\le 11.5\%$ (Music ignored) | $\ge 4.1$ (Good) |
| **ATM-06** | Heavy Acoustic Echo | N/A (AEC Test) | 450ms delayed speaker feedback | $\le 8.0\%$ (Echo cancelled) | $\ge 4.2$ (Good) |
| **ATM-07** | Severe Packet Loss | Network Simulation | $15\%$ Random UDP packet drop | $\le 13.5\%$ (PLC recovery) | $\ge 3.6$ (Acceptable) |
| **ATM-08** | Severe Network Jitter | Network Simulation | 120ms Jitter buffer variation | $\le 9.0\%$ (Buffer absorbed) | $\ge 4.0$ (Good) |
| **ATM-09** | Extreme Fast Speech | Clean ($> 220 \text{ WPM}$) | Rapid conversation cadence | $\le 12.0\%$ | $\ge 4.1$ (Good) |
| **ATM-10** | Whispered Speech | Low Energy ($< 35 \text{ dB}$) | Unvoiced vocal fold vibrations | $\le 14.5\%$ | $\ge 3.8$ (Fair) |
| **ATM-11** | Heavy Non-Native Accent | Clean (Top 10 Accents) | Regional phonetic substitution | $\le 11.0\%$ | $\ge 4.1$ (Good) |
| **ATM-12** | 4-Hour Soak Stream | Mixed Noise | Continuous multi-hour stream | $\le 8.5\%$ (Zero drift) | $\ge 4.2$ (Stable) |

---

## 2. Automated Acoustic Degradation Tooling

Tests are automated using **Toxiproxy** for network impairment and **SoX / FFmpeg** for synthetic acoustic noise injection:

```bash
#!/usr/bin/env bash
# inject_noise_and_eval.sh - Acoustic Benchmark Script

# 1. Mix clean audio with street noise at 10dB SNR
ffmpeg -i testdata/clean_speech.wav -i testdata/noise_street.wav \
  -filter_complex "[0:a][1:a]amix=inputs=2:weights=1 0.3[out]" \
  -map "[out]" -c:a pcm_s16le testdata/degraded_10db.wav

# 2. Run Headless Streaming Test Harness
./bin/vox-audio-tester --file=testdata/degraded_10db.wav --expected="I need to verify my checking account balance."

# 3. Calculate Word Error Rate (WER) via JiWER Python CLI
python3 -c "
import jiwer
ref = 'I need to verify my checking account balance.'
hyp = open('/tmp/test_output_transcript.txt').read()
wer = jiwer.wer(ref, hyp)
print(f'Calculated WER: {wer * 100:.2f}%')
assert wer <= 0.12, 'Acoustic degradation breached WER budget!'
"
```
