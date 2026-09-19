"""
Automated Pytest Suite for BhashaSetu AI Platform Microservice
Validates:
1. Hybrid RAG retrieval (BM25 + Dense + RRF + Cross-Encoder)
2. Multilingual translation & tribal script rendering (Santhali/Ol Chiki, Ho/Warang Chiti, Mundari/Devanagari)
3. Pedagogical adaptation with authentic tribal analogies
4. Automated COMET/XCOMET Quality Evaluation Gate
5. End-to-End synthesis pipeline orchestration
6. Batch translation & curriculum node catalog endpoints
"""

import pytest
from rag.engine import rag_engine, JCERT_KNOWLEDGE_BASE
from translation.providers import language_provider
from pedagogy.adapter import pedagogical_adapter
from quality.evaluator import quality_evaluator
from pipeline import unified_pipeline

def test_jcert_knowledge_base_integrity():
    assert len(JCERT_KNOWLEDGE_BASE) >= 15
    for node in JCERT_KNOWLEDGE_BASE:
        assert "chunk_id" in node
        assert "grade" in node
        assert "subject" in node
        assert "lo_code" in node
        assert "content_hindi" in node
        assert "cultural_keywords" in node
        assert "tribal_analogies" in node

def test_hybrid_rag_retrieval_leaves_and_trees():
    results = rag_engine.retrieve(
        query="साल और महुआ के पेड़ और पत्तियाँ",
        grade="GRADE_2",
        subject="ENVIRONMENTAL_STUDIES",
        top_k=2
    )
    assert len(results) > 0
    top_chunk = results[0]["chunk"]
    assert top_chunk["chunk_id"] == "JCERT_G2_EVS_01"
    assert top_chunk["grade"] == "GRADE_2"
    assert results[0]["provenance"]["lo_code"] == "LO-EVS-G2-03"

def test_hybrid_rag_retrieval_numeracy():
    results = rag_engine.retrieve(
        query="गिनती और 1 से 10 तक समूह बनाना",
        grade="GRADE_1",
        top_k=1
    )
    assert len(results) == 1
    assert results[0]["chunk"]["chunk_id"] == "JCERT_G1_MATH_01"

def test_santhali_translation_and_ol_chiki_script():
    resolved = language_provider.resolve_language("SANTHALI")
    assert resolved == "SANTHALI"
    res = language_provider.translate_concept("पेड़ों की पत्तियाँ और फूल", "SANTHALI")
    assert res["target_language"] == "SANTHALI"
    assert res["script_type"] == "OL_CHIKI"
    # Ensure native Ol Chiki script characters are returned
    assert "ᱫᱟᱨᱮ" in res["native_script_text"] or "ᱥᱟᱠᱟᱢ" in res["native_script_text"]
    assert len(res["transliteration_hindi"]) > 0

def test_ho_translation_and_warang_chiti():
    resolved = language_provider.resolve_language("HO")
    assert resolved == "HO"
    res = language_provider.translate_concept("जल और नदियां", "HO")
    assert res["target_language"] == "HO"
    assert res["script_type"] == "WARANG_CHITI"
    assert len(res["native_script_text"]) > 0

def test_mundari_translation_and_devanagari():
    resolved = language_provider.resolve_language("MUNDARI")
    assert resolved == "MUNDARI"
    res = language_provider.translate_concept("गिनती एक दो तीन", "MUNDARI")
    assert res["target_language"] == "MUNDARI"
    assert res["script_type"] == "DEVANAGARI"
    assert "मियाद" in res["native_script_text"] or "लेखा" in res["native_script_text"]

def test_pedagogical_adaptation():
    evidence = rag_engine.retrieve("पेड़ और पत्तियाँ", grade="GRADE_2", top_k=1)
    adaptation = pedagogical_adapter.adapt("पेड़ और पत्तियाँ", "GRADE_2", "SANTHALI", evidence)
    assert "cultural_analogy" in adaptation
    assert "local_story_context" in adaptation
    assert "classroom_activity" in adaptation

def test_quality_evaluation_gate():
    report = quality_evaluator.evaluate(
        hindi_source="पेड़ों की पत्तियाँ",
        target_output="ᱫᱟᱨᱮ ᱥᱟᱠᱟᱢ ᱟᱨ ᱵᱟᱦᱟ",
        target_lang="SANTHALI",
        evidence_text="हमारे आसपास साल और महुआ के पेड़ होते हैं।"
    )
    assert report["composite_score"] >= 0.70
    assert report["status"] in ["HIGH_CONFIDENCE", "MEDIUM_CONFIDENCE"]
    assert report["decision"] in ["AUTO_PUBLISH_CANDIDATE", "TEACHER_REVIEW_REQUIRED"]

def test_unified_synthesis_pipeline_e2e():
    output = unified_pipeline.execute_full_pipeline(
        hindi_prompt="पेड़ और पत्तियाँ",
        target_language="SANTHALI",
        grade_level="GRADE_2",
        subject="ENVIRONMENTAL_STUDIES",
        district="Dumka"
    )
    assert "lesson_id" in output
    assert output["target_language"] == "SANTHALI"
    assert output["pipeline_status"] in ["AUTO_PUBLISHED", "PENDING_EDUCATOR_REVIEW"]
    assert "multilingual_bundle" in output
    assert "offline_distribution_package" in output

def test_fastapi_voice_translate_endpoints():
    from fastapi.testclient import TestClient
    from main import app
    client = TestClient(app)

    # Test /api/v1/ai/voice/translate with transcription_hindi and fln_mode
    response = client.post("/api/v1/ai/voice/translate", json={
        "transcription_hindi": "नमस्ते बच्चों, आज हम पढ़ेंगे",
        "target_language": "SANTHALI",
        "fln_mode": True,
        "bilingual_relay": True
    })
    assert response.status_code == 200
    data = response.json()
    assert data["turn_id"].startswith("VOICE-")
    assert data["target_language"] == "SANTHALI"
    assert data["acoustic_engine"] == "hi-IN"
    assert data["speech_rate"] == 0.72
    assert data["bilingual_relay"]["enabled"] is True
    assert data["bilingual_relay"]["source_audio_pause_ms"] == 450
    assert len(data["translated_text"]) > 0

    # Test /api/v1/voice/translate with Ho language
    ho_resp = client.post("/api/v1/voice/translate", json={
        "hindi_transcript": "जल ही जीवन है",
        "target_language": "HO"
    })
    assert ho_resp.status_code == 200
    ho_data = ho_resp.json()
    assert ho_data["target_language"] == "HO"
    assert ho_data["script_type"] == "WARANG_CHITI"

def test_fastapi_rag_cache_endpoints():
    from fastapi.testclient import TestClient
    from main import app
    client = TestClient(app)

    stats_resp = client.get("/api/v1/rag/cache-stats")
    assert stats_resp.status_code == 200
    stats = stats_resp.json()
    assert "cache_hits" in stats
    assert "cache_misses" in stats
    assert "cache_size" in stats

    clear_resp = client.post("/api/v1/rag/cache-clear")
    assert clear_resp.status_code == 200
    assert clear_resp.json()["status"] == "CLEARED"

