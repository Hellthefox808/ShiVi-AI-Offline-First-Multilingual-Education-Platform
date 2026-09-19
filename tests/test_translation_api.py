"""
BhashaSetu AI — Translation API & MTB-MLE Indigenous Language Suite (v3.0.0-PROD)
Tests:
- Single text translation (Santhali, Ho, Mundari) with dual transliteration & FLN mode
- Student spoken reverse translation (tribal -> Hindi comprehension)
- 7 Pedagogical glossary domain categories and term lookup
- Full-text glossary search across Hindi, tribal script, and Latin transliteration
- G2P script transliteration (Ol Chiki <-> Devanagari <-> Latin)
- Indigenous language & script detection (Unicode ranges & lexical markers)
- Token-by-token bilingual alignment with phonetic metadata
- Roundtrip back-translation consistency audit
- District dialect adaptation (Kolhan, Santhal Pargana, Naguri)
- FastAPI endpoints via TestClient (all 9 endpoints return 200 OK)
"""

import os
import sys
import unittest

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
AI_DIR = os.path.join(ROOT_DIR, "services", "ai-platform")
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)
if AI_DIR not in sys.path:
    sys.path.insert(0, AI_DIR)

from translation.providers import (
    LanguageProviderService,
    language_provider,
    TRIBAL_LEXICON,
    GLOSSARY_CATEGORIES,
    TERM_CATEGORIES,
)
from main import app
from fastapi.testclient import TestClient


