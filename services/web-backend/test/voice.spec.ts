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
});
