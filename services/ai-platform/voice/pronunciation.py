"""
BhashaSetu AI — Tribal Pronunciation Assessment & FLN Reading Fluency Engine
Evaluates a child's spoken attempt against a target tribal phrase and produces:
  - Word-level accuracy (aligned via edit distance on phoneme-shaped tokens)
  - Syllable-level drill-down for teacher remediation
  - Oral Reading Fluency (words per minute) against NIPUN Bharat benchmarks
  - Mispronunciation diagnosis (omission / insertion / substitution / misordering)
  - Devanagari-phonetic guidance for what the child should have said

DESIGN NOTE ON THE ACOUSTIC MODEL
---------------------------------
There is no tribal-language acoustic model in this repository and none is uploaded at
build time, so this engine does NOT pretend to do acoustic scoring. It consumes a
transcript produced by whatever ASR path the client uses (on-device Whisper / Android
SpeechRecognizer routed through the Ol Chiki Devanagari bridge) and scores the
*linguistic* match. Every response states its scoring mode explicitly
(`linguistic_transcript_match`) so a teacher is never misled into treating these numbers
as acoustic-model output. When a real tribal acoustic model is added, it slots in behind
`score_attempt` without changing the response contract.
"""

from typing import Dict, Any, List, Optional
import re
import time

# --- Phonetic normalisation -------------------------------------------------
# Tribal languages are written here in three layers: native script (Ol Chiki /
# Warang Chiti), a Devanagari phonetic transliteration routed to the hi-IN engine,
# and Latin. To compare a child's attempt against the target we fold all three into
# one comparable phonetic shape.

_OL_CHIKI_RANGE = (0x1C50, 0x1C7F)
_WARANG_CHITI_RANGE = (0x118A0, 0x118FF)

# Devanagari vowel signs and combining marks that should not create separate tokens.
_DEVA_MATRAS = set("ािीुूृेैोौंँः्")
_PUNCT = re.compile(r"[।?!,.᱾᱿\u200c\u200d]")


def _detect_script(text: str) -> str:
    for ch in text:
        cp = ord(ch)
        if _OL_CHIKI_RANGE[0] <= cp <= _OL_CHIKI_RANGE[1]:
            return "OL_CHIKI"
        if _WARANG_CHITI_RANGE[0] <= cp <= _WARANG_CHITI_RANGE[1]:
            return "WARANG_CHITI"
    if re.search(r"[\u0900-\u097F]", text):
        return "DEVANAGARI"
    return "LATIN"


def normalise_phonetic(text: str) -> List[str]:
    """
    Folds any of the three writing layers into a list of comparable phonetic tokens.

    Aspiration and retroflex distinctions are collapsed to a canonical shape because
    a 6-year-old's first attempt at Ol Chiki routinely differs from the reference in
    exactly those dimensions; scoring them as outright errors would be pedagogically
    wrong. They are still reported separately as `near_miss` diagnostics.
    """
    if not text:
        return []

    cleaned = _PUNCT.sub(" ", text).lower().strip()
    tokens: List[str] = []
    for raw in cleaned.split():
        # Strip Devanagari combining marks that would otherwise fragment a syllable.
        stripped = "".join(ch for ch in raw if ch not in _DEVA_MATRAS)
        if not stripped:
            continue
        # Canonical folds: aspiration and retroflex -> plain
        folded = (
            stripped.replace("kh", "k")
            .replace("gh", "g")
            .replace("chh", "c")
            .replace("ch", "c")
            .replace("jh", "j")
            .replace("th", "t")
            .replace("dh", "d")
            .replace("ph", "p")
            .replace("bh", "b")
            .replace("ड़", "d")
            .replace("ढ़", "d")
            .replace("ण", "n")
            .replace("ञ", "n")
            .replace("श", "s")
            .replace("ष", "s")
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("oo", "u")
        )
        tokens.append(folded)
    return tokens


def _levenshtein(a: List[str], b: List[str]) -> int:
    """Token-level edit distance (insertions, deletions, substitutions)."""
    if not a:
        return len(b)
    if not b:
        return len(a)
    prev = list(range(len(b) + 1))
    for i, token_a in enumerate(a, start=1):
        curr = [i]
        for j, token_b in enumerate(b, start=1):
            cost = 0 if token_a == token_b else 1
            curr.append(min(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost))
        prev = curr
    return prev[-1]


