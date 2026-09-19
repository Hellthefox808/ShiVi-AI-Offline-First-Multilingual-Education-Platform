"""
Unit tests for Real-Time Streaming Voice Agent, VAD, Resampling, and Interruption Handling.
"""

import asyncio
import base64
import os
import sys
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from voice.streaming_agent import (
    AudioVADDetector,
    RealtimeVoiceSession,
    VoiceAgentConfig,
    AgentState,
    generate_pcm16_tone,
    resample_pcm16
)

class TestStreamingVoiceAgent(unittest.IsolatedAsyncioTestCase):

    def setUp(self):
        self.config = VoiceAgentConfig(
            sample_rate=24000,
            vad_threshold=0.02,
            silence_duration_ms=200,
            prefix_padding_ms=100,
            min_speech_duration_ms=60,
            interrupt_enabled=True,
            speaker_role="TEACHER",
            target_language="SANTHALI"
        )
        self.session = RealtimeVoiceSession(session_id="test_session_001", config=self.config)

    def test_resample_pcm16_16k_to_24k(self):
        # 100ms of 16kHz audio = 1600 samples = 3200 bytes
        audio_16k = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=16000, amplitude=0.3)
        self.assertEqual(len(audio_16k), 3200)

        # Resample 16kHz -> 24kHz
        audio_24k = resample_pcm16(audio_16k, src_rate=16000, dst_rate=24000)
        # Expected: 2400 samples = 4800 bytes
        self.assertEqual(len(audio_24k), 4800)

        # Same rate returns identical buffer without copy overhead
        unchanged = resample_pcm16(audio_16k, 16000, 16000)
        self.assertEqual(unchanged, audio_16k)

    def test_vad_silence_vs_speech_detection(self):
        vad = AudioVADDetector(
            sample_rate=24000,
            threshold=0.02,
            silence_duration_ms=200,
            min_speech_duration_ms=60
        )

        # 1. Complete silence (zeros)
        silence_bytes = bytes(4800)  # 2400 samples = 100ms at 24kHz
        is_speech, event = vad.process_frame(silence_bytes)
        self.assertFalse(is_speech)
        self.assertIsNone(event)
        self.assertEqual(vad.calculate_rms(silence_bytes), 0.0)

        # 2. Audible speech tone (440Hz, amplitude 0.3)
        speech_bytes = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.3)
        rms = vad.calculate_rms(speech_bytes)
        self.assertGreater(rms, 0.1)

        is_speech, event = vad.process_frame(speech_bytes)
        self.assertTrue(is_speech)
        self.assertEqual(event, "speech_started")
        self.assertTrue(vad.is_speech_active)

    def test_vad_prefix_padding_retention(self):
        vad = AudioVADDetector(
            sample_rate=24000,
            threshold=0.02,
            silence_duration_ms=200,
            prefix_padding_ms=100,
            min_speech_duration_ms=60
        )

        # Ingest 100ms silence tone (low energy background)
        low_noise = generate_pcm16_tone(100.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.005)
        vad.process_frame(low_noise)
        self.assertFalse(vad.is_speech_active)
        self.assertGreater(len(vad.prefix_buffer), 0)

        # Ingest speech frame
        speech_bytes = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.3)
        is_speech, event = vad.process_frame(speech_bytes)
        self.assertEqual(event, "speech_started")

        # Pop prefix padding
        prefix = vad.pop_prefix_padding()
        self.assertGreater(len(prefix), 0)
        self.assertEqual(len(vad.prefix_buffer), 0)

    def test_vad_speech_stopped_event_after_silence(self):
        vad = AudioVADDetector(
            sample_rate=24000,
            threshold=0.02,
            silence_duration_ms=100,
            min_speech_duration_ms=60
        )
        speech_bytes = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.3)
        vad.process_frame(speech_bytes)
        self.assertTrue(vad.is_speech_active)

        # Feed 150ms of silence
        silence_bytes = bytes(int(0.15 * 24000 * 2))
        is_speech, event = vad.process_frame(silence_bytes)
        self.assertFalse(is_speech)
        self.assertEqual(event, "speech_stopped")
        self.assertFalse(vad.is_speech_active)

    async def test_session_speech_flow_and_streaming_deltas(self):
        # Test feeding 16kHz client audio frame (automatic resampling to 24kHz)
        speech_16k = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=16000, amplitude=0.3)

        events = await self.session.handle_audio_frame(speech_16k, client_sample_rate=16000)
        self.assertTrue(any(e.get("type") == "input_audio_buffer.speech_started" for e in events))
        self.assertEqual(self.session.state, AgentState.LISTENING)

        # Commit and stream response
        collected_events = []
        async for event in self.session.commit_and_process(fallback_text="नमस्ते बच्चों"):
            collected_events.append(event)

        # Verify progressive transcript deltas (TTFT)
        transcript_deltas = [e for e in collected_events if e.get("type") == "response.audio_transcript.delta"]
        self.assertGreaterEqual(len(transcript_deltas), 1)

        # Verify transcript completion
        transcript_done = next((e for e in collected_events if e.get("type") == "response.audio_transcript.done"), None)
        self.assertIsNotNone(transcript_done)
        self.assertIn("ᱡᱚᱦᱟᱨ", transcript_done["translated_text"])
        self.assertEqual(transcript_done["script_type"], "OL_CHIKI")
        self.assertIn("ttft_ms", transcript_done)

        # Verify audio PCM16 deltas
        audio_deltas = [e for e in collected_events if e.get("type") == "response.audio.delta"]
        self.assertGreaterEqual(len(audio_deltas), 1)
        for delta in audio_deltas:
            raw_pcm = base64.b64decode(delta["delta"])
            self.assertGreater(len(raw_pcm), 0)
            self.assertEqual(delta["sample_rate"], 24000)

        # Verify metrics event
        metrics_event = next((e for e in collected_events if e.get("type") == "response.metrics"), None)
        self.assertIsNotNone(metrics_event)
        self.assertIn("ttfa_ms", metrics_event)
        self.assertIn("ttft_ms", metrics_event)
        self.assertIn("total_turn_ms", metrics_event)
        self.assertTrue(metrics_event["sla_compliant"])

        # Verify response done
        response_done = next((e for e in collected_events if e.get("type") == "response.done"), None)
        self.assertIsNotNone(response_done)
        self.assertEqual(self.session.state, AgentState.IDLE)

    async def test_barge_in_interruption_cancels_playback(self):
        # Manually set session state as SPEAKING
        self.session.state = AgentState.SPEAKING
        self.session.is_speaking = True
        self.session.active_turn_id = "turn_interruption_test"

        # Simulate user speaking while assistant is speaking (barge-in)
        loud_pcm = generate_pcm16_tone(600.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.4)
        events = await self.session.handle_audio_frame(loud_pcm)

        # Verify interruption event was emitted
        interrupt_event = next((e for e in events if e.get("type") == "response.interrupted"), None)
        self.assertIsNotNone(interrupt_event)
        self.assertEqual(interrupt_event["turn_id"], "turn_interruption_test")
        self.assertFalse(self.session.is_speaking)
        self.assertTrue(self.session._interrupted)

    def test_webrtc_offer_and_livekit_token_endpoints(self):
        from fastapi.testclient import TestClient
        from main import app

        client = TestClient(app)

        # 1. Test WebRTC SDP offer negotiation
        webrtc_res = client.post("/api/v1/voice/webrtc/offer", json={
            "sdp": "v=0\r\no=- 12345 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111\r\n",
            "type": "offer",
            "target_language": "SANTHALI",
            "speaker_role": "TEACHER"
        })
        self.assertEqual(webrtc_res.status_code, 200)
        data = webrtc_res.json()
        self.assertEqual(data["type"], "answer")
        self.assertIn("v=0", data["sdp"])
        self.assertIn("ice_servers", data)

        # 2. Test LiveKit room token minting
        livekit_res = client.post("/api/v1/voice/livekit/token", json={
            "room_name": "classroom-ranchi-01",
            "participant_name": "shikshak_ramesh",
            "role": "speaker",
            "target_language": "SANTHALI"
        })
        self.assertEqual(livekit_res.status_code, 200)
        lk_data = livekit_res.json()
        self.assertTrue(lk_data["token"].startswith("lk_"))
        self.assertTrue(lk_data["grants"]["can_publish"])
        self.assertEqual(lk_data["room_name"], "classroom-ranchi-01")

if __name__ == "__main__":
    unittest.main()
