"""
BhashaSetu AI — Voice AI Subsystem Fine-Tuning & Acoustic Benchmark Suite (v3.0.0-PROD)
Tests:
- 4 Pedagogical Voice Timbre Presets (CLEAR_EDUCATIONAL, WARM_TEACHER, EXPRESSIVE_STORYTELLER, YOUNG_STUDENT)
- Formant Modeling & Acoustic Resonance for Santhali, Ho, Mundari
- Phonetic Dictionary Coverage and G2P Normalization
- End-to-End Turn Processing with Relay Pause & Pitch Tuning
- Real-Time Streaming Voice Agent Delta Events & Latency
- Real-Time Factor (RTF < 0.20) and Mean Opinion Score (MOS >= 4.2)
"""

import asyncio
import base64
import math
import os
import re
import sys
import time
import unittest

# Ensure root and ai-platform directories are in path
ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
AI_DIR = os.path.join(ROOT_DIR, "services", "ai-platform")
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)
if AI_DIR not in sys.path:
    sys.path.insert(0, AI_DIR)

from voice.service import VoicePipelineService, voice_pipeline
from voice.streaming_agent import (
    RealtimeVoiceSession,
    VoiceAgentConfig,
    AgentState,
    generate_pcm16_tone
)
from translation.providers import LanguageProviderService, language_provider, TRIBAL_LEXICON


class TestVoiceFineTuning(unittest.TestCase):

    def setUp(self):
        self.pipeline = voice_pipeline
        self.lang_provider = language_provider

    # --- 1. Voice Timbre Presets & Pitch Profiles ---

    def test_voice_timbre_presets_exist_and_valid(self):
        expected_timbres = ["CLEAR_EDUCATIONAL", "WARM_TEACHER", "EXPRESSIVE_STORYTELLER", "YOUNG_STUDENT"]
        for timbre_name in expected_timbres:
            self.assertIn(timbre_name, self.pipeline.TIMBRE_PROFILES)
            profile = self.pipeline.TIMBRE_PROFILES[timbre_name]
            self.assertIn("pitch_semitones", profile)
            self.assertIn("speed_factor", profile)
            self.assertIn("gain_db", profile)
            self.assertIn("mos", profile)
            self.assertGreaterEqual(profile["mos"], 4.2, f"Timbre {timbre_name} MOS must be >= 4.2")

    def test_pitch_semitone_calculation(self):
        # Baseline pitch = 1.0 (0 semitone pitch shift from multiplier)
        result_clear = self.pipeline.process_voice_turn(
            "किताब", "SANTHALI", pitch=1.0, voice_timbre="CLEAR_EDUCATIONAL"
        )
        self.assertEqual(result_clear["audio_metadata"]["pitch_semitone_offset"], 0.0)

        # Higher pitch with YOUNG_STUDENT (+3.2 semitones timbre offset + 1.2x pitch factor)
        result_student = self.pipeline.process_voice_turn(
            "किताब", "SANTHALI", pitch=1.2, voice_timbre="YOUNG_STUDENT"
        )
        expected_semitones = round(12.0 * math.log2(1.2) + 3.2, 2)
        self.assertAlmostEqual(
            result_student["audio_metadata"]["pitch_semitone_offset"],
            expected_semitones,
            places=2
        )

    # --- 2. Tribal Formants Modeling ---

    def test_tribal_formants_acoustic_ranges(self):
        for lang in ["SANTHALI", "HO", "MUNDARI"]:
            self.assertIn(lang, self.pipeline.TRIBAL_FORMANTS)
            formants = self.pipeline.TRIBAL_FORMANTS[lang]
            f1 = formants["f1_hz"]
            f2 = formants["f2_hz"]
            f3 = formants["f3_hz"]

            # Vowel formants physiological checks
            self.assertTrue(300 <= f1 <= 900, f"{lang} F1 {f1} out of human vowel range")
            self.assertTrue(900 <= f2 <= 2500, f"{lang} F2 {f2} out of human vowel range")
            self.assertTrue(2200 <= f3 <= 3500, f"{lang} F3 {f3} out of human vowel range")

    # --- 3. Phonetic Dictionary & Normalization Coverage ---

    def test_tribal_lexicon_coverage_and_transliteration(self):
        for lang in ["SANTHALI", "HO", "MUNDARI"]:
            self.assertIn(lang, TRIBAL_LEXICON, f"Missing language {lang} in TRIBAL_LEXICON")
            terms = TRIBAL_LEXICON[lang]["terms"]
            self.assertGreaterEqual(
                len(terms), 35,
                f"{lang} lexicon must have at least 35 core pedagogical terms, found {len(terms)}"
            )

            for hindi_word, entry in terms.items():
                self.assertIn("native", entry, f"Missing native script for {hindi_word} in {lang}")
                self.assertIn("translit_hi", entry, f"Missing Hindi translit for {hindi_word} in {lang}")
                self.assertIn("translit_lat", entry, f"Missing Latin translit for {hindi_word} in {lang}")

    def test_phonetic_normalization_cleanliness(self):
        # Simulate acoustic G2P cleaner (matching Android TtsManager logic)
        def normalize_tribal_phonetics(raw: str) -> str:
            cleaned = raw.replace("᱾", "।").replace("᱿", "॥")
            # Strip parenthetical annotations like (दाक्') or (Dak')
            cleaned = re.sub(r"\([A-Za-z'’\u0900-\u097F\s]+\)", "", cleaned)
            cleaned = re.sub(r"\s+", " ", cleaned).strip()
            return cleaned

        test_cases = [
            ("ᱫᱟᱜ (Dak')", "ᱫᱟᱜ"),
            ("ᱫᱟᱜ (दाक्')", "ᱫᱟᱜ"),
            ("ᱥᱮᱪᱮᱫ ᱾ ᱯᱟᱲᱦᱟᱣ", "ᱥᱮᱪᱮᱫ । ᱯᱟᱲᱦᱟᱣ"),
            ("ᱡᱚᱦᱟᱨ (Johar) ᱜᱤᱫᱽᱨᱟᱹ", "ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ")
        ]
        for raw, expected in test_cases:
            self.assertEqual(normalize_tribal_phonetics(raw), expected)

    # --- 4. End-to-End Turn Processing with Tuning Parameters ---

    def test_process_voice_turn_all_timbres(self):
        timbres = ["CLEAR_EDUCATIONAL", "WARM_TEACHER", "EXPRESSIVE_STORYTELLER", "YOUNG_STUDENT"]
        pauses = [350, 450, 500, 600]

        for timbre, pause in zip(timbres, pauses):
            res = self.pipeline.process_voice_turn(
                hindi_transcript="नमस्ते बच्चों अपनी किताब खोलो",
                target_lang="SANTHALI",
                speaker_role="TEACHER",
                speech_rate=0.88,
                pitch=1.05,
                voice_timbre=timbre,
                relay_pause_ms=pause
            )
            self.assertEqual(res["audio_metadata"]["voice_timbre"], timbre)
            self.assertEqual(res["audio_metadata"]["relay_pause_ms"], pause)
            self.assertGreater(res["audio_metadata"]["predicted_mos_score"], 4.2)
            self.assertIn("formants", res["audio_metadata"])
            self.assertTrue(res["sla_compliant"])

    # --- 5. Real-Time Factor (RTF) Performance Benchmark ---

    def test_real_time_factor_benchmark(self):
        """
        RTF = (Processing Time) / (Audio Synthesis Duration)
        Target: RTF < 0.20 (at least 5x faster than real-time playback).
        """
        start = time.perf_counter()
        result = self.pipeline.process_voice_turn(
            hindi_transcript="जल ही जीवन है और हमें पानी बचाना चाहिए।",
            target_lang="SANTHALI",
            speech_rate=0.92,
            voice_timbre="CLEAR_EDUCATIONAL"
        )
        elapsed_sec = time.perf_counter() - start

        # Audio duration in metadata
        audio_dur_sec = result["audio_metadata"]["duration_ms"] / 1000.0
        rtf = elapsed_sec / audio_dur_sec

        self.assertLess(rtf, 0.20, f"RTF {rtf:.4f} exceeded 0.20 threshold (took {elapsed_sec*1000:.1f}ms for {audio_dur_sec}s audio)")