class TestTranslationAPI(unittest.TestCase):

    def setUp(self):
        self.provider = language_provider
        self.client = TestClient(app)

    # --- 1. Single Text Translation & Dual Transliteration ---

    def test_translate_santhali_teacher_mode(self):
        result = self.provider.translate_text(
            text="पानी जीवन है और पेड़ हमारे मित्र हैं।",
            target_language="SANTHALI",
            fln_mode=True,
            include_alignment=True,
        )
        self.assertEqual(result["target_language"], "SANTHALI")
        self.assertEqual(result["script_type"], "OL_CHIKI")
        self.assertIn("ᱫᱟᱨᱮ", result["translated_text"])
        self.assertTrue(len(result["transliteration_hindi"]) > 0)
        self.assertTrue(len(result["transliteration_latin"]) > 0)
        self.assertGreaterEqual(result["confidence_score"], 0.95)
        self.assertEqual(result["quality_status"], "HIGH_CONFIDENCE")
        self.assertTrue(result["fln_adapted"])
        self.assertIn("alignments", result)
        self.assertGreater(len(result["alignments"]), 0)

    def test_translate_ho_and_mundari(self):
        ho_res = self.provider.translate_text(text="पानी", target_language="HO")
        self.assertEqual(ho_res["target_language"], "HO")
        self.assertEqual(ho_res["script_type"], "WARANG_CHITI")
        self.assertIn("ᱫᱟᱺ", ho_res["translated_text"])

        mun_res = self.provider.translate_text(text="पानी", target_language="MUNDARI")
        self.assertEqual(mun_res["target_language"], "MUNDARI")
        self.assertEqual(mun_res["script_type"], "DEVANAGARI")
        self.assertIn("दाः", mun_res["translated_text"])

    def test_reverse_student_translation(self):
        res = self.provider.translate_text(
            text="ᱡᱚᱦᱟᱨ ᱢᱟᱪᱮᱛ ᱜᱚᱢᱠᱮ!",
            source_language="SANTHALI",
            speaker_role="STUDENT",
        )
        self.assertEqual(res["target_language"], "HINDI")
        self.assertIn("गुरुजी", res["translated_text"])
        self.assertEqual(res["speaker_role"], "STUDENT")

    # --- 2. Glossary Categories & Term Lookup ---

    def test_glossary_categories(self):
        categories = self.provider.get_glossary_categories()
        self.assertEqual(len(categories), 7)
        cat_ids = [c["id"] for c in categories]
        self.assertIn("flora_trees", cat_ids)
        self.assertIn("water_geography", cat_ids)
        self.assertIn("animals_fauna", cat_ids)
        self.assertIn("numeracy", cat_ids)
        self.assertIn("body_anatomy", cat_ids)
        self.assertIn("kinship_community", cat_ids)
        self.assertIn("culture_festivals", cat_ids)

    def test_glossary_term_retrieval(self):
        all_terms = self.provider.get_glossary()
        self.assertGreater(len(all_terms), 20)

        flora_terms = self.provider.get_glossary(category="flora_trees")
        self.assertTrue(all(t["category"] == "flora_trees" for t in flora_terms))

        santhali_terms = self.provider.get_glossary(language="SANTHALI")
        self.assertTrue(all(t["language"] == "SANTHALI" for t in santhali_terms))

    def test_glossary_search(self):
        results = self.provider.search_glossary(query="पानी")
        self.assertGreater(len(results), 0)
        self.assertTrue(any("पानी" in t["hindi_term"] for t in results))

        # Search by Latin transliteration
        latin_results = self.provider.search_glossary(query="dare")
        self.assertGreater(len(latin_results), 0)

    # --- 3. G2P Script Transliteration ---

    def test_transliterate_ol_chiki_to_devanagari(self):
        res = self.provider.transliterate_script(
            text="ᱫᱟᱜ",
            source_script="OL_CHIKI",
            target_script="DEVANAGARI",
        )
        self.assertEqual(res["source_script"], "OL_CHIKI")
        self.assertEqual(res["target_script"], "DEVANAGARI")
        self.assertEqual(res["transliterated_text"], "दआग")

    def test_transliterate_ol_chiki_to_latin(self):
        res = self.provider.transliterate_script(
            text="ᱫᱟᱜ",
            source_script="OL_CHIKI",
            target_script="LATIN",
        )
        self.assertEqual(res["transliterated_text"], "dag")

    def test_transliterate_auto_detect(self):
        res = self.provider.transliterate_script(
            text="ᱫᱟᱨᱮ",
            source_script="AUTO",
            target_script="DEVANAGARI",
        )
        self.assertEqual(res["source_script"], "OL_CHIKI")
        self.assertTrue(len(res["transliterated_text"]) > 0)

    # --- 4. Indigenous Language & Script Detection ---

    def test_detect_ol_chiki(self):
        res = self.provider.detect_language_and_script("ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ")
        self.assertEqual(res["detected_language"], "SANTHALI")
        self.assertEqual(res["detected_script"], "OL_CHIKI")
        self.assertEqual(res["iso_code"], "sat_Olck")
        self.assertTrue(res["is_indigenous_jharkhand"])
        self.assertGreaterEqual(res["confidence"], 0.95)

    def test_detect_warang_chiti(self):
        res = self.provider.detect_language_and_script("\U000118A0\U000118A5\U000118B0")
        self.assertEqual(res["detected_language"], "HO")
        self.assertEqual(res["detected_script"], "WARANG_CHITI")
        self.assertEqual(res["iso_code"], "hoc_Wara")
        self.assertTrue(res["is_indigenous_jharkhand"])

    def test_detect_mundari_marker(self):
        res = self.provider.detect_language_and_script("आबु तिसिंग इतुन")
        self.assertEqual(res["detected_language"], "MUNDARI")
        self.assertEqual(res["detected_script"], "DEVANAGARI")
        self.assertTrue(res["is_indigenous_jharkhand"])

    def test_detect_hindi_standard(self):
        res = self.provider.detect_language_and_script("नमस्ते बच्चों आज हम पढ़ेंगे")
        self.assertEqual(res["detected_language"], "HINDI")
        self.assertEqual(res["detected_script"], "DEVANAGARI")
        self.assertFalse(res["is_indigenous_jharkhand"])

    # --- 5. Token Alignment ---

    def test_token_alignment(self):
        res = self.provider.align_tokens("पानी और पेड़", target_language="SANTHALI")
        self.assertEqual(res["target_language"], "SANTHALI")
        self.assertEqual(res["script_type"], "OL_CHIKI")
        self.assertGreaterEqual(res["token_count"], 3)
        tokens = {t["source_token"]: t for t in res["tokens"]}
        self.assertIn("पानी", tokens)
        self.assertTrue(tokens["पानी"]["aligned"])
        self.assertEqual(tokens["पानी"]["target_token"], "ᱫᱟᱜ")

    # --- 6. Back-Translation Audit ---

    def test_back_translation_consistency(self):
        res = self.provider.back_translate("पानी", target_language="SANTHALI")
        self.assertEqual(res["target_language"], "SANTHALI")
        self.assertIn("ᱫᱟᱜ", res["forward_translation"])
        self.assertIn("पानी", res["back_translation_hindi"])
        self.assertGreaterEqual(res["semantic_similarity"], 0.75)
        self.assertIn(res["quality_verdict"], ["EXCELLENT_MATCH", "ACCEPTABLE"])

    # --- 7. Regional Dialect Adaptation ---

    def test_dialect_adaptation_kolhan(self):
        res = self.provider.adapt_dialect(
            text="पानी",
            target_language="SANTHALI",
            dialect_region="KOLHAN",
        )
        self.assertEqual(res["target_language"], "SANTHALI")
        self.assertEqual(res["dialect_region"], "KOLHAN")
        self.assertIn("ᱜᱮᱭᱟ", res["adapted_native_text"])
        self.assertGreater(len(res["dialect_notes"]), 0)

    def test_dialect_adaptation_naguri(self):
        res = self.provider.adapt_dialect(
            text="पेड़",
            target_language="MUNDARI",
            dialect_region="NAGURI",
        )
        self.assertEqual(res["target_language"], "MUNDARI")
        self.assertEqual(res["dialect_region"], "NAGURI")
        self.assertIn("गिदरा को", res["adapted_native_text"])

    # --- 8. FastAPI TestClient Endpoint Verification ---

    def test_fastapi_routes(self):
        # 1. /api/v1/translate
        r = self.client.post("/api/v1/translate", json={"text": "पानी", "target_language": "SANTHALI"})
        self.assertEqual(r.status_code, 200)
        self.assertIn("translated_text", r.json())

        # 2. /api/v1/ai/translate
        r = self.client.post("/api/v1/ai/translate", json={"text": "पेड़", "target_language": "HO"})
        self.assertEqual(r.status_code, 200)

        # 3. /api/v1/translate/glossary
        r = self.client.get("/api/v1/translate/glossary?category=flora_trees&language=SANTHALI")
        self.assertEqual(r.status_code, 200)
        self.assertIsInstance(r.json(), list)

        # 4. /api/v1/translate/glossary/search
        r = self.client.get("/api/v1/translate/glossary/search?q=पानी")
        self.assertEqual(r.status_code, 200)

        # 5. /api/v1/translate/glossary/categories
        r = self.client.get("/api/v1/translate/glossary/categories")
        self.assertEqual(r.status_code, 200)
        self.assertEqual(len(r.json()), 7)

        # 6. /api/v1/translate/transliterate
        r = self.client.post("/api/v1/translate/transliterate", json={
            "text": "ᱫᱟᱜ", "source_script": "OL_CHIKI", "target_script": "DEVANAGARI"
        })
        self.assertEqual(r.status_code, 200)

        # 7. /api/v1/translate/detect
        r = self.client.post("/api/v1/translate/detect", json={"text": "ᱫᱟᱜ"})
        self.assertEqual(r.status_code, 200)
        self.assertEqual(r.json()["detected_language"], "SANTHALI")

        # 8. /api/v1/translate/align
        r = self.client.post("/api/v1/translate/align", json={"text": "पानी और पेड़", "target_language": "SANTHALI"})
        self.assertEqual(r.status_code, 200)

        # 9. /api/v1/translate/back-translate
        r = self.client.post("/api/v1/translate/back-translate", json={"text": "पानी", "target_language": "SANTHALI"})
        self.assertEqual(r.status_code, 200)

        # 10. /api/v1/translate/dialect
        r = self.client.post("/api/v1/translate/dialect", json={
            "text": "पानी", "target_language": "SANTHALI", "dialect_region": "KOLHAN"
        })
        self.assertEqual(r.status_code, 200)


if __name__ == "__main__":
    unittest.main()
