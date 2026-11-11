import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AiClientService } from '../src/ai-client/ai-client.service';
import { VoiceController } from '../src/ai-client/voice.controller';

describe('BhashaSetu Voice Relay & Phonetic Synthesis', () => {
  let aiClientService: AiClientService;
  let voiceController: VoiceController;

  beforeEach(() => {
    aiClientService = new AiClientService();
    voiceController = new VoiceController(aiClientService);
  });

  it('should translate Hindi speech turn to Santhali Ol Chiki with bilingual relay metadata', async () => {
    const result = await voiceController.translateVoice({
      transcriptionHindi: 'पेड़ हमारे सच्चे मित्र हैं',
      targetLanguage: 'SANTHALI',
      flnMode: true,
      bilingualRelay: true,
    });

    assert.ok(result.turn_id.startsWith('VOICE-'));
    assert.equal(result.target_language, 'SANTHALI');
    assert.equal(result.script_type, 'OL_CHIKI');
    assert.ok(result.tribal_native_text.length > 0);
    assert.ok(result.transliteration_devanagari.length > 0);
    assert.equal(result.acoustic_engine, 'hi-IN');
    assert.equal(result.speech_rate, 0.72);
    assert.equal(result.bilingual_relay.enabled, true);
    assert.equal(result.bilingual_relay.source_audio_pause_ms, 450);
    assert.deepEqual(result.bilingual_relay.relay_sequence, [
      'SOURCE_HINDI',
      'PAUSE_450MS',
      'TRIBAL_PHONETIC_HI_IN',
    ]);
    assert.ok(result.comet_score >= 0.9);
  });

  it('should translate Hindi speech turn to Ho Warang Chiti with 0.72x FLN speech rate', async () => {
    const result = await voiceController.translateVoice({
      transcriptionHindi: 'बच्चे कक्षा में खेल रहे हैं',
      targetLanguage: 'HO',
      flnMode: true,
      bilingualRelay: true,
    });

    assert.equal(result.target_language, 'HO');
    assert.equal(result.script_type, 'WARANG_CHITI');
    assert.ok(result.transliteration_devanagari.includes('होनको'));
    assert.equal(result.speech_rate, 0.72);
    assert.equal(result.bilingual_relay.source_audio_pause_ms, 450);
  });

  it('should handle standard speech rate when FLN mode is false', async () => {
    const result = await aiClientService.translateVoiceTurn({
      transcriptionHindi: 'यह एक सामान्य वाक्य है',
      targetLanguage: 'MUNDARI',
      flnMode: false,
      bilingualRelay: false,
    });

    assert.equal(result.target_language, 'MUNDARI');
    assert.equal(result.speech_rate, 1.0);
    assert.equal(result.bilingual_relay.enabled, false);
  });

  it('should render a two-way STUDENT turn as Hindi comprehension, not teacher enunciation', async () => {
    // The local fallback previously ignored speakerRole entirely, so a student's mother-tongue
    // utterance came back as the teacher's Devanagari enunciation of the source text.
    const studentResult = await aiClientService.translateTwoWayVoiceTurn({
      studentTribalTranscript: 'ᱡᱚᱦᱟᱨ ᱢᱟᱪᱮᱛ ᱜᱚᱢᱠᱮ',
      targetLanguage: 'SANTHALI',
      speakerRole: 'STUDENT',
    });

    assert.equal(studentResult.speaker_role, 'STUDENT');
    assert.equal(studentResult.direction, 'TRIBAL_TO_HINDI');
    assert.equal(studentResult.script_type, 'DEVANAGARI');
    assert.ok(
      studentResult.transliteration_devanagari.includes('गुरुजी'),
      'student turn must yield Hindi comprehension for the teacher'
    );
    assert.equal(studentResult.bilingual_relay.enabled, false);

    // The teacher branch must remain an OL_CHIKI relay for the same endpoint
    const teacherResult = await aiClientService.translateTwoWayVoiceTurn({
      hindiTranscript: 'बच्चों, किताब खोलो',
      targetLanguage: 'SANTHALI',
      speakerRole: 'TEACHER',
    });

    assert.equal(teacherResult.speaker_role, 'TEACHER');
    assert.equal(teacherResult.direction, 'HINDI_TO_TRIBAL');
    assert.equal(teacherResult.script_type, 'OL_CHIKI');
    assert.equal(teacherResult.bilingual_relay.enabled, true);
    assert.ok(teacherResult.source_transcript.length > 0);
  });

  it('should keep FLN 0.72x default for two-way turns and honour an explicit rate', async () => {
    const flnDefault = await aiClientService.translateTwoWayVoiceTurn({
      hindiTranscript: 'नमस्ते बच्चों',
      targetLanguage: 'HO',
      speakerRole: 'TEACHER',
    });
    assert.equal(flnDefault.speech_rate, 0.72);

    const explicit = await aiClientService.translateTwoWayVoiceTurn({
      hindiTranscript: 'नमस्ते बच्चों',
      targetLanguage: 'HO',
      speakerRole: 'TEACHER',
      speechRate: 1.2,
    });
    assert.equal(explicit.speech_rate, 1.2, 'an explicit speech rate must not be overridden by FLN default');
  });
});
