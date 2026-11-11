import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AiClientService } from '../src/ai-client/ai-client.service';
import { VoiceController } from '../src/ai-client/voice.controller';

/**
 * These exercise the OFFLINE path deliberately: with no AI Platform reachable the
 * gateway must still grade a child's pronunciation and batch-translate, rather than
 * surfacing an error mid-lesson. That is the exact scenario of a Jharkhand classroom
 * with no carrier coverage.
 */
describe('BhashaSetu Voice API — Pronunciation, Batch & Capabilities', () => {
  let service: AiClientService;
  let controller: VoiceController;

  beforeEach(() => {
    service = new AiClientService();
    controller = new VoiceController(service);
  });

  it('should award full marks for an exact tribal utterance', async () => {
    const result = await service.scorePronunciation({
      referenceText: 'जोहार',
      spokenTranscript: 'जोहार',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(result.accuracy_percentage, 100);
    assert.equal(result.word_error_rate, 0);
    assert.equal(result.verdict, 'EXCELLENT');
  });

  it('should award partial credit and diagnose an omitted word', async () => {
    const result = await service.scorePronunciation({
      referenceText: 'जोहार गिदरा को',
      spokenTranscript: 'जोहार गिदरा',
      targetLanguage: 'SANTHALI',
      spokenDurationMs: 4200,
    });

    assert.equal(result.accuracy_percentage, 66.7);
    assert.equal(result.counts.reference_words, 3);
    assert.equal(result.counts.spoken_words, 2);
    assert.equal(result.verdict, 'DEVELOPING');
    assert.ok(result.verdict_hindi.length > 0, 'teacher-facing verdict must be in Hindi');
  });

  it('should measure oral reading fluency against NIPUN Bharat bands', async () => {
    const slow = await service.scorePronunciation({
      referenceText: 'जोहार गिदरा को',
      spokenTranscript: 'जोहार गिदरा को',
      targetLanguage: 'SANTHALI',
      spokenDurationMs: 30000, // 3 words in 30s = 6 wpm
    });
    assert.equal(slow.fluency.band, 'NEEDS_SUPPORT');
    assert.equal(slow.fluency.nipun_bharat_target_wpm, 30);

    const fluent = await service.scorePronunciation({
      referenceText: 'जोहार गिदरा को',
      spokenTranscript: 'जोहार गिदरा को',
      targetLanguage: 'SANTHALI',
      spokenDurationMs: 4000, // 3 words in 4s = 45 wpm
    });
    assert.equal(fluent.fluency.band, 'AT_BENCHMARK');
  });

  it('should never report a negative accuracy when the child adds extra words', async () => {
    const result = await service.scorePronunciation({
      referenceText: 'जोहार',
      spokenTranscript: 'जोहार जोहार जोहार अतिरिक्त शब्द',
      targetLanguage: 'SANTHALI',
    });
    assert.ok(result.accuracy_percentage >= 0, 'insertions must not drive accuracy negative');
    assert.ok(result.accuracy_percentage <= 100);
  });

  it('should clamp word error rate at 1.0 and report zero when the child is silent', async () => {
    const result = await service.scorePronunciation({
      referenceText: 'जोहार गिदरा को',
      spokenTranscript: '',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(result.accuracy_percentage, 0);
    assert.equal(result.word_error_rate, 1);
    assert.equal(result.verdict, 'NEEDS_PRACTICE');
  });

  it('should declare scoring mode and limitations in every score response', async () => {
    const result = await service.scorePronunciation({
      referenceText: 'ᱡᱚᱦᱟᱨ',
      spokenTranscript: 'ᱡᱚᱦᱟᱨ',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(result.scoring_mode, 'linguistic_transcript_match');
    assert.ok(
      result.scoring_mode_note && result.scoring_mode_note.length > 0,
      'the response must state that no tribal acoustic model was used'
    );
  });

  it('should refuse to score when the reference phrase is empty', async () => {
    const result = await service.scorePronunciation({
      referenceText: '',
      spokenTranscript: 'ᱡᱚᱦᱟᱨ',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(result.accuracy_percentage, null);
    assert.equal(result.error, 'REFERENCE_REQUIRED');
  });

  it('should aggregate a classroom batch with a class average and attention count', async () => {
    const result = await controller.scorePronunciationBatch({
      targetLanguage: 'SANTHALI',
      attempts: [
        { studentId: 'S1', referenceText: 'ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ', spokenTranscript: 'ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ' },
        { studentId: 'S2', referenceText: 'ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ', spokenTranscript: 'ᱮᱴᱟᱜ ᱠᱟᱛᱷᱟ' },
      ],
    });

    assert.equal(result.total_attempts, 2);
    assert.equal(result.scored_attempts, 2);
    assert.equal(result.class_average_accuracy, 50);
    assert.equal(result.needs_attention_count, 1, 'the below-threshold child must be flagged');
    assert.equal(result.results[0].student_id, 'S1');
  });

  it('should batch-translate many utterances and preserve index order', async () => {
    const result = await controller.batchTranslateVoice({
      transcripts: ['नमस्ते बच्चों', 'किताब खोलो', 'पानी पियो'],
      targetLanguage: 'HO',
      flnMode: true,
    });

    assert.equal(result.count, 3);
    assert.equal(result.failed, 0);
    assert.deepEqual(
      result.results.map((r: any) => r.index),
      [0, 1, 2],
      'batch results must stay aligned to their input positions'
    );
    assert.equal(result.speech_rate, 0.72, 'FLN mode must drive the 0.72x rate');
    for (const entry of result.results) {
      assert.ok(entry.tribal_native_text.length > 0);
      assert.equal(entry.script_type, 'WARANG_CHITI');
    }
  });

  it('should honour an explicit speech rate over the FLN default in batch mode', async () => {
    const result = await controller.batchTranslateVoice({
      transcripts: ['नमस्ते'],
      targetLanguage: 'SANTHALI',
      flnMode: true,
      speechRate: 1.2,
    });
    assert.equal(result.speech_rate, 1.2);
  });

  it('should route a student batch through the Hindi comprehension direction', async () => {
    const result = await controller.batchTranslateVoice({
      transcripts: ['ᱡᱚᱦᱟᱨ ᱢᱟᱪᱮᱛ ᱜᱚᱢᱠᱮ'],
      targetLanguage: 'SANTHALI',
      speakerRole: 'STUDENT',
    });
    assert.equal(result.speaker_role, 'STUDENT');
    assert.equal(result.results[0].direction, 'TRIBAL_TO_HINDI');
    assert.equal(result.results[0].script_type, 'DEVANAGARI');
  });

  it('should report capabilities with explicit limitations when AI Platform is unreachable', async () => {
    const caps = await controller.getVoiceCapabilities();
    assert.ok(caps.features.pronunciation_scoring);
    assert.equal(caps.limitations.tribal_acoustic_model, false);
    assert.equal(caps.limitations.pronunciation_scoring_mode, 'linguistic_transcript_match');
    assert.equal(caps.fln_defaults.speech_rate, 0.72);
    assert.equal(caps.fln_defaults.nipun_bharat_target_wpm, 30);
    assert.equal(caps.degraded, true, 'fallback capability response must admit it is degraded');
  });

  it('should keep pronunciation cache entries keyed per attempt', async () => {
    const a = await service.scorePronunciation({
      referenceText: 'जोहार',
      spokenTranscript: 'जोहार',
      targetLanguage: 'SANTHALI',
    });
    const b = await service.scorePronunciation({
      referenceText: 'जोहार',
      spokenTranscript: 'गिदरा',
      targetLanguage: 'SANTHALI',
    });
    assert.notEqual(
      a.accuracy_percentage,
      b.accuracy_percentage,
      'a different spoken transcript must not be served from the previous cache entry'
    );
  });
});