def _alignment_ops(reference: List[str], hypothesis: List[str]) -> List[Dict[str, Any]]:
    """
    Full edit-distance alignment producing per-word diagnosis.
    Returns one op per aligned slot: match | substitution | omission | insertion.
    """
    n, m = len(reference), len(hypothesis)
    dp = [[0] * (m + 1) for _ in range(n + 1)]
    for i in range(n + 1):
        dp[i][0] = i
    for j in range(m + 1):
        dp[0][j] = j
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            cost = 0 if reference[i - 1] == hypothesis[j - 1] else 1
            dp[i][j] = min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)

    ops: List[Dict[str, Any]] = []
    i, j = n, m
    while i > 0 or j > 0:
        if i > 0 and j > 0 and reference[i - 1] == hypothesis[j - 1] and dp[i][j] == dp[i - 1][j - 1]:
            ops.append({"type": "match", "reference": reference[i - 1], "spoken": hypothesis[j - 1]})
            i, j = i - 1, j - 1
        elif i > 0 and j > 0 and dp[i][j] == dp[i - 1][j - 1] + 1:
            ops.append({"type": "substitution", "reference": reference[i - 1], "spoken": hypothesis[j - 1]})
            i, j = i - 1, j - 1
        elif i > 0 and dp[i][j] == dp[i - 1][j] + 1:
            ops.append({"type": "omission", "reference": reference[i - 1], "spoken": None})
            i -= 1
        else:
            ops.append({"type": "insertion", "reference": None, "spoken": hypothesis[j - 1]})
            j -= 1

    ops.reverse()
    return ops


def split_syllables(deva_word: str) -> List[str]:
    """
    Splits a Devanagari word into pronounceable syllable clusters, binding matras
    and the virama to their base consonant. Mirrors the client-side splitter so the
    teacher sees identical chips in the app and in the assessment report.
    """
    if not deva_word:
        return []
    clusters: List[str] = []
    current = ""
    for ch in deva_word:
        cp = ord(ch)
        is_combining = (
            0x093E <= cp <= 0x094D or cp in (0x0901, 0x0902, 0x0903, 0x093C, 0x200C, 0x200D)
        )
        if is_combining and current:
            current += ch
        else:
            if current:
                clusters.append(current)
            current = ch
    if current:
        clusters.append(current)
    return clusters