class TestStreamingVoiceFineTuning(unittest.IsolatedAsyncioTestCase):

    async def test_streaming_agent_timbre_and_pitch_propagation(self):
        config = VoiceAgentConfig(
            sample_rate=24000,
            voice_timbre="EXPRESSIVE_STORYTELLER",
            pitch=1.12,
            speech_rate=0.85,
            relay_pause_ms=600,
            speaker_role="TEACHER",
            target_language="SANTHALI"
        )
        session = RealtimeVoiceSession(session_id="stream_tune_001", config=config)

        events = []
        async for event in session.commit_and_process(fallback_text="पेड़ों की रक्षा करो"):
            events.append(event)

        # Check transcript done event has timbre
        done_event = next(e for e in events if e.get("type") == "response.audio_transcript.done")
        self.assertEqual(done_event["voice_timbre"], "EXPRESSIVE_STORYTELLER")
        self.assertEqual(done_event["audio_metadata"]["relay_pause_ms"], 600)

        # Check audio delta events have timbre and pitch
        audio_deltas = [e for e in events if e.get("type") == "response.audio.delta"]
        self.assertGreater(len(audio_deltas), 0)
        for d in audio_deltas:
            self.assertEqual(d["voice_timbre"], "EXPRESSIVE_STORYTELLER")
            self.assertEqual(d["pitch"], 1.12)

        # Check metrics event
        metrics_event = next(e for e in events if e.get("type") == "response.metrics")
        self.assertEqual(metrics_event["voice_timbre"], "EXPRESSIVE_STORYTELLER")
        self.assertEqual(metrics_event["pitch"], 1.12)
        self.assertEqual(metrics_event["speech_rate"], 0.85)
        self.assertEqual(metrics_event["relay_pause_ms"], 600)
        self.assertGreaterEqual(metrics_event["predicted_mos_score"], 4.2)


if __name__ == "__main__":
    unittest.main()
