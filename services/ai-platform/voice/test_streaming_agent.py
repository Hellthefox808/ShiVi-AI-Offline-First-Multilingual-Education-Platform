"""
Unit tests for Real-Time Streaming Voice Agent, VAD, and Interruption Handling.
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
    generate_pcm16_tone
)

class TestStreamingVoiceAgent(unittest.IsolatedAsyncioTestCase):

    def setUp(self):
        self.config = VoiceAgentConfig(
            sample_rate=24000,
            vad_threshold=0.02,
            silence_duration_ms=200,
            interrupt_enabled=True,
            speaker_role="TEACHER",
            target_language="SANTHALI"
        )
        self.session = RealtimeVoiceSession(session_id="test_session_001", config=self.config)

    def test_vad_silence_vs_speech_detection(self):
        vad = AudioVADDetector(sample_rate=24000, threshold=0.02, silence_duration_ms=200)
        
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

    def test_vad_speech_stopped_event_after_silence(self):
        vad = AudioVADDetector(sample_rate=24000, threshold=0.02, silence_duration_ms=100)
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
        speech_bytes = generate_pcm16_tone(440.0, duration_seconds=0.1, sample_rate=24000, amplitude=0.3)
        
        # Ingest speech frame
        events = await self.session.handle_audio_frame(speech_bytes)
        self.assertTrue(any(e.get("type") == "input_audio_buffer.speech_started" for e in events))
        self.assertEqual(self.session.state, AgentState.LISTENING)

        # Commit and stream response
        collected_events = []
        async for event in self.session.commit_and_process(fallback_text="नमस्ते बच्चों"):
            collected_events.append(event)

        # Verify transcript completion
        transcript_done = next((e for e in collected_events if e.get("type") == "response.audio_transcript.done"), None)
        self.assertIsNotNone(transcript_done)
        self.assertIn("ᱡᱚᱦᱟᱨ", transcript_done["translated_text"])
        self.assertEqual(transcript_done["script_type"], "OL_CHIKI")

        # Verify audio PCM16 deltas
        audio_deltas = [e for e in collected_events if e.get("type") == "response.audio.delta"]
        self.assertGreaterEqual(len(audio_deltas), 1)
        for delta in audio_deltas:
            raw_pcm = base64.b64decode(delta["delta"])
            self.assertGreater(len(raw_pcm), 0)
            self.assertEqual(delta["sample_rate"], 24000)

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

if __name__ == "__main__":
    unittest.main()
