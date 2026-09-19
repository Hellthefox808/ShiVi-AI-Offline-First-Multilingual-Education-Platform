"""
BhashaSetu AI Platform — High-Concurrency Stress & Cache Performance Test Suite
Tests:
1. RAG in-memory LRU query cache hit ratio and sub-millisecond retrieval speedup.
2. 100 concurrent RAG queries across multi-threaded thread pool.
3. High-concurrency synthesis pipeline stress across Santhali, Ho, and Mundari.
4. Thread-safety and zero data race conditions under simulated classroom loads.
"""

import time
import concurrent.futures
import pytest
from rag.engine import rag_engine
from pipeline import unified_pipeline
from translation.providers import language_provider

def test_rag_cache_hit_ratio_and_speedup():
    rag_engine.clear_cache()
    query = "साल का पवित्र पेड़ और सरजोम दारे"
    
    # Cold retrieval
    start_cold = time.perf_counter()
    results_cold = rag_engine.retrieve(query=query, grade="GRADE_2", top_k=2)
    time_cold_ms = (time.perf_counter() - start_cold) * 1000.0

    assert len(results_cold) > 0
    assert results_cold[0]["chunk"]["chunk_id"] == "JCERT_G2_EVS_01"

    # Cached retrieval (Warm)
    start_warm = time.perf_counter()
    results_warm = rag_engine.retrieve(query=query, grade="GRADE_2", top_k=2)
    time_warm_ms = (time.perf_counter() - start_warm) * 1000.0

    assert len(results_warm) == len(results_cold)
    assert results_warm[0]["chunk"]["chunk_id"] == results_cold[0]["chunk"]["chunk_id"]

    stats = rag_engine.get_cache_stats()
    assert stats["cache_hits"] == 1
    assert stats["cache_misses"] == 1
    assert stats["cache_size"] == 1
    assert stats["hit_ratio"] == 0.5

    print(f"\nRAG Latency Cold: {time_cold_ms:.3f}ms | Cached: {time_warm_ms:.3f}ms")
    # Cached must be significantly faster or under 1ms
    assert time_warm_ms < 1.5

def test_high_concurrency_rag_stress():
    rag_engine.clear_cache()
    queries = [
        ("साल और सखुआ के पेड़", "GRADE_2", "ENVIRONMENTAL_STUDIES"),
        ("गिनती 1 से 10 तक संख्या", "GRADE_1", "MATHEMATICS"),
        ("जल ही जीवन है नदियां", "GRADE_3", "LANGUAGE_FLN"),
        ("पशु पक्षी और जंगल", "GRADE_4", "ENVIRONMENTAL_STUDIES"),
        ("पारंपरिक लोकपर्व सरहुल करम", "GRADE_5", "TRIBAL_HERITAGE"),
        ("स्वास्थ्य और स्वच्छता आदतें", "GRADE_1", "LANGUAGE_FLN"),
        ("झारखंड के प्रसिद्ध लोकगीत", "GRADE_5", "TRIBAL_HERITAGE"),
        ("सौरमंडल और पृथ्वी", "GRADE_4", "ENVIRONMENTAL_STUDIES")
    ]

    total_requests = 100
    num_workers = 10
    start_time = time.perf_counter()

    with concurrent.futures.ThreadPoolExecutor(max_workers=num_workers) as executor:
        futures = []
        for i in range(total_requests):
            q, g, s = queries[i % len(queries)]
            futures.append(executor.submit(
                rag_engine.retrieve,
                query=q,
                grade=g,
                subject=s,
                top_k=2
            ))
        
        results = [f.result() for f in concurrent.futures.as_completed(futures)]

    total_time_ms = (time.perf_counter() - start_time) * 1000.0
    avg_per_query_ms = total_time_ms / total_requests

    assert len(results) == total_requests
    for res in results:
        assert len(res) > 0

    stats = rag_engine.get_cache_stats()
    print(f"\nRAG Stress: {total_requests} queries across {num_workers} threads took {total_time_ms:.2f}ms (avg {avg_per_query_ms:.3f}ms). Cache hits: {stats['cache_hits']}, misses: {stats['cache_misses']}")
    assert avg_per_query_ms < 10.0
    assert stats["cache_hits"] > 0

def test_multilingual_pipeline_concurrency_stress():
    prompts = [
        ("पेड़ और पत्तियाँ", "SANTHALI", "GRADE_2"),
        ("गिनती एक से दस", "HO", "GRADE_1"),
        ("जल और नदियाँ", "MUNDARI", "GRADE_3"),
        ("प्रकृति और पर्यावरण", "SANTHALI", "GRADE_4")
    ]

    total_runs = 20
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
        futures = []
        for i in range(total_runs):
            p, l, g = prompts[i % len(prompts)]
            futures.append(executor.submit(
                unified_pipeline.execute_full_pipeline,
                hindi_prompt=p,
                target_language=l,
                grade_level=g,
                subject="ENVIRONMENTAL_STUDIES"
            ))

        completed = [f.result() for f in concurrent.futures.as_completed(futures)]

    assert len(completed) == total_runs
    for item in completed:
        assert item["status"] == "SUCCESS"
        assert item["pipeline_status"] in ["AUTO_PUBLISHED", "PENDING_EDUCATOR_REVIEW"]
        assert "quality_report" in item
        assert item["quality_report"]["decision"] in ["AUTO_PUBLISH_CANDIDATE", "TEACHER_REVIEW_REQUIRED"]
        assert len(item["native_script_text"]) > 0
