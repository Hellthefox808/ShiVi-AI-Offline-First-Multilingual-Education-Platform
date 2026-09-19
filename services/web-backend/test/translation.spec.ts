import { describe, it, beforeEach } from 'node:test';
import * as assert from 'node:assert/strict';
import { AiClientService } from '../src/ai-client/ai-client.service';
import { TranslationController } from '../src/ai-client/translation.controller';

describe('BhashaSetu Multilingual Translation Suite', () => {
  let aiClientService: AiClientService;
  let translationController: TranslationController;

  beforeEach(() => {
    aiClientService = new AiClientService();
    translationController = new TranslationController(aiClientService);
  });

  it('should translate single text to Santhali with dual transliteration', async () => {
    const res = await translationController.translate({
      text: 'पानी जीवन है',
      targetLanguage: 'SANTHALI',
      flnMode: true,
    });

    assert.equal(res.target_language, 'SANTHALI');
    assert.equal(res.script_type, 'OL_CHIKI');
    assert.ok(res.translated_text.length > 0);
    assert.ok(res.transliteration_hindi.length > 0);
    assert.ok(res.transliteration_latin.length > 0);
    assert.equal(res.quality_status, 'HIGH_CONFIDENCE');
  });

  it('should fetch glossary categories and terms', async () => {
    const categories = await translationController.getGlossaryCategories();
    assert.equal(categories.length, 7);
    assert.ok(categories.some((c: any) => c.id === 'flora_trees'));
    assert.ok(categories.some((c: any) => c.id === 'water_geography'));

    const terms = await translationController.getGlossary('flora_trees', 'SANTHALI');
    assert.ok(Array.isArray(terms));
    assert.ok(terms.length > 0);
  });

  it('should search glossary by keyword', async () => {
    const results = await translationController.searchGlossary('पानी', 'SANTHALI');
    assert.ok(Array.isArray(results));
    assert.ok(results.length > 0);
    assert.ok(results[0].hindi_term.includes('पानी'));
  });

  it('should transliterate scripts correctly', async () => {
    const res = await translationController.transliterate({
      text: 'ᱫᱟᱜ',
      sourceScript: 'OL_CHIKI',
      targetScript: 'DEVANAGARI',
      language: 'SANTHALI',
    });
    assert.ok(res.transliterated_text.length > 0);
    assert.equal(res.language, 'SANTHALI');
  });

  it('should detect Ol Chiki indigenous script', async () => {
    const res = await translationController.detect({
      text: 'ᱫᱟᱜ ᱫᱚ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾',
    });
    assert.equal(res.detected_language, 'SANTHALI');
    assert.equal(res.detected_script, 'OL_CHIKI');
    assert.equal(res.is_indigenous_jharkhand, true);
  });

  it('should align tokens between source and target', async () => {
    const res = await translationController.align({
      text: 'पानी और पेड़',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(res.target_language, 'SANTHALI');
    assert.ok(res.token_count > 0);
    assert.ok(Array.isArray(res.tokens));
  });

  it('should execute back-translation consistency audit', async () => {
    const res = await translationController.backTranslate({
      text: 'पानी जीवन है',
      targetLanguage: 'SANTHALI',
    });
    assert.equal(res.target_language, 'SANTHALI');
    assert.ok(res.semantic_similarity >= 0.7);
    assert.equal(res.quality_verdict, 'EXCELLENT_MATCH');
  });

  it('should adapt dialect for Kolhan region', async () => {
    const res = await translationController.adaptDialect({
      text: 'ᱫᱟᱜ ᱫᱚ ᱡᱤᱣᱤ ᱠᱟᱱᱟ᱾',
      targetLanguage: 'SANTHALI',
      dialectRegion: 'KOLHAN',
    });
    assert.equal(res.target_language, 'SANTHALI');
    assert.equal(res.dialect_region, 'KOLHAN');
    assert.ok(res.dialect_notes.length > 0);
  });
});