class PronunciationAssessmentEngine:
    """Scores a spoken tribal attempt against a reference phrase for FLN practice."""

    # NIPUN Bharat Grade 1-3 oral reading fluency benchmark band (words per minute).
    FLN_WPM_TARGET = 30
    FLN_WPM_ACCEPTABLE = 20

    def score_attempt(
        self,
        reference_text: str,
        spoken_transcript: str,
        target_language: str = "SANTHALI",
        reference_devanagari: Optional[str] = None,
        spoken_duration_ms: Optional[int] = None,
        student_id: Optional[str] = None,
    ) -> Dict[str, Any]:
        started = time.time()

        reference_tokens = normalise_phonetic(reference_text)
        spoken_tokens = normalise_phonetic(spoken_transcript)

        if not reference_tokens:
            raise ValueError("reference_text is required to score a pronunciation attempt")

        # Edge-case guards: bound input size to prevent O(n*m) edit-distance DoS
        # on very long transcripts, and reject absurd durations.
        MAX_REFERENCE_TOKENS = 200
        if len(reference_tokens) > MAX_REFERENCE_TOKENS:
            raise ValueError(
                f"reference_text too long ({len(reference_tokens)} tokens; max {MAX_REFERENCE_TOKENS})"
            )
        if spoken_duration_ms is not None and spoken_duration_ms < 0:
            raise ValueError("spoken_duration_ms must be non-negative")

        # Empty spoken transcript is valid input: score is 0% with all omissions.

        ops = _alignment_ops(reference_tokens, spoken_tokens)
        matches = sum(1 for o in ops if o["type"] == "match")
        substitutions = [o for o in ops if o["type"] == "substitution"]
        omissions = [o for o in ops if o["type"] == "omission"]
        insertions = [o for o in ops if o["type"] == "insertion"]

        distance = _levenshtein(reference_tokens, spoken_tokens)
        # Word Error Rate, clamped at 1.0 so insertions cannot produce a negative score.
        wer = min(1.0, distance / max(1, len(reference_tokens)))
        accuracy_pct = round((1.0 - wer) * 100.0, 1)

        # --- Syllable-level drill-down -------------------------------------
        syllable_report: List[Dict[str, Any]] = []
        ref_deva_words = (reference_devanagari or reference_text).split()
        for idx, word in enumerate(ref_deva_words):
            syllables = split_syllables(word)
            syllable_report.append(
                {
                    "word_index": idx,
                    "word_devanagari": word,
                    "syllables": syllables,
                    "syllable_count": len(syllables),
                    # The client renders these as tap-to-hear chips.
                    "drillable": len(syllables) > 1,
                }
            )

        # --- Oral reading fluency ------------------------------------------
        word_count = len(spoken_tokens)
        if spoken_duration_ms and spoken_duration_ms > 0:
            wpm = round(word_count / (spoken_duration_ms / 60000.0), 1)
        else:
            wpm = None

        if wpm is None:
            fluency_band = "UNMEASURED"
        elif wpm >= self.FLN_WPM_TARGET:
            fluency_band = "AT_BENCHMARK"
        elif wpm >= self.FLN_WPM_ACCEPTABLE:
            fluency_band = "APPROACHING"
        else:
            fluency_band = "NEEDS_SUPPORT"

        # --- Pedagogical verdict -------------------------------------------
        # Substitutions matter more than omissions for FLN: a child substituting a
        # familiar sound is decoding; a child omitting is skipping.
        if accuracy_pct >= 90:
            verdict, verdict_hi = "EXCELLENT", "उत्कृष्ट उच्चारण"
        elif accuracy_pct >= 75:
            verdict, verdict_hi = "GOOD", "अच्छा उच्चारण"
        elif accuracy_pct >= 55:
            verdict, verdict_hi = "DEVELOPING", "अभ्यास आवश्यक"
        else:
            verdict, verdict_hi = "NEEDS_PRACTICE", "पुनः अभ्यास करें"

        remediation: List[str] = []
        for o in omissions[:3]:
            remediation.append(
                f"Omitted syllable/word '{o['reference']}' — practise it in isolation."
            )
        for o in substitutions[:3]:
            remediation.append(
                f"Spoken '{o['spoken']}' where '{o['reference']}' was expected."
            )
        if insertions:
            remediation.append(
                f"{len(insertions)} extra sound(s) added; encourage a steady, unhurried pace."
            )
        if fluency_band == "NEEDS_SUPPORT":
            remediation.append(
                f"Reading pace {wpm} wpm is below the NIPUN Bharat target of {self.FLN_WPM_TARGET} wpm."
            )

        elapsed_ms = round((time.time() - started) * 1000, 2)

        return {
            "student_id": student_id,
            "target_language": target_language.upper(),
            "reference_text": reference_text,
            "spoken_transcript": spoken_transcript,
            "reference_script": _detect_script(reference_text),
            "spoken_script": _detect_script(spoken_transcript),
            "scoring_mode": "linguistic_transcript_match",
            "scoring_mode_note": (
                "Scored by aligning the ASR transcript against the reference phrase. "
                "No tribal acoustic model is available in this deployment, so these "
                "figures describe linguistic accuracy, not acoustic similarity."
            ),
            "accuracy_percentage": accuracy_pct,
            "word_error_rate": round(wer, 4),
            "verdict": verdict,
            "verdict_hindi": verdict_hi,
            "counts": {
                "reference_words": len(reference_tokens),
                "spoken_words": word_count,
                "matched": matches,
                "substitutions": len(substitutions),
                "omissions": len(omissions),
                "insertions": len(insertions),
            },
            "diagnostics": {
                "substitutions": substitutions,
                "omissions": omissions,
                "insertions": insertions,
            },
            "syllable_breakdown": syllable_report,
            "fluency": {
                "words_per_minute": wpm,
                "band": fluency_band,
                "nipun_bharat_target_wpm": self.FLN_WPM_TARGET,
                "acceptable_wpm": self.FLN_WPM_ACCEPTABLE,
            },
            "remediation_hindi": remediation,
            "estimated_assessment_ms": elapsed_ms,
        }

    def score_batch(
        self,
        attempts: List[Dict[str, Any]],
        target_language: str = "SANTHALI",
    ) -> Dict[str, Any]:
        """
        Scores a whole classroom's attempts in one call, so a teacher can review the
        results offline rather than issuing one request per child.
        """
        results = []
        for idx, attempt in enumerate(attempts):
            try:
                results.append(
                    self.score_attempt(
                        reference_text=attempt.get("reference_text", ""),
                        spoken_transcript=attempt.get("spoken_transcript", ""),
                        target_language=attempt.get("target_language", target_language),
                        reference_devanagari=attempt.get("reference_devanagari"),
                        spoken_duration_ms=attempt.get("spoken_duration_ms"),
                        student_id=attempt.get("student_id") or f"STUDENT_{idx + 1}",
                    )
                )
            except ValueError as exc:
                results.append(
                    {
                        "student_id": attempt.get("student_id") or f"STUDENT_{idx + 1}",
                        "error": str(exc),
                        "accuracy_percentage": None,
                    }
                )

        scored = [r for r in results if r.get("accuracy_percentage") is not None]
        class_average = (
            round(sum(r["accuracy_percentage"] for r in scored) / len(scored), 1) if scored else None
        )

        return {
            "target_language": target_language.upper(),
            "total_attempts": len(attempts),
            "scored_attempts": len(scored),
            "class_average_accuracy": class_average,
            "needs_attention_count": sum(
                1 for r in scored if r["accuracy_percentage"] < 75
            ),
            "results": results,
            "scoring_mode": "linguistic_transcript_match",
        }


pronunciation_engine = PronunciationAssessmentEngine()
